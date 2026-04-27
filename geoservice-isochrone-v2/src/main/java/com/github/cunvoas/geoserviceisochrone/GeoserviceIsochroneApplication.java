package com.github.cunvoas.geoserviceisochrone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import tools.jackson.databind.DeserializationFeature;




/**
 * Application MAIN.
 */
@SpringBootApplication
@ComponentScan(basePackages = { 
		"com.github.cunvoas.geoserviceisochrone",
		"com.github.cunvoas.metrics"
	})
@EnableAspectJAutoProxy
public class GeoserviceIsochroneApplication {

	/**
	 * main.
	 * @param args params
	 */
	public static void main(String[] args) {
		SpringApplication.run(GeoserviceIsochroneApplication.class, args);
	}
	
	
	/**
	 * Configuration des JSON.
	 * @return
	 */
	@Bean
	public JsonMapperBuilderCustomizer jsonCustomizer() {
	    return builder -> builder
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
			.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
			.disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
			.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
			.disable(DeserializationFeature.USE_NULL_FOR_MISSING_REFERENCE_VALUES)
		;
	}
	
/*
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				List<String> ss = List.of("http://localhost:8081", "https://autmel-maps.duckdns.org");
				
				registry
					.addMapping("/map")
					// FIXME to Variablize
						.allowedOrigins("http://localhost:8081", 
										"https://autmel-maps.duckdns.org"
										);
			}
		};
	}
*/
	
}
