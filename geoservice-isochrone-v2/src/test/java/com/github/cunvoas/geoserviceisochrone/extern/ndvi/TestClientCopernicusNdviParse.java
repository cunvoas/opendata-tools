package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class TestClientCopernicusNdviParse {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void lit_les_quatre_indices_de_la_meme_reponse() {
        String json = """
                {"data":[{
                  "interval":{"from":"2025-06-01T00:00:00Z","to":"2025-06-07T00:00:00Z"},
                  "outputs":{
                    "ndvi":{"bands":{"B0":{"stats":{"mean":0.6,"min":0.2,"max":0.8,"stDev":0.05,"sampleCount":120}}}},
                    "ndre":{"bands":{"B0":{"stats":{"mean":0.4,"min":0.1,"max":0.6,"stDev":0.03,"sampleCount":120}}}},
                    "gndvi":{"bands":{"B0":{"stats":{"mean":0.5,"min":0.2,"max":0.7,"stDev":0.04,"sampleCount":120}}}},
                    "savi":{"bands":{"B0":{"stats":{"mean":0.63,"min":0.15,"max":0.9,"stDev":0.06,"sampleCount":120}}}}
                  }
                }]}
                """;

        Optional<NdviStatistics> result = ClientCopernicusNdvi.parseResponse(MAPPER.readTree(json));

        assertTrue(result.isPresent());
        NdviStatistics stats = result.get();
        assertEquals(new BigDecimal("0.6"), stats.ndvi().mean());
        assertEquals(new BigDecimal("0.4"), stats.ndre().mean());
        assertEquals(new BigDecimal("0.5"), stats.gndvi().mean());
        assertEquals(new BigDecimal("0.63"), stats.savi().mean());
        assertEquals(new BigDecimal("0.06"), stats.savi().stdDev());
        assertEquals(120, stats.sampleCount());
        assertNotNull(stats.periodStart());
        assertNotNull(stats.acquisitionDate());
    }

    @Test
    void ignore_une_reponse_sans_pixel_valide() {
        String json = """
                {"data":[{"outputs":{"ndvi":{"bands":{"B0":{"stats":{"mean":null,"sampleCount":0}}}}}}]}
                """;

        assertTrue(ClientCopernicusNdvi.parseResponse(MAPPER.readTree(json)).isEmpty());
    }

    @Test
    void accepte_une_reponse_avec_savi_seul() {
        String json = """
                {"data":[{"outputs":{
                    "ndvi":{"bands":{"B0":{"stats":{"mean":null,"sampleCount":0}}}},
                    "savi":{"bands":{"B0":{"stats":{"mean":0.63,"min":0.2,"max":0.9,"stDev":0.05,"sampleCount":42}}}}
                  }}]}
                """;

        Optional<NdviStatistics> result = ClientCopernicusNdvi.parseResponse(MAPPER.readTree(json));

        assertTrue(result.isPresent());
        assertNull(result.get().ndvi());
        assertEquals(new BigDecimal("0.63"), result.get().savi().mean());
    }
}
