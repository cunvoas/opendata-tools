package com.github.cunvoas.geoserviceisochrone.config.warmup;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Warmup in case of serverless GCP hosting.
 */
@Configuration
public class WarmupConfig {
	
	/**
	 * Warmup bean for Spring context initialisation.
	 * @return warmed-up when OK.
	 */
	@Bean(name = "warmup")
	public String getWarmup() {
		return "\twarmed-up";
	}
}
