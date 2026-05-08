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
import com.github.cunvoas.geoserviceisochrone.service.park.IComputeCarreService;

/**
 * Service métier pour la gestion des parcs préfectoraux.
 * <p>
 * Ce service fournit des méthodes pour :
 * <ul>
 *   <li>Mettre à jour les entités ParcPrefecture en recalculant leur surface et leur association avec un ParcEtJardin</li>
 *   <li>Récupérer un ParcPrefecture par son identifiant</li>
 *   <li>Enregistrer ou mettre à jour un ParcPrefecture</li>
 *   <li>Préparer un ParcPrefecture à partir d'un nom et d'un polygone</li>
 *   <li>Normaliser et supprimer les accents d'une chaîne de caractères</li>
 * </ul>
 *
 * Les dépendances sont injectées via l'annotation @Autowired de Spring.
 */
@Service
public class ServiceParcPrefecture {
	
	@Autowired 
	private ParcPrefectureRepository parcPrefectureRepository;

	@Autowired 
	private ParkJardinRepository parkJardinRepository;

	@Autowired 
	private IComputeCarreService computeService;

	@Autowired 
	private CityService cityService;
	
	/**
	 * Met à jour tous les objets ParcPrefecture en recalculant leur surface et leur association avec un ParcEtJardin.
	 */
	public void update() {
		List<ParcPrefecture> pps = parcPrefectureRepository.findAll();
		for (ParcPrefecture pp : pps) {
			computeAndUpdate(pp);
		}
	}
	
	/**
	 * Récupère un ParcPrefecture par son identifiant.
	 * @param id Identifiant du ParcPrefecture
	 * @return ParcPrefecture correspondant ou null si non trouvé
	 */
	public ParcPrefecture getById(Long id) {
		Optional<ParcPrefecture> opt = parcPrefectureRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}

	/**
	 * Enregistre ou met à jour un ParcPrefecture.
	 * @param pp ParcPrefecture à enregistrer ou mettre à jour
	 * @return ParcPrefecture enregistré ou mis à jour
	 */
	public ParcPrefecture update(ParcPrefecture pp) {
		return parcPrefectureRepository.save(pp);
	}

	/**
	 * Calcule la surface et l'association ParcEtJardin pour un ParcPrefecture, puis met à jour l'entité si nécessaire.
	 * @param pp ParcPrefecture à traiter
	 * @return ParcPrefecture mis à jour
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
	 * Prépare un ParcPrefecture à partir d'un nom et d'un polygone, en associant la commune la plus proche.
	 * @param name Nom de la préfecture
	 * @param polygon Polygone représentant la zone du parc
	 * @return ParcPrefecture créé et enregistré
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
	 * Normalise une chaîne de caractères (NFKD).
	 * @param input Chaîne à normaliser
	 * @return Chaîne normalisée
	 */
	private static String normalize(String input) {
	    return input == null ? null : Normalizer.normalize(input, Normalizer.Form.NFKD);
	}
	/**
	 * Supprime les accents d'une chaîne de caractères.
	 * @param input Chaîne d'entrée
	 * @return Chaîne sans accents
	 */
	private static String removeAccents(String input) {
	    return normalize(input).replaceAll("\\p{M}", "");
	}

}