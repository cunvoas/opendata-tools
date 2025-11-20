package com.github.cunvoas.geoserviceisochrone.controller.mvc.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.service.admin.CityAssociationService;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;

/**
 * Contrôleur de gestion de l'association des villes aux communautés de communes (interface d'administration).
 * Permet d'afficher la liste des communautés avec leurs villes et d'associer/désassocier des villes.
 */
@Controller
@RequestMapping("/mvc/management/cityassoc")
public class CityAssociationControler {
	
	private String formName = "editCityAssociation";
	private String listName = "listCityAssociation";
	
	@Autowired
	private CityAssociationService cityAssociationService;
	
	@Autowired
	private ServiceReadReferences serviceReadReferences;

	/**
	 * Affiche la liste des communautés de communes avec leurs villes associées.
	 * @param regionId Identifiant de la région (optionnel, seulement pour admin)
	 * @param model Modèle de la vue
	 * @return Nom de la page de liste
	 */
	@GetMapping("/list")
	public String getList(@RequestParam(name = "regionId", required = false) Long regionId, Model model) {
		Contributeur contrib = (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		// Vérifier les droits d'accès
		if (!ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			throw new ExceptionAdmin("ACCESS_NOT_ALLOWED");
		}
		
		// Récupérer les communautés filtrées par région si nécessaire
		List<CommunauteCommune> communautes;
		if (regionId != null) {
			communautes = serviceReadReferences.getCommunauteByRegionId(regionId);
		} else {
			communautes = cityAssociationService.findAllCommunauteCommunes();
		}
		
		model.addAttribute(listName, communautes);
		model.addAttribute("regions", serviceReadReferences.getRegion());
		model.addAttribute("selectedRegionId", regionId);
		model.addAttribute("isAdmin", true);
		
		return listName;
	}
	
	/**
	 * Affiche le formulaire d'association de villes à une communauté de communes.
	 * @param communauteCommuneId Identifiant de la communauté de communes
	 * @param regionId Identifiant de la région (optionnel)
	 * @param model Modèle de la vue
	 * @return Nom de la page de formulaire
	 */
	@GetMapping("/edit")
	public String edit(
			@RequestParam(required = false) Long communauteCommuneId,
			@RequestParam(name = "regionId", required = false) Long regionId,
			Model model) {
		Contributeur contrib = (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		// Vérifier les droits d'accès
		if (!ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			throw new ExceptionAdmin("ACCESS_NOT_ALLOWED");
		}
		
		// Récupérer la communauté de communes sélectionnée ou créer une nouvelle
		CommunauteCommune selectedCommunaute = null;
		if (communauteCommuneId != null) {
			selectedCommunaute = cityAssociationService.findCommunauteCommuneById(communauteCommuneId);
		}
		
		// Récupérer les communautés de communes filtrées par région si nécessaire
		List<CommunauteCommune> communautes;
		if (regionId != null) {
			communautes = serviceReadReferences.getCommunauteByRegionId(regionId);
		} else {
			communautes = cityAssociationService.findAllCommunauteCommunes();
		}
		
		// Récupérer les villes associées à la communauté sélectionnée
		List<City> associatedCities = null;
		if (selectedCommunaute != null) {
			associatedCities = cityAssociationService.findCitiesByCommunauteCommune(selectedCommunaute.getId());
		}
		
		// Récupérer les villes filtrées par région si nécessaire
		List<City> allCities;
		if (regionId != null) {
			allCities = serviceReadReferences.getCityByRegionId(regionId);
		} else {
			allCities = cityAssociationService.findAllCities();
		}
		
		model.addAttribute("communautes", communautes);
		model.addAttribute("selectedCommunaute", selectedCommunaute);
		model.addAttribute("associatedCities", associatedCities);
		model.addAttribute("allCities", allCities);
		model.addAttribute("regions", serviceReadReferences.getRegion());
		model.addAttribute("selectedRegionId", regionId);
		model.addAttribute("isAdmin", true);
		
		return formName;
	}
	
	/**
	 * Associe une ville à une communauté de communes.
	 * @param cityId Identifiant de la ville
	 * @param communauteCommuneId Identifiant de la communauté de communes
	 * @param model Modèle de la vue
	 * @return Redirection vers la page d'édition
	 */
	@PostMapping("/associate")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public String associateCity(
			@RequestParam Long cityId, 
			@RequestParam Long communauteCommuneId, 
			Model model) {
		
		Contributeur contrib = (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		// Vérifier les droits d'accès
		if (!ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			throw new ExceptionAdmin("EDIT_NOT_ALLOWED");
		}
		
		// Associer la ville à la communauté de communes
		cityAssociationService.associateCity(cityId, communauteCommuneId);
		
		// Rediriger vers la page d'édition avec la communauté sélectionnée
		return "redirect:/mvc/management/cityassoc/edit?communauteCommuneId=" + communauteCommuneId;
	}
	
	/**
	 * Dissocie une ville de sa communauté de communes.
	 * @param cityId Identifiant de la ville
	 * @param communauteCommuneId Identifiant de la communauté de communes (pour redirection)
	 * @param model Modèle de la vue
	 * @return Redirection vers la page d'édition
	 */
	@PostMapping("/dissociate")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public String dissociateCity(
			@RequestParam Long cityId, 
			@RequestParam Long communauteCommuneId, 
			Model model) {
		
		Contributeur contrib = (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		// Vérifier les droits d'accès
		if (!ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			throw new ExceptionAdmin("EDIT_NOT_ALLOWED");
		}
		
		// Dissocier la ville
		cityAssociationService.dissociateCity(cityId);
		
		// Rediriger vers la page d'édition avec la communauté sélectionnée
		return "redirect:/mvc/management/cityassoc/edit?communauteCommuneId=" + communauteCommuneId;
	}
}
