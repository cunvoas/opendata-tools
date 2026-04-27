package com.github.cunvoas.geoserviceisochrone.config.mvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import com.github.cunvoas.geoserviceisochrone.config.mvc.ThymeleafConfiguration;

/**
 * Tests unitaires simples pour {@link ThymeleafConfiguration}.
 */
public class ThymeleafConfigurationTest {

    @Test
    public void addResourceHandlers_registersExpectedHandlers() {
        ThymeleafConfiguration cfg = new ThymeleafConfiguration();
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);

        // Should not throw and should call addResourceHandler at least for known patterns
        cfg.addResourceHandlers(registry);

        // verify some expected registrations (basic smoke checks)
        verify(registry).addResourceHandler("/pub/**");
        verify(registry).addResourceHandler("/mvc/static/**");
        verify(registry).addResourceHandler("*.ico");
        verify(registry).addResourceHandler("/mvc/**");
    }
}
