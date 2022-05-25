package at.ac.tuwien.ba.demo.api.endpoint.v1;

import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.ImageReqDto;
import at.ac.tuwien.ba.demo.api.endpoint.v1.mapper.WktMapper;
import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.exception.ValidationException;
import at.ac.tuwien.ba.demo.api.service.CoverageService;
import at.ac.tuwien.ba.demo.api.service.PlanetaryComputerService;
import at.ac.tuwien.ba.demo.api.util.GeoJsonToJtsConverter;
import at.ac.tuwien.ba.stac.client.core.Item;
import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;

@RestController
@RequestMapping(ImageEndpoint.BASE_URL)
@Validated
@CrossOrigin
public class ImageEndpoint {
    public static final String BASE_URL = "/v1/images";

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CoverageService coverageService;
    private final PlanetaryComputerService planetaryComputerService;
    private final GeoJsonToJtsConverter geoJsonToJtsConverter;
    private final WktMapper wktMapper;

    @Autowired
    public ImageEndpoint(
            CoverageService coverageService,
            PlanetaryComputerService planetaryComputerService,
            GeoJsonToJtsConverter geoJsonToJtsConverter,
            WktMapper wktMapper
    ) {
        this.coverageService = coverageService;
        this.planetaryComputerService = planetaryComputerService;
        this.geoJsonToJtsConverter = geoJsonToJtsConverter;
        this.wktMapper = wktMapper;
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
            @RequestParam String imageType
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

        Item signedItem;
        try {
            signedItem = this.planetaryComputerService.signItem(optItem.get());
        } catch (IOException e) {
            var msg = String.format("planetary computer could not sign %s", optItem.get());
            throw this.createServiceError(msg, e);
        }

        var optAsset = signedItem.getAsset(dto.getImageType());
        if (optAsset.isEmpty()) {
            var msg = String.format("item %s has no asset %s", signedItem, dto.getImageType());
            LOGGER.info(msg);
            throw new NotFoundException(msg);
        }
        var asset = optAsset.get();
        var type = asset.getType();
        if (type.isEmpty()
                || !type.get().equals("image/tiff; application=geotiff; profile=cloud-optimized")
        ){
            var msg = String.format("asset %s is not a geotiff", dto.getImageType());
            LOGGER.info(msg);
            throw new NotFoundException(msg);
        }

        var aoi = this.geoJsonToJtsConverter.convertGeometry(dto.getAreaOfInterest());
        aoi.setSRID(4326);

        GridCoverage2D coverage;
        try {
            var url = new URL(asset.getHref());
            coverage = this.coverageService.getCroppedCoverageFromGeotiff(url, aoi);
        } catch (IOException | FactoryException | TransformException e) {
            var msg = String.format("could not fetch geotiff from planetary computer href:%s", asset.getHref());
            throw this.createServiceError(msg, e);
        }

        GridCoverage2D transformedCoverage;
        var crsCode = "EPSG:3857";
        try {
            transformedCoverage = this.coverageService.transFromCoverage(coverage, crsCode);
        } catch (FactoryException e) {
            var msg = String.format("could not transform coverage to crs:%s", crsCode);
            throw this.createServiceError(msg, e);
        }

        try {
            return this.coverageService.coverageToBinary(transformedCoverage);
        } catch (IOException e) {
            var msg = "could not return binary";
            throw this.createServiceError(msg, e);
        }
    }

    private ServiceException createServiceError(String msg, Exception e){
        LOGGER.error(msg);
        LOGGER.error(e.getMessage());
        e.printStackTrace();
        return new ServiceException(msg, e);
    }

}
