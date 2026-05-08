package com.github.cunvoas.geoserviceisochrone.service.project;

import java.util.Set;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;

/**
 * Stratégie abstraite pour la recherche des carrés INSEE impactés par un projet.
 * Permet d'implémenter différents modes de recherche (voisinage, isochrone, etc).
 *
 * @author cunvoas
 */
public interface CarreRechercheStrategy {
	
    /**
     * Recherche les carrés INSEE impactés selon la stratégie.
     *
     * @param projectSimulator Le projet de simulation
     * @param urbanDistance Distance urbaine OMS
     * @return Ensemble des carrés impactés
     */
    Set<InseeCarre200mOnlyShape> findCarres(ProjectSimulator projectSimulator, Integer urbanDistance);
}