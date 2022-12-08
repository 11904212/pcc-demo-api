package at.ac.tuwien.ba.demo.api.service;

import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.exception.ValidationException;
import io.github11904212.java.stac.client.core.Item;
import mil.nga.sf.geojson.GeometryCollection;

import java.time.ZonedDateTime;
import java.util.List;

public interface ItemService {

    /**
     * get all items with are contained in the time interval and intersect the area of interest.
     * @param collections the collections of interest.
     * @param dateTimeFrom a datetime marking the start of the time interval.
     * @param dateTimeTo a datetime marking the end of the time interval.
     * @param aresOfInterest the area which should be intersected by the items.
     * @param limit the maximum of returned items.
     * @param filterCloudy trigger filtering based cloudiness.
     * @return a list of items that meet the conditions.
     */
    List<Item> getItemsInInterval(
            List<String> collections,
            ZonedDateTime dateTimeFrom,
            ZonedDateTime dateTimeTo,
            GeometryCollection aresOfInterest,
            int limit,
            boolean filterCloudy
    ) throws ValidationException, NotFoundException, ServiceException;


    /**
     * get an item by id.
     *
     * @param id the item id.
     * @return Optional of the found item or empty if nothing was found.
     */
    Item getItemById(String id) throws NotFoundException, ServiceException;

    /**
     * get a list of entries by their IDs.
     *
     * @param itemIds the list of item IDs.
     * @return the list of corresponding items.
     * @throws NotFoundException if one or more items could not be found.
     */
    List<Item> getItemsById(List<String> itemIds) throws NotFoundException, ServiceException;


    /**
     * signs the assets of an item to make them accessible.
     *
     * @param item the {@link Item}
     * @return the signed {@link Item}
     * @throws ServiceException in case the item could not be signed.
     */
    Item signItem(Item item) throws ServiceException;
}
