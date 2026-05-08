package com.github.cunvoas.geoserviceisochrone.config.property;

import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

/**
 * Configuration pour charger un fichier d’environnement externe (secret.env).
 * Permet de surcharger les propriétés de l’application avec un fichier spécifique.
 */
//@Configuration
public class EnvConfig {

	/**
	 * override .env name.
	 * @return
	 */
	@Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setLocation(new FileSystemResource("secret.env"));
        configurer.setIgnoreResourceNotFound(false);
        return configurer;
    }
}