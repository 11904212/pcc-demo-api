package at.ac.tuwien.ba.demo.api.reopsitory;

import at.ac.tuwien.ba.demo.api.exception.RepositoryException;
import io.github11904212.java.stac.client.core.Item;
import mil.nga.sf.geojson.GeometryCollection;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface PcStacRepository {

    /**
     * get an item by id.
     *
     * @param id the item id.
     * @return Optional of the found item or empty if nothing was found.
     */
    Optional<Item> getItemById(String id) throws RepositoryException;


    /**
     * signs the assets of an item to make them accessible.
     *
     * @param item the {@link Item}
     * @return the signed {@link Item}
     */
    Item signItem(Item item) throws RepositoryException;


    /**
     * get a list of entries by their IDs.
     *
     * @param itemIds the list of item IDs.
     * @return the list of corresponding items.
     */
    List<Item> getItemsById(List<String> itemIds) throws RepositoryException;


    /**
     * get all items with are contained in the time interval and intersect the area of interest.
     * @param collections the collections of interest.
     * @param dateTimeFrom a datetime marking the start of the time interval.
     * @param dateTimeTo a datetime marking the end of the time interval.
     * @param aresOfInterest the area which should be intersected by the items.
     * @param limit the maximum of returned items.
     * @return a list of items that meet the conditions.
     */
    List<Item> getItemsInIntervalAndAoi(
            List<String> collections,
            ZonedDateTime dateTimeFrom,
            ZonedDateTime dateTimeTo,
            GeometryCollection aresOfInterest,
            int limit
    ) throws RepositoryException;
}
