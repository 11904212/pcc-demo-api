package at.ac.tuwien.ba.demo.api.service;

import org.geotools.coverage.grid.GridCoverage2D;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.io.IOException;
import java.net.URL;


public interface CoverageService {

    /**
     * fetches a geotiff and returns a {@link GridCoverage2D} cropped to the given area of interest
     * @param href the {@link URL} of the requested geotiff
     * @param geometryAoi the requested area of interest
     * @return the cropped {@link GridCoverage2D}
     * @throws IOException if the coverage could not be fetched.
     */
    GridCoverage2D getCroppedCoverageFromGeotiff(URL href, Geometry geometryAoi) throws IOException, FactoryException, TransformException;

    /**
     * calculates a ndvi image from a nir and red image
     * @param nir the {@link GridCoverage2D} of the near infrared image
     * @param red the {@link GridCoverage2D} of the near red image
     * @return the computed {@link GridCoverage2D} containig the ndvi value per pixel as float
     */
    GridCoverage2D calcCoverageNdvi(GridCoverage2D nir, GridCoverage2D red);

    /**
     * transforms a given {@link GridCoverage2D} into another crs.
     *
     * @param coverage2D the {@link GridCoverage2D} to convert.
     * @param epsgCodeTarget the EPSG code of the target crs.
     * @return the transformed coverage
     * @throws FactoryException if given crs is unknown.
     */
    GridCoverage2D transFromCoverage(GridCoverage2D coverage2D, String epsgCodeTarget) throws FactoryException;

    /**
     * converts a {@link GridCoverage2D} into a binary representation of it
     * @param coverage2D the {@link GridCoverage2D} to convert
     * @return the coverage in binary from
     */
    byte[] coverageToBinary(GridCoverage2D coverage2D) throws IOException;

}
