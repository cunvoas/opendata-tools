package com.github.cunvoas.geoserviceisochrone.controller.mvc.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.Association;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.RegionRepository;
import com.github.cunvoas.geoserviceisochrone.service.admin.BatchJobService;

/**
 * Page controler for ComputeJob.
 */
@Controller
@RequestMapping("/mvc/management/jobs")
public class ComputeJobControler {

	private String formName = "manageJobs";
	
	@Autowired
	private BatchJobService batchJobService;
	
	@Autowired
	private RegionRepository regionRepository;
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private CityRepository cityRepository;
	

	/**
	 * edit my profil.
	 * @param model form
	 * @return page name
	 */
	@GetMapping
	@PreAuthorize("hasRole('ADMINISTRATOR') OR hasRole('ASSO_MANAGER')")
	public String getMyForm(Model model) {
		
		Contributeur contribConnected =  (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
//		Contributeur contrib = contributeurService.get(contribConnected.getId());
//		model.addAttribute(formName, cloneToForm(contrib, model));
		
		List<Association> assos = new ArrayList<>(1);
		//assos.add(contrib.getAssociation());
		model.addAttribute( "assos", assos );
		
		return formName;
	}
	
	
	/**
	 * populate model & form.
	 * @param model page model
	 * @param form page form
	 * @return form
	 */
	private FormComputeJob populateForm(Model model, FormComputeJob form) {
		if (form==null) {
			form = new FormComputeJob();
		}
		
		if (!form.hasStats()) {
			form.setStats(batchJobService.getGlobalStats());
		}
		
		return form;
	}
}
