package at.ac.tuwien.ba.demo.api.service.impl;

import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import at.ac.tuwien.ba.demo.api.service.CloudyService;
import at.ac.tuwien.ba.stac.client.core.Item;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class CloudyServiceImpl implements CloudyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    @Override
    public boolean isItemCloudy(Item item, Geometry aoi) throws ServiceException {
        LOGGER.info("check if item:{} is cloudy at aoi:{}", item, aoi);
        return false;
    }
}
