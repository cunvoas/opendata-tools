package com.github.cunvoas.geoserviceisochrone.controller.mvc.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.form.FormComputeProgress;
import com.github.cunvoas.geoserviceisochrone.model.admin.Association;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobProgressStat;
import com.github.cunvoas.geoserviceisochrone.service.compute.BatchJobService;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;

import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur de gestion des tâches de calcul (ComputeJob).
 * Permet d'afficher la page de gestion des jobs de calcul.
 */
@Controller
@RequestMapping("/mvc/management/jobs")
@Slf4j
public class ComputeJobControler {

	private static final String VIEW_PROGRESS = "computeProgress";
	private static final String FORM_KEY = "progressForm";

	private String formName = "manageJobs";

	private final BatchJobService batchJobService;
	private final ServiceReadReferences serviceReadReferences;
	private final ApplicationBusinessProperties applicationBusinessProperties;

	@Autowired
	public ComputeJobControler(BatchJobService batchJobService,
	                           ServiceReadReferences serviceReadReferences,
	                           ApplicationBusinessProperties applicationBusinessProperties) {
		this.batchJobService = batchJobService;
		this.serviceReadReferences = serviceReadReferences;
		this.applicationBusinessProperties = applicationBusinessProperties;
	}

	// -------------------------------------------------------------------------
	// GET : affichage initial de la page de progression
	// -------------------------------------------------------------------------

	@GetMapping("/progress")
	@PreAuthorize("hasAuthority('ADMINISTRATOR')")
	public String getProgress(@ModelAttribute(FORM_KEY) FormComputeProgress form, Model model) {
		populate(form);
		model.addAttribute(FORM_KEY, form);
		return VIEW_PROGRESS;
	}

	// -------------------------------------------------------------------------
	// POST : changement de région
	// -------------------------------------------------------------------------

	@PostMapping("/progress/region")
	@PreAuthorize("hasAuthority('ADMINISTRATOR')")
	public String changeRegion(@ModelAttribute(FORM_KEY) FormComputeProgress form, Model model) {
		form.setIdCommunauteDeCommunes(null);
		form.setIdCommune(null);
		return getProgress(form, model);
	}

	// -------------------------------------------------------------------------
	// POST : changement d'EPCI
	// -------------------------------------------------------------------------

	@PostMapping("/progress/epci")
	@PreAuthorize("hasAuthority('ADMINISTRATOR')")
	public String changeEpci(@ModelAttribute(FORM_KEY) FormComputeProgress form, Model model) {
		form.setIdCommune(null);
		return getProgress(form, model);
	}

	// -------------------------------------------------------------------------
	// POST : changement de ville
	// -------------------------------------------------------------------------

	@PostMapping("/progress/city")
	@PreAuthorize("hasAuthority('ADMINISTRATOR')")
	public String changeCity(@ModelAttribute(FORM_KEY) FormComputeProgress form, Model model) {
		return getProgress(form, model);
	}

	// -------------------------------------------------------------------------
	// POST : changement d'année
	// -------------------------------------------------------------------------

	@PostMapping("/progress/year")
	@PreAuthorize("hasAuthority('ADMINISTRATOR')")
	public String changeYear(@ModelAttribute(FORM_KEY) FormComputeProgress form, Model model) {
		return getProgress(form, model);
	}

	// -------------------------------------------------------------------------
	// Méthode privée de peuplement du modèle
	// -------------------------------------------------------------------------

	private void populate(FormComputeProgress form) {
		// Auto-preset par contexte utilisateur si aucune région n'est encore sélectionnée
		if (form.getIdRegion() == null) {
			form.autoLocate();
			form.setIdCommune(null);
		}

		log.debug("populate() - idRegion={}, idEpci={}, idCommune={}, annee={}",
			form.getIdRegion(), form.getIdCommunauteDeCommunes(), form.getIdCommune(), form.getAnnee());

		// listes (régions toujours chargées)
		form.setRegions(serviceReadReferences.getRegion());

		if (form.getIdRegion() != null) {
			form.setCommunautesDeCommunes(serviceReadReferences.getCommunauteByRegionId(form.getIdRegion()));

			if (form.getIdCommunauteDeCommunes() != null) {
				form.setCommunes(serviceReadReferences.getCityByCommunauteCommuneId(form.getIdCommunauteDeCommunes()));
			} else {
				form.setCommunes(new ArrayList<>());
			}
		} else {
			form.setCommunautesDeCommunes(new ArrayList<>());
			form.setCommunes(new ArrayList<>());
		}

		// années disponibles
		form.setAnnees(List.of(applicationBusinessProperties.getInseeAnnees()));

		// calcul des stats uniquement si un EPCI est sélectionné
		List<ComputeJobProgressStat> stats;
		if (form.getIdCommunauteDeCommunes() == null) {
			// pas d'EPCI → aucun résultat affiché
			stats = new ArrayList<>();
		} else if (form.getIdCommune() != null) {
			// ville sélectionnée → détail par ville
			stats = batchJobService.getProgressStatsCityLevel(
				form.getIdCommune(), null, null, form.getAnnee());
		} else {
			// EPCI sélectionné sans ville → agrégation EPCI (1 ligne par EPCI+année)
			stats = batchJobService.getProgressStatsEpciLevel(
				form.getIdCommunauteDeCommunes(), null, form.getAnnee());
		}
		form.setStats(stats);
	}

	// -------------------------------------------------------------------------
	// Page de gestion des jobs (existante)
	// -------------------------------------------------------------------------

	/**
	 * Affiche la page de gestion des jobs de calcul pour l'utilisateur connecté.
	 */
	@GetMapping
	@PreAuthorize("hasAuthority('ADMINISTRATOR') OR hasAuthority('ASSO_MANAGER')")
	public String getMyForm(Model model) {
		List<Association> assos = new ArrayList<>(1);
		model.addAttribute("assos", assos);
		return formName;
	}
}