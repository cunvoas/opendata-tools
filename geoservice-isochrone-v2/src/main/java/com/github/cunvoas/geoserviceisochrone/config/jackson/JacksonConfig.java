package com.github.cunvoas.geoserviceisochrone.config.jackson;

import org.locationtech.jts.geom.Geometry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.module.SimpleModule;

/**
 * Configuration Jackson : enregistre le serializer JTS pour les géométries GeoJSON.
 * Le module est exposé comme bean Spring : Spring Boot l'enregistre automatiquement
 * dans l'ObjectMapper via JacksonAutoConfiguration.
 * @see https://docs.spring.io/spring-boot/reference/features/json.html#features.json.jackson.custom-serializers-and-deserializers
 * @see https://github.com/spring-projects/spring-data-mongodb/issues/5100
 */
@Configuration
public class JacksonConfig {

	/**
	 * Module JTS exposé comme bean : Spring Boot l'auto-enregistre dans l'ObjectMapper.
	 * Le ValueSerializerModifier garantit que JtsGeometrySerializer est utilisé pour
	 * TOUS les sous-types concrets de Geometry (GeometryCollection, MultiPolygon, etc.).
	 * NB: @EnableWebMvc ne doit PAS être présent dans la configuration MVC,
	 * sinon Spring MVC crée son propre ObjectMapper ignorant ce module.
	 */
	@Bean
	public SimpleModule jtsGeoJsonModule() {
		SimpleModule module = new SimpleModule("JtsGeoJsonModule");
		module.addSerializer(Geometry.class, new JtsGeometrySerializer());
		module.setSerializerModifier(new GeometrySerializerModifier());
		return module;
	}
}