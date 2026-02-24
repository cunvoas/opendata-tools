package com.github.cunvoas.geoserviceisochrone.service.project;

import org.springframework.stereotype.Component;

/**
 * Factory pour fournir la stratégie de recherche des carrés INSEE impactés.
 * Permet de choisir dynamiquement la stratégie selon le mode de recherche.
 *
 * @author cunvoas
 */
@Component
public class CarreRechercheStrategyFactory {


    public CarreRechercheStrategy getStrategy(ModeRechercheCarre mode) {
        switch (mode) {
            case NEIGHBORS:
                return new NeighborsCarreRechercheStrategy();
            case GEOMETRY_REDUCER_ISOCHRONE:
                return new GeometryReducerIsochroneCarreRechercheStrategy();
            default:
                throw new IllegalArgumentException("Mode de recherche inconnu: " + mode);
        }
    }
}