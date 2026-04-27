package com.github.cunvoas.geoserviceisochrone.config.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration CORS (Cross-Origin Resource Sharing).
 * <p>
 * Permet de définir les origines autorisées et les méthodes HTTP acceptées.
 * </p>
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class CorsConfig {
	
	@Value("web.cors.allowed-origins")
	private String productionSite;
	
	/**
	 * Source de configuration CORS pour l'application.
	 * @return la configuration CORS
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(productionSite));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "HEAD"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}