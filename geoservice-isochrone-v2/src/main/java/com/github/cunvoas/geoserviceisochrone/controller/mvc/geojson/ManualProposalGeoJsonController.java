package com.github.cunvoas.geoserviceisochrone.controller.mvc.geojson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

@RestController
@RequestMapping("/mvc/geojson/manualProposal")
public class ManualProposalGeoJsonController {

    private final GeoMapServiceV2 geoMapService;

    @Autowired
    public ManualProposalGeoJsonController(GeoMapServiceV2 geoMapService) {
        this.geoMapService = geoMapService;
    }

    @GetMapping("/{insee}")
    public GeoJsonRoot getManualProposalsByInsee(@PathVariable("insee") String insee) {
        return geoMapService.findManualProposalByInsee(insee);
    }
}
