package com.github.cunvoas.geoserviceisochrone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Classe de base abstraite pour tous les tests.
 * Gère l'initialisation de Mockito et la configuration commune.
 */
@SpringBootTest
@ActiveProfiles({"test"})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class BaseTest {
    
    /**
     * Initialise les annotations Mockito avant chaque test.
     * Appelée automatiquement par JUnit 5.
     */
    @BeforeEach
    public void initializeMocks() {
        MockitoAnnotations.openMocks(this);
    }
}
