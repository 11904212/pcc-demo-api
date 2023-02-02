package at.ac.tuwien.ba.demo.api.endpoint.v1.dto.in;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mil.nga.sf.geojson.GeoJsonObject;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

import static at.ac.tuwien.ba.demo.api.endpoint.v1.dto.SampleConst.*;

/**
 * a dto for requesting statistics of a list of items for a given area.
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NdviStatsReqDto {

    @Schema(example = SAMPLE_ITEM_JSON_LIST)
    @NotEmpty
    private List<String> itemIds;

    @Schema(example = SAMPLE_GEOJSON)
    @NotNull
    private GeoJsonObject areaOfInterest;

}
