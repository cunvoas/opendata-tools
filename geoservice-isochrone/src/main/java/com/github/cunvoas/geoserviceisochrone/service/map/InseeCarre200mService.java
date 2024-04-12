package com.github.cunvoas.geoserviceisochrone.service.map;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionGeo;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkTypeService;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author cus
 */
@Service
@Slf4j
@Log
public class InseeCarre200mService {
	
	private static final Double SURFACE_CARRE=40000d;

	@Autowired
	private InseeCarre200mRepository inseeCare200mRepository;
	@Autowired
	private InseeCarre200mShapeRepository inseeCarre200mShapeRepository;
	@Autowired
	private InseeCarre200mComputedRepository inseeCarre200mComputedRepository;


	@Autowired
	private ParkAreaRepository parkAreaRepository;
	@Autowired
	private ParkAreaComputedRepository parkAreaComputedRepository;
	@Autowired
	private ParkJardinRepository parkJardinRepository;
	
	
	public List<ParcEtJardin> findAround(Point p, double distanceM) {
		// log.info("Looking for city around ({},{}) withing {} meters", lat, lon, distanceM);
		return parkJardinRepository.findNearWithinDistance(p, distanceM);
	}
	
	
	
	/**
	 * @deprecated
	 * @param id
	 */
//	public void computeCarre(String id) {
//		this.computeCarre(inseeCare200mRepository.findById(id).get());
//	}
//	public void computeParkArea(Long id) {
//		this.computeParkArea(parkAreaRepository.findById(id).get());
//	}
	
	
	/**
	 * Compute ParkEntrance from ParkArea and List<ParkEntrance>.
	 * @param park
	 * @deprecated */
//	public void computeParkArea(ParkArea park) {
//		
//		// skip un-precalculated park
//		if(park.getPolygon()==null) {
//			log.warn("please process {} first",park.getName());
//			return;
//		}
//		
//		log.warn("computePark( {}-{} )",park.getId(), park.getName());
//		
//		ParkAreaComputed parcCpu=null;
//		Optional<ParkAreaComputed> parcCpuOpt = parkAreaComputedRepository.findById(park.getId());
//		if (parcCpuOpt.isPresent()) {
//			parcCpu = parcCpuOpt.get();
//		} else {
//			parcCpu = new ParkAreaComputed();
//			parcCpu.setId(park.getId());
//			parcCpu.setOms(Boolean.TRUE);
//		}
//		
//		Optional<ParcEtJardin> pjOpt = parkJardinRepository.findById(park.getIdParcEtJardin());
//		if (pjOpt.isPresent()) {
//			parcCpu.setSurface(new BigDecimal(Math.round(pjOpt.get().getSurface())));
//			
////			if (pjOpt.get().getSurface()<400d) {	// 20m x 20m
////				parcCpu.setOms(Boolean.FALSE);
////				
////			} else if ("Cimetière".equals(pjOpt.get().getType())) {
////				parcCpu.setOms(Boolean.FALSE);
////				
////			} else if ("Jardin de poche".equals(pjOpt.get().getType())) {
////				parcCpu.setOms(Boolean.FALSE);
////				
////			} else if ("Pelouse".equals(pjOpt.get().getType())) {
////				parcCpu.setOms(Boolean.FALSE);
////				
////			} else if ("Place".equals(pjOpt.get().getType())) {
////				parcCpu.setOms(Boolean.FALSE);
////			}
//		}
//		
//		// surface intersection algorithm
//		BigDecimal population = BigDecimal.ZERO;
//		// find carre200m shapes that match the park
//		List<InseeCarre200mShape> shapes = inseeCarre200mShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(park.getPolygon()));
//		for (InseeCarre200mShape carreShape : shapes) {
//			Geometry parkOnCarre = carreShape.getGeoShape().intersection(park.getPolygon());
//			Long surfIntersect = getSurface(parkOnCarre);
//			
//			//lookup for carre200m data
//			InseeCarre200m carre =inseeCare200mRepository.findByIdInspire(carreShape.getIdInspire());
//			Long popCar =Math.round(Double.valueOf(carre.getPopulation()));
//			
//			Long popIntersect = Math.round(popCar*surfIntersect/SURFACE_CARRE);
//			population = population.add(new BigDecimal(popIntersect));
//		}
//		parcCpu.setPopulation(population);
//		if (! BigDecimal.ZERO.equals(population)) {
//			parcCpu.setSurfacePerInhabitant(parcCpu.getSurface().divide(population, 1, RoundingMode.HALF_EVEN));
//		}
//		parcCpu.setUpdated(new Date());
//		
//		log.warn("surface per inhabitant: {}", parcCpu.getSurfacePerInhabitant());
//		parkAreaComputedRepository.save(parcCpu);
//	}
	
	private Long getSurface(Geometry geom) {
		return inseeCare200mRepository.getSurface(geom);
	}
//	/**
//	 * @deprecated
//	 * @param carre
//	 */
//	public void computeCarre(InseeCarre200m carre) {
//		
//
//		log.warn("computeCarre( {} )",carre.getId());
//		
//		InseeCarre200mComputed computed = null;
//		Optional<InseeCarre200mComputed> opt = inseeCarre200mComputedRepository.findById(carre.getId());
//		if (opt.isPresent()) {
//			computed = opt.get();
//		} else {
//			computed = new InseeCarre200mComputed();
//			computed.setIdCarre200(carre.getId());
//			computed.setUpdated(new Date());
//		}
//
//		// find shapes
//		InseeCarre200mShape carreShape = inseeCarre200mShapeRepository.findByIdInspire(carre.getIdInspire());
//		// find parks in square shape
//		List<ParkArea> areas = parkAreaRepository.findParkInMapArea(GeometryQueryHelper.toText(carreShape.getGeoShape()));
//		
////		Date lastUpdate=new Date();
//		Geometry polygonPark = null;
//		
//		for (ParkArea parkArea : areas) {
////			// put the last update date from the park update
////			if (lastUpdate==null) {
////				lastUpdate = parkArea.getUpdated();
////			} else if (lastUpdate!=null && lastUpdate.compareTo(parkArea.getUpdated())<0) {
////				lastUpdate = parkArea.getUpdated();
////			}
//			
//			// merge areas for parks
//			if (polygonPark == null) {
//				polygonPark = parkArea.getPolygon();
//			} else {
//				polygonPark = polygonPark.union(parkArea.getPolygon());
//			}
//		}
//
//		if (!areas.isEmpty() && polygonPark!=null) {
//
//			// compute surface with accessible parks
//			Geometry parkOnCarre = carreShape.getGeoShape().intersection(polygonPark);
//			
//			// compute surface of parks
//			Long surfaceParkAccess = getSurface(parkOnCarre);
//			//computed.setSurfaceWithPark(new BigDecimal(surfaceParkAccess));
//	
//			if (StringUtils.isNotBlank(carre.getPopulation())) {
//				Double inhabitant = Double.valueOf(carre.getPopulation());
//				computed.setPopAll(new BigDecimal(Math.round(inhabitant)));
//				Long popIn = Math.round(inhabitant*surfaceParkAccess/SURFACE_CARRE);
//				computed.setPopIncluded(new BigDecimal(popIn));
//				computed.setPopExcluded(new BigDecimal(inhabitant-popIn));
//			}
//			
//			
//			computed.setUpdated(new Date());
//			inseeCarre200mComputedRepository.save(computed);
//		}
//	}
	
}
