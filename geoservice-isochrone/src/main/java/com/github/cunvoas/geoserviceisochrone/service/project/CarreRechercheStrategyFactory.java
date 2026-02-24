package com.github.cunvoas.geoserviceisochrone.service.project;

import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.util.GeometryPointReducer;

/**
 * Factory pour fournir la stratégie de recherche des carrés INSEE impactés.
 * Permet de choisir dynamiquement la stratégie selon le mode de recherche.
 *
 * @author cunvoas
 */
@Component
public class CarreRechercheStrategyFactory {
    private final GeometryPointReducer geometryPointReducer;
    private final ServiceReadReferences serviceReadReferences;

    public CarreRechercheStrategyFactory(GeometryPointReducer geometryPointReducer, ServiceReadReferences serviceReadReferences) {
        this.geometryPointReducer = geometryPointReducer;
        this.serviceReadReferences = serviceReadReferences;
    }

    public CarreRechercheStrategy getStrategy(ModeRechercheCarre mode) {
        switch (mode) {
            case NEIGHBORS:
                return new NeighborsCarreRechercheStrategy();
            case GEOMETRY_REDUCER_ISOCHRONE:
                return new GeometryReducerIsochroneCarreRechercheStrategy(geometryPointReducer, serviceReadReferences);
            default:
                throw new IllegalArgumentException("Mode de recherche inconnu: " + mode);
        }
    }
}