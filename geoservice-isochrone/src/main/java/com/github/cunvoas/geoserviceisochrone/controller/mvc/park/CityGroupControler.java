package com.github.cunvoas.geoserviceisochrone.controller.mvc.park;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.cunvoas.geoserviceisochrone.model.admin.Association;


/**
 * Page controler com2co.
 */
@Controller
@RequestMapping("/mvc/cityGroup")
public class CityGroupControler {
	
	private String formName = "commingSoon";


	/**
	 * get page with population.
	 * @param model
	 * @return page populated
	 */
	@GetMapping
	public String getForm(Model model) {
		model.addAttribute(formName, new Association());
		return formName;
	}

	/**
	 * save.
	 * @param asso BO
	 * @param model form
	 * @return page populated
	 */
	@PostMapping
	public String save(@ModelAttribute Association asso, Model model) {
		model.addAttribute("formName", asso);
		
		
		return formName;
	}
}
