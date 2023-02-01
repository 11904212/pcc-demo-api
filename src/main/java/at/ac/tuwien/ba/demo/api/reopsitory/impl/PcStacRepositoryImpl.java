package at.ac.tuwien.ba.demo.api.reopsitory.impl;

import at.ac.tuwien.ba.demo.api.exception.RepositoryException;
import at.ac.tuwien.ba.demo.api.reopsitory.PcStacRepository;
import io.github11904212.java.stac.client.StacClient;
import io.github11904212.java.stac.client.core.Item;
import io.github11904212.java.stac.client.search.ItemCollection;
import io.github11904212.java.stac.client.search.dto.QueryParameter;
import io.github11904212.pcc.PlanetaryComputerClient;
import mil.nga.sf.geojson.GeometryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PcStacRepositoryImpl implements PcStacRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PlanetaryComputerClient planetaryComputer;
    private final StacClient stacClient;

    public PcStacRepositoryImpl(
            PlanetaryComputerClient planetaryComputer
    ) {
        this.planetaryComputer = planetaryComputer;
        this.stacClient = planetaryComputer.getStacClientInstance();
    }


    @Override
    public Optional<Item> getItemById(String id) throws RepositoryException {
        var query = new QueryParameter();
        query.setIds(List.of(id));
        return Optional.ofNullable(this.getItemsByQuery(query).get(0));
    }


    @Override
    public Item signItem(Item item) throws RepositoryException {
        try {
            return this.planetaryComputer.sign(item);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public List<Item> getItemsById(List<String> itemIds) throws RepositoryException {
        var query = new QueryParameter();
        query.setIds(itemIds);

        return getItemsByQuery(query);
    }

    @Override
    public List<Item> getItemsInIntervalAndAoi(
            List<String> collections,
            ZonedDateTime dateTimeFrom,
            ZonedDateTime dateTimeTo,
            GeometryCollection aresOfInterest,
            int limit
    ) throws RepositoryException {
        var query = new QueryParameter();
        query.setCollections(collections);

        if (dateTimeFrom.isEqual(dateTimeTo)) {
            query.setDatetime(dateTimeFrom.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        } else {
            query.setDatetime(String.format("%s/%s",
                    dateTimeFrom.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    dateTimeTo.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            );
        }

        query.setIntersects(aresOfInterest);
        query.setLimit(limit);


        return getItemsByQuery(query);
    }

    private List<Item> getItemsByQuery(QueryParameter query) throws RepositoryException {
        LOGGER.debug("called with query: {}", query);

        ItemCollection results;
        try {
            results = this.stacClient.search(query);
        } catch (IOException e) {
            LOGGER.error("could not find items with query:{}", query);
            throw new RepositoryException(e);
        } catch (InterruptedException e) {
            LOGGER.error("thread got interrupted", e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
        var itemList = results.getItems();

        if (itemList != null && !itemList.isEmpty()) {
            return itemList;
        } else {
            return Collections.emptyList();
        }
    }

}
