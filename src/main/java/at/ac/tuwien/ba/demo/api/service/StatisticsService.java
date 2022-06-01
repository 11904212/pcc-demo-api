package at.ac.tuwien.ba.demo.api.service;

import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.out.NdviStatsDto;
import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.stac.client.core.Item;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

public interface StatisticsService {

    /**
     * calculates the statistics of a given {@link Item}.
     *
     * @param item the given item.
     * @param areaOfInterest the area for which the calculation shall be performed.
     * @return a {@link NdviStatsDto} containing the calculated results.
     */
    NdviStatsDto calsNdviStatistics(Item item, Geometry areaOfInterest) throws ServiceException, NotFoundException;

    /**
     * calculates the statistics of a given list of {@link Item}s.
     *
     * @param itemList the given list of items.
     * @param areaOfInterest the area for which the calculation shall be performed.
     * @return a list of {@link NdviStatsDto} containing the calculated results.
     *         items for which no statistic could be calculated (unknown id, no intersection with area)
     *         are excluded from the resulting list.
     */
    List<NdviStatsDto> calsNdviStatistics(List<Item> itemList, Geometry areaOfInterest);
}
