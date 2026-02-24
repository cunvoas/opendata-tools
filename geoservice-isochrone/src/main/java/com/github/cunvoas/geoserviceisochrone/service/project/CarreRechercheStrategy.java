package com.github.cunvoas.geoserviceisochrone.service.project;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import java.util.List;
import java.util.Set;

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
     * @param carreShapes Tous les carrés de la commune
     * @param carreShapesProjet Les carrés directement impactés par le projet
     * @param urbanDistance Distance urbaine OMS
     * @return Ensemble des carrés impactés
     */
    Set<InseeCarre200mOnlyShape> findCarres(ProjectSimulator projectSimulator, List<InseeCarre200mOnlyShape> carreShapes, List<InseeCarre200mOnlyShape> carreShapesProjet, Integer urbanDistance);
}