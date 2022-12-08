package at.ac.tuwien.ba.demo.api.service.impl;

import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.RepositoryException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.exception.ValidationException;
import at.ac.tuwien.ba.demo.api.reopsitory.PcStacRepository;
import at.ac.tuwien.ba.demo.api.service.CloudyService;
import at.ac.tuwien.ba.demo.api.service.ItemService;
import at.ac.tuwien.ba.demo.api.util.GeoJsonToJtsConverter;
import io.github11904212.java.stac.client.core.Item;
import mil.nga.sf.geojson.GeometryCollection;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ItemServiceImpl implements ItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PcStacRepository pcStacRepository;
    private final CloudyService cloudyService;

    private final GeoJsonToJtsConverter geoJsonToJtsConverter;

    public ItemServiceImpl(
            PcStacRepository pcStacRepository,
            CloudyService cloudyService,
            GeoJsonToJtsConverter geoJsonToJtsConverter
    ) {
        this.pcStacRepository = pcStacRepository;
        this.cloudyService = cloudyService;
        this.geoJsonToJtsConverter = geoJsonToJtsConverter;
    }


    @Override
    public List<Item> getItemsInInterval(
            List<String> collections,
            ZonedDateTime dateTimeFrom,
            ZonedDateTime dateTimeTo,
            GeometryCollection aresOfInterest,
            int limit,
            boolean filterCloudy
    ) throws ValidationException, ServiceException {

        List<Item> resultList;
        try {
            resultList = this.pcStacRepository.getItemsInIntervalAndAoi(
                    collections,
                    dateTimeFrom,
                    dateTimeTo,
                    aresOfInterest,
                    limit
            );
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        }

        if (filterCloudy) {
            resultList = this.cloudyService.filterCloudyItems(
                    resultList,
                    convertToJtsGeometry(aresOfInterest)
            );
        }

        LOGGER.debug("returned {} items", resultList.size());

        return resultList;
    }

    @Override
    public Item getItemById(String id) throws NotFoundException, ServiceException {
        Optional<Item> optItem;
        try {
            optItem = this.pcStacRepository.getItemById(id);
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        }

        if (optItem.isEmpty()) {
            var msg = "could not find item with id: " + id;
            LOGGER.info(msg);
            throw new NotFoundException(msg);
        }
        return optItem.get();
    }

    @Override
    public List<Item> getItemsById(List<String> itemIds) throws NotFoundException, ServiceException {

        List<Item> items;
        try {
            items = this.pcStacRepository.getItemsById(itemIds);
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        }

        if (items.size() != itemIds.size()) {
            Set<String> idSet = new HashSet<>(itemIds);
            items.forEach(item -> idSet.remove(item.getId()));
            LOGGER.debug("could not find item with ids: {}", idSet);
            throw new NotFoundException("could not find the items with IDs: " + idSet);
        } else {
            return items;
        }
    }

    @Override
    public Item signItem(Item item) throws ServiceException {
        Item signedItem;
        try {
            signedItem = this.pcStacRepository.signItem(item);
        } catch (RepositoryException e) {
            var msg = String.format("planetary computer could not sign %s", item);
            throw new ServiceException(msg, e);
        }
        return signedItem;
    }

    private Geometry convertToJtsGeometry(GeometryCollection aresOfInterest) throws ValidationException {
        try {
            return this.geoJsonToJtsConverter.convertGeometryCollection(aresOfInterest);
        } catch (IllegalArgumentException e) {
            LOGGER.error("could not convert GeoJson to JTS. geoJson:{}", aresOfInterest);
            throw new ValidationException("error while converting aresOfInterest. plead check the formatting again.");
        }
    }
}
