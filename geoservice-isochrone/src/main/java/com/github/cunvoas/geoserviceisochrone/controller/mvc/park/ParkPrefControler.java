package com.github.cunvoas.geoserviceisochrone.controller.mvc.park;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkList;
import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkListItem;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkTypeService;

/**
 * Controler for prefecture reverse and reintegration.
 * 
 */
@Controller
@RequestMapping("/mvc/pref")
public class ParkPrefControler {
	
	@Autowired
	private ServiceReadReferences serviceReadReferences;
	
	@Autowired
	private ParkTypeService parkTypeService;
	

	
	private String formName = "listParkPref";

	

	@PostMapping("/region")
	public String changeRegion(@ModelAttribute FormParkList form, Model model) {
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
	public String changeCommunauteDeCommune(@ModelAttribute FormParkList form, Model model) {
		form.setIdCommune(null);
		return getForm(form, model);
	}
	
	@PostMapping("/city")
	public String changeCity(@ModelAttribute FormParkList form, Model model) {
		return getForm(form, model);
	}
	
	
	@GetMapping
	public String getForm(@ModelAttribute FormParkList form, Model model) {
		
		form = populateForm(form);
		model.addAttribute("formParkList", form);
		model.addAttribute("regions", form.getRegions());
		model.addAttribute("communautesDeCommunes", form.getCommunautesDeCommunes());
		model.addAttribute("communes", form.getCommunes());
		
		if (form.getIdCommune()!=null) {
			Pageable p = PageRequest.of(form.getPage()-1, form.getSize());
			Page<FormParkListItem> page = populateTableList(form.getIdCommune(), p);
			model.addAttribute("formParkListItems", page);
		}
		
		return formName;
	}
	

	protected FormParkList populateForm( FormParkList form) {
		
		if (form==null || form.getIdRegion()==null) {
			form = new FormParkList();
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
		}
		
		
		form.setListTypePark(parkTypeService.findAll());
		return form;
	}
		

	private Page<FormParkListItem> populateTableList(Long idCity, Pageable page) {
		City city = serviceReadReferences.getCityById(idCity);
		
		Page<ParcEtJardin> pgPj = serviceReadReferences.getParcEtJardinByCityId(idCity, page);
		
		List<FormParkListItem> lstItems = new ArrayList<>();
		for (ParcEtJardin pj : pgPj) {
			FormParkListItem item = new FormParkListItem();
			if (city.getRegion()!=null) {
				item.setIdRegion(city.getRegion().getId());
				item.setNameRegion(city.getRegion().getName());
			}
			if (city.getCommunauteCommune()!=null) {
				item.setIdCommunauteDeCommunes(city.getCommunauteCommune().getId());
				item.setNameCommunauteDeCommunes(city.getCommunauteCommune().getName());
			}
			item.setIdCommune(idCity);
			item.setNameCommune(city.getName());
			item.setIdPark(pj.getId());
			item.setNameQuartier(pj.getQuartier());
			item.setNamePark(pj.getName());
			
			ParkArea pa = serviceReadReferences.getByIdParcEtJardin(pj.getId());
			if (pa!=null) {
				
				item.setIdArea(pa.getId());
				item.setLastIsochroneUpdate(pa.getUpdated());
				
				if(!CollectionUtils.isEmpty(pa.getEntrances())) {
					Date maxDate=null;
					for (ParkEntrance entr : pa.getEntrances()) {
						if (maxDate==null && entr.getUpdateDate()!=null) {
							maxDate = entr.getUpdateDate();
						} else if (maxDate.compareTo(entr.getUpdateDate())<0) {
							maxDate = entr.getUpdateDate();
						}
					}
					item.setLastEntranceUpdate(maxDate);
				}
			}
			
			lstItems.add(item);
		}
		Page<FormParkListItem> pgItems  = new PageImpl<>(lstItems, pgPj.getPageable(), pgPj.getNumberOfElements());
		return pgItems;
		
	}
	

}
