package at.ac.tuwien.ba.demo.api.endpoint.v1.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mil.nga.sf.geojson.GeoJsonObject;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CoverageReqDto {

    @NotNull
    private GeoJsonObject areaOfInterest;

    @NotBlank
    private String itemId;

    @NotBlank
    private String imageType;

}
