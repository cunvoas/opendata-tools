package com.github.cunvoas.geoserviceisochrone.controller.mvc.admin;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.form.FormComputeRequest;
import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.TokenManagement;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;

@Controller
@RequestMapping("/mvc/management/compute")
public class ComputeRequestControler {

    private final ServiceReadReferences serviceReadReferences;
    private final ApplicationBusinessProperties applicationBusinessProperties;
    private final TokenManagement tokenManagement;

    @Autowired
    public ComputeRequestControler(ServiceReadReferences serviceReadReferences,
            ApplicationBusinessProperties applicationBusinessProperties,
            TokenManagement tokenManagement) {
        this.serviceReadReferences = serviceReadReferences;
        this.applicationBusinessProperties = applicationBusinessProperties;
        this.tokenManagement = tokenManagement;
    }

    @GetMapping("/request")
    //@PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public String show(Model model) {
        FormComputeRequest form = new FormComputeRequest();
        locateUser(form);
        return populateModel(form, model);
    }

    private void locateUser(FormComputeRequest form) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof Contributeur contrib) {
            form.setIdRegion(contrib.getIdRegion());
            form.setIdCommunauteDeCommunes(contrib.getIdCommunauteCommune());
            form.setIdCommune(contrib.getIdCommune());
        }
    }

    @PostMapping("/region")
    public String changeRegion(@ModelAttribute FormComputeRequest form, Model model) {
        form.setIdCommunauteDeCommunes(null);
        form.setIdCommune(null);
        return populateModel(form, model);
    }

    @PostMapping("/epci")
    public String changeEpci(@ModelAttribute FormComputeRequest form, Model model) {
        form.setIdCommune(null);
        return populateModel(form, model);
    }

    @PostMapping("/city")
    public String changeCity(@ModelAttribute FormComputeRequest form, Model model) {
        return populateModel(form, model);
    }

    private String populateModel(FormComputeRequest form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("regions", serviceReadReferences.getRegion());

        if (form.getIdRegion() != null) {
            model.addAttribute("epcis", serviceReadReferences.getCommunauteByRegionId(form.getIdRegion()));

            if (form.getIdCommunauteDeCommunes() != null) {
                model.addAttribute("cities", serviceReadReferences.getCityByCommunauteCommuneId(form.getIdCommunauteDeCommunes()));
            } else {
                model.addAttribute("cities", serviceReadReferences.getCityByRegionId(form.getIdRegion()));
            }
        } else {
            model.addAttribute("epcis", new ArrayList<>());
            model.addAttribute("cities", new ArrayList<>());
        }

        model.addAttribute("years", applicationBusinessProperties.getInseeAnnees());
        model.addAttribute("defaultYear", applicationBusinessProperties.getDerniereAnnee());
        model.addAttribute("token", tokenManagement.getValidToken());
        return "requestCompute";
    }
}
