package com.github.cunvoas.geoserviceisochrone.service.ignTopo;

import java.util.List;

import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.ignTopo.IgnTopoVegetal;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.ignTopo.IgnTopoVegetalRepository;

@Service
public class IgnTopoService {
	@Autowired
	private IgnTopoVegetalRepository ignTopoVegetalRepository;

    
	public List<IgnTopoVegetal> getVegetal(String inseeCode) {
		return ignTopoVegetalRepository.findByInseeId(inseeCode);
	}	
	

    public List<IgnTopoVegetal> findAllCarreByArea(Polygon polygon) {
    	return ignTopoVegetalRepository.findTopoVegetalByMapArea(GeometryQueryHelper.toText(polygon));
    }
    
    public List<IgnTopoVegetal> findAllCarreByInseeAndArea(String insee, Polygon polygon) {
    	return ignTopoVegetalRepository.findTopoVegetalByVilleProcheMapArea(insee, GeometryQueryHelper.toText(polygon));
    }

    public List<IgnTopoVegetal> findAllCarreByCom2coIdAndArea(Long com2coId, Polygon polygon) {
    	return ignTopoVegetalRepository.findTopoVegetalByCom2CoAndMapArea(com2coId, GeometryQueryHelper.toText(polygon));
    }
}	
