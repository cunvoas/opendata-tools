package com.github.cunvoas.geoserviceisochrone.service.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionExtract;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.MapperIsoChrone;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.DtoIsoChroneParser;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.IsoChroneClientService;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoIsoChrone;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorIsochone;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ProjectSimulatorlIsochroneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Stratégie de recherche utilisant la réduction géométrique et l'isochrone IGN.
 * Pour chaque point de la géométrie réduite, appelle le service isochrone pour déterminer les carrés impactés.
 *
 * @author cunvoas
 */
@Component
@Slf4j
public class GeometryReducerIsochroneCarreRechercheStrategy implements CarreRechercheStrategy {
	@Autowired
    private GeometryPointReducer geometryPointReducer;
	@Autowired
    private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	@Autowired
    private ProjectSimulatorlIsochroneRepository projectSimulatorlIsochroneRepository;
	

	@Autowired
	private IsoChroneClientService clientIsoChrone;
	@Autowired
	private DtoIsoChroneParser dtoIsoChroneParser;	
	@Autowired
	private MapperIsoChrone mapperIsoChrone;
	
	
    @Override
    public Set<InseeCarre200mOnlyShape> findCarres(ProjectSimulator projectSimulator, Integer urbanDistance) {
        Set<InseeCarre200mOnlyShape> carreForSimulation = new HashSet<>();
        
        // reduce numbers on shape points
        Geometry reducedGeom = geometryPointReducer.reduceConvexHullToMax10Min6(projectSimulator.getShapeArea());
        List<Point> points = geometryPointReducer.getPoints(reducedGeom);
        
        List<ProjectSimulatorIsochone> isochrones = List.of();
        if (projectSimulator.getId()!=null) {
        	isochrones = projectSimulatorlIsochroneRepository.findByIdProjectSimulator(projectSimulator.getId());
        	isochrones.sort(COMP_INSTANCE);
        }

        // for seat
    	ProjectSimulatorIsochone toSearch=new ProjectSimulatorIsochone();
    	toSearch.setIdProjectSimulator(projectSimulator.getId());
    	

        List<ProjectSimulatorIsochone> isochronesNew = new ArrayList<>();
        for (Point point : points) {
        	toSearch.setPoint(point);
        	ProjectSimulatorIsochone psiFound=null;
        	int pos = Collections.binarySearch(isochrones, toSearch);
        	if (pos>=0) {
        		psiFound = isochrones.get(pos);
        	} else {
        		psiFound =new ProjectSimulatorIsochone();
        		psiFound.setIdProjectSimulator(projectSimulator.getId());
        		psiFound.setPoint(point);
        		psiFound.setProcessed(true);
        		isochronesNew.add(psiFound);
        	}
        	if (psiFound.getIsochrone()==null) {
        		// call IGN
        		Geometry geom = this.getIsoChroneFromIGN(point, String.valueOf(urbanDistance));
        		psiFound.setIsochrone(geom);
        		psiFound.setProcessed(true);
        		isochronesNew.add(psiFound);
        	}
        	
        }

    	// merge isochrones
        Geometry merged = projectSimulator.getShapeArea();
        for (ProjectSimulatorIsochone isoNew : isochronesNew) {
        	merged = merged.union(isoNew.getIsochrone());
		}
        projectSimulator.setInfluenceArea(merged);
        
    	// clean orphans (need analysis
        isochrones.removeAll(isochronesNew);
        projectSimulatorlIsochroneRepository.deleteAll(isochrones);
        projectSimulatorlIsochroneRepository.saveAll(isochronesNew);
        

        //TODO: Appeler le service isochrone IGN pour chaque point
        List<InseeCarre200mOnlyShape> impactedCarres = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(merged, true);
        carreForSimulation.addAll(impactedCarres);
        
        
        return carreForSimulation;
    }
    
    
    private Geometry getIsoChroneFromIGN(Point point, String distance) {
		log.warn("getIsoChrone {}");
		Geometry ret=null;
		try {
			Coordinate coord = new Coordinate(
					point.getX(),
					point.getY());
			
			// 300 en zone dense, 1200 sinon
			String ignResp = clientIsoChrone.getIsoChrone(coord, distance);
			
			if (StringUtils.isNotBlank(ignResp)) {
				DtoIsoChrone dtoIsoChone = dtoIsoChroneParser.parseBasicIsoChrone(ignResp);
				ret = mapperIsoChrone.map( dtoIsoChone);
			} else {
				log.warn("IGN_UPDATE (no IGN response)");
			}
			
			Thread.sleep( (long)(80+Math.random()*50) );
		} catch (Exception e) {
			log.error("IGN_UPDATE {}", point);
			throw new ExceptionExtract("IGN_UPDATE");
		}
		
		return ret;
    }
    
    /**
     * comparator for binary search.
     */
    private PSIComparator COMP_INSTANCE = new PSIComparator();
    private class PSIComparator implements Comparator<ProjectSimulatorIsochone> {
		@Override
		public int compare(ProjectSimulatorIsochone o1, ProjectSimulatorIsochone o2) {
			CompareToBuilder cbt=new CompareToBuilder();
			cbt.append(o1.getIdProjectSimulator(), o2.getIdProjectSimulator());
			cbt.append(o1.getPoint(), o2.getPoint());
			return cbt.toComparison();
		}
    	
    }
}