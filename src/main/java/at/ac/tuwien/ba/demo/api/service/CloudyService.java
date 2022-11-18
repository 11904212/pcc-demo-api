package at.ac.tuwien.ba.demo.api.service;

import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import io.github11904212.java.stac.client.core.Item;
import org.locationtech.jts.geom.Geometry;

import java.util.List;


public interface CloudyService {

    /**
     * checks if the area of interest of an item is cloudy.
     *
     * @param item the {@link Item} to check.
     * @param aoi the area of interest
     * @return whether the item is cloudy or not.
     */
    boolean isItemCloudy(Item item, Geometry aoi) throws ServiceException;

    /**
     * filters a given list and returns a list of cloud free items.
     *
     * @param items the given list of {@link Item}s to check.
     * @param aoi the area of interest
     * @return a list of all cloud free items.
     */
    List<Item> filterCloudyItems(List<Item> items, Geometry aoi) throws ServiceException;
}
