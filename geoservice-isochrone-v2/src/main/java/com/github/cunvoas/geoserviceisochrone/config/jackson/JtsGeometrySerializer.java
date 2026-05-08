package com.github.cunvoas.geoserviceisochrone.config.jackson;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer Jackson 3.x pour les géométries JTS en format GeoJSON.
 */
public class JtsGeometrySerializer extends StdSerializer<Geometry> {

    public JtsGeometrySerializer() {
        super(Geometry.class);
    }

    @Override
    public void serialize(Geometry geometry, JsonGenerator gen, SerializationContext provider)
            throws JacksonException {
        if (geometry == null) {
            gen.writeNull();
            return;
        }
        try {
            writeGeometry(geometry, gen);
        } catch (JacksonException je) {
            throw je;
        } catch (Exception e) {
            throw new JacksonException(gen, "Error serializing JTS Geometry", e) {};
        }
    }

    private void writeGeometry(Geometry geometry, JsonGenerator gen) throws Exception {
        gen.writeStartObject();
        gen.writeStringProperty("type", geometry.getGeometryType());
        if (geometry instanceof Point point) {
            gen.writeName("coordinates");
            writeCoordinate(point.getX(), point.getY(), gen);
        } else if (geometry instanceof LineString ls) {
            gen.writeName("coordinates");
            gen.writeStartArray();
            for (int i = 0; i < ls.getNumPoints(); i++) {
                writeCoordinate(ls.getPointN(i).getX(), ls.getPointN(i).getY(), gen);
            }
            gen.writeEndArray();
        } else if (geometry instanceof Polygon polygon) {
            gen.writeName("coordinates");
            writePolygonCoordinates(polygon, gen);
        } else if (geometry instanceof MultiPoint mp) {
            gen.writeName("coordinates");
            gen.writeStartArray();
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Point p = (Point) mp.getGeometryN(i);
                writeCoordinate(p.getX(), p.getY(), gen);
            }
            gen.writeEndArray();
        } else if (geometry instanceof MultiLineString mls) {
            gen.writeName("coordinates");
            gen.writeStartArray();
            for (int i = 0; i < mls.getNumGeometries(); i++) {
                LineString ls = (LineString) mls.getGeometryN(i);
                gen.writeStartArray();
                for (int j = 0; j < ls.getNumPoints(); j++) {
                    writeCoordinate(ls.getPointN(j).getX(), ls.getPointN(j).getY(), gen);
                }
                gen.writeEndArray();
            }
            gen.writeEndArray();
        } else if (geometry instanceof MultiPolygon mp) {
            gen.writeName("coordinates");
            gen.writeStartArray();
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                writePolygonCoordinates((Polygon) mp.getGeometryN(i), gen);
            }
            gen.writeEndArray();
        } else if (geometry instanceof GeometryCollection gc) {
            gen.writeName("geometries");
            gen.writeStartArray();
            for (int i = 0; i < gc.getNumGeometries(); i++) {
                writeGeometry(gc.getGeometryN(i), gen);
            }
            gen.writeEndArray();
        }
        gen.writeEndObject();
    }

    private void writeCoordinate(double x, double y, JsonGenerator gen) throws Exception {
        gen.writeStartArray();
        gen.writeNumber(x);
        gen.writeNumber(y);
        gen.writeEndArray();
    }

    private void writePolygonCoordinates(Polygon polygon, JsonGenerator gen) throws Exception {
        gen.writeStartArray();
        // Exterior ring
        gen.writeStartArray();
        for (int i = 0; i < polygon.getExteriorRing().getNumPoints(); i++) {
            writeCoordinate(polygon.getExteriorRing().getPointN(i).getX(),
                    polygon.getExteriorRing().getPointN(i).getY(), gen);
        }
        gen.writeEndArray();
        // Interior rings (holes)
        for (int h = 0; h < polygon.getNumInteriorRing(); h++) {
            gen.writeStartArray();
            for (int i = 0; i < polygon.getInteriorRingN(h).getNumPoints(); i++) {
                writeCoordinate(polygon.getInteriorRingN(h).getPointN(i).getX(),
                        polygon.getInteriorRingN(h).getPointN(i).getY(), gen);
            }
            gen.writeEndArray();
        }
        gen.writeEndArray();
    }
}