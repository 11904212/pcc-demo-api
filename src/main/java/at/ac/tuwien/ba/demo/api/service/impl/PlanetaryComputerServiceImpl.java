package at.ac.tuwien.ba.demo.api.service.impl;

import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.service.PlanetaryComputerService;
import io.github11904212.java.stac.client.StacClient;
import io.github11904212.java.stac.client.core.Item;
import io.github11904212.java.stac.client.search.ItemCollection;
import io.github11904212.java.stac.client.search.dto.QueryParameter;
import io.github11904212.pcc.PlanetaryComputerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PlanetaryComputerServiceImpl implements PlanetaryComputerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PlanetaryComputerClient planetaryComputer;
    private final StacClient stacClient;

    public PlanetaryComputerServiceImpl(
            PlanetaryComputerClient planetaryComputer
    ) {
        this.planetaryComputer = planetaryComputer;
        this.stacClient = planetaryComputer.getStacClientInstance();
    }


    @Override
    public Optional<Item> getItemById(String id) {
        var query = new QueryParameter();
        query.setIds(List.of(id));
        return Optional.ofNullable(this.getItemsByQuery(query).get(0));
    }

    @Override
    public List<Item> getItemsByQuery(QueryParameter query) {
        LOGGER.debug("called with query: {}", query);

        ItemCollection results;
        try {
            results = this.stacClient.search(query);
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("could not find items with query:{}", query);
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        } catch (InterruptedException e) {
            LOGGER.error("thread got interrupted");
            LOGGER.error(e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
        var itemList = results.getItems();
        //var filteredList = itemList.stream().filter(item -> )
        if (itemList != null && !itemList.isEmpty()) {
            return itemList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Item signItem(Item item) throws ServiceException {
        Item signedItem;
        try {
            signedItem = this.planetaryComputer.sign(item);
        } catch (IOException e) {
            var msg = String.format("planetary computer could not sign %s", item);
            throw new ServiceException(msg, e);
        }
        return signedItem;
    }

    @Override
    public List<Item> getItemsById(List<String> itemIds) {
        var query = new QueryParameter();
        query.setIds(itemIds);

        return getItemsByQuery(query);
    }
    
}
