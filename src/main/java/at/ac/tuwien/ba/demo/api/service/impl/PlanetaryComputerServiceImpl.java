package at.ac.tuwien.ba.demo.api.service.impl;

import at.ac.tuwien.ba.demo.api.service.PlanetaryComputerService;
import at.ac.tuwien.ba.pcc.PlanetaryComputer;
import at.ac.tuwien.ba.pcc.PlanetaryComputerImpl;
import at.ac.tuwien.ba.stac.client.StacClient;
import at.ac.tuwien.ba.stac.client.core.Item;
import at.ac.tuwien.ba.stac.client.search.ItemCollection;
import at.ac.tuwien.ba.stac.client.search.dto.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PlanetaryComputerServiceImpl implements PlanetaryComputerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private PlanetaryComputer planetaryComputer;
    private StacClient stacClient;

    public PlanetaryComputerServiceImpl() {
        this.init();
    }

    private void init(){
        // TODO: move this into pcc module
        try {
            this.planetaryComputer = new PlanetaryComputerImpl();
        } catch (MalformedURLException e) {
            LOGGER.error("""
                a severe error has occurred.
                the deposited url of the planetary computers is incorrectly formatted.
                please report this incident to the developer of the module.
                """);
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        this.stacClient = this.planetaryComputer.getStacClientInstance();
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

        return results.getItems();
    }

    @Override
    public Item signItem(Item item) throws IOException {
        return this.planetaryComputer.sign(item);
    }
}
