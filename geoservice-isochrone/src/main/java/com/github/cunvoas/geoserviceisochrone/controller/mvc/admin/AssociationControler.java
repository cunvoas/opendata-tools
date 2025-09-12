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
import com.github.cunvoas.geoserviceisochrone.model.admin.Association;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.service.admin.AssociationService;


/**
 * Contrôleur de gestion des associations (interface d'administration).
 * Permet d'afficher, d'ajouter, d'éditer et de sauvegarder les associations.
 */
@Controller
@RequestMapping("/mvc/management/asso")
public class AssociationControler {
	
	private String formName = "editAssociation";
	private String listName = "listAssociation";
	
	@Autowired
	private AssociationService associationService;

	/**
	 * Affiche la liste des associations accessibles à l'utilisateur connecté.
	 * @param model Modèle de la vue
	 * @return Nom de la page de liste
	 */
	@GetMapping("/list")
	public String getList(Model model) {
		Contributeur contrib =  (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		model.addAttribute(listName, associationService.findByContextUser(contrib));
		return listName;
	}
	
	/**
	 * Affiche le formulaire d'ajout d'une nouvelle association.
	 * @param model Modèle de la vue
	 * @return Nom de la page de formulaire
	 */
	@GetMapping("/add")
	public String add(Model model) {
		Association association = new Association();
		
		model.addAttribute(formName, association);
		return formName;
	}
	
	
	/**
	 * Affiche le formulaire d'édition d'une association existante.
	 * @param id Identifiant de l'association
	 * @param model Modèle de la vue
	 * @return Nom de la page de formulaire
	 */
	@GetMapping("/edit")
	public String edit(@RequestParam Long id, Model model) {
		Association association = associationService.findById(id);
		
		model.addAttribute(formName, association);
		return formName;
	}

	/**
	 * Sauvegarde une association (création ou modification).
	 * @param asso Association à sauvegarder
	 * @param bindingResult Résultat de la validation
	 * @param modelMap Map du modèle
	 * @param model Modèle de la vue
	 * @return Nom de la page de formulaire
	 */
	@PostMapping("/edit")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public String save(@ModelAttribute Association asso, 
			final BindingResult bindingResult, 
			final ModelMap modelMap, final Model model) {
		Contributeur contrib =  (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Association userAsso = contrib.getAssociation();
		
		
		boolean granted=false;
		if (ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			granted=true;
			
		} else if (ContributeurRole.ASSOCIATION_MANAGER.equals(contrib.getRole())
			&& asso.getId().equals(userAsso.getId())) {
			granted=true;
		}
		
		
		if (granted) {
			asso = associationService.save(asso);
		} else {
			throw new ExceptionAdmin("EDIT_NOT_ALLOWED");
		}
		
		model.addAttribute(formName, asso);
		
		return formName;
	}
}