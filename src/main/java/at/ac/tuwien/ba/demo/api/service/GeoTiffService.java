package at.ac.tuwien.ba.demo.api.service;

import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.stac.client.core.Item;
import org.locationtech.jts.geom.Geometry;

public interface GeoTiffService {

    byte[] getTciGeoTiff(Item item, Geometry aoi) throws ServiceException, NotFoundException;

    byte[] getNdviGeoTiff(Item item, Geometry aoi) throws NotFoundException, ServiceException;

}
