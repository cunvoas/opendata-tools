package com.github.cunvoas.geoserviceisochrone.controller.mvc.consult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormConsultMap;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;

import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur lecture seule pour la page de consultation cartographique.
 * Permet de sélectionner Région / EPCI / Commune et centre la carte ;
 * les couches GeoJSON sont chargées côté front avec les endpoints existants.
 */
@Controller
@RequestMapping("/mvc/consult")
@Slf4j
public class ConsultMapController {

    @Autowired
    private ServiceReadReferences serviceReadReferences;

    private static final String VIEW = "consultMap";
    private static final String FORM_KEY = "territoire";

    @GetMapping({"", "/map"})
    public String show(@ModelAttribute FormConsultMap form, Model model) {
        populate(form);
        model.addAttribute(FORM_KEY, form);
        model.addAttribute("regions", form.getRegions());
        model.addAttribute("communautesDeCommunes", form.getCommunautesDeCommunes());
        model.addAttribute("communes", form.getCommunes());
        return VIEW;
    }

    @PostMapping("/region")
    public String changeRegion(@ModelAttribute FormConsultMap form, Model model) {
        form.setIdCommunauteDeCommunes(null);
        form.setIdCommune(null);
        return show(form, model);
    }

    @PostMapping("/epci")
    public String changeEpci(@ModelAttribute FormConsultMap form, Model model) {
        form.setIdCommune(null);
        return show(form, model);
    }

    @PostMapping("/city")
    public String changeCity(@ModelAttribute FormConsultMap form, Model model) {
        if (form.getIdCommune() != null) {
            Coordinate c = serviceReadReferences.getCoordinate(form.getIdCommune());
            if (c != null) {
                form.setMapLng(String.valueOf(c.getX()));
                form.setMapLat(String.valueOf(c.getY()));
            }
        }
        return show(form, model);
    }

    private void populate(FormConsultMap form) {
        log.debug("populate() - idRegion={}, idCommunauteDeCommunes={}, idCommune={}", 
            form.getIdRegion(), form.getIdCommunauteDeCommunes(), form.getIdCommune());

        // Auto preset par contexte utilisateur si aucune région n'est encore sélectionnée
        if (form.getIdRegion() == null) {
            form.autoLocate();
        }

        // listes (régions toujours chargées)
        form.setRegions(serviceReadReferences.getRegion());

        if (form.getIdRegion() != null) {
            form.setCommunautesDeCommunes(serviceReadReferences.getCommunauteByRegionId(form.getIdRegion()));

            if (form.getIdCommunauteDeCommunes() != null) {
                form.setCommunes(serviceReadReferences.getCityByCommunauteCommuneId(form.getIdCommunauteDeCommunes()));
                log.debug("Communes chargées depuis EPCI {}: {} communes", form.getIdCommunauteDeCommunes(), 
                    form.getCommunes() != null ? form.getCommunes().size() : 0);
            } else {
                form.setCommunes(serviceReadReferences.getCityByRegionId(form.getIdRegion()));
                log.debug("Communes chargées depuis région {}: {} communes", form.getIdRegion(), 
                    form.getCommunes() != null ? form.getCommunes().size() : 0);
            }
        } else {
            form.setCommunautesDeCommunes(new java.util.ArrayList<>());
            form.setCommunes(new java.util.ArrayList<>());
        }

        // Noms pour affichage (région / EPCI)
        if (form.getIdRegion() != null && form.getRegions() != null) {
            form.getRegions().stream()
                .filter(r -> r.getId().equals(form.getIdRegion()))
                .findFirst()
                .ifPresent(r -> form.setNameRegion(r.getName()));
        }
        if (form.getIdCommunauteDeCommunes() != null && form.getCommunautesDeCommunes() != null) {
            form.getCommunautesDeCommunes().stream()
                .filter(c -> c.getId().equals(form.getIdCommunauteDeCommunes()))
                .findFirst()
                .ifPresent(c -> form.setNameCommunauteDeCommunes(c.getName()));
        }

        // Présélection automatique d'une commune si aucune choisie mais liste disponible
        if (form.getIdCommune() == null && form.getCommunes() != null && !form.getCommunes().isEmpty()) {
            form.setIdCommune(form.getCommunes().get(0).getId());
            log.debug("Preset commune par défaut: {}", form.getIdCommune());
        }

        if (form.getIdCommune() != null) {
            form.setNameCommune(serviceReadReferences.getCityById(form.getIdCommune()).getName());
            if (form.getMapLat() == null || form.getMapLng() == null) {
                Coordinate c = serviceReadReferences.getCoordinate(form.getIdCommune());
                if (c != null) {
                    form.setMapLng(String.valueOf(c.getX()));
                    form.setMapLat(String.valueOf(c.getY()));
                }
            }
        }
    }
}
