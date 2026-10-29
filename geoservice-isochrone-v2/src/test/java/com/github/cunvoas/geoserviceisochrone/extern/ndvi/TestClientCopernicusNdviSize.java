package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

class TestClientCopernicusNdviSize {

    private final GeometryFactory factory = new GeometryFactory();

    @Test
    void calcule_la_taille_a_10_metres() {
        Polygon parc = factory.createPolygon(new Coordinate[] {
                new Coordinate(3.000, 50.600),
                new Coordinate(3.014, 50.600),
                new Coordinate(3.014, 50.609),
                new Coordinate(3.000, 50.609),
                new Coordinate(3.000, 50.600)
        });

        int[] size = ClientCopernicusNdvi.requestSize(parc);

        assertTrue(size[0] >= 98 && size[0] <= 100, "largeur ~990 m à 10 m : " + size[0]);
        assertEquals(100, size[1], "hauteur ~995 m à 10 m");
    }

    @Test
    void plafonne_la_taille_a_512_pixels() {
        Polygon grand = factory.createPolygon(new Coordinate[] {
                new Coordinate(0.0, 0.0),
                new Coordinate(10.0, 0.0),
                new Coordinate(10.0, 10.0),
                new Coordinate(0.0, 10.0),
                new Coordinate(0.0, 0.0)
        });

        int[] size = ClientCopernicusNdvi.requestSize(grand);

        assertEquals(512, size[0]);
        assertEquals(512, size[1]);
    }
}
