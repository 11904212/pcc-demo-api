package at.ac.tuwien.ba.demo.api.endpoint.v1;

import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.in.ImageReqDto;
import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.ImageType;
import at.ac.tuwien.ba.demo.api.endpoint.v1.mapper.GeoTiffMapper;
import at.ac.tuwien.ba.demo.api.endpoint.v1.mapper.WktMapper;
import at.ac.tuwien.ba.demo.api.endpoint.v1.validation.AreaOfIntrestValidator;
import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.exception.ValidationException;
import at.ac.tuwien.ba.demo.api.service.ImageService;
import at.ac.tuwien.ba.demo.api.service.ItemService;
import at.ac.tuwien.ba.demo.api.util.GeoJsonToJtsConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.lang.invoke.MethodHandles;

import static at.ac.tuwien.ba.demo.api.endpoint.v1.dto.SampleConst.*;

@RestController
@RequestMapping(ImageEndpoint.BASE_URL)
@Validated
public class ImageEndpoint {
    public static final String BASE_URL = "/v1/images";

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final GeoJsonToJtsConverter geoJsonToJtsConverter;
    private final WktMapper wktMapper;

    private final GeoTiffMapper geoTiffMapper;

    private final ItemService itemService;
    private final ImageService imageService;
    private final AreaOfIntrestValidator aoiValidator;

    @Autowired
    public ImageEndpoint(
            GeoJsonToJtsConverter geoJsonToJtsConverter,
            WktMapper wktMapper,
            GeoTiffMapper geoTiffMapper,
            ItemService itemService,
            ImageService imageService,
            AreaOfIntrestValidator aoiValidator
    ) {
        this.geoJsonToJtsConverter = geoJsonToJtsConverter;
        this.wktMapper = wktMapper;
        this.geoTiffMapper = geoTiffMapper;
        this.itemService = itemService;
        this.imageService = imageService;
        this.aoiValidator = aoiValidator;
    }

    @GetMapping(
            value = "geotiff",
            produces = "image/tiff"
    )
    public @ResponseBody byte[] getGeoTiff(
            @Schema(example = SAMPLE_ITEM_ID)
            @NotBlank
            @RequestParam String itemId,

            @Schema(example = SAMPLE_WKT)
            @NotBlank
            @RequestParam String areaOfInterest,

            @Schema(example = SAMPLE_IMAGE_TYPE)
            @NotNull
            @RequestParam ImageType imageType
    ) throws NotFoundException, ServiceException, ValidationException {
        LOGGER.info("GET " + BASE_URL + " itemId={} areaOfInterest={} imageType={}",
                itemId, areaOfInterest, imageType
        );

        ImageReqDto dto = new ImageReqDto();
        dto.setItemId(itemId);
        var geom = wktMapper.wktToGeometryCollection(areaOfInterest);
        dto.setAreaOfInterest(geom);
        dto.setImageType(imageType);
        return fetchGeoTiff(dto);
    }

    @PostMapping (
            value = "geotiff",
            produces = "image/tiff"
    )
    public @ResponseBody byte[] getGeoTiff(
            @Valid
            @RequestBody ImageReqDto dto
    ) throws NotFoundException, ServiceException, ValidationException {
        LOGGER.info("POST " + BASE_URL + " dto={}", dto);

        return fetchGeoTiff(dto);
    }

    private byte[] fetchGeoTiff(ImageReqDto dto) throws NotFoundException, ServiceException, ValidationException {

        aoiValidator.validate(dto.getAreaOfInterest());

        var item =  this.itemService.getItemById(dto.getItemId());

        var aoi = this.geoJsonToJtsConverter.convertGeometry(dto.getAreaOfInterest());
        aoi.setSRID(4326);

        var coverage = switch (dto.getImageType()) {
            case TCI -> imageService.getTciImage(item, aoi);
            case NDVI -> imageService.getNdviImage(item, aoi);
        };

        return geoTiffMapper.coverageToBinary(coverage);
    }
}
