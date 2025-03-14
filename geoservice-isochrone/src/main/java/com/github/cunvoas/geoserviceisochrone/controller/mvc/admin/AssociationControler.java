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
 * Page controler for association.
 */
@Controller
@RequestMapping("/mvc/management/asso")
public class AssociationControler {
	
	private String formName = "editAssociation";
	private String listName = "listAssociation";
	
	@Autowired
	private AssociationService associationService;

	/**
	 * call list page.
	 * @param model form model
	 * @return page name
	 */
	@GetMapping("/list")
	public String getList(Model model) {
		Contributeur contrib =  (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		model.addAttribute(listName, associationService.findByContextUser(contrib));
		return listName;
	}
	
	/**
	 * @param model form model
	 * @return page name
	 */
	@GetMapping("/add")
	public String add(Model model) {
		Association association = new Association();
		
		model.addAttribute(formName, association);
		return formName;
	}
	
	
	/**
	 * call edit page.
	 * @param id of asso
	 * @param model form model
	 * @return page name
	 */
	@GetMapping("/edit")
	public String edit(@RequestParam Long id, Model model) {
		Association association = associationService.findById(id);
		
		model.addAttribute(formName, association);
		return formName;
	}

	/**
	 * save methode.
	 * @param asso BO
	 * @param bindingResult html bind
	 * @param modelMap map
	 * @param model form
	 * @return page name
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
