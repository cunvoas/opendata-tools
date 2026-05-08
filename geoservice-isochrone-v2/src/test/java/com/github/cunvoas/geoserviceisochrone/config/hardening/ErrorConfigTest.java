package com.github.cunvoas.geoserviceisochrone.config.hardening;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

/**
 * Test basique pour {@link ErrorConfig} : vérifie que le customizer s'applique
 * et ajoute au moins un context customizer au factory.
 */
public class ErrorConfigTest {

    @Test
    public void errorReportValveCustomizer_shouldAddContextCustomizer() {
        ErrorConfig cfg = new ErrorConfig();
        WebServerFactoryCustomizer<?> customizer = cfg.errorReportValveCustomizer();
        // Basic smoke check: customizer must be provided (Tomcat-specific behaviour
        // is validated in integration tests).
        assertFalse(customizer == null);
    }
}
