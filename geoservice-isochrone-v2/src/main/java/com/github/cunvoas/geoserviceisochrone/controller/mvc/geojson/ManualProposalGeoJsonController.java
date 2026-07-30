package com.github.cunvoas.geoserviceisochrone.controller.mvc.geojson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;
import com.github.cunvoas.geoserviceisochrone.service.proposal.ManualProposalEvaluationService;

@RestController
@RequestMapping("/mvc/geojson/manualProposal")
public class ManualProposalGeoJsonController {

    private final GeoMapServiceV2 geoMapService;
    private final ManualProposalEvaluationService evaluationService;

    @Autowired
    public ManualProposalGeoJsonController(GeoMapServiceV2 geoMapService,
                                           ManualProposalEvaluationService evaluationService) {
        this.geoMapService = geoMapService;
        this.evaluationService = evaluationService;
    }

    @GetMapping("/{insee}")
    public GeoJsonRoot getManualProposalsByInsee(@PathVariable("insee") String insee,
                                                  @RequestParam("annee") Integer annee) {
        return geoMapService.findManualProposalByInsee(insee, annee);
    }

    @GetMapping("/{insee}/evaluate")
    public GeoJsonRoot evaluate(@PathVariable("insee") String insee,
                                @RequestParam(value = "annee", required = false) Integer annee) {
        return evaluationService.loadEvaluation(insee, annee);
    }

    @PostMapping("/{insee}/evaluate")
    public GeoJsonRoot computeEvaluation(@PathVariable("insee") String insee,
                                         @RequestParam("annee") Integer annee) {
        return evaluationService.evaluate(insee, annee);
    }
}
