package at.ac.tuwien.ba.demo.api.endpoint.v1.mapper;

import at.ac.tuwien.ba.demo.api.exception.ValidationException;
import mil.nga.sf.Geometry;
import mil.nga.sf.geojson.FeatureConverter;
import mil.nga.sf.geojson.GeometryCollection;
import mil.nga.sf.util.SFException;
import mil.nga.sf.wkt.GeometryReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

@Component
public class WktMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GeometryCollection wktToGeometryCollection(String wkt) throws ValidationException {
        Geometry geom;
        try {
            geom = GeometryReader.readGeometry(wkt);
        } catch (IOException | SFException e) {
            LOGGER.error("failed to convert wkt:{}", wkt);
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            throw new ValidationException("given wkt was invalid, please check the formatting");
        }
        return new GeometryCollection(
                List.of(FeatureConverter.toGeometry(geom))
        );
    }
}
