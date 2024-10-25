package com.github.cunvoas.geoserviceisochrone;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;



@SpringBootApplication
@ComponentScan(basePackages = { "com.github.cunvoas.geoserviceisochrone" })
@EnableAspectJAutoProxy
public class GeoserviceIsochroneApplication {

		
	public static void main(String[] args) {
		SpringApplication.run(GeoserviceIsochroneApplication.class, args);
	}

/*    
	-- https://reflectoring.io/spring-cors/
	
	@Bean
	public WebMvcConfigurer corsMappingConfigurer() {
	   return new WebMvcConfigurer() {
	       @Override
	       public void addCorsMappings(CorsRegistry registry) {
	           WebConfigProperties.Cors cors = webConfigProperties.getCors();
	           registry.addMapping("/**")
	             .allowedOrigins(cors.getAllowedOrigins())
	             .allowedMethods(cors.getAllowedMethods())
	             .maxAge(cors.getMaxAge())
	             .allowedHeaders(cors.getAllowedHeaders())
	             .exposedHeaders(cors.getExposedHeaders());
	       }
	   };
	}
*/

	
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
	
	
}
