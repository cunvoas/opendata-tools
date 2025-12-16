package com.github.cunvoas.geoserviceisochrone.controller.mvc.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
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

    @Autowired
    private ProjectSimulatorService projectSimulatorService;
    
    @Autowired
    private ServiceReadReferences serviceReadReferences;

    private static final String VIEW = "simulateur";
    private static final String FORM_KEY = "formSimulateur";

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
        return VIEW;
    }
    
    /**
     * Change region selection.
     */
    @PostMapping("/region")
    public String changeRegion(@ModelAttribute FormProjectSimulator form, Model model) {
        form.setIdCommunauteDeCommunes(null);
        form.setIdCommune(null);
        return show(form, model);
    }
    
    /**
     * Change EPCI selection.
     */
    @PostMapping("/commDeCo")
    public String changeEpci(@ModelAttribute FormProjectSimulator form, Model model) {
        form.setIdCommune(null);
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
        }
        return show(form, model);
    }
    
    /**
     * Load a project for editing.
     */
    @PostMapping("/load")
    public String loadProject(@RequestParam("projectId") Long projectId, @ModelAttribute FormProjectSimulator form, Model model) {
        log.info("loadProject() - projectId={}", projectId);
        ProjectSimulator ps = projectSimulatorService.getById(projectId);
        if (ps != null) {
            form.setId(ps.getId());
            form.setAnnee(ps.getAnnee());
            form.setIsDense(ps.getIsDense());
            form.setPopulation(ps.getPopulation());
            form.setFloorSurface(ps.getFloorSurface());
            form.setDensityPerAccommodation(ps.getDensityPerAccommodation());
            form.setAvgAreaAccommodation(ps.getAvgAreaAccommodation());
            form.setSurfaceArea(ps.getSurfaceArea());
            form.setSurfacePark(ps.getSurfacePark());
            form.setIdCommune(ps.getIdCommune());
            
            // Recharger le contexte territorial
            if (ps.getIdCommune() != null) {
                Coordinate c = serviceReadReferences.getCoordinate(ps.getIdCommune());
                if (c != null) {
                    form.setMapLng(String.valueOf(c.getX()));
                    form.setMapLat(String.valueOf(c.getY()));
                }
            }
        }
        return show(form, model);
    }

    /**
     * Handle form submission.
     * @param form form object
     * @param model Spring model
     * @return view name
     */
    @PostMapping("/compute")
    public String compute(@ModelAttribute FormProjectSimulator form, Model model) {
        log.info("compute() - form={}", form);
        // TODO: traitement du simulateur
        populate(form);
        model.addAttribute(FORM_KEY, form);
        model.addAttribute("regions", form.getRegions());
        model.addAttribute("communautesDeCommunes", form.getCommunautesDeCommunes());
        model.addAttribute("communes", form.getCommunes());
        return VIEW;
    }
    
    /**
     * Populate form with reference data.
     */
    private void populate(FormProjectSimulator form) {
        log.debug("populate() - idRegion={}, idCommunauteDeCommunes={}, idCommune={}",
            form.getIdRegion(), form.getIdCommunauteDeCommunes(), form.getIdCommune());
        
        // Auto preset par contexte utilisateur si aucune région n'est encore sélectionnée
        if (form.getIdRegion() == null) {
            form.autoLocate();
            if (form.getIdCommune() != null) {
                Coordinate location = serviceReadReferences.getCoordinate(form.getIdCommune());
                if (location != null) {
                    form.setMapLng(String.valueOf(location.getX()));
                    form.setMapLat(String.valueOf(location.getY()));
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
            com.github.cunvoas.geoserviceisochrone.model.opendata.City city = serviceReadReferences.getCityById(form.getIdCommune());
            form.setNameCommune(city != null ? city.getName() : "");
        }
    }

    /**
     * Fetch a ProjectSimulator by its id (REST endpoint).
     * @param id identifier of the project simulator
     * @return the ProjectSimulator or null if not found
     */
    @GetMapping("/byId")
    @ResponseBody
    public ProjectSimulator getById(@RequestParam("id") Long id) {
        ProjectSimulator ps = projectSimulatorService.getById(id);
        log.debug("getById({})-> {}", id, ps);
        return ps;
    }

    /**
     * Persist a ProjectSimulator (REST endpoint).
     * @param projectSimulator payload
     * @return saved instance
     */
    @PostMapping("/save")
    @ResponseBody
    public ProjectSimulator save(@RequestBody ProjectSimulator projectSimulator) {
        ProjectSimulator saved = projectSimulatorService.save(projectSimulator);
        log.info("Saved ProjectSimulator id={}", saved != null ? saved.getId() : null);
        return saved;
    }
}
