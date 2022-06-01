package at.ac.tuwien.ba.demo.api.endpoint.v1;

import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.ImageReqDto;
import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.ImageType;
import at.ac.tuwien.ba.demo.api.endpoint.v1.mapper.WktMapper;
import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.exception.ValidationException;
import at.ac.tuwien.ba.demo.api.service.ImageService;
import at.ac.tuwien.ba.demo.api.service.PlanetaryComputerService;
import at.ac.tuwien.ba.demo.api.util.GeoJsonToJtsConverter;
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
import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(ImageEndpoint.BASE_URL)
@Validated
public class ImageEndpoint {
    public static final String BASE_URL = "/v1/images";

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PlanetaryComputerService planetaryComputerService;
    private final GeoJsonToJtsConverter geoJsonToJtsConverter;
    private final WktMapper wktMapper;
    private final ImageService imageService;

    @Autowired
    public ImageEndpoint(
            PlanetaryComputerService planetaryComputerService,
            GeoJsonToJtsConverter geoJsonToJtsConverter,
            WktMapper wktMapper,
            ImageService imageService
    ) {
        this.planetaryComputerService = planetaryComputerService;
        this.geoJsonToJtsConverter = geoJsonToJtsConverter;
        this.wktMapper = wktMapper;
        this.imageService = imageService;
    }

    @GetMapping(
            value = "geotiff",
            produces = "image/tiff"
    )
    public @ResponseBody byte[] getGeoTiff(
            @NotBlank
            @RequestParam String itemId,

            @NotBlank
            @RequestParam String areaOfInterest,

            @NotBlank
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
        return getGeoTiff(dto);
    }

    @PostMapping (
            value = "geotiff",
            produces = "image/tiff"
    )
    public @ResponseBody byte[] getGeoTiff(
            @Valid
            @RequestBody ImageReqDto dto
    ) throws NotFoundException, ServiceException {
        LOGGER.info("POST " + BASE_URL + " dto={}", dto);

        var optItem = this.planetaryComputerService.getItemById(dto.getItemId());
        if (optItem.isEmpty()) {
            var msg = "could not find item with id: " + dto.getItemId();
            LOGGER.info(msg);
            throw new NotFoundException(msg);
        }

        var item =  optItem.get();

        var aoi = this.geoJsonToJtsConverter.convertGeometry(dto.getAreaOfInterest());
        aoi.setSRID(4326);

        return switch (dto.getImageType()) {
            case TCI -> imageService.getTciGeoTiff(item, aoi);
            case NDVI -> imageService.getNdviGeoTiff(item, aoi);
        };
    }
}
