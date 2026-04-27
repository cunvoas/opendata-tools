package com.github.cunvoas.geoserviceisochrone.config.warmup;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour le warmup en cas d'hébergement serverless sur GCP.
 * Permet d'initialiser le contexte Spring au démarrage de l'application.
 */
@Configuration
public class WarmupConfig {
	/**
	 * Bean de warmup pour l'initialisation du contexte Spring.
	 * @return "warmed-up" lorsque l'initialisation est réussie.
	 */
	@Bean(name = "warmup")
	public String getWarmup() {
		return "\twarmed-up";
	}
}