package com.github.cunvoas.geoserviceisochrone;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Classe de base pour les tests d'intégration AVEC contexte Spring.
 * À utiliser pour tester les services avec injection de dépendances réelles.
 */
@SpringBootTest
@ActiveProfiles({"test"})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class BaseIntegrationTest {
    
}
