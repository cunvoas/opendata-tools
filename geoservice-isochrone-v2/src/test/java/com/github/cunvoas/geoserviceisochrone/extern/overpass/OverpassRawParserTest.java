package com.github.cunvoas.geoserviceisochrone.extern.overpass;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.model.opendata.ParkOverpass;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkOverpassRepository;

import tools.jackson.databind.ObjectMapper;

class OverpassRawParserTest {
    private ObjectMapper objectMapper;
    private ParkOverpassRepository parkOverpassRepository;
    private OverpassRawParser parser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        parkOverpassRepository = mock(ParkOverpassRepository.class);
        parser = new OverpassRawParser(objectMapper, parkOverpassRepository);
    }

    @Test
    void testParseElements_Way_withDemoGeoJson() throws Exception {
        List<ParkOverpass> results = new ArrayList<>();
        try (InputStream in = getClass().getResourceAsStream("/overpass/01_way.geo.json")) {
            assertNotNull(in, "demo.geo.json should be found in test resources");
            parser.parseElements(in, results::add);
        }
        assertFalse(results.isEmpty(), "Should parse at least one ParkOverpass");
        boolean foundValid = results.stream().anyMatch(p -> p.getId() != null && p.getType() != null);
        assertTrue(foundValid, "At least one ParkOverpass should have non-null id and type");
    }

    @Test
    void testParseElements_Relation_withDemoGeoJson() throws Exception {
        List<ParkOverpass> results = new ArrayList<>();
        try (InputStream in = getClass().getResourceAsStream("/overpass/02_relation.geo.json")) {
            assertNotNull(in, "demo.geo.json should be found in test resources");
            parser.parseElements(in, results::add);
        }
        assertFalse(results.isEmpty(), "Should parse at least one ParkOverpass");
        boolean foundValid = results.stream().anyMatch(p -> p.getId() != null && p.getType() != null);
        assertTrue(foundValid, "At least one ParkOverpass should have non-null id and type");
    }


    @Test
    void testParseEntityFromPath_callsRepositorySave() throws Exception {
        Path path = Path.of(getClass().getResource("/overpass/demo.geo.json").toURI());
        parser.parseEntityFromPath(path);
        verify(parkOverpassRepository, atLeastOnce()).save(any(ParkOverpass.class));
    }

    @Test
    void testGeodeticArea_equatorSquare1deg() {
        // Carré de 1° x 1° à l'équateur (0,0)-(0,1)-(1,1)-(1,0)-(0,0)
        List<com.github.cunvoas.geoserviceisochrone.extern.overpass.common.LatLon> poly = List.of(
                latlon(0, 0),
                latlon(0, 1),
                latlon(1, 1),
                latlon(1, 0),
                latlon(0, 0)
        );
        double area = parser.geodeticArea(poly);
        // Aire attendue ≈ 12364 km² (1.2364e7 m²)
        assertTrue(area > 1.2e7 && area < 1.27e7, "Area should be close to 1.2364e7 m², got: " + area);
    }

    @Test
    void testMap_setsSurfaceForWay() {
        // Way simple : triangle sur la France
        com.github.cunvoas.geoserviceisochrone.extern.overpass.rawdto.Way way = new com.github.cunvoas.geoserviceisochrone.extern.overpass.rawdto.Way();
        way.id = 1L;
        way.type = "way";
        way.geometry = List.of(
                latlon(48.0, 2.0),
                latlon(48.0, 2.1),
                latlon(48.1, 2.05),
                latlon(48.0, 2.0)
        );
        way.tags = java.util.Map.of("leisure", "park", "name", "Test Parc");
        ParkOverpass out = parser.map(way);
        assertNotNull(out.getSurface(), "Surface should be set");
        assertTrue(out.getSurface() > 0, "Surface should be positive");
    }

    // Helper
    private static com.github.cunvoas.geoserviceisochrone.extern.overpass.common.LatLon latlon(double lat, double lon) {
        com.github.cunvoas.geoserviceisochrone.extern.overpass.common.LatLon l = new com.github.cunvoas.geoserviceisochrone.extern.overpass.common.LatLon();
        l.lat = lat;
        l.lon = lon;
        return l;
    }
}