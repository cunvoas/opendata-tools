package com.github.cunvoas.geoserviceisochrone;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.support.PostgisContextTestBase;

/**
 * Test de sanité pour vérifier que le contexte Spring se charge correctement
 * (PostGIS via Testcontainers, ignoré si Docker est absent).
 */
@DisplayName("Tests d'intégration Spring - Sanité")
class TestApplicationContext extends PostgisContextTestBase {

    @Test
    @DisplayName("le contexte Spring se charge sans erreur")
    void test_applicationContext_loads() {
        assertNotNull(this);
    }
}
