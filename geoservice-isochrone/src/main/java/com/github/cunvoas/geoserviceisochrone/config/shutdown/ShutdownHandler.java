package com.github.cunvoas.geoserviceisochrone.config.shutdown;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ShutdownHandler  {
    
    @PreDestroy
    public void destroy() {
		log.error("GracefulShutdown|DETECTED|@PreDestroy");
    	// kill web connections
    	// NATIVE in SpringBoot

    	// kill batch process
    	// native with DisposableBean implementator
    	// custom code Here
    	
    	// kill SGBR connections
    	// NATIVE in SpringBoot
		log.error("GracefulShutdown|READY2STOP|@PreDestroy");
    }
}
