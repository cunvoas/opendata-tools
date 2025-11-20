package com.github.cunvoas.geoserviceisochrone.service.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;

/**
 * Service de gestion de l'association des villes aux communautés de communes.
 * Permet d'effectuer des opérations d'association et de désassociation entre villes et communautés.
 */
@Service
public class CityAssociationService {
	
	@Autowired
	private CityRepository cityRepository;
	
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	
	/**
	 * Recherche une ville par son identifiant.
	 * 
	 * @param id Identifiant de la ville
	 * @return City ou null si non trouvée
	 */
	public City findCityById(Long id) {
		Optional<City> opt = cityRepository.findById(id);
		return opt.orElse(null);
	}
	
	/**
	 * Recherche une communauté de communes par son identifiant.
	 * 
	 * @param id Identifiant de la communauté de communes
	 * @return CommunauteCommune ou null si non trouvée
	 */
	public CommunauteCommune findCommunauteCommuneById(Long id) {
		Optional<CommunauteCommune> opt = communauteCommuneRepository.findById(id);
		return opt.orElse(null);
	}
	
	/**
	 * Retourne toutes les villes triées par nom.
	 * 
	 * @return Liste de toutes les villes
	 */
	public List<City> findAllCities() {
		return cityRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}
	
	/**
	 * Retourne toutes les communautés de communes triées par nom.
	 * 
	 * @return Liste de toutes les communautés de communes
	 */
	public List<CommunauteCommune> findAllCommunauteCommunes() {
		return communauteCommuneRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}
	
	/**
	 * Retourne les villes d'une communauté de communes.
	 * 
	 * @param communauteCommuneId Identifiant de la communauté de communes
	 * @return Liste des villes de la communauté
	 */
	public List<City> findCitiesByCommunauteCommune(Long communauteCommuneId) {
		return cityRepository.findByCommunauteCommuneId(communauteCommuneId);
	}
	
	/**
	 * Retourne les villes d'une région données.
	 * 
	 * @param regionId Identifiant de la région
	 * @return Liste des villes de la région
	 */
	public List<City> findCitiesByRegion(Long regionId) {
		return cityRepository.findByRegionId(regionId);
	}
	
	/**
	 * Associe une ville à une communauté de communes.
	 * 
	 * @param cityId Identifiant de la ville
	 * @param communauteCommuneId Identifiant de la communauté de communes
	 * @return City mise à jour
	 */
	@Transactional
	public City associateCity(Long cityId, Long communauteCommuneId) {
		City city = findCityById(cityId);
		CommunauteCommune communauteCommune = findCommunauteCommuneById(communauteCommuneId);
		
		if (city != null && communauteCommune != null) {
			city.setCommunauteCommune(communauteCommune);
			return cityRepository.save(city);
		}
		
		return null;
	}
	
	/**
	 * Dissocie une ville de sa communauté de communes.
	 * 
	 * @param cityId Identifiant de la ville
	 * @return City mise à jour
	 */
	@Transactional
	public City dissociateCity(Long cityId) {
		City city = findCityById(cityId);
		
		if (city != null) {
			city.setCommunauteCommune(null);
			return cityRepository.save(city);
		}
		
		return null;
	}
	
	/**
	 * Sauvegarde une ville (création ou modification).
	 * 
	 * @param city City à sauvegarder
	 * @return City sauvegardée
	 */
	@Transactional
	public City save(City city) {
		return cityRepository.save(city);
	}
}
