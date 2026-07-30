package com.github.cunvoas.geoserviceisochrone.controller.mvc.project;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.form.FormManualProposal;
import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.TokenManagement;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualParkProposal;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.proposal.ManualProposalService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/mvc/proposal/manual")
@Slf4j
public class ManualProposalController {

    private static final String VIEW = "manualProposal";
    private static final String FORM_KEY = "formManualProposal";

    private final ServiceReadReferences serviceReadReferences;
    private final TokenManagement tokenManagement;
    private final ManualProposalService manualProposalService;
    private final ApplicationBusinessProperties applicationBusinessProperties;

    @Autowired
    public ManualProposalController(ServiceReadReferences serviceReadReferences,
                                    TokenManagement tokenManagement,
                                    ManualProposalService manualProposalService,
                                    ApplicationBusinessProperties applicationBusinessProperties) {
        this.serviceReadReferences = serviceReadReferences;
        this.tokenManagement = tokenManagement;
        this.manualProposalService = manualProposalService;
        this.applicationBusinessProperties = applicationBusinessProperties;
    }

    @GetMapping
    public String show(@ModelAttribute FormManualProposal form, Model model) {
        log.debug("show() - form={}", form);
        populate(form, model);
        loadProposals(form, model);
        return VIEW;
    }

    @PostMapping("/region")
    public String changeRegion(@ModelAttribute FormManualProposal form, Model model) {
        form.setIdCommunauteDeCommunes(null);
        form.setIdCommune(null);
        form.setCodeInsee(null);
        return show(form, model);
    }

    @PostMapping("/commDeCo")
    public String changeEpci(@ModelAttribute FormManualProposal form, Model model) {
        form.setIdCommune(null);
        form.setCodeInsee(null);
        return show(form, model);
    }

    @PostMapping("/city")
    public String changeCity(@ModelAttribute FormManualProposal form, Model model) {
        if (form.getIdCommune() != null) {
            Coordinate c = serviceReadReferences.getCoordinate(form.getIdCommune());
            if (c != null) {
                form.setMapLng(String.valueOf(c.getX()));
                form.setMapLat(String.valueOf(c.getY()));
            }
            City city = serviceReadReferences.getCityById(form.getIdCommune());
            if (city != null) {
                form.setCodeInsee(city.getInseeCode());
                form.setNameCommune(city.getName());
            }
        }
        return show(form, model);
    }

    @PostMapping("/save")
    public String save(@ModelAttribute FormManualProposal form, Model model) {
        log.info("save() - form={}", form);

        Boolean isValid = tokenManagement.isTokenValid(form.getToken());
        if (Boolean.FALSE.equals(isValid)) {
            log.warn("Token invalide ou expiré");
            model.addAttribute("tokenInvalid", true);
            return show(form, model);
        }

        if (form.getCodeInsee() == null || form.getCodeInsee().isEmpty()) {
            log.warn("Code INSEE manquant");
            model.addAttribute("saveError", "Erreur: Aucune commune sélectionnée");
            return show(form, model);
        }

        try {
            manualProposalService.save(form);
            model.addAttribute("saveSuccess", "Proposition enregistrée");
            FormManualProposal freshForm = new FormManualProposal();
            freshForm.setIdRegion(form.getIdRegion());
            freshForm.setIdCommunauteDeCommunes(form.getIdCommunauteDeCommunes());
            freshForm.setIdCommune(form.getIdCommune());
            freshForm.setCodeInsee(form.getCodeInsee());
            freshForm.setNameCommune(form.getNameCommune());
            freshForm.setMapLat(form.getMapLat());
            freshForm.setMapLng(form.getMapLng());
            freshForm.setAnnee(form.getAnnee());
            return show(freshForm, model);
        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde", e);
            model.addAttribute("saveError", "Erreur: " + e.getMessage());
        }

        return show(form, model);
    }

    @PostMapping("/delete")
    public String delete(@ModelAttribute FormManualProposal form, Model model) {
        log.info("delete() - form={}", form);

        if (form.getId() == null) {
            model.addAttribute("deleteError", "Erreur: Aucune proposition sélectionnée");
            return show(form, model);
        }

        try {
            manualProposalService.delete(form.getId());
            model.addAttribute("deleteSuccess", "Proposition supprimée");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression", e);
            model.addAttribute("deleteError", "Erreur: " + e.getMessage());
        }

        return show(form, model);
    }

    private void populate(FormManualProposal form, Model model) {
        if (form == null) return;

        form.setToken(tokenManagement.getValidToken());

        if (form.getIdRegion() == null) {
            form.autoLocate();
            if (form.getIdCommune() != null) {
                setMapCoordinates(form);
                setInseeCode(form);
            }
        }

        if (form.getAnnee() == null) {
            form.setAnnee(applicationBusinessProperties.getDerniereAnnee());
        }

        form.setRegions(serviceReadReferences.getRegion());

        if (form.getIdRegion() != null) {
            loadRegionData(form);
        } else {
            form.setCommunautesDeCommunes(new ArrayList<>());
            form.setCommunes(new ArrayList<>());
        }

        setCityAndEpciNames(form);

        List<Integer> anneeList = new ArrayList<>(List.of(applicationBusinessProperties.getInseeAnnees()));
        java.util.Collections.reverse(anneeList);
        model.addAttribute("listAnnee", anneeList);

        model.addAttribute(FORM_KEY, form);
        model.addAttribute("regions", form.getRegions());
        model.addAttribute("communautesDeCommunes", form.getCommunautesDeCommunes());
        model.addAttribute("communes", form.getCommunes());
    }

    private void setMapCoordinates(FormManualProposal form) {
        Coordinate location = serviceReadReferences.getCoordinate(form.getIdCommune());
        if (location != null) {
            form.setMapLng(String.valueOf(location.getX()));
            form.setMapLat(String.valueOf(location.getY()));
        }
    }

    private void setInseeCode(FormManualProposal form) {
        if (form.getCodeInsee() == null || form.getCodeInsee().isEmpty()) {
            City city = serviceReadReferences.getCityById(form.getIdCommune());
            if (city != null) {
                form.setCodeInsee(city.getInseeCode());
                form.setNameCommune(city.getName());
            }
        }
    }

    private void loadRegionData(FormManualProposal form) {
        form.setCommunautesDeCommunes(serviceReadReferences.getCommunauteByRegionId(form.getIdRegion()));

        if (form.getCommunautesDeCommunes() != null && form.getCommunautesDeCommunes().size() == 1
                && form.getIdCommunauteDeCommunes() == null) {
            form.setIdCommunauteDeCommunes(form.getCommunautesDeCommunes().get(0).getId());
        }

        if (form.getIdCommunauteDeCommunes() != null) {
            form.setCommunes(serviceReadReferences.getCityByCommunauteCommuneId(form.getIdCommunauteDeCommunes()));
        } else {
            form.setCommunes(serviceReadReferences.getCityByRegionId(form.getIdRegion()));
        }
    }

    private void setCityAndEpciNames(FormManualProposal form) {
        if (form.getIdCommune() != null) {
            City city = serviceReadReferences.getCityById(form.getIdCommune());
            form.setNameCommune(city != null ? city.getName() : "");
        }
        if (form.getIdCommunauteDeCommunes() != null) {
            com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune comm2co =
                serviceReadReferences.getCommunauteCommuneById(form.getIdCommunauteDeCommunes());
            form.setNameCommunauteDeCommunes(comm2co != null ? comm2co.getName() : "");
        }
    }

    private void loadProposals(FormManualProposal form, Model model) {
        if (form.getCodeInsee() != null && !form.getCodeInsee().isEmpty()) {
            List<ManualParkProposal> proposals = manualProposalService.findByInsee(form.getCodeInsee(), form.getAnnee());
            model.addAttribute("proposals", proposals);
        } else {
            model.addAttribute("proposals", new ArrayList<>());
        }
    }
}
