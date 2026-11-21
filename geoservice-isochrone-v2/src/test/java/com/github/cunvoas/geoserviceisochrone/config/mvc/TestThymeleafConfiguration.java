package com.github.cunvoas.geoserviceisochrone.config.mvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * Tests unitaires simples pour {@link ThymeleafConfiguration}.
 */
class ThymeleafConfigurationTest {

	@Test
	void addResourceHandlers_registersExpectedHandlers() {
		ThymeleafConfiguration cfg = new ThymeleafConfiguration();
		ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
		ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
		when(registry.addResourceHandler(any(String[].class))).thenReturn(registration);
		when(registration.addResourceLocations(any(String[].class))).thenReturn(registration);

		cfg.addResourceHandlers(registry);

		verify(registry).addResourceHandler("/pub/**");
		verify(registry).addResourceHandler("/mvc/static/**");
		verify(registry).addResourceHandler("*.ico");
		verify(registry).addResourceHandler("/mvc/**");
		verify(registration, times(4)).addResourceLocations(any(String[].class));
	}
}
