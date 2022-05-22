package at.ac.tuwien.ba.demo.api.util;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * convert geometry form mil.nga.sf.geojson to org.locationtech.jts.geom.
 */
@Component
public class GeoJsonToJtsConverter {

    private final GeometryFactory jtsFactory;

    private GeoJsonToJtsConverter(){
        this.jtsFactory = JTSFactoryFinder.getGeometryFactory();
    }

    public Geometry convertGeometry(
            mil.nga.sf.geojson.GeoJsonObject geoJson
    ) {
        if (geoJson instanceof mil.nga.sf.geojson.Point point){
            return convertPoint(point);
        } else if (geoJson instanceof mil.nga.sf.geojson.LineString lineString){
            return convertLineString(lineString);
        } else if (geoJson instanceof mil.nga.sf.geojson.Polygon polygon){
            return convertPolygon(polygon);
        } else if (geoJson instanceof mil.nga.sf.geojson.MultiPoint multiPoint){
            return convertMultiPoint(multiPoint);
        } else if (geoJson instanceof mil.nga.sf.geojson.MultiLineString multiLineString){
            return convertMultiLineString(multiLineString);
        } else if (geoJson instanceof mil.nga.sf.geojson.MultiPolygon multiPolygon){
            return convertMultiPolygon(multiPolygon);
        } else if (geoJson instanceof mil.nga.sf.geojson.GeometryCollection geometryCollection) {
            return convertGeometryCollection(
                    geometryCollection
            );
        } else if (geoJson instanceof mil.nga.sf.geojson.Feature feature) {
            return convertFeature(feature);
        } else if (geoJson instanceof mil.nga.sf.geojson.FeatureCollection featureCollection) {
            return convertFeatureCollection(featureCollection);
        }
        throw new IllegalArgumentException("unknown type");
    }

    public Point convertPoint(mil.nga.sf.geojson.Point gjPoint) {
        return jtsFactory.createPoint(
                sfPointToCoordinate(gjPoint)
        );
    }

    public LineString convertLineString(mil.nga.sf.geojson.LineString gjLineString) {
        return jtsFactory.createLineString(
                positionListToCoordinateArray(
                        gjLineString.getCoordinates()
                )
        );
    }

    public Polygon convertPolygon(mil.nga.sf.geojson.Polygon gjPolygon) {
        var sfCoords = gjPolygon.getCoordinates();
        LinearRing shell = null;
        List<LinearRing> holes = new ArrayList<>();
        for (var positions : sfCoords) {
            var coords = positionListToCoordinateArray(positions);
            var ring = jtsFactory.createLinearRing(coords);
            if (shell == null) {
                shell = ring;
            } else {
                holes.add(ring);
            }
        }
        return jtsFactory.createPolygon(
                shell,
                holes.toArray(LinearRing[]::new)
        );
    }

    public MultiPoint convertMultiPoint(mil.nga.sf.geojson.MultiPoint gjMultiPoint) {
        return jtsFactory.createMultiPoint(
                gjMultiPoint.getPoints().stream()
                        .map(this::convertPoint)
                        .toArray(Point[]::new)
        );
    }

    public MultiLineString convertMultiLineString(mil.nga.sf.geojson.MultiLineString gjMultiLineString) {
        return jtsFactory.createMultiLineString(
                gjMultiLineString.getLineStrings().stream().map(this::convertLineString)
                        .toArray(LineString[]::new)
        );
    }

    public MultiPolygon convertMultiPolygon(mil.nga.sf.geojson.MultiPolygon gjMultiPolygon) {
        return jtsFactory.createMultiPolygon(
                gjMultiPolygon.getPolygons().stream()
                        .map(this::convertPolygon)
                        .toArray(Polygon[]::new)
        );
    }

    public GeometryCollection convertGeometryCollection(
            mil.nga.sf.geojson.GeometryCollection gjGeometryCollection
    ){
        return jtsFactory.createGeometryCollection(
                gjGeometryCollection.getGeometries().stream()
                        .map(this::convertGeometry)
                        .toArray(Geometry[]::new)
        );
    }

    public Geometry convertFeature(mil.nga.sf.geojson.Feature gjFeature) {
        return convertGeometry(gjFeature.getGeometry());
    }

    public GeometryCollection convertFeatureCollection(
            mil.nga.sf.geojson.FeatureCollection gjFeatureCollection
    ) {
        return jtsFactory.createGeometryCollection(
                gjFeatureCollection.getFeatures().stream()
                        .map(this::convertFeature)
                        .toArray(Geometry[]::new)
        );
    }

    private Coordinate positionToCoordinate(mil.nga.sf.geojson.Position position) {
        if (position.hasZ()) {
            return new Coordinate(position.getX(), position.getY(), position.getZ());
        } else {
            return new Coordinate(position.getX(), position.getY());
        }
    }

    private Coordinate[] positionListToCoordinateArray(List<mil.nga.sf.geojson.Position> positionList){
        return positionList.stream()
                .map(this::positionToCoordinate)
                .toArray(Coordinate[]::new);
    }

    private Coordinate sfPointToCoordinate(mil.nga.sf.geojson.Point point) {
        var coordinates = point.getCoordinates();
        if (coordinates.hasZ()) {
            return new Coordinate(coordinates.getX(), coordinates.getY(), coordinates.getZ());
        } else {
            return new Coordinate(coordinates.getX(), coordinates.getY());
        }
    }

}

