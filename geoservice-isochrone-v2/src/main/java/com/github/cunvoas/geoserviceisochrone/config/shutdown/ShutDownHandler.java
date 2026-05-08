package com.github.cunvoas.geoserviceisochrone.config.shutdown;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import com.github.cunvoas.geoserviceisochrone.service.compute.BatchJobService;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Gestionnaire avancé de l'arrêt (shutdown) de l'application avec accès au contexte Spring.
 * <p>
 * Permet d'exécuter du code personnalisé lors de l'arrêt, notamment pour arrêter des batchs ou libérer des ressources.
 * </p>
 */
@Configuration
@Slf4j
public class ShutDownHandler implements ApplicationContextAware {

	private BatchJobService batchJobService;
	/**
	 * Méthode obligatoire pour ApplicationContextAware.
	 * Permet de récupérer le contexte Spring.
	 * @param applicationContext le contexte Spring
	 * @throws BeansException en cas d'erreur
	 */
	private ApplicationContext applicationContext=null;
	 
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

    /**
     * Méthode appelée automatiquement avant la destruction du bean (arrêt de l'application).
     * Permet d'effectuer des opérations de nettoyage ou de libération de ressources.
     */
    @PreDestroy
    public void destroy() {
    	log.warn("SIGTERM ShutDownHandler.destroy() gracefull started");
    	// kill web connections
    	// NATIVE in SpringBoot

    	// kill batch process
    	// native with DisposableBean implementator
    	
    	// custom code Here
    	// log.warn("SIGTERM gracefull termination initiated");
    	
    	// kill SGBR connections
    	// NATIVE in SpringBoot
    	log.warn("SIGTERM ShutDownHandler.destroy() gracefull DONE");
    }
    
    /**
     * Met en pause l'exécution pendant un certain temps (ms).
     * @param ms durée en millisecondes
     */
    private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			log.warn("sleep fails");
		}
    }

}