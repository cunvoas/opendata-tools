package com.github.cunvoas.geoserviceisochrone;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;




/**
 * Application MAIN.
 */
@SpringBootApplication
@ComponentScan(basePackages = { 
		"com.github.cunvoas.geoserviceisochrone",
		"com.github.cunvoas.metrics"
	})
@EnableAspectJAutoProxy
@EnableScheduling
public class GeoserviceIsochroneApplication {

		
	/**
	 * main.
	 * @param args params
	 */
	public static void main(String[] args) {
		SpringApplication.run(GeoserviceIsochroneApplication.class, args);
	}
	
    /**
     * activate tasks.
     * @return Executor
     */
    @Bean
    public Executor taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
    

    /**
     * setup schduler.
     * @return TaskScheduler
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
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
