package at.ac.tuwien.ba.demo.api.service;

import org.geotools.coverage.grid.GridCoverage2D;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;


public interface ImageProcessingService {

    /**
     * crops a {@link GridCoverage2D} to a given area of interest
     * @param coverage2D the {@link GridCoverage2D} to be cropped.
     * @param geometryAoi the area of interest as {@link Geometry}, crs (SRID) will be used if set
     *                    otherwise WGS84 will be assumed.
     * @return the cropped {@link GridCoverage2D}
     * @throws FactoryException if given crs is unknown.
     * @throws TransformException if the transformation to the aoi crs failed.
     */
    GridCoverage2D cropToAoi(GridCoverage2D coverage2D, Geometry geometryAoi) throws FactoryException, TransformException;

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
    GridCoverage2D transfromCoverageToCrs(GridCoverage2D coverage2D, String epsgCodeTarget) throws FactoryException;

}
