package com.github.cunvoas.geoserviceisochrone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test de sanité pour vérifier que le contexte Spring se charge correctement.
 */
@DisplayName("Tests d'intégration Spring - Sanité")
@SpringBootTest
@ActiveProfiles({"test"})
class TestApplicationContext {

    @Test
    @DisplayName("le contexte Spring se charge sans erreur")
    void test_applicationContext_loads() {
        assertNotNull(this);
    }
}
