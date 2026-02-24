package com.github.cunvoas.geoserviceisochrone.service.project;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.util.GeometryPointReducer;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * Stratégie de recherche utilisant la réduction géométrique et l'isochrone IGN.
 * Pour chaque point de la géométrie réduite, appelle le service isochrone pour déterminer les carrés impactés.
 *
 * @author cunvoas
 */
public class GeometryReducerIsochroneCarreRechercheStrategy implements CarreRechercheStrategy {
    private final GeometryPointReducer geometryPointReducer;
    private final ServiceReadReferences serviceReadReferences;

    public GeometryReducerIsochroneCarreRechercheStrategy(GeometryPointReducer geometryPointReducer, ServiceReadReferences serviceReadReferences) {
        this.geometryPointReducer = geometryPointReducer;
        this.serviceReadReferences = serviceReadReferences;
    }

    @Override
    public Set<InseeCarre200mOnlyShape> findCarres(ProjectSimulator projectSimulator, List<InseeCarre200mOnlyShape> carreShapes, List<InseeCarre200mOnlyShape> carreShapesProjet, Integer urbanDistance) {
        Set<InseeCarre200mOnlyShape> carreForSimulation = new HashSet<>();
        Geometry reducedGeom = geometryPointReducer.reduceConvexHullToMax10Min6(projectSimulator.getShapeArea());
        List<Point> points = geometryPointReducer.getPoints(reducedGeom);
        for (Point point : points) {
            // TODO: Appeler le service isochrone IGN pour chaque point
            // List<InseeCarre200mOnlyShape> impactedCarres = serviceReadReferences.findCarresByIsochrone(point, urbanDistance);
            // carreForSimulation.addAll(impactedCarres);
        }
        return carreForSimulation;
    }
}