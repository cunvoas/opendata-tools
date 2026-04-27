package com.github.cunvoas.geoserviceisochrone.config.warmup;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Servlet de warmup pour GCP.
 * <p>
 * Dans Server Engine, si l'application démarre trop lentement, elle peut être automatiquement arrêtée par le lanceur.
 * Ce composant permet d'assurer un bon démarrage.
 * </p>
 * @see https://cloud.google.com/appengine/docs/standard/go/configuring-warmup-requests
 */
@Component
@WebListener
@Slf4j
public class WarmupListener implements ServletContextListener  {

	/**
	 * Bean de warmup injecté pour vérifier l'initialisation du contexte.
	 */
	@Autowired
	private String warmup;

	/**
	 * Méthode appelée lors de l'initialisation du contexte servlet.
	 * @param sce événement d'initialisation du contexte
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		log.info("Starting, warming ....");
		String q = "no";
		try {
			q = warmup;
		} catch (BeansException e) {
			q="error";
		}
		if (q==null) {
			log.info("... not yet !");
		} else {
			log.info(q);
		}
	}

	/**
	 * Méthode appelée lors de la destruction du contexte servlet.
	 * @param sce événement de destruction du contexte
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		log.warn("... Shutting down ...");
	}

}