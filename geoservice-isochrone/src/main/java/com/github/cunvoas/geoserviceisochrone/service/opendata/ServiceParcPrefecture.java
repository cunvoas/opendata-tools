package com.github.cunvoas.geoserviceisochrone.service.opendata;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusPrefEnum;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParcPrefectureRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.map.CityService;
import com.github.cunvoas.geoserviceisochrone.service.park.ComputeServiceV2;

/**
 * Business Service impl.
 */
@Service
public class ServiceParcPrefecture {
	
	@Autowired 
	private ParcPrefectureRepository parcPrefectureRepository;

	@Autowired 
	private ParkJardinRepository parkJardinRepository;

	@Autowired 
	private ComputeServiceV2 computeService;

	@Autowired 
	private CityService cityService;
	
	/**
	 * update ParcPrefecture.
	 */
	public void update() {
		List<ParcPrefecture> pps = parcPrefectureRepository.findAll();
		for (ParcPrefecture pp : pps) {
			computeAndUpdate(pp);
		}
	}
	
	/**
	 * getById.
	 * @param id ParcPrefecture
	 * @return ParcPrefecture
	 */
	public ParcPrefecture getById(Long id) {
		Optional<ParcPrefecture> opt = parcPrefectureRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}

	/**
	 * update.
	 * @param pp ParcPrefecture
	 * @return ParcPrefecture
	 */
	public ParcPrefecture update(ParcPrefecture pp) {
		return parcPrefectureRepository.save(pp);
	}

	/**
	 * computeAndUpdate.
	 * @param pp ParcPrefecture
	 * @return ParcPrefecture
	 */
	public ParcPrefecture computeAndUpdate(ParcPrefecture pp) {
		boolean updated=false;
		
		// compute surface then update
		if (pp.getSurface() == null) {
			Long s = computeService.getSurface(pp.getArea());
			pp.setSurface(s);
			updated=true;
		}
		

		if (pp.getParcEtJardin()==null ) { //&& pp.getCommune().getId()==2878) {
			String s = GeometryQueryHelper.toText(pp.getArea());
			List<ParcEtJardin> pjs=parkJardinRepository.findByArea(s);
			if (pjs!=null && !pjs.isEmpty()) {
				if (pjs.size()==1) {
					pp.setParcEtJardin(pjs.get(0));
					pp.setName(pjs.get(0).getName());
					updated=true;
					pp.setStatus(ParcStatusPrefEnum.VALID);
				} else {
					for (ParcEtJardin pj : pjs) {
						s = GeometryQueryHelper.toText(pj.getCoordonnee());
						List<ParcPrefecture>  pps = parcPrefectureRepository.findByArea(s);
						if (pps!=null && pps.size()==1) {
							pp.setParcEtJardin(pj);
							pp.setName(pj.getName());
							updated=true;
							pp.setStatus(ParcStatusPrefEnum.VALID);
						} else {
							pp.setParcEtJardin(pjs.get(0));
							pp.setName(pjs.get(0).getName());
							updated=true;
							pp.setStatus(ParcStatusPrefEnum.TO_QUALIFY);
						}
					}
				}				
				
			} else {
				updated=true;
				pp.setStatus(ParcStatusPrefEnum.NO_MATCH);
			}
		}
		
		if (updated) {
			pp= parcPrefectureRepository.save(pp);
		}
		return pp;
	}

	/**
	 * prepareFromSite.
	 * @param name NamePrefecture
	 * @param polygon Polygon
	 * @return ParcPrefecture
	 */
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
		computeAndUpdate(pp);
		return pp;
	}
	
	
	/**
	 * normalize.
	 * @param input string
	 * @return string
	 */
	private static String normalize(String input) {
	    return input == null ? null : Normalizer.normalize(input, Normalizer.Form.NFKD);
	}
	/**
	 * removeAccents.
	 * @param input string
	 * @return string
	 */
	private static String removeAccents(String input) {
	    return normalize(input).replaceAll("\\p{M}", "");
	}

}
