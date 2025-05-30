package com.github.cunvoas.geoserviceisochrone.controller.mvc.park;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkEdit;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkTypeService;

import lombok.extern.slf4j.Slf4j;


/**
 * Page controler for park.
 */
@Controller
@RequestMapping("/mvc/park")
@Slf4j
@Deprecated
public class ParkEditControler {
	
	private String formName = "editPark";

	@Autowired
	private ServiceReadReferences serviceReadReferences;
	@Autowired
	private ServiceParcPrefecture serviceParcPrefecture;
	
	@Autowired
	private ParkTypeService parkTypeService;

	/**
	 * edit page
	 * @param idRegion region
	 * @param idComm2Co com2co
	 * @param idCommune city
	 * @param idPark park
	 * @param form form
	 * @param model form
	 * @return page name
	 */
	@GetMapping("/edit")
	public String editPark(
			@RequestParam("idRegion") Long idRegion, 
			@RequestParam("idComm2Co")Long idComm2Co, 
			@RequestParam("idCommune") Long idCommune, 
			@RequestParam("idPark") Long idPark,
			@ModelAttribute FormParkEdit form,
			Model model) {
		
		if (form==null ) {
			form = new FormParkEdit();
		}
		form.setIdRegion(idRegion);
		form.setIdCommunauteDeCommunes(idComm2Co);
		form.setIdCommune(idCommune);
		form.setIdPark(idPark);
		
		return populateForm(form, model);
	}
	
	
	/**
	 * save.
	 * @param form form
	 * @param model form
	 * @return page name
	 */
	@PostMapping("/edit")
	public String savePark( @ModelAttribute FormParkEdit form, Model model) {
		
		ParcPrefecture pf = form.getParcPrefecture();
		
		ParcPrefecture parcPref = serviceParcPrefecture.getById( pf.getId() );
		if (parcPref!=null) {
			log.error("Processed: "+ pf.getProcessed());
			parcPref.setProcessed(pf.getProcessed());
			parcPref = serviceParcPrefecture.update(parcPref);
		}
		
		// hotfix
		form.setIdRegion(pf.getIdRegion());
		form.setIdCommunauteDeCommunes(pf.getIdCommunauteDeCommunes());
		form.setIdCommune(pf.getIdCommune());
		form.setIdPark(pf.getIdPark());
		
		return populateForm(form, model);
	}
	
	
	/**
	 * populate form.
	 * @param form form
	 * @param model form
	 * @return page name
	 */
	protected String populateForm( FormParkEdit form, Model model) {
		
		List<Region> regions = serviceReadReferences.getRegion();
		List<CommunauteCommune> com2cos = serviceReadReferences.getCommunauteByRegionId(form.getIdRegion());
		List<City> cities = serviceReadReferences.getCityByCommunauteCommuneId(form.getIdCommunauteDeCommunes());
		
		model.addAttribute("listParkTypes", parkTypeService.findAll());
		

		form.setRegions(regions);
		form.setCommunautesDeCommunes(com2cos);
		form.setCommunes(cities);
		
		
		if (form.getIdCommune()!=null) {
			City city = serviceReadReferences.getCityById(form.getIdCommune());
			form.setNameCommune(city!=null?city.getName():"");
		}

		model.addAttribute(formName, form);
		
		// populate business
		ParcEtJardin petj = serviceReadReferences.getParcEtJardinById(form.getIdPark());
		if (petj!=null) {
			form.setParcEtJardin(petj);
			model.addAttribute("parcLatLng", petj.getLatLng());
			
			ParcPrefecture pPref = serviceReadReferences.getParcPrefectureByParcEtJardinId(form.getIdPark());
			if (pPref!=null) {
				form.setParcPrefecture(pPref);
				model.addAttribute("parcPrefecture", pPref);
				// hotfix
				pPref.setIdRegion(form.getIdRegion());
				pPref.setIdCommunauteDeCommunes(form.getIdCommunauteDeCommunes());
				pPref.setIdCommune(form.getIdCommune());
				pPref.setIdPark(form.getIdPark());
			}
			
		} else {
			// default on Lille
			model.addAttribute("parcLatLng", "50.628040512635025,3.0682105159282456");
		}
		model.addAttribute("ParcEtJardin", form.getParcEtJardin());
		
		form.setParkArea(serviceReadReferences.getByIdParcEtJardin(form.getIdPark()));
		if (form.getParkArea()!=null) {
			model.addAttribute("ParkArea", form.getParkArea());
			
			form.setParkAreaComputed(serviceReadReferences.getParkAreaComputedById(form.getParkArea().getId()));
			model.addAttribute("ParkAreaComputed", form.getParkAreaComputed());
		}
		// populate lists
		model.addAttribute("regions", regions);
		model.addAttribute("communautesDeCommunes", com2cos);
		model.addAttribute("communes", form.getCommunes());
		
		return formName;
	}
	
}
