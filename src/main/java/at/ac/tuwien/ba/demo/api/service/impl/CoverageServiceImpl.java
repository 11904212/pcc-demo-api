package at.ac.tuwien.ba.demo.api.service.impl;

import at.ac.tuwien.ba.demo.api.service.CoverageService;
import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageioimpl.plugins.cog.CogImageInputStreamSpi;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.cog.CogSourceSPIProvider;
import it.geosolutions.imageioimpl.plugins.cog.HttpRangeReader;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.Operations;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.media.jai.RasterFactory;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;

@Service
public class CoverageServiceImpl implements CoverageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public CoverageServiceImpl() {
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    @Override
    public GridCoverage2D getCroppedCoverageFromGeotiff(URL url, Geometry geometryAoi) throws IOException, FactoryException, TransformException {
        LOGGER.debug("fetch tiff form url: {} and aoi:{}", url, geometryAoi);

        var coverage = this.getCoverage(url);

        var geomTargetCRS = transformGeometryToCoverageCrs(geometryAoi, coverage);

        return cropToGeometryIntersection(coverage, geomTargetCRS);

    }

    @Override
    public GridCoverage2D calcCoverageNdvi(GridCoverage2D nir, GridCoverage2D red) {
        LOGGER.debug("calculating ndvi image from nir:{} and red:{}", nir, red);

        var rasterNIR = nir.getRenderedImage().getData();
        var rasterRed = red.getRenderedImage().getData();

        if (
                rasterNIR.getMinX() != rasterRed.getMinX()
                        || rasterNIR.getMinY() != rasterRed.getMinY()
                        || rasterNIR.getNumBands() != rasterRed.getNumBands()
                        || rasterNIR.getHeight() != rasterRed.getHeight()
                        || rasterNIR.getWidth() != rasterRed.getWidth()
        ) {
            throw new IllegalArgumentException("given input not computable");
        }

        int numBands = rasterNIR.getNumBands();
        int height = rasterNIR.getHeight();
        int width = rasterNIR.getWidth();

        int[] pixelRowNIR = new int[width * numBands];
        int[] pixelRowRed = new int[width * numBands];
        float[][] matrixNdvi = new float[height][width];
        for (int i = 0; i < height; i++) {
            // read next row from nir and red
            rasterNIR.getPixels(rasterNIR.getMinX(), rasterNIR.getMinY() + i, width, 1, pixelRowNIR);
            rasterRed.getPixels(rasterRed.getMinX(), rasterRed.getMinY() + i, width, 1, pixelRowRed);

            // compute ndvi
            for (int k=0; k<pixelRowNIR.length; k++){
                float valNIR;
                float valRed;
                valNIR = pixelRowNIR[k];
                valRed = pixelRowRed[k];
                matrixNdvi[i][k] = ( valNIR - valRed ) / ( valNIR + valRed );

            }
        }

        var factory = new GridCoverageFactory();
        var envelop = nir.getEnvelope();

        return factory.create(
                "ndvi",
                matrixNdvi,
                envelop
        );
    }

    @Override
    public GridCoverage2D transFromCoverage(GridCoverage2D coverage2D, String epsgCodeTarget) throws FactoryException {
        LOGGER.debug("transform coverage:{} to target crs:{}", coverage2D, epsgCodeTarget);

        CoordinateReferenceSystem targetCRS = CRS.decode(epsgCodeTarget);
        return  (GridCoverage2D) Operations.DEFAULT.resample(coverage2D, targetCRS);
    }


    @Override
    public byte[] coverageToBinary(GridCoverage2D coverage2D) throws IOException {
        LOGGER.debug("writing binary file of {}", coverage2D);

        var stream = new ByteArrayOutputStream();
        var writer = new GeoTiffWriter(stream);
        writer.write(coverage2D, null);
        writer.dispose();
        return stream.toByteArray();
    }

    private GridCoverage2D getCoverage(URL url) throws IOException {
        BasicAuthURI cogUri = new BasicAuthURI(url, false);
        HttpRangeReader rangeReader =
                new HttpRangeReader(cogUri.getUri(), CogImageReadParam.DEFAULT_HEADER_LENGTH);
        CogSourceSPIProvider input =
                new CogSourceSPIProvider(
                        cogUri,
                        new CogImageReaderSpi(),
                        new CogImageInputStreamSpi(),
                        rangeReader.getClass().getName());

        GeoTiffReader reader = new GeoTiffReader(input);

        return reader.read(null);
    }

    private Geometry transformGeometryToCoverageCrs(Geometry geometry, GridCoverage2D coverage) throws FactoryException, TransformException {
        CoordinateReferenceSystem sourceCRS;
        if (geometry.getSRID() != 0) {
            sourceCRS = CRS.decode("EPSG:" + geometry.getSRID());
        } else {
            sourceCRS = CRS.decode("EPSG:4326");
        }

        CoordinateReferenceSystem targetCRS = coverage.getCoordinateReferenceSystem();
        MathTransform mathTransform = CRS.findMathTransform(sourceCRS, targetCRS);

        return JTS.transform(geometry, mathTransform);
    }

    private GridCoverage2D cropToEnvelope(GridCoverage2D coverage, ReferencedEnvelope envelope) {
        Operations ops = new Operations(null);
        return  (GridCoverage2D) ops.crop(coverage, envelope);
    }

    private GridCoverage2D cropToGeometryIntersection(GridCoverage2D coverage, Geometry geometry) throws TransformException {

        ReferencedEnvelope envelope = new ReferencedEnvelope(geometry.getEnvelopeInternal(), coverage.getCoordinateReferenceSystem());

        var coveragePreCrop = cropToEnvelope(coverage, envelope);

        var geomGrid = JTS.transform(geometry, coveragePreCrop.getGridGeometry().getCRSToGrid2D());

        var jtsFactory = JTSFactoryFinder.getGeometryFactory();

        var raster = coveragePreCrop.getRenderedImage().getData();

        int numBands = raster.getNumBands();
        int height = raster.getHeight();
        int width = raster.getWidth();
        int offsetX = raster.getMinX();
        int offsetY = raster.getMinY();

        WritableRaster cropRaster = RasterFactory.createBandedRaster(
                raster.getDataBuffer().getDataType(), width, height, numBands, null
        );
        double[] pixelRow = new double[width * numBands];
        for (int relY = 0; relY < height; relY++) {

            raster.getPixels(raster.getMinX(), raster.getMinY() + relY, width, 1, pixelRow);

            for (int relX = 0; relX < width; relX++) {
                var absX = offsetX + relX;
                var absY = offsetY + relY;
                var coordinates = new Coordinate[5];
                coordinates[0] = new Coordinate(absX - 0.5, absY + 0.5);
                coordinates[1] = new Coordinate(absX + 0.5, absY + 0.5);
                coordinates[2] = new Coordinate(absX + 0.5, absY - 0.5);
                coordinates[3] = new Coordinate(absX - 0.5, absY - 0.5);
                coordinates[4] = coordinates[0];
                var outlinePixel = jtsFactory.createLinearRing(coordinates);
                boolean keepPixel = geomGrid.intersects(outlinePixel);
                int pixelPosRaster = (numBands * relX);
                for (int m = 0; m < numBands; m++){
                    if (keepPixel) {
                        cropRaster.setSample(relX, relY, m, pixelRow[pixelPosRaster+m]);
                    } else {
                        cropRaster.setSample(relX, relY, m, 0);
                    }

                }
            }
        }

        var factory = new GridCoverageFactory();

        return factory.create(
                coveragePreCrop.getName(),
                cropRaster,
                coveragePreCrop.getEnvelope()
        );
    }
}
