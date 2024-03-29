package at.ac.tuwien.ba.demo.api.endpoint.v1;

import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.out.NdviStatsDto;
import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.in.NdviStatsReqDto;
import at.ac.tuwien.ba.demo.api.endpoint.v1.mapper.WktMapper;
import at.ac.tuwien.ba.demo.api.endpoint.v1.validation.AreaOfIntrestValidator;
import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.exception.ValidationException;
import at.ac.tuwien.ba.demo.api.service.ItemService;
import at.ac.tuwien.ba.demo.api.service.StatisticsService;
import at.ac.tuwien.ba.demo.api.util.GeoJsonToJtsConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.lang.invoke.MethodHandles;
import java.util.List;

import static at.ac.tuwien.ba.demo.api.endpoint.v1.dto.SampleConst.SAMPLE_ITEM_LIST;
import static at.ac.tuwien.ba.demo.api.endpoint.v1.dto.SampleConst.SAMPLE_WKT;

@RestController
@RequestMapping(StatisticsEndpoint.BASE_URL)
@Validated
public class StatisticsEndpoint {
    public static final String BASE_URL = "/v1/statistics";

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemService itemService;
    private final StatisticsService statisticsService;
    private final WktMapper wktMapper;
    private final GeoJsonToJtsConverter geoJsonToJtsConverter;
    private final AreaOfIntrestValidator areaOfIntrestValidator;

    @Autowired
    public StatisticsEndpoint(
            ItemService itemService,
            StatisticsService statisticsService,
            WktMapper wktMapper,
            GeoJsonToJtsConverter geoJsonToJtsConverter,
            AreaOfIntrestValidator areaOfIntrestValidator) {
        this.itemService = itemService;
        this.statisticsService = statisticsService;
        this.wktMapper = wktMapper;
        this.geoJsonToJtsConverter = geoJsonToJtsConverter;
        this.areaOfIntrestValidator = areaOfIntrestValidator;
    }

    /**
     * returns statistics for a given set of items.
     *
     * @param itemIds the ids of the items.
     * @param aresOfInterest items should intersect with this area of interest.
     *                       the string must be formatted as well known text and
     *                       the coordinates must be given in WGS84 format (longitude, latitude).
     *                       given area must be less the 10km2.
     * @return the list of {@link NdviStatsDto}s containing the statistics.
     *         items for which no statistic could be calculated (unknown id,
     *         no intersection with area) are excluded from the resulting list.
     */
    @GetMapping(value = "ndvi", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<NdviStatsDto> getNdviStatistics(
            @Schema(example = SAMPLE_ITEM_LIST)
            @NotEmpty
            @RequestParam List<String> itemIds,

            @Schema(example = SAMPLE_WKT)
            @NotBlank
            @RequestParam String aresOfInterest
    ) throws ValidationException, NotFoundException, ServiceException {
        LOGGER.info("GET " + BASE_URL + "/ndvi?itemIds={}", itemIds);

        var collection = wktMapper.wktToGeometryCollection(aresOfInterest);
        var jtsCollection = geoJsonToJtsConverter.convertGeometryCollection(collection);

        return getNdviStatistics(itemIds, jtsCollection);
    }

    /**
     * returns statistics for a given set of items.
     *
     * @param body the request body containing all param as the get methode.
     * @return the list of {@link NdviStatsDto}s containing the statistics.
     *         items for which no statistic could be calculated (unknown id,
     *         no intersection with area) are excluded from the resulting list.
     */
    @PostMapping(value = "ndvi", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<NdviStatsDto> getNdviStatistics(
            @Valid
            @RequestBody NdviStatsReqDto body
            ) throws NotFoundException, ServiceException, ValidationException {
        LOGGER.info("POST " + BASE_URL + "/ndvi body={}", body);

        var geom = geoJsonToJtsConverter.convertGeometry(body.getAreaOfInterest());
        Geometry[] geomArray = { geom };
        var collection = JTSFactoryFinder.getGeometryFactory().createGeometryCollection(geomArray);

        return getNdviStatistics(body.getItemIds(), collection);
    }

    private List<NdviStatsDto> getNdviStatistics(List<String> itemIds, GeometryCollection collection)
            throws NotFoundException, ServiceException, ValidationException {
        areaOfIntrestValidator.validate(collection);
        var items = this.itemService.getItemsById(itemIds);
        return statisticsService.calsNdviStatistics(items, collection);
    }

}
