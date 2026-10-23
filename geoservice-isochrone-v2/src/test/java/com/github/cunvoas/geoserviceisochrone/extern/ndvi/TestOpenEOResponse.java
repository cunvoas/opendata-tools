package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

/**
 * Tests de la désérialisation Jackson des records du package ndvi
 * (mise en correspondance des noms de champs JSON et tolérance aux champs inconnus).
 */
class TestOpenEOResponse {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserialise_result_et_ndviMean() throws Exception {
        OpenEOResponse response = mapper.readValue(
            "{\"result\":{\"ndvi\":0.42},\"ndviMean\":0.42}", OpenEOResponse.class);

        assertEquals(0.42, response.ndviMean());
        assertEquals(0.42, response.result().get("ndvi"));
    }

    @Test
    void ignore_les_champs_JSON_inconnus() throws Exception {
        OpenEOResponse response = mapper.readValue(
            "{\"ndviMean\":0.75,\"unknownField\":\"x\",\"other\":123}", OpenEOResponse.class);

        assertEquals(0.75, response.ndviMean());
        assertNull(response.result());
    }

    @Test
    void deserialise_token_OIDC_keycloak_champ_snake_case() throws Exception {
        // Réponse réelle Keycloak : champs supplémentaires à ignorer + snake_case
        CdsTokenResponse token = mapper.readValue(
            "{\"access_token\":\"jwt-123\",\"expires_in\":600,\"token_type\":\"Bearer\","
                + "\"refresh_expires_in\":1800,\"scope\":\"openid\"}",
            CdsTokenResponse.class);

        assertEquals("jwt-123", token.accessToken());
        assertEquals(600L, token.expiresIn());
    }
}
