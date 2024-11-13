package com.github.cunvoas.geoserviceisochrone.service.park;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionGeo;
import com.github.cunvoas.geoserviceisochrone.extern.helper.DistanceHelper;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

@Service
public class ParkJardinService {
	
	@Autowired 
	private ParkJardinRepository parkJardinRepository;

	@Autowired 
	private CadastreRepository cadastreRepository;

	@Autowired 
	private InseeCarre200mOnlyShapeRepository surfaceRepo;
	
	
	/**
	 * save ParcEtJardin.
	 * @param parcEtJardin
	 * @return
	 */
	public ParcEtJardin save(ParcEtJardin parcEtJardin) {
		
		
		
		if (parcEtJardin.getId()==null) {
			
			// check park vs city location
			City city = parcEtJardin.getCommune();
			boolean checkIntersect = true;
			boolean match = false;
			if (city.getCoordinate()!=null) {
				Double distance = DistanceHelper.crowFlyDistance(city.getCoordinate(), parcEtJardin.getCoordonnee());
				if(distance<1) {
					checkIntersect = false;
					match=true;
				}
			}
			if (checkIntersect) {
				Cadastre c = cadastreRepository.getReferenceById(city.getInseeCode());
				match = c.getGeoShape().contains(parcEtJardin.getCoordonnee());
			}
			if (!match) {
				throw new ExceptionGeo("PARK_NOT_IN_CITY");
			}
		} else {
			// MàJ
			ParcEtJardin prev = parkJardinRepository.getReferenceById(parcEtJardin.getId());
			if (!prev.getCommune().equals(parcEtJardin.getCommune())) {
				throw new ExceptionGeo("PARK_CHANGE_CITY");
			}
			
		}
		
		if (ParcSourceEnum.AUTMEL.equals(parcEtJardin.getSource())) {
			Long s = surfaceRepo.getSurface(parcEtJardin.getContour());
			parcEtJardin.setSurface(s.doubleValue());
		}
		
		return parkJardinRepository.saveAndFlush(parcEtJardin);
	}

}
