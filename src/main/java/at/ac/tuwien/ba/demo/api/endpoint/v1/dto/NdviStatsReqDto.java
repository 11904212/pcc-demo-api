package at.ac.tuwien.ba.demo.api.endpoint.v1.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mil.nga.sf.geojson.GeoJsonObject;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * a dto for requesting statistics of a list of items for a given area.
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NdviStatsReqDto {

    @NotEmpty
    private List<String> itemIds;

    @NotNull
    private GeoJsonObject areaOfInterest;

}
