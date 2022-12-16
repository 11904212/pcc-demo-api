package at.ac.tuwien.ba.demo.api.endpoint.v1.validation;

import at.ac.tuwien.ba.demo.api.exception.ValidationException;
import at.ac.tuwien.ba.demo.api.util.GeoJsonToJtsConverter;
import mil.nga.sf.geojson.GeoJsonObject;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Set;

@Component
public class AreaOfIntrestValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Set<String> SUPPORTED_TYPES =
            Set.of("Polygon", "MultiPolygon", "GeometryCollection");

    private static final Double ALLOWED_AREA = 10d; // km2

    private static final Double AREA_TOLERANCE = 1.1; // 10%

    private final GeoJsonToJtsConverter geoJsonToJtsConverter;

    @Autowired
    public AreaOfIntrestValidator(
            GeoJsonToJtsConverter geoJsonToJtsConverter
    ) {
        this.geoJsonToJtsConverter = geoJsonToJtsConverter;
    }

    public void validate(GeoJsonObject geoJson) throws ValidationException {
        LOGGER.debug("validate {}", geoJson);

        if (geoJson == null) {
            throw new ValidationException("the requested area of interest was null.");
        }
        var type = geoJson.getType();
        if (type == null || !SUPPORTED_TYPES.contains(type)) {
            throw new ValidationException("the requested geoJson type not supported. use types: " + SUPPORTED_TYPES);
        }

        checkRequestedArea(geoJson);
    }

    public void validate(Geometry geometry) throws ValidationException {
        LOGGER.debug("validate {}", geometry);

        if (geometry == null) {
            throw new ValidationException("the requested area of interest was null.");
        }
        if (!geometry.isValid()) {
            throw new ValidationException("the requested area of interest is not valid.");
        }
        if (!geometry.isSimple()) {
            throw new ValidationException("the requested area of interest is not simple (has self intersections).");
        }

        checkRequestedArea(geometry);
    }

    private void checkRequestedArea(Geometry geometry) throws ValidationException {
        var area = calcArea(geometry);
        if (area > (ALLOWED_AREA * AREA_TOLERANCE * 1000000) ) {
            throw new ValidationException("the requested area of interest is to big. maximum is: " + ALLOWED_AREA + "km2");
        }
    }

    private void checkRequestedArea(GeoJsonObject geometry) throws ValidationException {
        var jtsGeom = geoJsonToJtsConverter.convertGeometry(geometry);
        checkRequestedArea(jtsGeom);
    }

    // source: https://blog.ianturton.com/geotools,/projections/2017/08/01/area-of-a-polygon.html
    private double calcArea(Geometry geometry) throws ValidationException {
        Point centroid = geometry.getCentroid();
        try {
            String code = "AUTO:42001," + centroid.getX() + "," + centroid.getY();
            CoordinateReferenceSystem auto = CRS.decode(code);

            MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);

            Geometry projed = JTS.transform(geometry, transform);
            return projed.getArea();
        } catch (MismatchedDimensionException | TransformException | FactoryException e) {
            LOGGER.debug("faild to calculate area for: {}", geometry);
            throw new ValidationException("could not calculate area of polygon for validation");
        }
    }
}
