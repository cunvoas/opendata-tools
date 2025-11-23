package com.github.cunvoas.geoserviceisochrone.controller.mvc.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.TokenManagement;
import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.service.admin.CommunauteCommuneService;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;

/**
 * Contrôleur de gestion des communautés de communes (interface d'administration).
 * Permet d'afficher, d'ajouter, d'éditer et de sauvegarder les communautés de communes.
 */
@Controller
@RequestMapping("/mvc/management/comm2co")
public class CommunauteCommuneControler {
	
	private String formName = "editCommunauteCommune";
	private String listName = "listCommunauteCommune";
	
	@Autowired
	private CommunauteCommuneService communauteCommuneService;
	
	@Autowired
	private ServiceReadReferences serviceReadReferences;
	
	@Autowired
	private TokenManagement tokenManagement;

	/**
	 * Affiche la liste des communautés de communes accessibles à l'utilisateur connecté.
	 * @param regionId Identifiant de la région (optionnel, seulement pour admin)
	 * @param model Modèle de la vue
	 * @return Nom de la page de liste
	 */
	@GetMapping("/list")
	public String getList(@RequestParam(name = "regionId", required = false) Long regionId, Model model) {
		Contributeur contrib = (Contributeur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		// Déterminer la région à utiliser
		Long effectiveRegionId = regionId;
		boolean isAdmin = ContributeurRole.ADMINISTRATOR.equals(contrib.getRole());
		
		if (!isAdmin) {
			// Non-admin : utiliser la région du contributeur
			effectiveRegionId = contrib.getIdRegion();
		} else if (regionId == null) {
			// Admin sans filtre : utiliser sa région par défaut
			effectiveRegionId = contrib.getIdRegion();
		}
		
		model.addAttribute(listName, communauteCommuneService.findByContextUserAndRegion(contrib, effectiveRegionId));
		model.addAttribute("regions", serviceReadReferences.getRegion());
		model.addAttribute("selectedRegionId", effectiveRegionId);
		model.addAttribute("isAdmin", isAdmin);
		return listName;
	}
	
	/**
	 * Affiche le formulaire d'ajout d'une nouvelle communauté de communes.
	 * @param model Modèle de la vue
	 * @return Nom de la page de formulaire
	 */
	@GetMapping("/add")
	public String add(Model model) {
		Contributeur contrib = (Contributeur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		boolean isAdmin = ContributeurRole.ADMINISTRATOR.equals(contrib.getRole());
		
		CommunauteCommune communauteCommune = new CommunauteCommune();
		
		// Pour les non-admins, préremplir avec leur région
		if (!isAdmin && contrib.getIdRegion() != null) {
			Region region = serviceReadReferences.getRegion().stream()
				.filter(r -> r.getId().equals(contrib.getIdRegion()))
				.findFirst()
				.orElse(null);
			communauteCommune.setRegion(region);
		}
		
		model.addAttribute(formName, communauteCommune);
		
		// Admin : toutes les régions, non-admin : seulement leur région
		if (isAdmin) {
			model.addAttribute("regions", serviceReadReferences.getRegion());
		} else if (contrib.getIdRegion() != null) {
			model.addAttribute("regions", serviceReadReferences.getRegion().stream()
				.filter(r -> r.getId().equals(contrib.getIdRegion()))
				.collect(java.util.stream.Collectors.toList()));
		} else {
			model.addAttribute("regions", java.util.Collections.emptyList());
		}
		
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("token", tokenManagement.getValidToken());
		return formName;
	}
	
	/**
	 * Affiche le formulaire d'édition d'une communauté de communes existante.
	 * @param id Identifiant de la communauté de communes
	 * @param model Modèle de la vue
	 * @return Nom de la page de formulaire
	 */
	@GetMapping("/edit")
	public String edit(@RequestParam(name = "id") Long id, Model model) {
		Contributeur contrib = (Contributeur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		boolean isAdmin = ContributeurRole.ADMINISTRATOR.equals(contrib.getRole());
		
		CommunauteCommune communauteCommune = communauteCommuneService.findById(id);
		
		model.addAttribute(formName, communauteCommune);
		
		// Admin : toutes les régions, non-admin : seulement leur région
		if (isAdmin) {
			model.addAttribute("regions", serviceReadReferences.getRegion());
		} else if (contrib.getIdRegion() != null) {
			model.addAttribute("regions", serviceReadReferences.getRegion().stream()
				.filter(r -> r.getId().equals(contrib.getIdRegion()))
				.collect(java.util.stream.Collectors.toList()));
		} else {
			model.addAttribute("regions", java.util.Collections.emptyList());
		}
		
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("token", tokenManagement.getValidToken());
		return formName;
	}

	/**
	 * Sauvegarde une communauté de communes (création ou modification).
	 * @param comm2co Communauté de communes à sauvegarder
	 * @param bindingResult Résultat de la validation
	 * @param modelMap Map du modèle
	 * @param model Modèle de la vue
	 * @return Nom de la page de formulaire
	 */
	@PostMapping("/edit")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public String save(@ModelAttribute CommunauteCommune comm2co, 
			final BindingResult bindingResult, 
			final ModelMap modelMap, final Model model) {
		Contributeur contrib = (Contributeur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		boolean granted = false;
		if (ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			granted = true;
		} else if (ContributeurRole.ASSOCIATION_MANAGER.equals(contrib.getRole())) {
			// Les gestionnaires d'association peuvent également modifier (à affiner selon les besoins)
			granted = true;
		}
		
		if (granted) {
			comm2co = communauteCommuneService.save(comm2co);
			model.addAttribute("successMessage", "La communauté de communes a été sauvegardée avec succès.");
		} else {
			throw new ExceptionAdmin("EDIT_NOT_ALLOWED");
		}
		
		boolean isAdmin = ContributeurRole.ADMINISTRATOR.equals(contrib.getRole());
		model.addAttribute(formName, comm2co);
		
		// Admin : toutes les régions, non-admin : seulement leur région
		if (isAdmin) {
			model.addAttribute("regions", serviceReadReferences.getRegion());
		} else if (contrib.getIdRegion() != null) {
			model.addAttribute("regions", serviceReadReferences.getRegion().stream()
				.filter(r -> r.getId().equals(contrib.getIdRegion()))
				.collect(java.util.stream.Collectors.toList()));
		} else {
			model.addAttribute("regions", java.util.Collections.emptyList());
		}
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("token", tokenManagement.getValidToken());
		
		return formName;
	}
}
