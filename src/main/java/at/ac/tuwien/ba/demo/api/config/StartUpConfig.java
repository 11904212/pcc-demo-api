package at.ac.tuwien.ba.demo.api.config;

import at.ac.tuwien.ba.demo.api.endpoint.v1.ImageEndpoint;
import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.ImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class StartUpConfig implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String AOI_WKT = "POLYGON((15.343316760000002 48.212645589999994,15.343316070000002 48.21261221,15.34436989 48.20911283000001,15.34791199 48.20942912999999,15.34667922 48.21352336999999,15.34412474 48.212866700000006,15.34412169 48.21287631000001,15.343346749999998 48.21268229000003,15.343323929999999 48.212669189999986,15.343316760000002 48.212645589999994))";

    private static final String ITEM_ID = "S2A_MSIL2A_20221111T100241_R122_T33UWP_20221111T225423";
    private final ImageEndpoint imageEndpoint;

    public StartUpConfig(ImageEndpoint imageEndpoint) {
        this.imageEndpoint = imageEndpoint;
    }


    /*
    calls an arbitrary cog at application startup.
    this initializes the geotools reader resulting in reduced response time for first queries.
    furthermore, it solves problems with concurrent COG queries.
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // enforce longitude, latitude order
        System.setProperty("org.geotools.referencing.forceXY", "true");

        // never relaeas the EPSG database to prevent long response times.
        // see: https://github.com/geotools/geotools/blob/main/modules/library/referencing/src/main/java/org/geotools/referencing/factory/epsg/ThreadedEpsgFactory.java
        System.setProperty("org.geotools.epsg.factory.timeout", "-1");
        LOGGER.info("app started, trying to fetch a random image");
        try {
            imageEndpoint.getGeoTiff(
                    ITEM_ID,
                    AOI_WKT,
                    ImageType.TCI
            );
        } catch (Exception e) {
            LOGGER.error("start up failed, terminating application.", e);
            event.getApplicationContext().close();
            return;
        }

        LOGGER.info("start up successful");
    }
}
