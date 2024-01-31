package com.github.cunvoas.geoserviceisochrone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bedatadriven.jackson.datatype.jts.JtsModule;

@Configuration
public class JtsConfig {

	@Bean
	public JtsModule jtsModule() {
		JtsModule module = new JtsModule();
//		module.addSerializer( new LocalDateTimeSerializer( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) );
//		module.addDeserializer(Date.class, new LocalDateTimeDeserializer( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) );
		
	    return module;
	}
	
//	@Bean
//	public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
//		Module module = null;//new GeolatteGeomModule( WGS84 );
//		
//		return new Jackson2ObjectMapperBuilder()
////				.modules( module )
//				.serializers( new LocalDateTimeSerializer( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) )
//				.deserializers( new LocalDateTimeDeserializer( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) );
//	}
}
