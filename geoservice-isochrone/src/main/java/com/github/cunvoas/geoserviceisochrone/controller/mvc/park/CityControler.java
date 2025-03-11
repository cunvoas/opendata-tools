package com.github.cunvoas.geoserviceisochrone.controller.mvc.park;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.cunvoas.geoserviceisochrone.model.admin.Association;


/**
 * Page controler for city.
 */
@Controller
@RequestMapping("/mvc/city")
public class CityControler {
	
	private String formName = "commingSoon";

	 
	@GetMapping
	public String getForm(Model model) {
		model.addAttribute(formName, new Association());
		return formName;
	}

	@PostMapping
	public String save(@ModelAttribute Association asso, Model model) {
		model.addAttribute("formName", asso);
		
		
		return formName;
	}
}
