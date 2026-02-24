package com.github.cunvoas.geoserviceisochrone.service.project;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Stratégie de recherche basée sur le voisinage des carrés INSEE.
 * Utilise la méthode ProjectSimulatorHelper.findNeighbors pour déterminer les carrés impactés.
 *
 * @author cunvoas
 */
public class NeighborsCarreRechercheStrategy implements CarreRechercheStrategy {
    @Override
    public Set<InseeCarre200mOnlyShape> findCarres(ProjectSimulator projectSimulator, List<InseeCarre200mOnlyShape> carreShapes, List<InseeCarre200mOnlyShape> carreShapesProjet, Integer urbanDistance) {
        Set<InseeCarre200mOnlyShape> carreForSimulation = new HashSet<>();
        for (InseeCarre200mOnlyShape carreShapeProject : carreShapesProjet) {
            List<InseeCarre200mOnlyShape> neighbors = ProjectSimulatorHelper.findNeighbors(carreShapeProject, carreShapes, urbanDistance);
            carreForSimulation.addAll(neighbors);
        }
        return carreForSimulation;
    }
}