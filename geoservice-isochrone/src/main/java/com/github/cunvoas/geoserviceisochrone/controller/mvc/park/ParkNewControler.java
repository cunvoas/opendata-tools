package com.github.cunvoas.geoserviceisochrone.controller.mvc.park;

import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkList;
import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkNew;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkTypeService;

/**
 * Controler for prefecture reverse and reintegration.
 * 
 */
@Controller
@RequestMapping("/mvc/park/new")
public class ParkNewControler {
	
	@Autowired
	private ServiceReadReferences serviceReadReferences;
	
	@Autowired
	private ParkTypeService parkTypeService;
	
	private String formName = "newPark";

	@PostMapping("/region")
	public String changeRegion(@ModelAttribute FormParkNew form, Model model) {
		form.setIdCommunauteDeCommunes(null);
		form.setIdCommune(null);
		return getForm(form, model);
	}
	
	@GetMapping("/comm2co")
	public List<CommunauteCommune> getCommunauteCommuneByRegion(@RequestParam("regionId") Long id, @RequestParam("txt") String txt){
		
		List<CommunauteCommune> comm2cos=null;
		if (id==null) {
			comm2cos= serviceReadReferences.getCommunauteCommune();
		} else {
			comm2cos= serviceReadReferences.getCommunauteByRegionId(id);
		}
		if (txt==null || txt.trim().length()==0) {
			return comm2cos;
		} else {
			return comm2cos.stream()
					.filter(comm2co -> comm2co.getName()
                    .toLowerCase()
                    .contains(txt.toLowerCase()))
					.limit(15)
					.collect(Collectors.toList());
		}
	}
	
	@PostMapping("/commDeCo")
	public String changeCommunauteDeCommune(@ModelAttribute FormParkNew form, Model model) {
		form.setIdCommune(null);
		return getForm(form, model);
	}
	
	@PostMapping("/city")
	public String changeCity(@ModelAttribute FormParkNew form, Model model) {
		return getForm(form, model);
	}
	
	
	/**
	 * First call a.
	 * @param form
	 * @param model
	 * @return
	 */
	@GetMapping
	public String getForm(@ModelAttribute FormParkNew form, Model model) {

		form = populateForm(form);
		model.addAttribute(formName, form);
		model.addAttribute("regions", form.getRegions());
		model.addAttribute("communautesDeCommunes", form.getCommunautesDeCommunes());
		model.addAttribute("communes", form.getCommunes());
		model.addAttribute("parkTypes", parkTypeService.findAll());
		model.addAttribute("parkSources", serviceReadReferences.getParcSource());
		
		return formName;
	}
	

	/**
	 *  form data filler.
	 * @param form
	 * @return
	 */
	protected FormParkNew populateForm( FormParkNew form) {
		
		if (form==null || form.getIdRegion()==null) {
			form = new FormParkNew();
			form.autoLocate();
		}
		
		// Populate Selection List
		form.setRegions(serviceReadReferences.getRegion());
		form.setCommunautesDeCommunes(serviceReadReferences.getCommunauteByRegionId(form.getIdRegion()));
		
		// if only one preselect it
		if (form.getCommunautesDeCommunes()!=null && form.getCommunautesDeCommunes().size()==1) {
			form.setIdCommunauteDeCommunes(form.getCommunautesDeCommunes().get(0).getId());
		}
		
		if (form.getIdRegion()!=null) {
			if (form.getIdCommunauteDeCommunes()!=null) {
				form.setCommunes(serviceReadReferences.getCityByCommunauteCommuneId(form.getIdCommunauteDeCommunes()));
			} else {
				form.setCommunes(serviceReadReferences.getCityByRegionId(form.getIdRegion()));
			}
		}
		
		if (form.getIdCommune()!=null) {
			City city = serviceReadReferences.getCityById(form.getIdCommune());
			form.setNameCommune(city!=null?city.getName():"");
			form.setCommune(city);
		}
		
		
		
		return form;
	}
		

	

}
