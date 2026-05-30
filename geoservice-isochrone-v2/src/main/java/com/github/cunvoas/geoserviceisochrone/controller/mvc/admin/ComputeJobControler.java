package com.github.cunvoas.geoserviceisochrone.controller.mvc.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.cunvoas.geoserviceisochrone.model.admin.Association;
import com.github.cunvoas.geoserviceisochrone.service.compute.BatchJobService;

/**
 * Contrôleur de gestion des tâches de calcul (ComputeJob).
 * Permet d'afficher la page de gestion des jobs de calcul.
 */
@Controller
@RequestMapping("/mvc/management/jobs")
public class ComputeJobControler {

	private String formName = "manageJobs";
	
	private final BatchJobService batchJobService;

	@Autowired
	public ComputeJobControler(BatchJobService batchJobService) {
		this.batchJobService = batchJobService;
	}
	

	/**
	 * Affiche la page de progression des jobs de calcul.
	 * @param model Modèle de la vue
	 * @return Nom de la page de progression
	 */
	@GetMapping("/progress")
	@PreAuthorize("hasAuthority('ADMINISTRATOR')")
	public String getProgress(Model model) {
		model.addAttribute("stats", batchJobService.getGroupedProgressStats());
		return "computeProgress";
	}


	/**
	 * Affiche la page de gestion des jobs de calcul pour l'utilisateur connecté.
	 */
	@GetMapping
	@PreAuthorize("hasAuthority('ADMINISTRATOR') OR hasAuthority('ASSO_MANAGER')")
	public String getMyForm(Model model) {
		List<Association> assos = new ArrayList<>(1);
		model.addAttribute( "assos", assos );
		return formName;
	}
}