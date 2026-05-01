package com.github.cunvoas.geoserviceisochrone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.mockito.MockitoAnnotations;

/**
 * Classe de base pour les tests unitaires SANS contexte Spring.
 * À utiliser pour tester les services, utils et logique métier isolée.
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class BaseUnitTest {
    
    protected AutoCloseable mockResources;
    
    /**
     * Initialise les annotations Mockito avant chaque test.
     */
    @BeforeEach
    public void initializeMocks() {
        mockResources = MockitoAnnotations.openMocks(this);
    }
    
    /**
     * Nettoie les ressources après chaque test.
     */
    protected void cleanup() {
        if (mockResources != null) {
            try {
                mockResources.close();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }
}
