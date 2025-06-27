package com.github.cunvoas.geoserviceisochrone.config.shutdown;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import com.github.cunvoas.geoserviceisochrone.service.admin.BatchJobService;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ShutDownHandler implements ApplicationContextAware {

	
	private BatchJobService batchJobService;
	/**
	 * mandatory for ApplicationContextAware
	 */
	private ApplicationContext applicationContext=null;
	 
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

    
    @PreDestroy
    public void destroy() {
    	log.error("Callback triggered - @PreDestroy - GracefulShutdown");
    	
    	// kill web connections
    	// NATIVE in SpringBoot

    	// kill batch process
    	// batchJobService by DisposableBean
    	
    	// kill SGBR connections
    	// NATIVE in SpringBoot
    	
    }
    
    private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			log.warn("sleep fails");
		}
    }


}
