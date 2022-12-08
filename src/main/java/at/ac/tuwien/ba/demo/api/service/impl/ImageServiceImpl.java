package at.ac.tuwien.ba.demo.api.service.impl;

import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.reopsitory.PcCogRepository;
import at.ac.tuwien.ba.demo.api.service.ImageProcessingService;
import at.ac.tuwien.ba.demo.api.service.ImageService;
import at.ac.tuwien.ba.demo.api.service.ItemService;
import at.ac.tuwien.ba.demo.api.util.SupportedCollections;
import io.github11904212.java.stac.client.core.Asset;
import io.github11904212.java.stac.client.core.Item;
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
public class ImageServiceImpl implements ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PcCogRepository cogRepository;

    private final ImageProcessingService processingService;
    private final ItemService itemService;

    @Autowired
    public ImageServiceImpl(
            PcCogRepository cogRepository,
            ImageProcessingService processingService,
            ItemService itemService
    ) {
        this.cogRepository = cogRepository;
        this.processingService = processingService;
        this.itemService = itemService;
    }

    @Override
    public byte[] getTciGeoTiff(Item item, Geometry aoi) throws ServiceException, NotFoundException {

        var transformedCoverage = getTciImage(item, aoi);

        return coverageToBinary(transformedCoverage);
    }

    @Override
    public byte[] getNdviGeoTiff(Item item, Geometry aoi) throws NotFoundException, ServiceException {

        var transformedCoverage = getNdviImage(item, aoi);

        return coverageToBinary(transformedCoverage);
    }

    @Override
    public GridCoverage2D getTciImage(Item item, Geometry aoi) throws ServiceException, NotFoundException {

        var collectionInfo = getCollectionInfo(item);

        var signedItem = itemService.signItem(item);

        var optAsset = signedItem.getAsset(collectionInfo.getTciBand());
        if (optAsset.isEmpty()) {
            var msg = String.format("item %s has no asset %s", signedItem, collectionInfo.getTciBand());
            throw new ServiceException(msg);
        }

        var asset = optAsset.get();

        validateAssetType(asset);

        var coverage = getGridCoverage2D(asset, aoi);

        return transformGridCoverage2D(coverage, "EPSG:3857");
    }

    @Override
    public GridCoverage2D getNdviImage(Item item, Geometry aoi) throws NotFoundException, ServiceException {

        var collectionInfo = getCollectionInfo(item);

        var signedItem = itemService.signItem(item);

        var optAssetRed = signedItem.getAsset(collectionInfo.getRedBand());
        var optAssetNir = signedItem.getAsset(collectionInfo.getNirBand());
        if (optAssetRed.isEmpty() || optAssetNir.isEmpty()) {
            var msg = String.format("item %s has no assets %s, %s",
                    signedItem, collectionInfo.getRedBand(), collectionInfo.getNirBand()
            );
            throw new ServiceException(msg);
        }

        var assetRed = optAssetRed.get();
        var assetNir = optAssetNir.get();

        validateAssetType(assetRed);
        validateAssetType(assetNir);

        var coverageRed = getGridCoverage2D(assetRed, aoi);
        var coverageNir = getGridCoverage2D(assetNir, aoi);

        var coverageNdvi = processingService.calcCoverageNdvi(coverageNir, coverageRed);

        return transformGridCoverage2D(coverageNdvi, "EPSG:3857");
    }


    private void validateAssetType(Asset asset) throws ServiceException {
        var type = asset.getType();
        if (type.isEmpty()
                || !type.get().equals("image/tiff; application=geotiff; profile=cloud-optimized")
        ){
            var msg = String.format("asset %s is not a cloud-optimized geotiff", asset);
            throw new ServiceException(msg);
        }
    }

    private GridCoverage2D getGridCoverage2D(Asset asset, Geometry aoi) throws ServiceException {
        try {
            var url = new URL(asset.getHref());
            var lazyCov = this.cogRepository.fetchCoverageFromUrl(url);
            return this.processingService.cropToAoi(lazyCov, aoi);
        } catch (IOException | FactoryException | TransformException e) {
            var msg = String.format("could not fetch geotiff from planetary computer href:%s", asset.getHref());
            throw new ServiceException(msg, e);
        }
    }

    private GridCoverage2D transformGridCoverage2D(GridCoverage2D coverage, String crsCode) throws ServiceException {
        try {
            return this.processingService.transfromCoverageToCrs(coverage, crsCode);
        } catch (FactoryException e) {
            var msg = String.format("could not transform coverage to crs:%s", crsCode);
            throw new ServiceException(msg, e);
        }
    }

    private byte[] coverageToBinary(GridCoverage2D coverage) throws ServiceException {
        try {
            return this.processingService.coverageToBinary(coverage);
        } catch (IOException e) {
            var msg = "could not return binary";
            throw new ServiceException(msg, e);
        }
    }

    private SupportedCollections getCollectionInfo(Item item) throws NotFoundException {
        var collection = item.getCollection();
        if (collection.isEmpty()
                || !SupportedCollections.map.containsKey(collection.get())
        ) {
            LOGGER.info("user requested unsupported item: {}", item);
            throw new NotFoundException("the requested item is not supported. suported collections: "
                    + SupportedCollections.map.keySet()
            );
        }

        return SupportedCollections.map.get(collection.get());
    }

}
