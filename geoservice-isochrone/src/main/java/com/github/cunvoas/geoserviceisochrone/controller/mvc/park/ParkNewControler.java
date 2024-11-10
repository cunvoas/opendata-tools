package com.github.cunvoas.geoserviceisochrone.controller.mvc.park;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkNew;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusPrefEnum;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkTypeService;

import lombok.extern.slf4j.Slf4j;

/**
 * Controler for prefecture reverse and reintegration.
 * 
 */
@Controller
@RequestMapping("/mvc/park/new")
@Slf4j
public class ParkNewControler {
	
	@Autowired
	private ServiceReadReferences serviceReadReferences;
	
	@Autowired
	private ServiceParcPrefecture serviceParcPrefecture;
	
	@Autowired
	private ParkTypeService parkTypeService;
	
	@Autowired
	private GeoJson2GeometryHelper geoJson2GeometryHelper;
	
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
		Coordinate location = serviceReadReferences.getCoordinate(form.getIdCommune());
		form.setMapLng(String.valueOf(location.getX()));
		form.setMapLat(String.valueOf(location.getY()));
		return getForm(form, model);
	}
	
	protected ParcEtJardin map(final FormParkNew form) {
		ParcEtJardin pj=null;
		if (form.getIdPark()==null) {
			pj = serviceReadReferences.getParcEtJardinById(form.getIdPark());
		} else {
			pj = new ParcEtJardin();
			pj.setSource(ParcSourceEnum.AUTMEL);
		}
		pj.setStatus(ParcStatusEnum.TO_QUALIFY);
		//TODO : ParkType with object

		City commune = serviceReadReferences.getCity(form.getIdCommune());
		pj.setCommune(commune);
		
		
		pj.setName(form.getName());
		pj.setQuartier(form.getQuartier());
		pj.setType(form.getType());
		pj.setSousType(form.getSousType());
		
		
		String sGeom = form.getSGeometry();
		try {
			Geometry geom = null;
			
			if (sGeom!=null) {
				geom = geoJson2GeometryHelper.parseGeoman(sGeom);
			}
			
			if (geom!=null) {
				pj.setCoordonnee(geom.getCentroid());
				pj.setContour(geom);
			}
		} catch (JsonProcessingException e) {
			log.error("geoman parsing error = ", sGeom);
		}	
		
		
		return pj;
	}
	
	;
	@PostMapping("/save")
	public String save(@ModelAttribute FormParkNew form, Model model) {
		log.warn("Generic save: {}", form);
		
		ParcEtJardin pj = this.map(form);
		
		
		
		
		
		// create poly
		if("new".equals(form.getEtatAction())) {
			log.warn("NEW: {}", form);
			this.addPark(form, model);
			
		} else if("add".equals(form.getEtatAction())) {
			log.warn("ADD: {}", form);
			this.addPark(form, model);
			
		} else if("edit".equals(form.getEtatAction())) {
			log.warn("EDIT: {}", form);
			this.updPark(form, model);
			
		// change poly
		} else if("change".equals(form.getEtatAction())) {
			log.warn("CHANGE: {}", form);
			this.updPark(form, model);
		
		// remove poly
		} else if("remove".equals(form.getEtatAction())) {
			log.warn("REMOVE: {}", form);
			this.delPark(form, model);
			
		} else {
			//nothing todo
			log.warn("Nothing to DO: {}", form);
		}
		
		
		return getForm(form, model);
	}
	
	protected void addPark( FormParkNew form, Model model) {
		//TODO
		
		 if("pref".equals(form.getEtat())) {
			 
			 
		 } else if ("p&j".equals(form.getEtat())) {
			 
		 }
		
	}
	
	protected void updPark( FormParkNew form, Model model) {
		//TODO
		
	}
	
	protected void delPark( FormParkNew form, Model model) {
		//TODO
		
		 if("pref".equals(form.getEtat())) {
			 Optional<ParcPrefecture> opt = serviceReadReferences.getParcPrefectureById( form.getId());
			 if (opt.isPresent()) {
				 ParcPrefecture pp = opt.get();
				 // logical delete
				 pp.setStatus(ParcStatusPrefEnum.CANCEL);
				 serviceParcPrefecture.update(pp);
			 }
			 form.setId(null);
			 form.setEtat(null);
			 form.setEtatAction(null);
			 
			 
		 } else if ("p&j".equals(form.getEtat())) {
			 
		 }
		
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
		if (form==null) {
			form = new FormParkNew();	
		}
		if (form.getIdRegion()==null) {
			form.autoLocate();
			
			Coordinate location = serviceReadReferences.getCoordinate(form.getIdCommune());
			form.setMapLng(String.valueOf(location.getX()));
			form.setMapLat(String.valueOf(location.getY()));
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
