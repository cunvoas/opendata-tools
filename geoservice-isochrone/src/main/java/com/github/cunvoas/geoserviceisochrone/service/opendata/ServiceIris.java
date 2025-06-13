package com.github.cunvoas.geoserviceisochrone.service.opendata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.digest.MurmurHash2;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisData;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisDataRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * ServiceIris.
 */
/**
 * @param irisShape
 * @return
 */
@Service
@Slf4j
public class ServiceIris {
	
	@Autowired
	private IrisDataRepository irisDataRepository;
	@Autowired
	private IrisShapeRepository irisShapeRepository;
	
	@Transactional
	public void saveAllData(List<IrisData> datas) {
		irisDataRepository.saveAll(datas);
	}
	
	@Transactional
	public void saveAllShape(List<IrisShape> datas) {
		irisShapeRepository.saveAll(datas);
	}
	
	public void computeFootprint() {
		List<IrisShape> shapes = irisShapeRepository.findByFootprintIsNull();
		for (IrisShape irisShape : shapes) {
			this.update(irisShape);
		}
	}
	
	
	@Transactional
	public IrisShape update(IrisShape irisShape) {
		
		Geometry geom = irisShape.getContour();
		
		if (geom!=null) {
			List<String> sCoords= new ArrayList<>();
			Coordinate[] coords = geom.getCoordinates();
			sCoords.add(String.format("%s", coords.length));
			for (Coordinate coord : coords) {
				sCoords.add(String.format("X@%s:Y@%s", coord.x, coord.y));
			}
			Collections.sort(sCoords);
			StringBuilder sb = new StringBuilder();
			for (String sCoord : sCoords) {
				sb.append(sCoord).append("|");
			}
			
			// MurmurHash2 is fastest hash non crypto
			Integer footprint = MurmurHash2.hash32(sb.toString());
			
			irisShape.setFootprint(footprint);
			return irisShapeRepository.save(irisShape);
		} else {
			return irisShape;
		}
	}
	
}
