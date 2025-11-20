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

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
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

	/**
	 * Affiche la liste des communautés de communes accessibles à l'utilisateur connecté.
	 * @param model Modèle de la vue
	 * @return Nom de la page de liste
	 */
	@GetMapping("/list")
	public String getList(Model model) {
		Contributeur contrib = (Contributeur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		model.addAttribute(listName, communauteCommuneService.findByContextUser(contrib));
		return listName;
	}
	
	/**
	 * Affiche le formulaire d'ajout d'une nouvelle communauté de communes.
	 * @param model Modèle de la vue
	 * @return Nom de la page de formulaire
	 */
	@GetMapping("/add")
	public String add(Model model) {
		CommunauteCommune communauteCommune = new CommunauteCommune();
		
		model.addAttribute(formName, communauteCommune);
		model.addAttribute("regions", serviceReadReferences.getRegion());
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
		CommunauteCommune communauteCommune = communauteCommuneService.findById(id);
		
		model.addAttribute(formName, communauteCommune);
		model.addAttribute("regions", serviceReadReferences.getRegion());
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
		} else {
			throw new ExceptionAdmin("EDIT_NOT_ALLOWED");
		}
		
		model.addAttribute(formName, comm2co);
		model.addAttribute("regions", serviceReadReferences.getRegion());
		
		return formName;
	}
}
