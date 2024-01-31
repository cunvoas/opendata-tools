package com.github.cunvoas.geoserviceisochrone.controller.mvc.park;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkEdit;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;


@Controller
@RequestMapping("/mvc/park")
public class ParkEditControler {
	
	private String formName = "editPark";

	@Autowired
	private ServiceReadReferences serviceReadReferences;
	
	@GetMapping("/edit")
	public String gotoEntrance(
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
		form.setIdComm2Co(idComm2Co);
		form.setIdCommune(idCommune);
		form.setIdPark(idPark);
		
		return populateForm(form, model);
	}
	
	@PostMapping("/edit")
	public String gotoEntrance(
			@ModelAttribute FormParkEdit form,
			Model model) {
		
		// TODO
		
		
		return populateForm(form, model);
	}
	
	

	
	protected String populateForm( FormParkEdit form, Model model) {

		if (form.getIdCommune()!=null) {
			City city = serviceReadReferences.getCityById(form.getIdCommune());
			form.setNameCommune(city!=null?city.getName():"");
		}
		
		// populate business
		form.setParcEtJardin(serviceReadReferences.getParcEtJardinById(form.getIdPark()));
		model.addAttribute("ParcEtJardin", form.getParcEtJardin());
		form.setParkArea(serviceReadReferences.getParkAreaById(form.getIdPark()));
		model.addAttribute("ParkArea", form.getParkArea());
		form.setParkAreaComputed(serviceReadReferences.getParkAreaComputedById(form.getIdPark()));
		model.addAttribute("ParkAreaComputed", form.getParkAreaComputed());
		form.setParkEntrances(serviceReadReferences.getEntranceByParkId(form.getIdPark()));
		model.addAttribute("ParkEntrances", form.getParkEntrances());
		
		// check if update required
		if (form.getParkEntrances()!=null && !form.getParkEntrances().isEmpty()) {
			Date upd = form.getParkArea().getUpdated();
			for (ParkEntrance pe : form.getParkEntrances()) {
				if (upd.before(pe.getUpdateDate())) {
					form.setComputeNeeded(Boolean.TRUE);
					break;
				}
			}
		}
		
		
		return formName;
	}
	
}
