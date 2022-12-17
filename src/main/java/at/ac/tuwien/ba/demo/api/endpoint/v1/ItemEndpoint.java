package at.ac.tuwien.ba.demo.api.endpoint.v1;

import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.out.ItemInfoDto;
import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.in.ItemReqDto;
import at.ac.tuwien.ba.demo.api.endpoint.v1.mapper.ItemMapper;
import at.ac.tuwien.ba.demo.api.endpoint.v1.mapper.WktMapper;
import at.ac.tuwien.ba.demo.api.endpoint.v1.validation.AreaOfIntrestValidator;
import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.exception.ValidationException;
import at.ac.tuwien.ba.demo.api.service.ItemService;
import mil.nga.sf.geojson.FeatureConverter;
import mil.nga.sf.geojson.GeoJsonObject;
import mil.nga.sf.geojson.GeometryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = ItemEndpoint.BASE_URL)
@Validated
public class ItemEndpoint {
    public static final String BASE_URL = "/v1/items";
    public static final int MAX_RESULTS = 100;
    public static final int DEFAULT_RESULTS = 10;
    public static final boolean DEFAULT_FILTER_CLOUDY = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemService itemService;

    private final ItemMapper itemMapper;
    private final WktMapper wktMapper;
    private final AreaOfIntrestValidator aoiValidator;

    @Autowired
    public ItemEndpoint(

            ItemService itemService, ItemMapper itemMapper,
            WktMapper wktMapper,
            AreaOfIntrestValidator aoiValidator
    ) {
        this.itemService = itemService;

        this.itemMapper = itemMapper;
        this.wktMapper = wktMapper;
        this.aoiValidator = aoiValidator;
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
            @NotEmpty
            @RequestParam List<String> collections,

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @PastOrPresent
            @RequestParam ZonedDateTime dateTimeFrom,

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @PastOrPresent
            @RequestParam(required = false) ZonedDateTime dateTimeTo,

            @NotBlank
            @RequestParam String aresOfInterest,

            @Positive
            @Max(value = MAX_RESULTS)
            @RequestParam(required = false) Integer limit,

            @RequestParam(required = false) Boolean filterCloudy
            ) throws ValidationException, NotFoundException, ServiceException {

        LOGGER.info("GET " + BASE_URL + " collections={} dateFrom={} dateTo={} aresOfInterest={} limit={}",
                collections, dateTimeFrom, dateTimeTo, aresOfInterest, limit
        );

        GeometryCollection collection = wktMapper.wktToGeometryCollection(aresOfInterest);

        return this.searchItems(
                collections,
                dateTimeFrom,
                Optional.ofNullable(dateTimeTo),
                collection,
                Optional.ofNullable(limit),
                Optional.ofNullable(filterCloudy)
        );
    }

    /**
     * a query endpoint to get items matching the given query parameters.
     * since complex geometries formatted as wkt can become very long.
     * this endpoint offers an alternative to the get request.
     * the required parameters must all be passed in the request-body.
     *
     * @param itemReqDto the request body containing all param as the get methode
     * @return the list of matching {@link ItemInfoDto}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemInfoDto> getItems(
            @Valid
            @NotNull
            @RequestBody ItemReqDto itemReqDto
            ) throws ValidationException, NotFoundException, ServiceException {

        LOGGER.info("POST " + BASE_URL + " body={}", itemReqDto);

        var collection = geoJsonToGeometryCollection(itemReqDto.getAreaOfInterest());
        return this.searchItems(
                itemReqDto.getCollections(),
                itemReqDto.getDateTimeFrom(),
                Optional.ofNullable(itemReqDto.getDateTimeTo()),
                collection,
                Optional.ofNullable(itemReqDto.getLimit()),
                Optional.ofNullable(itemReqDto.getFilterCloudy())
        );
    }

    private List<ItemInfoDto> searchItems(
            List<String> collections,
            ZonedDateTime dateTimeFrom,
            Optional<ZonedDateTime> dateTimeTo,
            GeometryCollection aresOfInterest,
            Optional<Integer> limit,
            Optional<Boolean> filterCloudy
    ) throws ValidationException, NotFoundException, ServiceException {

        aoiValidator.validate(aresOfInterest);

        var dateTimeToDefault = dateTimeTo.orElse(ZonedDateTime.now(ZoneId.of("UTC")));


        if (dateTimeFrom.isAfter(dateTimeToDefault)) {
            LOGGER.debug("invalid request dateFrom({}) is before dateTo({})", dateTimeFrom, dateTimeTo);
            throw new ValidationException("the given dateTimeFrom must be before the given dateTimeTo");
        }

        var items = this.itemService.getItemsInInterval(
                collections,
                dateTimeFrom,
                dateTimeToDefault,
                aresOfInterest,
                limit.orElse(DEFAULT_RESULTS),
                filterCloudy.orElse(DEFAULT_FILTER_CLOUDY)
        );

        if (items.isEmpty()) {
            LOGGER.debug("could not find items for from: {}, to: {} and aoi: {}", dateTimeFrom, dateTimeTo, aresOfInterest);
        }

        return items.stream().map(itemMapper::itemToDto).toList();
    }


    private GeometryCollection geoJsonToGeometryCollection(GeoJsonObject geoJson) {
        return new GeometryCollection(
                List.of(FeatureConverter.toGeometry(geoJson))
        );
    }

}
