package com.github.cunvoas.geoserviceisochrone.service.entrance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Laposte;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.LaposteRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParcPrefectureRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.RegionRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service métier permettant la lecture et l'accès aux différentes références du domaine (régions, communes, parcs, etc.).
 * Fournit des méthodes pour récupérer les entités principales utilisées dans l'application à partir des repositories associés.
 */
@Service
@Slf4j
public class ServiceReadReferences {
	

	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	@Autowired
	private RegionRepository regionRepository;
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private CityRepository cityRepository;
	@Autowired
	private CadastreRepository cadastreRepository;
	@Autowired
	private LaposteRepository laposteRepository;
	@Autowired
	private ParkJardinRepository parkJardinRepository;
	@Autowired
	private ParkAreaRepository parkAreaRepository;
	@Autowired
	private ParkAreaComputedRepository parkAreaComputedRepository;
	@Autowired
	private ParkEntranceRepository parkEntranceRepository;
	@Autowired
	private ParcPrefectureRepository parcPrefectureRepository;
	
	
	
	/**
	 * Retourne la liste des régions triées par nom.
	 * @return liste des régions
	 */
	public List<Region> getRegion() {
		return regionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}
	
	/**
	 * Retourne une entité ParkAreaComputed par son identifiant pour l'année courante.
	 * @param id identifiant de ParkAreaComputed
	 * @return ParkAreaComputed ou null si non trouvé
	 */
	public ParkAreaComputed getParkAreaComputedById(Long id) {
		return this.getParkAreaComputedById(id, applicationBusinessProperties.getDerniereAnnee());
	}
	/**
	 * Retourne une entité ParkAreaComputed par son identifiant et une année donnée.
	 * @param id identifiant de ParkAreaComputed
	 * @param annee année recherchée
	 * @return ParkAreaComputed ou null si non trouvé
	 */
	public ParkAreaComputed getParkAreaComputedById(Long id, Integer annee) {
		Optional<ParkAreaComputed> opt = parkAreaComputedRepository.findByIdAndAnnee(id, annee);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}
	/**
	 * Retourne une entité ParkArea par son identifiant.
	 * @param id identifiant de ParkArea
	 * @return ParkArea ou null si non trouvé
	 */
	public ParkArea getParkAreaById(Long id) {
		Optional<ParkArea> opt = parkAreaRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}
	/**
	 * Retourne une entité ParkArea à partir de l'identifiant d'un ParcEtJardin.
	 * @param id identifiant de ParcEtJardin
	 * @return ParkArea ou null si non trouvé
	 */
	public ParkArea getByIdParcEtJardin(Long id) {
		return  parkAreaRepository.findByIdParcEtJardin(id);
	}
	
	/**
	 * Retourne une entité CommunauteCommune à partir de l'identifiant.
	 * @param id
	 * @return
	 */
	public CommunauteCommune getCommunauteCommuneById(Long id) {
		Optional<CommunauteCommune> opt=communauteCommuneRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}
	
	
	/**
	 * Retourne la liste des communautés de communes triées par nom.
	 * @return liste des communautés de communes
	 */
	public List<CommunauteCommune> getCommunauteCommune() {
		return communauteCommuneRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}
	/**
	 * Retourne la liste des communautés de communes d'une région donnée.
	 * @param id identifiant de la région
	 * @return liste des communautés de communes
	 */
	public List<CommunauteCommune> getCommunauteByRegionId(Long id) {
		return communauteCommuneRepository.findByRegionId(id);
	}
	
	/**
	 * Retourne la liste des villes d'une région donnée.
	 * @param id identifiant de la région
	 * @return liste des villes
	 */
	public List<City> getCityByRegionId(Long id) {
		return cityRepository.findByRegionId(id);
	}
	
	/**
	 * Retourne une ville par son identifiant.
	 * @param id identifiant de la ville
	 * @return la ville ou null si non trouvée
	 */
	public City getCityById(Long id) {
		Optional<City> opt=cityRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}


	/**
	 * Retourne une référence de ville par son identifiant (sans chargement complet).
	 * @param id identifiant de la ville
	 * @return la ville
	 */
	public City getCity(Long id) {
		return cityRepository.getReferenceById(id);
	}
	
	/**
	 * Retourne les coordonnées d'une ville à partir de son identifiant.
	 * @param id identifiant de la ville
	 * @return coordonnées (longitude, latitude) ou null si non trouvées
	 */
	public Coordinate getCoordinate(Long id) {
		Coordinate location=null;
		
		if (id!=null) {
			City city = cityRepository.getReferenceById(id);
			
			Cadastre cadastre=cadastreRepository.getReferenceById(city.getInseeCode());
			if (cadastre!=null && cadastre.getGeoShape()!=null) {
				org.locationtech.jts.geom.Coordinate coord=null;
				if ( cadastre.getGeoShape() instanceof Point) {
					coord = ((Point)cadastre.getGeoShape()).getCoordinate();
				} else if ( cadastre.getGeoShape() instanceof Polygon) {
					coord = ((Polygon)cadastre.getGeoShape()).getCentroid().getCoordinate();
				} else if ( cadastre.getGeoShape() instanceof MultiPolygon) {
					coord = ((MultiPolygon)cadastre.getGeoShape()).getCentroid().getCoordinate();
				}
				
				if (coord!=null) {
					location = new Coordinate(coord.getX(), coord.getY());
				}
			}
		
			if (location==null) {
				Laposte poste = laposteRepository.getReferenceById(city.getInseeCode());
				if (poste!=null) {
					String gps = poste.getCoordonneesGps();
					String[] coord = gps.split(", ");
					location = new Coordinate(Double.valueOf(coord[1]), Double.valueOf(coord[1]));
				}
			}
		}
		return location;
	}
	
	
	/**
	 * Retourne la liste des villes d'une communauté de communes.
	 * @param id identifiant de la communauté de communes
	 * @return liste des villes
	 */
	public List<City> getCityByCommunauteCommuneId(Long id) {
		return cityRepository.findByCommunauteCommuneId(id);
	}

	/**
	 * Retourne la liste des parcs et jardins d'une ville.
	 * @param id identifiant de la ville
	 * @return liste des parcs et jardins
	 */
	public List<ParcEtJardin> getParcEtJardinByCityId(Long id) {
		return parkJardinRepository.findByCityId(id);
	}
	/**
	 * Retourne une page de parcs et jardins d'une ville.
	 * @param idCommune identifiant de la ville
	 * @param pageable pagination
	 * @return page de parcs et jardins
	 */
	public Page<ParcEtJardin> getParcEtJardinByCityId(Long idCommune, Pageable pageable) {
		return parkJardinRepository.findByCityId(idCommune, pageable);
	}
	
	/**
	 * Retourne une page de parcs et jardins d'une communauté de communes selon le cas d'usage.
	 * @param idComm2co identifiant de la communauté de communes
	 * @param parkCase cas d'usage (merge, compute, ...)
	 * @param pageable pagination
	 * @return page de parcs et jardins
	 */
	public Page<ParcEtJardin> getParcEtJardinByComm2coId(Long idComm2co, String parkCase, Pageable pageable) {
		Page<ParcEtJardin> rets = null;
		
		if ("merge".equalsIgnoreCase(parkCase)) {
			rets = parkJardinRepository.findByComm2CoIdToMerge(idComm2co, pageable);
		} else if ("compute".equalsIgnoreCase(parkCase)) {
			rets = parkJardinRepository.findByComm2CoIdToCompute(idComm2co, pageable);
		} else {
			rets = parkJardinRepository.findByComm2CoId(idComm2co, pageable);
		}
		
		return rets;
	}
	/**
	 * Retourne une page de parcs et jardins d'une ville selon le cas d'usage.
	 * @param id identifiant de la ville
	 * @param parkCase cas d'usage (merge, compute, ...)
	 * @param pageable pagination
	 * @return page de parcs et jardins
	 */
	public Page<ParcEtJardin> getParcEtJardinByCityId(Long id, String parkCase, Pageable pageable) {
		if ("merge".equalsIgnoreCase(parkCase)) {
			return parkJardinRepository.findByCityIdToMerge(id, pageable);
		} else if ("compute".equalsIgnoreCase(parkCase)) {
			return parkJardinRepository.findByCityIdToCompute(id, pageable);
		} else {
			return parkJardinRepository.findByCityId(id, pageable);
		}
	}
	
	/**
	 * Retourne un parc ou jardin par son identifiant.
	 * @param id identifiant du parc ou jardin
	 * @return ParcEtJardin ou null si non trouvé
	 */
	public ParcEtJardin getParcEtJardinById(Long id) {
		Optional<ParcEtJardin> opt=parkJardinRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}

	/**
	 * Retourne un parc préfectoral par son identifiant.
	 * @param id identifiant du parc préfectoral
	 * @return Optional contenant le parc préfectoral ou vide si non trouvé
	 */
	public Optional<ParcPrefecture> getParcPrefectureById(Long id) {
		Optional<ParcPrefecture> opt=parcPrefectureRepository.findById(id);
		return opt;
	}
	
	/**
	 * Retourne un parc préfectoral à partir de l'identifiant d'un parc ou jardin.
	 * @param id identifiant du parc ou jardin
	 * @return ParcPrefecture ou null si non trouvé
	 */
	public ParcPrefecture getParcPrefectureByParcEtJardinId(Long id) {
		List<ParcPrefecture> lst=parcPrefectureRepository.findByParcEtJardinId(id);
		if (!CollectionUtils.isEmpty(lst)) {
			return lst.get(0);
		}
		return null;
	}

	/**
	 * Retourne la liste des entrées d'un parc à partir de son identifiant.
	 * @param id identifiant du parc
	 * @return liste des entrées du parc
	 */
	public List<ParkEntrance> getEntranceByParkId(Long id) {
		return parkEntranceRepository.findByParkId(id);
	}
	/**
	 * Retourne une entrée de parc par son identifiant.
	 * @param id identifiant de l'entrée
	 * @return ParkEntrance ou null si non trouvé
	 */
	public ParkEntrance getEntranceById(Long id) {
		Optional<ParkEntrance> opt=parkEntranceRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
		
	}
	
	/**
	 * Retourne la liste des sources de parcs disponibles (enum).
	 * @return liste des sources de parcs
	 */
	public List<ParcSourceEnum> getParcSource() {
		List<ParcSourceEnum> l = new ArrayList<>();
		l.add(ParcSourceEnum.OPENDATA);
		l.add(ParcSourceEnum.PREFECTURE);
		l.add(ParcSourceEnum.AUTMEL);
		return l;
	}
}
