package com.github.cunvoas.geoserviceisochrone.config.shutdown;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler for shutdown signal.
 */
@Configuration
@Slf4j
public class ShutdownHandler  {
    
    @PreDestroy
    public void destroy() {
    	log.warn("SIGTERM detected");
    	// kill web connections
    	// NATIVE in SpringBoot

    	// kill batch process
    	// native with DisposableBean implementator
    	
    	
    	// custom code Here
    	// log.warn("SIGTERM gracefull termination initiated");
    	
    	// kill SGBR connections
    	// NATIVE in SpringBoot
    	log.warn("SIGTERM gracefull termination done");
    }
}
