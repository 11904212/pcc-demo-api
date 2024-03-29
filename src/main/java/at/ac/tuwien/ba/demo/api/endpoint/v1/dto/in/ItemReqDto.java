package at.ac.tuwien.ba.demo.api.endpoint.v1.dto.in;

import at.ac.tuwien.ba.demo.api.endpoint.v1.ItemEndpoint;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mil.nga.sf.geojson.GeoJsonObject;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import java.time.ZonedDateTime;
import java.util.List;

import static at.ac.tuwien.ba.demo.api.endpoint.v1.dto.SampleConst.*;

/**
 * a dto for item requests.
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ItemReqDto {

    @NotEmpty
    @Schema(example = SAMPLE_COLLECTION_JSON)
    private List<String> collections;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @PastOrPresent
    @Schema(example = SAMPLE_DATETIME_FROM)
    private ZonedDateTime dateTimeFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @PastOrPresent
    @Schema(example = SAMPLE_DATETIME_TO)
    private ZonedDateTime dateTimeTo;

    @Nullable
    @Positive @Max(value = ItemEndpoint.MAX_RESULTS)
    private Integer limit;

    @Nullable
    private Boolean filterCloudy;

    @NotNull
    @Schema(example = SAMPLE_GEOJSON)
    private GeoJsonObject areaOfInterest;
}
