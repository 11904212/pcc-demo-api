package at.ac.tuwien.ba.demo.api.endpoint.v1.dto.in;

import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.ImageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mil.nga.sf.geojson.GeoJsonObject;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static at.ac.tuwien.ba.demo.api.endpoint.v1.dto.SampleConst.*;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class ImageReqDto {

    @Schema(example = SAMPLE_ITEM_ID)
    @NotBlank
    private String itemId;

    @Schema(example = SAMPLE_IMAGE_TYPE)
    @NotNull
    private ImageType imageType;

    @Schema(example = SAMPLE_GEOJSON)
    @NotNull
    private GeoJsonObject areaOfInterest;

}
