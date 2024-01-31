package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Laposte;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.LaposteRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Log
public class ComputeService {

	// 200m x 200m = 4 10^4
	private static final Double SURFACE_CARRE = 40_000d;
	
	@Autowired
	private LaposteRepository laposteRepository;
	@Autowired
	private CadastreRepository cadastreRepository;
	
	@Autowired
	private InseeCarre200mShapeRepository inseeCarre200mShapeRepository;
	@Autowired
	private InseeCarre200mRepository inseeCare200mRepository;
	@Autowired
	private InseeCarre200mComputedRepository inseeCarre200mComputedRepository;
	
	@Autowired
	private ParkAreaRepository parkAreaRepository;
	@Autowired
	private ParkAreaComputedRepository parkAreaComputedRepository;
	@Autowired
	private ParkJardinRepository parkJardinRepository;
	
	
	
	public void computeByPostalCode(String postalCode) {
		Set<Cadastre> uniques = new HashSet<>();
		List<Laposte> postes = laposteRepository.findByPostalCode(postalCode);
		for (Laposte laposte : postes) {
			Cadastre cadastre = cadastreRepository.findById(laposte.getIdInsee()).get();
			uniques.add(cadastre);
		}
		for (Cadastre cadastre : uniques) {
			computeByCadastre(cadastre);
		}
		
		
	}
	
	public void computeByInseeCode(String inseeCode) {
		Cadastre cadastre = cadastreRepository.findById(inseeCode).get();
		computeByCadastre(cadastre);
	}
	
	/**
	 * Computes population that can access a park at once.
	 * @param cadastre
	 * 
	 * Algorithm:
	 *  step 1: Find area of the city.
	 *  step 2: Find 200m squares that matches.
	 *  step 3: for each, check and compute parks.
	 */
	public void computeByCadastre(Cadastre cadastre) {
		
		// find all square in city area
		List<InseeCarre200mShape> shapes = inseeCarre200mShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(cadastre.getGeoShape()));
		
		// iterate on each
		for (InseeCarre200mShape carreShape : shapes) {
			
			// get already computed square results
			InseeCarre200mComputed computed = null;
			Optional<InseeCarre200mComputed> opt = inseeCarre200mComputedRepository.findById(carreShape.getIdCarreHab());
			if (opt.isPresent()) {
				computed = opt.get();
			} else {
				// or create it
				computed = new InseeCarre200mComputed();
				computed.setIdCarre200(carreShape.getIdCarreHab());
				computed.setUpdated(new Date());
			} 
			
			// get insee data for the square
			InseeCarre200m carre = inseeCare200mRepository.findById(carreShape.getIdCarreHab()).get();
			
			// find parks in square shape
			List<ParkArea> parkAreas = parkAreaRepository.findParkInMapArea(GeometryQueryHelper.toText(carreShape.getGeoShape()));
			
			// compute all surface of isochrone of parks 
			Geometry polygonPark = null;
			for (ParkArea parkArea : parkAreas) {
				// merge areas for parks
				if (polygonPark == null) {
					polygonPark = parkArea.getPolygon();
				} else {
					polygonPark = polygonPark.union(parkArea.getPolygon());
				}
			}
			
			Long surfaceParkAccess = 0L;
			Double inhabitant = 0d;
			
			if (StringUtils.isNotBlank(carre.getPopulation())) {
				inhabitant = Double.valueOf(carre.getPopulation());
				computed.setPopAll(new BigDecimal(Math.round(inhabitant)));
			}
			
			if (polygonPark!=null
		//		 &&   (lastUpdate==null || computed.getUpdated()!=null && computed.getUpdated().compareTo(lastUpdate)<0)
				) {

				// compute surface with accessible parks
				Geometry parkOnCarre = carreShape.getGeoShape().intersection(polygonPark);
				
				// compute surface of parks
				surfaceParkAccess = getSurface(parkOnCarre);
				computed.setSurfaceWithPark(new BigDecimal(surfaceParkAccess));
		
				if (StringUtils.isNotBlank(carre.getPopulation())) {
					
					// fix eventual round error
					inhabitant = computed.getPopAll().doubleValue();
					
					Long popIn = Math.round(inhabitant*surfaceParkAccess/SURFACE_CARRE);
					computed.setPopIncluded(new BigDecimal(popIn));
					computed.setPopExcluded(new BigDecimal(inhabitant-popIn));
				} else {
					computed.setPopAll(BigDecimal.ZERO);
					computed.setPopIncluded(BigDecimal.ZERO);
					computed.setPopExcluded(BigDecimal.ZERO);
				}
			} else {

				computed.setSurfaceWithPark(new BigDecimal(surfaceParkAccess));
				// no parks is accesible
				computed.setPopIncluded(BigDecimal.ZERO);
				computed.setPopExcluded(computed.getPopAll());
			}
			computed.setUpdated(new Date());
			inseeCarre200mComputedRepository.save(computed);
		
		}	
	}
	
	public Long getSurface(Geometry geom) {
		return inseeCare200mRepository.getSurface(geom);
	}

}
