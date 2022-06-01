package at.ac.tuwien.ba.demo.api.service.impl;

import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.service.CloudyService;
import at.ac.tuwien.ba.demo.api.service.CoverageService;
import at.ac.tuwien.ba.demo.api.service.PlanetaryComputerService;
import at.ac.tuwien.ba.demo.api.util.GeoJsonToJtsConverter;
import at.ac.tuwien.ba.demo.api.util.SupportedCollections;
import at.ac.tuwien.ba.stac.client.core.Item;
import org.geotools.coverage.grid.GridCoverage2D;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;

@Service
public class CloudyServiceImpl implements CloudyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PlanetaryComputerService pccService;
    private final GeoJsonToJtsConverter geoJsonToJtsConverter;
    private final CoverageService coverageService;

    @Autowired
    public CloudyServiceImpl(
            PlanetaryComputerService pccService,
            GeoJsonToJtsConverter geoJsonToJtsConverter,
            CoverageService coverageService
    ) {
        this.pccService = pccService;
        this.geoJsonToJtsConverter = geoJsonToJtsConverter;
        this.coverageService = coverageService;
    }

    @Override
    public boolean isItemCloudy(Item item, Geometry aoi) throws ServiceException {
        LOGGER.info("check if item:{} is cloudy at aoi:{}", item, aoi);

        SupportedCollections collectionInfo;
        var collectionId = item.getCollection();
        if (collectionId.isPresent()
                && SupportedCollections.map.containsKey(collectionId.get())){
            collectionInfo = SupportedCollections.map.get(collectionId.get());
        } else {
            throw new ServiceException("unsupported collection: " + item.getCollection());
        }

        if (item.getGeometry() != null) {
            var bboxGeom = this.geoJsonToJtsConverter.convertGeometry(
                    item.getGeometry()
            );
            if (!bboxGeom.intersects(aoi)) {
                LOGGER.debug("item and bbox not intersection");
                throw new ServiceException("the item({}) dose not intersect the given area of interest." + item.getId());
            }
        }

        Item signedItem = pccService.signItem(item);

        var asset = signedItem.getAsset(collectionInfo.getCloudBand());
        if (asset.isEmpty()) {
            LOGGER.error("predefined asset{} could not be found, check if api has changed"
                    , collectionInfo.getCollectionId()
            );
            throw new ServiceException("could not find a suitable cloud image");
        }

        GridCoverage2D couldImage;
        try {
            couldImage = coverageService.getCroppedCoverageFromGeotiff(new URL(asset.get().getHref()), aoi);
        } catch (IOException | FactoryException | TransformException e) {
            LOGGER.error("could not fetch tiff: {}", asset.get().getHref());
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            throw new ServiceException("could not fetch image.");
        }


        var raster = couldImage.getRenderedImage().getData();

        int numBands = raster.getNumBands();
        int height = raster.getHeight();
        int width = raster.getWidth();

        int[] pixelRow = new int[width * numBands];
        for (int i = 0; i < height; i++) {
            // read next row
            raster.getPixels(raster.getMinX(), raster.getMinY() + i, width, 1, pixelRow);

            for (int val : pixelRow) {
                if (!collectionInfo.getCloudFreePixels().contains(val)) {
                    return true;
                }
            }
        }

        return false;
    }
}
