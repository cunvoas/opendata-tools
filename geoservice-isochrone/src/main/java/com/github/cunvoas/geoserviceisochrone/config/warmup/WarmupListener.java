package com.github.cunvoas.geoserviceisochrone.config.warmup;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Servlet for GCP warm-up.
 * @see https://cloud.google.com/appengine/docs/standard/go/configuring-warmup-requests
 * @author cunvoas
 */
@Component
@WebListener
@Slf4j
public class WarmupListener implements ServletContextListener  {

	@Autowired
	private String warmup;

	/**
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
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
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		log.warn("... Shutting down ...");
	}

}
