package com.github.cunvoas.geoserviceisochrone.service.opendata;

import java.text.Normalizer;
import java.util.List;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParcPrefectureRepository;
import com.github.cunvoas.geoserviceisochrone.service.map.CityService;
import com.github.cunvoas.geoserviceisochrone.service.map.InseeCarre200mService;
import com.github.cunvoas.geoserviceisochrone.service.park.ComputeService;

@Service
public class ServiceParcPrefecture {
	
	@Autowired 
	private ParcPrefectureRepository parcPrefectureRepository;

	@Autowired 
	private ComputeService computeService;

	@Autowired 
	private CityService cityService;
	@Autowired 
	private InseeCarre200mService inseeCarre200mService;
	
	public void update() {
		List<ParcPrefecture> pps = parcPrefectureRepository.findAll();
		for (ParcPrefecture pp : pps) {
			update(pp);
		}
	}
	
	public ParcPrefecture update(ParcPrefecture pp) {
		boolean updated=false;
		
		// surface update
		if (pp.getSurface() == null) {
			Long s = computeService.getSurface(pp.getArea());
			pp.setSurface(s);
			updated=true;
		}
		
		if (pp.getParcEtJardin()==null ) { //&& pp.getCommune().getId()==2878) {
			List<ParcEtJardin> pjs=inseeCarre200mService.findAround(pp.getPoint(), 0.07);
			if (pjs!=null && !pjs.isEmpty()) {
				pp.setParcEtJardin(pjs.get(0));
				pp.setName(pjs.get(0).getName());
				updated=true;
			}
		}
		
		if (updated) {
			pp= parcPrefectureRepository.save(pp);
		}
		return pp;
	}
	
	public ParcPrefecture prepareFromSite(String name, Polygon polygon) {

		ParcPrefecture pp = new ParcPrefecture();
		pp.setNamePrefecture(name);
		pp.setArea(polygon);
		Point p = polygon.getInteriorPoint();
		pp.setPoint(p);
		Long s = computeService.getSurface(polygon);
		pp.setSurface(s);
		
		String nomVille = null;
		if (name!=null) {
			 nomVille = removeAccents(name.split(":")[0].trim().toUpperCase());
		}
		
		// approx 10km
		List<City> cities = cityService.findAround(p, 0.07);
		if (cities!=null) {
			pp.setCommune(cities.get(0));
			for (City city : cities) {
				if (city.getName().equals(nomVille)) {
					pp.setCommune(city);
					break;
				}
			}
		}
		
		pp = parcPrefectureRepository.save(pp);
		update(pp);
		return pp;
	}
	
	
	private static String normalize(String input) {
	    return input == null ? null : Normalizer.normalize(input, Normalizer.Form.NFKD);
	}
	private static String removeAccents(String input) {
	    return normalize(input).replaceAll("\\p{M}", "");
	}

}
