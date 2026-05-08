package com.github.cunvoas.geoserviceisochrone.extern.overpass;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkOverpassRepository;

import tools.jackson.databind.ObjectMapper;

class OverpassGeojsonParserTest {
    private ObjectMapper objectMapper;
    private ParkOverpassRepository parkOverpassRepository;
    private OverpassGeojsonParser parser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        parkOverpassRepository = org.mockito.Mockito.mock(ParkOverpassRepository.class);
        parser = new OverpassGeojsonParser(objectMapper, parkOverpassRepository);
    }

    @Test
    void testParseElements_withMultipolyGeojson() throws Exception {
        List<com.github.cunvoas.geoserviceisochrone.extern.overpass.geojsondto.DtoGeojsonFeature> results = new ArrayList<>();
        try (InputStream in = getClass().getResourceAsStream("/overpass/multipoly.geojson")) {
            assertNotNull(in, "multipoly.geojson should be found in test resources");
            parser.parseElements(in, results::add);
        }
        assertFalse(results.isEmpty(), "Should parse at least one feature");
        boolean foundValid = results.stream().anyMatch(f -> f.properties != null && f.geometry != null);
        assertTrue(foundValid, "At least one feature should have properties and geometry");
    }

    @Test
    void testParseEntityFromPath_callsRepositorySave() throws Exception {
        Path path = Path.of(getClass().getResource("/overpass/multipoly.geojson").toURI());
        parser.parseEntityFromFilePath(path);
        org.mockito.Mockito.verify(parkOverpassRepository, org.mockito.Mockito.atLeastOnce()).saveAll(org.mockito.Mockito.anyList());
    }
}
