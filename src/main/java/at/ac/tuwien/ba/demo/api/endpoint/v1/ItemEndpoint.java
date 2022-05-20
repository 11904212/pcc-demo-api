package at.ac.tuwien.ba.demo.api.endpoint.v1;

import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.ItemInfoDto;
import mil.nga.sf.geojson.GeoJsonObject;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = ItemEndpoint.BASE_URL)
public class ItemEndpoint {
    public static final String BASE_URL = "/v1/items";

    /**
     * a query endpoint to get items matching the given query parameters.
     *
     * @param collections a list of ids which should be searched
     * @param dateFrom items should not be older than this date
     * @param dateTo items should not be newer than this date
     * @param aresOfInterest items should intersect with this area of interest.
     *                       the string must be formatted as well known text and
     *                       the coordinates must be given in WGS84 format (longitude, latitude).
     * @return the list of matching {@link ItemInfoDto}
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemInfoDto> getItems(
            @RequestParam List<String> collections,
            @RequestParam @PastOrPresent LocalDate dateFrom,
            @RequestParam @PastOrPresent LocalDate dateTo,
            @RequestParam @NotBlank String aresOfInterest
            ){
        return Collections.emptyList();
    }

    /**
     * a query endpoint to get items matching the given query parameters.
     * since complex geometries formatted as wkt can become very long.
     * this endpoint offers an alternative to the get request.
     * the required parameters must all be passed in the request-body.
     *
     * @param collections a list of ids which should be searched
     * @param dateFrom items should not be older than this date
     * @param dateTo items should not be newer than this date
     * @param aresOfInterest items should intersect with this area of interest.
     *                       formatted as GeoJson using the WGS84 coordinate system (longitude, latitude).
     * @return the list of matching {@link ItemInfoDto}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemInfoDto> getItems(
            @RequestParam List<String> collections,
            @RequestParam @PastOrPresent LocalDate dateFrom,
            @RequestParam @PastOrPresent LocalDate dateTo,
            @RequestBody @NotNull GeoJsonObject aresOfInterest
            ){
        return Collections.emptyList();
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

}
