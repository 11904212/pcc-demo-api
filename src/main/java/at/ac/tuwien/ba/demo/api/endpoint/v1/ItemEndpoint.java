package at.ac.tuwien.ba.demo.api.endpoint.v1;

import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.ItemInfoDto;
import at.ac.tuwien.ba.demo.api.endpoint.v1.mapper.ItemMapper;
import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ValidationException;
import at.ac.tuwien.ba.demo.api.service.PlanetaryComputerService;
import at.ac.tuwien.ba.stac.client.search.dto.QueryParameter;
import mil.nga.sf.geojson.FeatureConverter;
import mil.nga.sf.geojson.GeoJsonObject;
import mil.nga.sf.geojson.GeometryCollection;
import mil.nga.sf.wkt.GeometryReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.PastOrPresent;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping(value = ItemEndpoint.BASE_URL)
public class ItemEndpoint {
    public static final String BASE_URL = "/v1/items";
    public static final int MAX_RESULTS = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PlanetaryComputerService pccService;
    private final ItemMapper itemMapper;

    @Autowired
    public ItemEndpoint(
            PlanetaryComputerService pccService,
            ItemMapper itemMapper) {
        this.pccService = pccService;
        this.itemMapper = itemMapper;
    }

    /**
     * a query endpoint to get items matching the given query parameters.
     *
     * @param collections a list of ids which should be searched.
     * @param dateTimeFrom items should not be older than this date.
     * @param dateTimeTo items should not be newer than this date (default is current datetime).
     * @param aresOfInterest items should intersect with this area of interest.
     *                       the string must be formatted as well known text and
     *                       the coordinates must be given in WGS84 format (longitude, latitude).
     * @param limit an upper limit on the number of results returned (default 100)
     * @return the list of matching {@link ItemInfoDto}
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemInfoDto> getItems(
            @RequestParam @NotEmpty List<String> collections,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @PastOrPresent ZonedDateTime dateTimeFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @PastOrPresent ZonedDateTime dateTimeTo,

            @RequestParam @NotBlank String aresOfInterest,

            @RequestParam(required = false, defaultValue = "" + MAX_RESULTS) int limit
            ) throws ValidationException, NotFoundException {

        GeometryCollection collection;
        try {
            collection = wktToGeoJson(aresOfInterest);
        } catch (IOException e) {
            LOGGER.error("failed to convert wkt:{}", aresOfInterest);
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            throw new ValidationException("given wkt was invalid, please check the formatting");
        }
        return this.getItems(
                collections,
                dateTimeFrom,
                dateTimeTo,
                collection,
                limit
        );
    }

    /**
     * a query endpoint to get items matching the given query parameters.
     * since complex geometries formatted as wkt can become very long.
     * this endpoint offers an alternative to the get request.
     * the required parameters must all be passed in the request-body.
     *
     * @param collections a list of ids which should be searched.
     * @param dateTimeFrom items should not be older than this date.
     * @param dateTimeTo items should not be newer than this date (default is current datetime).
     * @param aresOfInterest items should intersect with this area of interest.
     *                       formatted as GeoJson using the WGS84 coordinate system (longitude, latitude)
     * @param limit an upper limit on the number of results returned (default 100)
     * @return the list of matching {@link ItemInfoDto}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemInfoDto> getItems(
            @RequestParam @NotEmpty List<String> collections,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @PastOrPresent ZonedDateTime dateTimeFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @PastOrPresent ZonedDateTime dateTimeTo,

            @RequestBody @NotNull GeometryCollection aresOfInterest,

            @RequestParam(defaultValue = "" + MAX_RESULTS) int limit
            ) throws ValidationException, NotFoundException {

        LOGGER.info("GET|POST " + BASE_URL + " collections={} dateFrom={} dateTo={} aresOfInterest={} limit={}",
                collections, dateTimeFrom, dateTimeTo, aresOfInterest, limit
        );

        if (dateTimeTo == null) {
            dateTimeTo = ZonedDateTime.now(ZoneId.of("UTC"));
        }

        if (dateTimeFrom.isAfter(dateTimeTo)) {
            LOGGER.debug("invalid request dateFrom({}) is before dateTo({})", dateTimeFrom, dateTimeTo);
            throw new ValidationException("the given dateFrom must be before the given dateTo");
        }

        var query = new QueryParameter();
        query.setCollections(collections);

        if (dateTimeFrom.isEqual(dateTimeTo)) {
            query.setDatetime(dateTimeFrom.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        } else {
            query.setDatetime(String.format("%s/%s",
                    dateTimeFrom.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    dateTimeTo.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            );
        }

        query.setIntersects(aresOfInterest);
        query.setLimit(limit);

        var resultList = this.pccService.getItemsByQuery(query);
        if (resultList.isEmpty()) {
            LOGGER.debug("could not find items for query: {}", query);
            throw new NotFoundException("could not find items");
        }

        var result = resultList.stream().map(itemMapper::itemToDto).toList();

        LOGGER.debug("returned {} items", result.size());
        return result;
    }

    /**
     * returns whether the given range of an item is cloudy.
     *
     * @param itemId the id of the item
     * @param aresOfInterest the area of the item to check.
     *                       the string must be formatted as well known text and
     *                       the coordinates must be given in WGS84 format (longitude, latitude).
     * @return whether the given range of an item is cloudy
     */
    @GetMapping("{itemId}/cloudy")
    @ResponseStatus(HttpStatus.OK)
    public Boolean isCloudy(
            @PathVariable @NotBlank String itemId,
            @RequestParam @NotBlank String aresOfInterest
    ){
        return Boolean.FALSE;
    }

    /**
     * returns whether the given range of an item is cloudy.
     *
     * @param itemId the id of the item
     * @param aresOfInterest the area of the item to check.
     *                       formatted as GeoJson using the WGS84 coordinate system (longitude, latitude).
     * @return whether the given range of an item is cloudy
     */
    @PostMapping("{itemId}/cloudy")
    @ResponseStatus(HttpStatus.OK)
    public Boolean isCloudy(
            @PathVariable @NotBlank String itemId,
            @RequestBody @Null GeoJsonObject aresOfInterest
    ){
        return Boolean.FALSE;
    }

    private GeometryCollection wktToGeoJson(String wkt) throws IOException {
        var geom = GeometryReader.readGeometry(wkt);
        return new GeometryCollection(
                List.of(FeatureConverter.toGeometry(geom))
        );
    }

}
