package com.github.cunvoas.geoserviceisochrone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bedatadriven.jackson.datatype.jts.JtsModule;

/**
 * Setup for JTS (geo libs)
 */
@Configuration
public class JtsConfig {

	/**
	 * Config Bean.
	 * @return the json module configured.
	 */
	@Bean
	public JtsModule jtsModule() {
		JtsModule module = new JtsModule();
//		module.addSerializer( new LocalDateTimeSerializer( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) );
//		module.addDeserializer(Date.class, new LocalDateTimeDeserializer( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) );
		
	    return module;
	}
	
}
