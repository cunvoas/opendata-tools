package com.github.cunvoas.geoserviceisochrone;

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

    
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry
					.addMapping("/map")
					//FIXME to Variablize
						.allowedOrigins("http://localhost:8081", 
										"https://autmel-maps.duckdns.org"
										);
			}
		};
	}
	
	
}
