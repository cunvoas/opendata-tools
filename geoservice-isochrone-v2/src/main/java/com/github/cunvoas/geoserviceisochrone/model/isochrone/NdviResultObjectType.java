package com.github.cunvoas.geoserviceisochrone.model.isochrone;

/**
 * Type d'objet auquel se rattache une mesure NDVI.
 *
 * <p>{@link ParkAreaComputed} et {@link com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin}
 * représentent la même notion de parc à deux états de calcul différents.</p>
 */
public enum NdviResultObjectType {
	/** Zone de parc disposant de calculs OMS. */
	PARK_AREA_COMPUTED,
	/** Parc ou jardin brut, sans calcul OMS. */
	PARC_ET_JARDIN,
	/** Zone IRIS. */
	IRIS,
	/** Emprise cadastrale. */
	CADASTRE,
	/** Carreau 200 m. */
	CARRE_200M
}
