package at.ac.tuwien.ba.demo.api.service.impl;

import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.service.CoverageService;
import at.ac.tuwien.ba.demo.api.service.GeoTiffService;
import at.ac.tuwien.ba.demo.api.service.PlanetaryComputerService;
import at.ac.tuwien.ba.demo.api.util.SupportedCollections;
import at.ac.tuwien.ba.stac.client.core.Asset;
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
public class GeoTiffServiceImpl implements GeoTiffService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CoverageService coverageService;
    private final PlanetaryComputerService planetaryComputerService;

    @Autowired
    public GeoTiffServiceImpl(
            CoverageService coverageService,
            PlanetaryComputerService planetaryComputerService
    ) {
        this.coverageService = coverageService;
        this.planetaryComputerService = planetaryComputerService;
    }

    @Override
    public byte[] getTciGeoTiff(Item item, Geometry aoi) throws ServiceException, NotFoundException {

        var collectionInfo = getCollectionInfo(item);

        var signedItem = signeItem(item);

        var optAsset = signedItem.getAsset(collectionInfo.getTciBand());
        if (optAsset.isEmpty()) {
            var msg = String.format("item %s has no asset %s", signedItem, collectionInfo.getTciBand());
            throw new ServiceException(msg);
        }

        var asset = optAsset.get();

        validateAssetType(asset);

        var coverage = getGridCoverage2D(asset, aoi);

        var transformedCoverage = transformGridCoverage2D(coverage, "EPSG:3857");

        return coverageToBinary(transformedCoverage);
    }

    @Override
    public byte[] getNdviGeoTiff(Item item, Geometry aoi) throws NotFoundException, ServiceException {

        var collectionInfo = getCollectionInfo(item);

        var signedItem = signeItem(item);

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

        var coverageNdvi = coverageService.calcCoverageNdvi(coverageNir, coverageRed);

        var transformedCoverage = transformGridCoverage2D(coverageNdvi, "EPSG:3857");

        return coverageToBinary(transformedCoverage);
    }

    private Item signeItem(Item item) throws ServiceException {
        Item signedItem;
        try {
            signedItem = this.planetaryComputerService.signItem(item);
        } catch (IOException e) {
            var msg = String.format("planetary computer could not sign %s", item);
            throw new ServiceException(msg, e);
        }
        return signedItem;
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
            return this.coverageService.getCroppedCoverageFromGeotiff(url, aoi);
        } catch (IOException | FactoryException | TransformException e) {
            var msg = String.format("could not fetch geotiff from planetary computer href:%s", asset.getHref());
            throw new ServiceException(msg, e);
        }
    }

    private GridCoverage2D transformGridCoverage2D(GridCoverage2D coverage, String crsCode) throws ServiceException {
        try {
            return this.coverageService.transFromCoverage(coverage, crsCode);
        } catch (FactoryException e) {
            var msg = String.format("could not transform coverage to crs:%s", crsCode);
            throw new ServiceException(msg, e);
        }
    }

    private byte[] coverageToBinary(GridCoverage2D coverage) throws ServiceException {
        try {
            return this.coverageService.coverageToBinary(coverage);
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
