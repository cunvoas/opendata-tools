package com.github.cunvoas.geoserviceisochrone.service.project;

/**
 * Enumération des modes de recherche des carrés INSEE impactés.
 * NEIGHBORS : recherche par voisinage.
 * GEOMETRY_REDUCER_ISOCHRONE : recherche par géométrie réduite et isochrone.
 *
 * @author cunvoas
 */
public enum ModeRechercheCarre {
    NEIGHBORS,
    GEOMETRY_REDUCER_ISOCHRONE
}