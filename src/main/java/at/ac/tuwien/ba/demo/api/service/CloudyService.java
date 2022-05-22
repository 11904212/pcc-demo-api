package at.ac.tuwien.ba.demo.api.service;

import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.stac.client.core.Item;
import org.locationtech.jts.geom.Geometry;


public interface CloudyService {

    /**
     * checks if the area of interest of an item is cloudy.
     *
     * @param item the {@link Item} to check.
     * @param aoi the area of interest
     * @return whether the item is cloudy or not.
     */
    boolean isItemCloudy(Item item, Geometry aoi) throws ServiceException;
}
