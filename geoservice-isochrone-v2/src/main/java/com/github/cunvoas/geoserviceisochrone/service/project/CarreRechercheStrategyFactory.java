package com.github.cunvoas.geoserviceisochrone.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory pour fournir la stratégie de recherche des carrés INSEE impactés.
 * Permet de choisir dynamiquement la stratégie selon le mode de recherche.
 *
 * @author cunvoas
 */
@Component
public class CarreRechercheStrategyFactory {
	@Autowired
	private NeighborsCarreRechercheStrategy neighborsCarreRechercheStrategy;
	@Autowired
	private GeometryReducerIsochroneCarreRechercheStrategy geometryReducerIsochroneCarreRechercheStrategy;
    
	
	public CarreRechercheStrategy getStrategy(ModeRechercheCarre mode) {
        switch (mode) {
            case NEIGHBORS:
                return neighborsCarreRechercheStrategy;
            case GEOMETRY_REDUCER_ISOCHRONE:
                return geometryReducerIsochroneCarreRechercheStrategy;
            default:
                throw new IllegalArgumentException("Mode de recherche inconnu: " + mode);
        }
    }
}