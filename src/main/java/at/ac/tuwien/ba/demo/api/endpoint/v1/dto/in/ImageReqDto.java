package at.ac.tuwien.ba.demo.api.endpoint.v1.dto.in;

import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.ImageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mil.nga.sf.geojson.GeoJsonObject;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class ImageReqDto {

    @NotBlank
    private String itemId;

    @NotNull
    private ImageType imageType;

    @NotNull
    private GeoJsonObject areaOfInterest;

}
