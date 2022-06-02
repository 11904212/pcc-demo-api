package at.ac.tuwien.ba.demo.api.service.impl;

import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.out.NdviStatsDto;
import at.ac.tuwien.ba.demo.api.exception.NotFoundException;
import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.service.ImageService;
import at.ac.tuwien.ba.demo.api.service.StatisticsService;
import at.ac.tuwien.ba.stac.client.core.Item;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ImageService imageService;

    @Autowired
    public StatisticsServiceImpl(
            ImageService imageService
    ) {
        this.imageService = imageService;
    }

    @Override
    public NdviStatsDto calsNdviStatistics(Item item, Geometry areaOfInterest) throws ServiceException, NotFoundException {

        var ndviImage = imageService.getNdviImage(item, areaOfInterest);

        var raster = ndviImage.getRenderedImage().getData();
        var height = raster.getHeight();
        var width = raster.getWidth();
        int numBands = raster.getNumBands();

        if (numBands != 1) {
            LOGGER.error("fatal error, ndvi image had mor then one band. item:{}, aoi:{}", item, areaOfInterest);
            throw new ServiceException("could not process ndvi imag of item: " + item);
        }

        float[] row = new float[width * numBands];

        float ndviMin = Float.MAX_VALUE;
        float ndviMax = Float.MIN_VALUE;
        float ndviAvg = 0f;
        long sumPixels = 0L;

        for (int i = 0; i < height; i++) {
            // read next row from raster
            raster.getPixels(raster.getMinX(), raster.getMinY() + i, width, 1, row);

            for (float pixelVal : row) {
                if (Float.isFinite(pixelVal)) {
                    ndviMin = Math.min(pixelVal, ndviMin);
                    ndviMax = Math.max(pixelVal, ndviMax);
                    ndviAvg += pixelVal;
                    sumPixels += 1;
                }
            }
        }
        if (sumPixels == 0) {
            throw new ServiceException("cant calculate a statistic for an empty ndvi image");
        }
        ndviAvg /= sumPixels;

        var dto = new NdviStatsDto();
        dto.setNdviMin(ndviMin);
        dto.setNdviMax(ndviMax);
        dto.setNdviAvg(ndviAvg);
        dto.setItemId(item.getId());

        return dto;
    }

    @Override
    public List<NdviStatsDto> calsNdviStatistics(List<Item> itemList, Geometry areaOfInterest) {

        var emptyStats = new NdviStatsDto();

        return itemList.parallelStream().map(item -> {
            try {
                return calsNdviStatistics(item, areaOfInterest);
            } catch (ServiceException | NotFoundException e) {
                LOGGER.error("error while calculation ndvi stats", e);
                return emptyStats;
            }
        }).filter(stat -> !stat.equals(emptyStats))
                .toList();
    }

}
