package com.github.cunvoas.geoserviceisochrone.controller.mvc.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.TokenManagement;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
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
    public String showRequestPage(Model model) {
        List<Region> regions = serviceReadReferences.getRegion();
        model.addAttribute("regions", regions);
        model.addAttribute("years", applicationBusinessProperties.getInseeAnnees());
        model.addAttribute("defaultYear", applicationBusinessProperties.getDerniereAnnee());
        model.addAttribute("token", tokenManagement.getValidToken());
        return "requestCompute";
    }
}
