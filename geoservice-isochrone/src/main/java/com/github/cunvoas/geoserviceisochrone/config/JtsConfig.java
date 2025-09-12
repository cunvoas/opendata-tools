package com.github.cunvoas.geoserviceisochrone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bedatadriven.jackson.datatype.jts.JtsModule;

/**
 * Configuration pour la bibliothèque JTS (librairies géospatiales).
 */
@Configuration
public class JtsConfig {

	/**
	 * Bean de configuration pour le module JTS JSON.
	 * @return le module JSON configuré pour JTS
	 */
	@Bean
	public JtsModule jtsModule() {
		JtsModule module = new JtsModule();
//		module.addSerializer( new LocalDateTimeSerializer( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) );
//		module.addDeserializer(Date.class, new LocalDateTimeDeserializer( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) );
		
	    return module;
	}
	
}