package com.github.cunvoas.geoserviceisochrone.service;

import java.util.Optional;

import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.ignTopo.IgnTopoVegetal;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IgnTopoService {

//	public void splitMultiPolygonToPolygon(String inseeId) {
//		Optional<IgnTopoVegetal> vegetalOpt = ignTopoVegetalRepository.findByInseeId(inseeId);
//		if (vegetalOpt.isPresent()) {
//			IgnTopoVegetal vegetal = vegetalOpt.get();
//			
//			MultiPolygon mp = (MultiPolygon)vegetal.getGeometry();
//			
//			
//			for (int i = 0; i < mp.getNumGeometries(); i++) {
//			    Polygon polygon = (Polygon)mp.getGeometryN(i);
//			    
//			    IgnTopoVegetalSplited tivs = new IgnTopoVegetalSplited();
//			    tivs.setInseeId(inseeId);
//			    tivs.setGeometry(polygon);
//			    tivs.setCentre(polygon.getCentroid());
//			    tivs.setSurface(polygon.getArea());
//			    
//			    ignTopoVegetalSplitedRepository.save(tivs);
//			}
//			
//		} else {
//			log.warn("IgnTopoVegetal with inseeId {} not found.", inseeId);
//		}
//	}
}
