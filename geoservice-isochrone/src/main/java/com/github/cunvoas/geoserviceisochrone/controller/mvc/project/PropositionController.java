package com.github.cunvoas.geoserviceisochrone.controller.mvc.project;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bedatadriven.jackson.datatype.jts.serialization.GeometrySerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.cunvoas.geoserviceisochrone.controller.form.FormProposition;
import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.TokenManagement;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller to manage ProjectSimulator domain object with UI views and REST endpoints.
 */
@Controller
@RequestMapping("/mvc/proposal")
@Slf4j
public class PropositionController {

    private static final String VIEW = "proposal";
    private static final String FORM_KEY = "formProposal";

    @Autowired
    private ServiceReadReferences serviceReadReferences;

    @Autowired
    private GeoJson2GeometryHelper geoJson2GeometryHelper;
    
    @Autowired
    private TokenManagement tokenManagement;

    private ObjectMapper geometryWriter;

    public PropositionController() {
        geometryWriter = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Geometry.class, new GeometrySerializer());
        geometryWriter.registerModule(module);
    }

    /**
     * Display the simulator view.
     * @param form form object
     * @param model Spring model
     * @return view name
     */
    @GetMapping
    public String show(@ModelAttribute FormProposition form, Model model) {
        log.debug("show() - form={}", form);
        populate(form);
        
        model.addAttribute(FORM_KEY, form);
        model.addAttribute("regions", form.getRegions());
        model.addAttribute("communautesDeCommunes", form.getCommunautesDeCommunes());
        model.addAttribute("communes", form.getCommunes());
        model.addAttribute("token", tokenManagement.getValidToken());
        
        return VIEW;
    }	
    
    /**
     * Change region selection.
     */
    @PostMapping("/region")
    public String changeRegion(@ModelAttribute FormProposition form, Model model) {
        form.setIdCommunauteDeCommunes(null);
        form.setIdCommune(null);
        model.addAttribute("token", tokenManagement.getValidToken());
        return show(form, model);
    }
    
    /**
     * Change EPCI selection.
     */
    @PostMapping("/commDeCo")
    public String changeEpci(@ModelAttribute FormProposition form, Model model) {
        form.setIdCommune(null);
        model.addAttribute("token", tokenManagement.getValidToken());
        return show(form, model);
    }
    
    /**
     * Change city selection.
     */
    @PostMapping("/city")
    public String changeCity(@ModelAttribute FormProposition form, Model model) {
        if (form.getIdCommune() != null) {
            Coordinate c = serviceReadReferences.getCoordinate(form.getIdCommune());
            if (c != null) {
                form.setMapLng(String.valueOf(c.getX()));
                form.setMapLat(String.valueOf(c.getY()));
            }

        }
        model.addAttribute("token", tokenManagement.getValidToken());
        return show(form, model);
    }
    


    
    /**
     * Populate form with reference data.
     */
    private void populate(FormProposition form) {
    	if (form==null) {
    		return;
    	}
        log.debug("populate() - idRegion={}, idCommunauteDeCommunes={}, idCommune={}",
            form.getIdRegion(), form.getIdCommunauteDeCommunes(), form.getIdCommune());
        
        // Auto preset par contexte utilisateur si aucune région n'est encore sélectionnée
        if (form.getIdRegion() == null) {
            form.autoLocate();
            if (form.getIdCommune() != null) {
            	
            	// on a déjà localisé avec le projet
            	if (form.getMapLng()==null && form.getMapLng()==null) {
	                Coordinate location = serviceReadReferences.getCoordinate(form.getIdCommune());
	                if (location != null) {
	                    form.setMapLng(String.valueOf(location.getX()));
	                    form.setMapLat(String.valueOf(location.getY()));
	                }
            	}

            }
        }
        
        // Load regions
        form.setRegions(serviceReadReferences.getRegion());
        
        if (form.getIdRegion() != null) {
            form.setCommunautesDeCommunes(serviceReadReferences.getCommunauteByRegionId(form.getIdRegion()));
            
            // if only one preselect it
            if (form.getCommunautesDeCommunes() != null && form.getCommunautesDeCommunes().size() == 1 && form.getIdCommunauteDeCommunes() == null) {
                form.setIdCommunauteDeCommunes(form.getCommunautesDeCommunes().get(0).getId());
            }
            
            if (form.getIdCommunauteDeCommunes() != null) {
                form.setCommunes(serviceReadReferences.getCityByCommunauteCommuneId(form.getIdCommunauteDeCommunes()));
            } else {
                form.setCommunes(serviceReadReferences.getCityByRegionId(form.getIdRegion()));
            }
        } else {
            form.setCommunautesDeCommunes(new java.util.ArrayList<>());
            form.setCommunes(new java.util.ArrayList<>());
        }
        
        // Set city name if city is selected
        if (form.getIdCommune() != null) {
            City city = serviceReadReferences.getCityById(form.getIdCommune());
            form.setNameCommune(city != null ? city.getName() : "");
        }
    }


}
