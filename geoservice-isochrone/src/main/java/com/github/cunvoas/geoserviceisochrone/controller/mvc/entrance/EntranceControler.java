package com.github.cunvoas.geoserviceisochrone.controller.mvc.entrance;

import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkEntrance;
import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkEntranceDetail;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapService;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkService;


@Controller
@RequestMapping("/mvc/entrance")
public class EntranceControler {
	
	@Autowired
	private ServiceReadReferences serviceReadReferences;
	@Autowired
	private ServiceOpenData serviceOpenData;

	@Autowired
	private ParkService parkService;
	
    
	
	private String formName = "editEntrance";

	
	
	@GetMapping
	public String getForm(@ModelAttribute FormParkEntrance form, Model model) {

		form = populateForm(form);
		this.populateModel(model, form);
		return formName;
	}
	
	@GetMapping("/goto")
	public String gotoEntrance(
			@RequestParam("idRegion") Long idRegion, 
			@RequestParam("idComm2Co")Long idComm2Co, 
			@RequestParam("idCommune") Long idCommune, 
			@RequestParam("idPark") Long idPark,
			@ModelAttribute FormParkEntrance form,
			Model model) {
		
		if (form==null ) {
			form = new FormParkEntrance();
		}
		form.setIdRegion(idRegion);
		form.setIdCommunauteDeCommunes(idComm2Co);
		form.setIdCommune(idCommune);
		form.setIdPark(idPark);
		
		return getForm(form, model);
	}

	protected void populateModel(Model model, FormParkEntrance form) {
		model.addAttribute(formName, form);
		model.addAttribute("regions", form.getRegions());
		model.addAttribute("communautesDeCommunes", form.getCommunautesDeCommunes());
		model.addAttribute("communes", form.getCommunes());
		model.addAttribute("parks", form.getParks());
		model.addAttribute("parkEntrances", form.getParkEntrances());
		model.addAttribute("parkEntranceDetail", form.getParkEntranceDetail());
	}
	
	protected FormParkEntrance populateForm( FormParkEntrance form) {
		if (form==null || form.getIdRegion()==null) {
			form = new FormParkEntrance();
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
			form.setParks(serviceReadReferences.getParcEtJardinByCityId(form.getIdCommune()));
			City city = serviceReadReferences.getCityById(form.getIdCommune());
			form.setNameCommune(city!=null?city.getName():"");
			if (form.getIdPark()==null && city.getCoordinate()!=null) {
				Point point = city.getCoordinate();
				form.setMapLng(String.valueOf(point.getCoordinates()[0].x));
				form.setMapLat(String.valueOf(point.getCoordinates()[0].y));
			}
		}
		
		if (form.getIdPark()!=null) {
			form.setNamePark("");
			form.setParkEntrances(serviceReadReferences.getEntranceByParkId(form.getIdPark()));
			form.setParkArea(parkService.getParkAreaByIdParcEtJardin(form.getIdPark()));
			
			ParcEtJardin parcEtJardin = serviceReadReferences.getParcEtJardinById(form.getIdPark());
			form.setParcEtJardin(parcEtJardin);

			if (parcEtJardin!=null) {
				form.setNamePark(parcEtJardin.getName());
				if (parcEtJardin.getCoordonnee()!=null) {
					Point point = parcEtJardin.getCoordonnee();
					form.setMapLng(String.valueOf(point.getCoordinates()[0].x));
					form.setMapLat(String.valueOf(point.getCoordinates()[0].y));
				}
			}
			
			// populate in case on goto from listPark
			form.setParkEntranceDetail(new FormParkEntranceDetail(form));
		}
		
		if (form.getIdEntrance()!=null) {
			ParkEntrance parkEntrance = serviceReadReferences.getEntranceById(form.getIdEntrance());
			form.setParkEntrance(parkEntrance);
			form.setParkEntranceDetail(new FormParkEntranceDetail(parkEntrance, form));
		} else {
			if (CollectionUtils.isEmpty(form.getParkEntrances())) {
				// empty entry
				form.setParkEntranceDetail(new FormParkEntranceDetail(form));
			} else {
				// preset on 1st entry
				ParkEntrance first = form.getParkEntrances().get(0);
				form.setParkEntranceDetail(new FormParkEntranceDetail(first, form));
			}
		}
		
		return form;
	}
	

	//SELECT Management
	@GetMapping("/region")
	public List<Region> getRegion(){
		return serviceReadReferences.getRegion();
	}
	@PostMapping("/region")
	public String changeRegion(@ModelAttribute FormParkEntrance form, Model model) {
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
	public String changeCommunauteDeCommune(@ModelAttribute FormParkEntrance form, Model model) {
		form.setIdCommune(null);
		form.setIdPark(null);
		form.setParcEtJardin(null);
		form.setParkArea(null);
		form.setIdEntrance(null);
		return getForm(form, model);
	}
	
	@PostMapping("/city")
	public String changeCity(@ModelAttribute FormParkEntrance form, Model model) {
		form.setIdPark(null);
		form.setParcEtJardin(null);
		form.setParkArea(null);
		form.setIdEntrance(null);
		return getForm(form, model);
	}
	
	@PostMapping("/park")
	public String changePark(@ModelAttribute FormParkEntrance form, Model model) {
		form.setIdEntrance(null);
		
		
		return getForm(form, model);
	}

	@PostMapping("/entrance")
	public String changeEntrance(@ModelAttribute FormParkEntrance form, Model model) {
		return getForm(form, model);
	}
	
	@PostMapping("/mergeIsochrone")
	public String mergeIsochrone(@ModelAttribute FormParkEntranceDetail formDetail, Model model) {
		// rebuild parent form context
		FormParkEntrance form=new FormParkEntrance(formDetail);
		
		ParkArea pa = serviceReadReferences.getParkAreaById(form.getAreaId());

		parkService.mergeParkAreaEntrance(pa);
		
		return getForm(form, model);
	}
	
	
	@PostMapping("/editEntrance")
	public String saveEntrance(@ModelAttribute FormParkEntranceDetail formDetail, Model model) {
		// rebuild parent form context
		FormParkEntrance form=new FormParkEntrance(formDetail);
		
		ParkEntrance parkEntrance=null;
		// update case
		if (formDetail.getEntranceId()!=null  || "upd".equals(formDetail.getMode())) {
			parkEntrance = serviceReadReferences.getEntranceById(formDetail.getEntranceId());
		
		} else {  // new insert case
			parkEntrance = new ParkEntrance();
			ParkArea pa = null;
			if (formDetail.getAreaId()!=null) {
				pa = serviceReadReferences.getParkAreaById(formDetail.getAreaId());
				parkEntrance.setParkArea(pa);
			}
		}
		parkEntrance.setDescription(formDetail.getDescription());
		parkEntrance.setEntranceLink(formDetail.getEntranceLink());
		
		boolean withIgn=false;
		Point newPoint = GeoShapeHelper.getPoint(formDetail.getEntranceLng(), formDetail.getEntranceLat());
		if ( parkEntrance.getEntrancePoint()!=null ) {
			if (!parkEntrance.getEntrancePoint().equals(newPoint)) {
				parkEntrance.setEntrancePoint(newPoint);
				withIgn=true;
			}
		} else {
			parkEntrance.setEntrancePoint(newPoint);
			withIgn=true;
		}
		
//		City city = serviceReadReferences.getCityById(form.getIdCommune());
//		String distance = serviceOpenData.getDistanceDense(city);
//		parkService.saveEdited(parkEntrance, distance, withIgn);
		
		parkService.saveEdited(parkEntrance, withIgn, formDetail.getIdPark(), formDetail.getIdCommune());
		return getForm(form, model);
	}
	
	@GetMapping("/city")
	public List<City> getCityByCommunauteCommune(@RequestParam("comm2coId") Long id, @RequestParam("txt") String txt){
		List<City> cities = serviceReadReferences.getCityByCommunauteCommuneId(id);
		
		if (txt==null || txt.trim().length()==0) {
			return cities;
		} else {
			
			return cities.stream()
					.filter(city -> city.getDisplay().toLowerCase()
                    .contains(txt.toLowerCase()))
					.limit(15).collect(Collectors.toList());
		}
	}
	
}
