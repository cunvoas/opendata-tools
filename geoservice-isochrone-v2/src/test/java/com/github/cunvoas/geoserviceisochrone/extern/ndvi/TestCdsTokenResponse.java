package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

/**
 * Vérifie la désérialisation de la réponse OIDC CDSE (snake_case, champs inconnus ignorés).
 */
class TestCdsTokenResponse {

    private final ObjectMapper mapper = new ObjectMapper();

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
