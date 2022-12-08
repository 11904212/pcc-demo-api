package at.ac.tuwien.ba.demo.api.service;

import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import io.github11904212.java.stac.client.core.Item;
import org.geotools.coverage.grid.GridCoverage2D;
import org.locationtech.jts.geom.Geometry;

public interface ImageService {

    /**
     * returns the true color image of given area of the item encoded as geotiff.
     *
     * @param item an unsigned item.
     * @param aoi the area of interest.
     * @return the image as geotiff
     * @throws ServiceException if the item is not supported or could not be signed
     * @throws NotFoundException if the item is not part of a supported collection
     */
    byte[] getTciGeoTiff(Item item, Geometry aoi) throws ServiceException, NotFoundException;

    /**
     * returns the ndvi image of given area of the item encoded as geotiff.
     *
     * @param item an unsigned item.
     * @param aoi the area of interest.
     * @return the image as geotiff
     * @throws ServiceException if the item is not supported or could not be signed
     * @throws NotFoundException if the item is not part of a supported collection
     */
    byte[] getNdviGeoTiff(Item item, Geometry aoi) throws NotFoundException, ServiceException;

    /**
     * returns the true color image of given area of the item.
     *
     * @param item an unsigned item.
     * @param aoi the area of interest.
     * @return the image as {@link GridCoverage2D}
     * @throws ServiceException if the item is not supported or could not be signed
     * @throws NotFoundException if the item is not part of a supported collection
     */
    GridCoverage2D getTciImage(Item item, Geometry aoi) throws ServiceException, NotFoundException;

    /**
     * returns the ndvi image of given area of the item.
     *
     * @param item an unsigned item.
     * @param aoi the area of interest.
     * @return the image as {@link GridCoverage2D}
     * @throws ServiceException if the item is not supported or could not be signed
     * @throws NotFoundException if the item is not part of a supported collection
     */
    GridCoverage2D getNdviImage(Item item, Geometry aoi) throws NotFoundException, ServiceException;


}
