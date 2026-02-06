package com.github.cunvoas.geoserviceisochrone.controller.mvc.project;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bedatadriven.jackson.datatype.jts.serialization.GeometrySerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.cunvoas.geoserviceisochrone.controller.form.FormProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.TokenManagement;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorWork;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.project.ProjectSimulatorService;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller to manage ProjectSimulator domain object with UI views and REST endpoints.
 */
@Controller
@RequestMapping("/mvc/simulation")
@Slf4j
public class ProjectSimulatorController {

    private static final String VIEW = "simulateur";
    private static final String FORM_KEY = "formSimulateur";

    @Autowired
    private ProjectSimulatorService projectSimulatorService;
    
    @Autowired
    private ServiceReadReferences serviceReadReferences;

    @Autowired
    private GeoJson2GeometryHelper geoJson2GeometryHelper;
    
    @Autowired
    private TokenManagement tokenManagement;

    private ObjectMapper geometryWriter;

    public ProjectSimulatorController() {
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
    public String show(@ModelAttribute FormProjectSimulator form, Model model) {
        log.debug("show() - form={}", form);
        populate(form);
        
        List<ProjectSimulator> projects = form.getIdCommune() != null ? projectSimulatorService.findByCity(form.getIdCommune()) : List.of();
        log.info("show() - idCommune={}, projects count={}", form.getIdCommune(), projects.size());
        
        model.addAttribute(FORM_KEY, form);
        model.addAttribute("regions", form.getRegions());
        model.addAttribute("communautesDeCommunes", form.getCommunautesDeCommunes());
        model.addAttribute("communes", form.getCommunes());
        model.addAttribute("projects", projects);
        model.addAttribute("token", tokenManagement.getValidToken());
        
        if (form.getId() != null) {
        	List<ProjectSimulatorWork> works = projectSimulatorService.getProjectWorks(form.getId());
        	model.addAttribute("projectWorks", works.stream().distinct().toList());
		}
        return VIEW;
    }
    
    /**
     * Change region selection.
     */
    @PostMapping("/region")
    public String changeRegion(@ModelAttribute FormProjectSimulator form, Model model) {
        form.setIdCommunauteDeCommunes(null);
        form.setIdCommune(null);
        model.addAttribute("token", tokenManagement.getValidToken());
        return show(form, model);
    }
    
    /**
     * Change EPCI selection.
     */
    @PostMapping("/commDeCo")
    public String changeEpci(@ModelAttribute FormProjectSimulator form, Model model) {
        form.setIdCommune(null);
        model.addAttribute("token", tokenManagement.getValidToken());
        return show(form, model);
    }
    
    /**
     * Change city selection.
     */
    @PostMapping("/city")
    public String changeCity(@ModelAttribute FormProjectSimulator form, Model model) {
        if (form.getIdCommune() != null) {
            Coordinate c = serviceReadReferences.getCoordinate(form.getIdCommune());
            if (c != null) {
                form.setMapLng(String.valueOf(c.getX()));
                form.setMapLat(String.valueOf(c.getY()));
            }
            if (form.getIsDense() == null) {
                Boolean dense = serviceReadReferences.isCityDense(form.getIdCommune());
                form.setIsDense(dense);
            }
        }
        model.addAttribute("token", tokenManagement.getValidToken());
        return show(form, model);
    }
    
    /**
     * Load a project for editing.
     */
    @GetMapping("/load")
    public String loadProject(@RequestParam("projectId") Long projectId, @ModelAttribute FormProjectSimulator form, Model model) {
        log.info("loadProject() - projectId={}", projectId);
        ProjectSimulator ps = projectSimulatorService.getById(projectId);
        if (ps != null) {
            form.setId(ps.getId());
            form.setAnnee(ps.getAnnee());
            form.setIsDense(ps.getIsDense());
            form.setPopulation(ps.getPopulation());
            form.setFloorSurface(ps.getSurfaceFloor());
            form.setDensityPerAccommodation(ps.getDensityPerAccommodation());
            form.setAvgAreaAccommodation(ps.getAvgAreaAccommodation());
            form.setSurfaceArea(ps.getSurfaceArea());
            form.setSurfacePark(ps.getSurfacePark());
            form.setIdCommune(ps.getIdCommune());
            form.setName(ps.getName());
            
            if (ps.getCenterArea()!=null) {
            	 form.setMapLng(String.valueOf(ps.getCenterArea().getX()));
                 form.setMapLat(String.valueOf(ps.getCenterArea().getY()));
                 
            } else if (ps.getIdCommune() != null) {
                Coordinate c = serviceReadReferences.getCoordinate(ps.getIdCommune());
                if (c != null) {
                    form.setMapLng(String.valueOf(c.getX()));
                    form.setMapLat(String.valueOf(c.getY()));
                }
            }
            
            
           
        }
        model.addAttribute("token", tokenManagement.getValidToken());
        return show(form, model);
    }

    /**
     * Handle form submission and return JSON response for AJAX requests.
     * @param sGeometry GeoJSON string representing the project zone
     * @param form form object
     * @param model Spring model
     * @return JSON response or view name
     */
    @PostMapping("/compute")
    public String compute(
            @ModelAttribute FormProjectSimulator form,
            @RequestParam(required = false) String sGeometry,
            @RequestParam("token") String token,
            Model model) {
        log.info("compute() - form={}, sGeometry={}", form, sGeometry != null ? "provided" : "null");
        
        // Anti double-soumission côté serveur: validation du token temporel
        Boolean isValid = tokenManagement.isTokenValid(token);
        if (Boolean.FALSE.equals(isValid)) {
            log.warn("Token invalide ou expiré lors du POST /compute");
            model.addAttribute("tokenInvalid", true);
            // réinjecter un token valide pour permettre une nouvelle soumission
            model.addAttribute("token", tokenManagement.getValidToken());
            populate(form);
            model.addAttribute(FORM_KEY, form);
            model.addAttribute("regions", form.getRegions());
            model.addAttribute("communautesDeCommunes", form.getCommunautesDeCommunes());
            model.addAttribute("communes", form.getCommunes());
            List<ProjectSimulator> projects = form.getIdCommune() != null ? projectSimulatorService.findByCity(form.getIdCommune()) : List.of();
            model.addAttribute("projects", projects);
            return VIEW;
        }
        
        
        Geometry geometry = null;
        String sGeom = form.getSGeometry();
		try {
			if ( StringUtils.isNotBlank(sGeom) && sGeom.indexOf("features\":[]") < 0 ) {
				log.info("start process parseGeoman");
				geometry = geoJson2GeometryHelper.parseGeoman(sGeom);
				log.info("end process parseGeoman");
			} else {
				log.info("Géométrie vide ou supprimée");
			}
		} catch (JsonProcessingException e) {
			log.error("geoman parsing error = ", sGeom);
		}	
        
        // Traitement du simulateur avec la géométrie (comme pour NewPark)
        ProjectSimulator bo = mapToBo(form, geometry);
        bo = projectSimulatorService.simulate(bo);
        
        return show(form, model);
    }
    
    
    private ProjectSimulator mapToBo(FormProjectSimulator form, Geometry geometry ) {
    	ProjectSimulator bo =null;
    	if (form != null && form.getId()!=null) {
    		bo  = projectSimulatorService.getById(form.getId());
		}
    	if(bo == null) {
			bo = new ProjectSimulator();
		}
    	
    	bo.setId(form.getId());
		bo.setAnnee(form.getAnnee());
		bo.setIsDense(form.getIsDense());
		bo.setPopulation(form.getPopulation());
		bo.setSurfaceFloor(form.getFloorSurface());
		bo.setDensityPerAccommodation(form.getDensityPerAccommodation());
		bo.setAvgAreaAccommodation(form.getAvgAreaAccommodation());
		bo.setSurfaceArea(form.getSurfaceArea());
		bo.setSurfacePark(form.getSurfacePark());
		bo.setIdCommune(form.getIdCommune());
		bo.setName(form.getName());
		
		// Assigner la géométrie parsée (toujours mettre à jour, même si null pour suppression)
		bo.setShapeArea(geometry);
		if (geometry != null) {
			bo.setCenterArea(geometry.getCentroid());
			log.debug("mapToBo() - Geometry assigned: type={}", geometry.getGeometryType());
		} else {
			bo.setCenterArea(null);
			log.debug("mapToBo() - Geometry cleared");
		}
    	return bo;
    }
    
    /**
     * Populate form with reference data.
     */
    private void populate(FormProjectSimulator form) {
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

                if (form.getIsDense() == null) {
                    Boolean dense = serviceReadReferences.isCityDense(form.getIdCommune());
                    form.setIsDense(dense);
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
