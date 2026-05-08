package com.github.cunvoas.geoserviceisochrone.config.jackson;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.SerializationContext;

/**
 * Tests d'intégration légers pour vérifier la sérialisation GeoJSON des géométries JTS
 * via le module configuré dans {@link JacksonConfig}.
 */
public class JtsGeometrySerializerTest {

    private JsonFactory jsonFactory;
    private GeometryFactory gf;

    @BeforeEach
    public void setUp() {
        // We'll exercise the serializer directly using a JsonFactory to avoid
        // depending on ObjectMapper wiring in unit tests.
        jsonFactory = new JsonFactory();

        gf = new GeometryFactory();
    }

    @Test
    public void serializePoint_shouldContainTypeAndCoordinates() throws Exception {
        Point p = gf.createPoint(new Coordinate(1.5, 2.5));
        String json = serializeWithSerializer(p);
        assertTrue(json.contains("\"type\":\"Point\""), "should contain type Point");
        assertTrue(json.contains("\"coordinates\""), "should contain coordinates");
        assertTrue(json.contains("1.5") && json.contains("2.5"), "should contain coordinates values");
    }

    @Test
    public void serializePolygon_shouldContainTypeAndCoordinates() throws Exception {
        Coordinate[] coords = new Coordinate[] { new Coordinate(0,0), new Coordinate(1,0), new Coordinate(1,1), new Coordinate(0,0) };
        Polygon poly = gf.createPolygon(gf.createLinearRing(coords), null);
        String json = serializeWithSerializer(poly);
        assertTrue(json.contains("\"type\":\"Polygon\""), "should contain type Polygon");
        assertTrue(json.contains("\"coordinates\""), "should contain coordinates");
        assertTrue(json.contains("0.0") && json.contains("1.0"), "should contain coordinate values");
    }

    private String serializeWithSerializer(org.locationtech.jts.geom.Geometry geom) throws Exception {
        java.io.StringWriter writer = new java.io.StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(writer);
        JtsGeometrySerializer serializer = new JtsGeometrySerializer();
        // pass null for SerializationContext; serializer implementation here
        // does not use it directly.
        serializer.serialize(geom, gen, (SerializationContext) null);
        gen.flush();
        return writer.toString();
    }

}
