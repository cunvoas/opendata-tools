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

/**
 * Contrôleur REST servant les données GeoJSON pour les propositions manuelles de parcs
 * et leur évaluation d'impact par carreau.
 */
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

    /**
     * Récupère les propositions manuelles sous forme GeoJSON pour une commune et une année données.
     * 
     * @param insee Code INSEE de la commune
     * @param annee Année cible
     * @return GeoJsonRoot contenant les formes géométriques des parcs proposés
     */
    @GetMapping("/{insee}")
    public GeoJsonRoot getManualProposalsByInsee(@PathVariable("insee") String insee,
                                                  @RequestParam("annee") Integer annee) {
        return geoMapService.findManualProposalByInsee(insee, annee);
    }

    /**
     * Charge l'évaluation pré-enregistrée des propositions manuelles (mode lecture seule).
     * 
     * @param insee Code INSEE de la commune
     * @param annee Année cible
     * @return GeoJsonRoot contenant les carreaux et leurs valeurs avant/après
     */
    @GetMapping("/{insee}/evaluate")
    public GeoJsonRoot evaluate(@PathVariable("insee") String insee,
                                @RequestParam(value = "annee", required = false) Integer annee) {
        return evaluationService.loadEvaluation(insee, annee);
    }

    /**
     * Calcule et persiste une nouvelle évaluation d'impact des propositions manuelles (déclenché par le bouton 'Évaluer').
     * 
     * @param insee Code INSEE de la commune
     * @param annee Année cible
     * @return GeoJsonRoot contenant les résultats du nouveau calcul
     */
    @PostMapping("/{insee}/evaluate")
    public GeoJsonRoot computeEvaluation(@PathVariable("insee") String insee,
                                         @RequestParam("annee") Integer annee) {
        return evaluationService.evaluate(insee, annee);
    }
}
