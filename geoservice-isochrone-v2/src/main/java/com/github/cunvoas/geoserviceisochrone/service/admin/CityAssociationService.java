package com.github.cunvoas.geoserviceisochrone.service.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;

/**
 * Service de gestion de l'association des villes aux communautés de communes.
 * Permet d'effectuer des opérations d'association et de désassociation entre villes et communautés.
 */
@Service
@Slf4j
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
	
	/**
	 * Associe plusieurs villes à une communauté de communes à partir d'un fichier CSV contenant des codes INSEE.
	 * Format attendu : une colonne avec les codes INSEE (5 caractères), avec ou sans en-tête.
	 * 
	 * @param file Fichier CSV uploadé
	 * @param communauteCommuneId Identifiant de la communauté de communes
	 * @return CsvUploadResult avec les statistiques d'import
	 */
	@Transactional
	public CsvUploadResult associateCitiesFromCsv(MultipartFile file, Long communauteCommuneId) {
		CsvUploadResult result = new CsvUploadResult();
		
		CommunauteCommune communauteCommune = findCommunauteCommuneById(communauteCommuneId);
		if (communauteCommune == null) {
			result.addError("Communauté de communes non trouvée (ID: " + communauteCommuneId + ")");
			return result;
		}
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
			String line;
			int lineNumber = 0;
			
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				line = line.trim();
				
				// Ignorer les lignes vides
				if (line.isEmpty()) {
					continue;
				}
				
				// Ignorer la première ligne si elle ressemble à un en-tête
				if (lineNumber == 1 && (line.toLowerCase().contains("insee") || line.toLowerCase().contains("code"))) {
					continue;
				}
				
				// Gérer les CSV avec séparateurs (prendre la première colonne)
				String inseeCode = line.split("[,;\\t]")[0].trim();
				
				// Valider le format du code INSEE (5 chiffres)
				if (!inseeCode.matches("\\d{5}")) {
					result.addError("Ligne " + lineNumber + ": Code INSEE invalide '" + inseeCode + "' (attendu: 5 chiffres)");
					continue;
				}
				
				// Rechercher la ville par code INSEE
				City city = cityRepository.findByInseeCode(inseeCode);
				
				if (city == null) {
					result.addNotFound(inseeCode);
					log.warn("Ville non trouvée pour le code INSEE: {}", inseeCode);
				} else {
					// Vérifier si la ville est déjà associée à une communauté
					if (city.getCommunauteCommune() != null) {
						if (city.getCommunauteCommune().getId().equals(communauteCommuneId)) {
							result.addAlreadyAssociated(inseeCode + " (" + city.getName() + ")");
						} else {
							result.addError("Ligne " + lineNumber + ": La ville '" + city.getName() + "' (" + inseeCode + ") est déjà associée à '" + city.getCommunauteCommune().getName() + "'");
						}
					} else {
						// Associer la ville
						city.setCommunauteCommune(communauteCommune);
						cityRepository.save(city);
						result.addSuccess(inseeCode + " (" + city.getName() + ")");
						log.info("Ville associée: {} ({}) -> {}", city.getName(), inseeCode, communauteCommune.getName());
					}
				}
			}
			
		} catch (IOException e) {
			log.error("Erreur lors de la lecture du fichier CSV", e);
			result.addError("Erreur lors de la lecture du fichier: " + e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Classe pour stocker le résultat de l'upload CSV.
	 */
	@Data
	public static class CsvUploadResult {
		private List<String> successList = new ArrayList<>();
		private List<String> notFoundList = new ArrayList<>();
		private List<String> alreadyAssociatedList = new ArrayList<>();
		private List<String> errorList = new ArrayList<>();
		
		public void addSuccess(String item) {
			successList.add(item);
		}
		
		public void addNotFound(String inseeCode) {
			notFoundList.add(inseeCode);
		}
		
		public void addAlreadyAssociated(String item) {
			alreadyAssociatedList.add(item);
		}
		
		public void addError(String error) {
			errorList.add(error);
		}
		
		public int getTotalProcessed() {
			return successList.size() + notFoundList.size() + alreadyAssociatedList.size() + errorList.size();
		}
		
		public boolean hasErrors() {
			return !errorList.isEmpty();
		}
	}
}
