package com.github.cunvoas.geoserviceisochrone.util;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;

import static org.junit.jupiter.api.Assertions.*;

public class GeometryPointReducerTest {
    private static final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void testReduceLineString() {
        Coordinate[] coords = new Coordinate[] {
            new Coordinate(0, 0),
            new Coordinate(1, 0.000001), // quasi-aligné
            new Coordinate(2, 0),
            new Coordinate(3, 1), // extrême
            new Coordinate(4, 0),
            new Coordinate(5, 0),
        };
        LineString line = factory.createLineString(coords);
        GeometryPointReducer reducer = new GeometryPointReducer();
        Geometry reduced = reducer.reduce(line);
        // On attend que le point quasi-aligné soit supprimé
        assertTrue(reduced.getNumPoints() < coords.length, "Should reduce points");
        assertEquals(reduced.getGeometryType(), "LineString");
    }

    @Test
    void testReduceShortLine() {
        Coordinate[] coords = new Coordinate[] {
            new Coordinate(0, 0),
            new Coordinate(1, 1),
            new Coordinate(2, 2),
        };
        LineString line = factory.createLineString(coords);
        GeometryPointReducer reducer = new GeometryPointReducer();
        Geometry reduced = reducer.reduce(line);
        // Trop court pour réduire
        assertEquals(line, reduced);
    }

    @Test
    void testReduceNullOrEmpty() {
        GeometryPointReducer reducer = new GeometryPointReducer();
        assertNull(reducer.reduce(null));
        LineString empty = factory.createLineString(new Coordinate[]{});
        assertTrue(reducer.reduce(empty).isEmpty());
    }
}
