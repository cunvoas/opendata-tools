package com.github.cunvoas.geoserviceisochrone.service.project;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorIsochone;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ProjectSimulatorlIsochroneRepository;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;

/**
 * Stratégie de recherche utilisant la réduction géométrique et l'isochrone IGN.
 * Pour chaque point de la géométrie réduite, appelle le service isochrone pour déterminer les carrés impactés.
 *
 * @author cunvoas
 */
@Component
public class GeometryReducerIsochroneCarreRechercheStrategy implements CarreRechercheStrategy {
	@Autowired
    private GeometryPointReducer geometryPointReducer;
	@Autowired
    private ServiceReadReferences serviceReadReferences;
	@Autowired
    private ProjectSimulatorlIsochroneRepository projectSimulatorlIsochroneRepository;

    @Override
    public Set<InseeCarre200mOnlyShape> findCarres(ProjectSimulator projectSimulator, List<InseeCarre200mOnlyShape> carreShapes, List<InseeCarre200mOnlyShape> carreShapesProjet, Integer urbanDistance) {
        Set<InseeCarre200mOnlyShape> carreForSimulation = new HashSet<>();
        Geometry reducedGeom = geometryPointReducer.reduceConvexHullToMax10Min6(projectSimulator.getShapeArea());
        List<Point> points = geometryPointReducer.getPoints(reducedGeom);
        
        List<ProjectSimulatorIsochone> isochrones = List.of();
        if (projectSimulator.getId()!=null) {
        	isochrones = projectSimulatorlIsochroneRepository.findByIdProjectSimulator(projectSimulator.getId());
        }
        
        for (Point point : points) {
             //TODO: Appeler le service isochrone IGN pour chaque point
//             List<InseeCarre200mOnlyShape> impactedCarres = serviceReadReferences.findCarresByIsochrone(point, urbanDistance);
//             carreForSimulation.addAll(impactedCarres);
        }
        return carreForSimulation;
    }
}