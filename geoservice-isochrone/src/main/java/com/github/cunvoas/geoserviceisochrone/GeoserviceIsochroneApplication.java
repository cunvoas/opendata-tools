package com.github.cunvoas.geoserviceisochrone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;



@SpringBootApplication
@ComponentScan(basePackages = { "com.github.cunvoas.geoserviceisochrone" })
@EnableAspectJAutoProxy
public class GeoserviceIsochroneApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeoserviceIsochroneApplication.class, args);
	}

    
//	@Bean
//	public WebMvcConfigurer corsConfigurer() {
//		return new WebMvcConfigurer() {
//			@Override
//			public void addCorsMappings(CorsRegistry registry) {
//				registry.addMapping("/*")
//					.allowedOrigins("http://localhost:8080", 
//									"http://geoservice.go.yo.fr/"
//									);
//			}
//		};
//	}
	
	
}
