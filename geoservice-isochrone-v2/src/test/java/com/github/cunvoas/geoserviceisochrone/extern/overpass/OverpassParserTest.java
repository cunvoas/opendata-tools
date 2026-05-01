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

class OverpassParserTest {
    private ObjectMapper objectMapper;
    private ParkOverpassRepository parkOverpassRepository;
    private OverpassParser parser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        parkOverpassRepository = mock(ParkOverpassRepository.class);
        parser = new OverpassParser(objectMapper, parkOverpassRepository);
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

    // ...autres tests unitaires sur map, mapPolygon, mapMultiPolygon, etc. à ajouter pour la couverture
}
