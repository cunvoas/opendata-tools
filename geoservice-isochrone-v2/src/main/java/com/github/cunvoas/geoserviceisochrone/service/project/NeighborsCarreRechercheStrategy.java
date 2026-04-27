package com.github.cunvoas.geoserviceisochrone.service.project;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Stratégie de recherche basée sur le voisinage des carrés INSEE.
 * Utilise la méthode ProjectSimulatorHelper.findNeighbors pour déterminer les carrés impactés.
 *
 * @author cunvoas
 * 
 */
@Component
public class NeighborsCarreRechercheStrategy implements CarreRechercheStrategy {

	@Autowired
	private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	
    @Override
    public Set<InseeCarre200mOnlyShape> findCarres(ProjectSimulator projectSimulator, Integer urbanDistance) {

		List<InseeCarre200mOnlyShape> carreShapes = inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(projectSimulator.getInsee(), true);
		List<InseeCarre200mOnlyShape> carreShapesProjet = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(projectSimulator.getShapeArea());

		
    	Set<InseeCarre200mOnlyShape> carreForSimulation = new HashSet<>();
        for (InseeCarre200mOnlyShape carreShapeProject : carreShapesProjet) {
            List<InseeCarre200mOnlyShape> neighbors = NeighborsHelper.findNeighbors(carreShapeProject, carreShapes, urbanDistance);
            carreForSimulation.addAll(neighbors);
        }
        return carreForSimulation;
    }
}