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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
	 * Les non-admin sont redirigés vers la page d'édition de leur communauté.
	 * @param regionId Identifiant de la région (optionnel, seulement pour admin)
	 * @param model Modèle de la vue
	 * @return Nom de la page de liste ou redirection
	 */
	@GetMapping("/list")
	public String getList(@RequestParam(name = "regionId", required = false) Long regionId, Model model) {
		Contributeur contrib = (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		boolean isAdmin = ContributeurRole.ADMINISTRATOR.equals(contrib.getRole());
		
		// Si non-admin, rediriger directement vers la page d'édition
		if (!isAdmin) {
			return "redirect:/mvc/management/cityassoc/edit";
		}
		
		// Si aucune région n'est spécifiée, utiliser la région du profil de l'admin
		if (regionId == null && contrib.getIdRegion() != null) {
			regionId = contrib.getIdRegion();
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
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("readOnly", false);
		
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
			@RequestParam(name = "communauteCommuneId", required = false) Long communauteCommuneId,
			@RequestParam(name = "regionId", required = false) Long regionId,
			Model model) {
		Contributeur contrib = (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		boolean isAdmin = ContributeurRole.ADMINISTRATOR.equals(contrib.getRole());
		boolean readOnly = !isAdmin;
		
		// Si non-admin, forcer l'affichage de sa propre communauté en lecture seule
		if (!isAdmin) {
			if (contrib.getIdCommunauteCommune() == null) {
				throw new ExceptionAdmin("NO_COMMUNITY_ASSIGNED");
			}
			communauteCommuneId = contrib.getIdCommunauteCommune();
			// Forcer la région correspondante
			CommunauteCommune userCommunaute = cityAssociationService.findCommunauteCommuneById(communauteCommuneId);
			if (userCommunaute != null && userCommunaute.getRegion() != null) {
				regionId = userCommunaute.getRegion().getId();
			}
		} else {
			// Si admin et aucune région n'est spécifiée, utiliser la région du profil
			if (regionId == null && contrib.getIdRegion() != null) {
				regionId = contrib.getIdRegion();
			}
		}
		
		// Récupérer la communauté de communes sélectionnée
		CommunauteCommune selectedCommunaute = null;
		if (communauteCommuneId != null) {
			selectedCommunaute = cityAssociationService.findCommunauteCommuneById(communauteCommuneId);
		}
		
		// Récupérer les communautés de communes filtrées par région si nécessaire
		// Pour les non-admin, on ne charge que leur communauté
		List<CommunauteCommune> communautes;
		if (!isAdmin && selectedCommunaute != null) {
			communautes = List.of(selectedCommunaute);
		} else if (regionId != null) {
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
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("readOnly", readOnly);
		
		return formName;
	}
	
	/**
	 * Associe une ville à une communauté de communes.
	 * @param cityId Identifiant de la ville
	 * @param communauteCommuneId Identifiant de la communauté de communes
	 * @param regionId Identifiant de la région (pour conserver le filtre)
	 * @param model Modèle de la vue
	 * @return Redirection vers la page d'édition
	 */
	@PostMapping("/associate")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public String associateCity(
			@RequestParam("cityId") Long cityId, 
			@RequestParam("communauteCommuneId") Long communauteCommuneId,
			@RequestParam(name = "regionId", required = false) Long regionId,
			Model model) {
		
		Contributeur contrib = (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		// Vérifier les droits d'accès
		if (!ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			throw new ExceptionAdmin("EDIT_NOT_ALLOWED");
		}
		
		// Associer la ville à la communauté de communes
		cityAssociationService.associateCity(cityId, communauteCommuneId);
		
		// Rediriger vers la page d'édition avec la communauté sélectionnée
		String redirectUrl = "redirect:/mvc/management/cityassoc/edit?communauteCommuneId=" + communauteCommuneId;
		if (regionId != null) {
			redirectUrl += "&regionId=" + regionId;
		}
		return redirectUrl;
	}
	
	/**
	 * Dissocie une ville de sa communauté de communes.
	 * @param cityId Identifiant de la ville
	 * @param communauteCommuneId Identifiant de la communauté de communes (pour redirection)
	 * @param regionId Identifiant de la région (pour conserver le filtre)
	 * @param model Modèle de la vue
	 * @return Redirection vers la page d'édition
	 */
	@PostMapping("/dissociate")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public String dissociateCity(
			@RequestParam("cityId") Long cityId, 
			@RequestParam("communauteCommuneId") Long communauteCommuneId,
			@RequestParam(name = "regionId", required = false) Long regionId,
			Model model) {
		
		Contributeur contrib = (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		// Vérifier les droits d'accès
		if (!ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			throw new ExceptionAdmin("EDIT_NOT_ALLOWED");
		}
		
		// Dissocier la ville
		cityAssociationService.dissociateCity(cityId);
		
		// Rediriger vers la page d'édition avec la communauté sélectionnée
		String redirectUrl = "redirect:/mvc/management/cityassoc/edit?communauteCommuneId=" + communauteCommuneId;
		if (regionId != null) {
			redirectUrl += "&regionId=" + regionId;
		}
		return redirectUrl;
	}
	
	/**
	 * Traite l'upload d'un fichier CSV pour associer plusieurs villes à une communauté de communes.
	 * @param file Fichier CSV contenant les codes INSEE
	 * @param communauteCommuneId Identifiant de la communauté de communes
	 * @param regionId Identifiant de la région (pour conserver le filtre)
	 * @param redirectAttributes Attributs pour la redirection
	 * @return Redirection vers la page d'édition
	 */
	@PostMapping("/uploadCsv")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public String uploadCsv(
			@RequestParam("file") MultipartFile file,
			@RequestParam("communauteCommuneId") Long communauteCommuneId,
			@RequestParam(name = "regionId", required = false) Long regionId,
			RedirectAttributes redirectAttributes) {
		
		Contributeur contrib = (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		// Vérifier les droits d'accès
		if (!ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			throw new ExceptionAdmin("EDIT_NOT_ALLOWED");
		}
		
		// Vérifier que le fichier n'est pas vide
		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Le fichier CSV est vide");
			return "redirect:/mvc/management/cityassoc/edit?communauteCommuneId=" + communauteCommuneId + 
					(regionId != null ? "&regionId=" + regionId : "");
		}
		
		// Vérifier le type de fichier
		String filename = file.getOriginalFilename();
		if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
			redirectAttributes.addFlashAttribute("errorMessage", "Le fichier doit être au format CSV");
			return "redirect:/mvc/management/cityassoc/edit?communauteCommuneId=" + communauteCommuneId +
					(regionId != null ? "&regionId=" + regionId : "");
		}
		
		// Traiter le fichier CSV
		CityAssociationService.CsvUploadResult result = cityAssociationService.associateCitiesFromCsv(file, communauteCommuneId);
		
		// Construire le message de résultat
		StringBuilder message = new StringBuilder();
		if (!result.getSuccessList().isEmpty()) {
			message.append(result.getSuccessList().size()).append(" ville(s) associée(s) avec succès. ");
		}
		if (!result.getAlreadyAssociatedList().isEmpty()) {
			message.append(result.getAlreadyAssociatedList().size()).append(" ville(s) déjà associée(s). ");
		}
		if (!result.getNotFoundList().isEmpty()) {
			message.append(result.getNotFoundList().size()).append(" code(s) INSEE non trouvé(s). ");
		}
		
		// Ajouter les messages au modèle
		if (result.hasErrors() || !result.getNotFoundList().isEmpty()) {
			redirectAttributes.addFlashAttribute("warningMessage", message.toString());
			redirectAttributes.addFlashAttribute("uploadResult", result);
		} else {
			redirectAttributes.addFlashAttribute("successMessage", message.toString());
		}
		
		// Rediriger vers la page d'édition avec la communauté sélectionnée
		return "redirect:/mvc/management/cityassoc/edit?communauteCommuneId=" + communauteCommuneId +
				(regionId != null ? "&regionId=" + regionId : "");
	}
}
