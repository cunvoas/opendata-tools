package com.github.cunvoas.geoserviceisochrone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;




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
