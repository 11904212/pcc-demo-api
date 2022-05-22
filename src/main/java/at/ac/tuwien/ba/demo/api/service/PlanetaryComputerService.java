package at.ac.tuwien.ba.demo.api.service;

import at.ac.tuwien.ba.stac.client.core.Item;
import at.ac.tuwien.ba.stac.client.search.dto.QueryParameter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface PlanetaryComputerService {

    /**
     * get an item by id.
     *
     * @param id the item id.
     * @return Optional of the found item or empty if nothing was found.
     */
    Optional<Item> getItemById(String id);

    /**
     * get items by query.
     *
     * @param query the {@link QueryParameter}
     * @return a list of found items. could be an empty list.
     */
    List<Item> getItemsByQuery(QueryParameter query);

    /**
     * signs the assets of an item to make them accessible.
     *
     * @param item the {@link Item}
     * @return the signed {@link Item}
     * @throws IOException in case the item could not be signed.
     */
    Item signItem(Item item) throws IOException;
}
