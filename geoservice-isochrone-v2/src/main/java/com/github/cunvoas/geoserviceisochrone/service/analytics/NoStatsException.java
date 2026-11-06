package com.github.cunvoas.geoserviceisochrone.service.analytics;

/**
 * Aucune ligne de statistiques pour la commune ou l'EPCI demandé.
 * Les routes API la traduisent en 404 ; la publication la laisse échouer comme avant.
 */
public class NoStatsException extends RuntimeException {

	public NoStatsException(String cible) {
		super("Aucune statistique pour " + cible);
	}
}
