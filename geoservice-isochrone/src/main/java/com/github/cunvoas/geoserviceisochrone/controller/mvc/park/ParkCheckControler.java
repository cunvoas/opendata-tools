package com.github.cunvoas.geoserviceisochrone.controller.mvc.park;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
 * Page controler for parck check.
 */
@Controller
@RequestMapping("/mvc/park")
@Slf4j
public class ParkCheckControler {
	
	private String formName = "checkPark";

	@Autowired
	private ServiceReadReferences serviceReadReferences;
	@Autowired
	private ServiceParcPrefecture serviceParcPrefecture;
	
	@Autowired
	private ParkTypeService parkTypeService;

	@GetMapping("/check")
	public String checkPark(
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
