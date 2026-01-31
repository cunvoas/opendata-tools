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
import com.github.cunvoas.geoserviceisochrone.controller.form.FormProposition;
import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.TokenManagement;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalMeta;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ParkProposalMetaRepository;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.solver.ServicePropositionParc;

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
	private ApplicationBusinessProperties applicationBusinessProperties;
	
    @Autowired
    private TokenManagement tokenManagement;
    
    @Autowired
    private ServicePropositionParc servicePropositionParc;
    
    @Autowired
    private ParkProposalMetaRepository parkProposalMetaRepository;

    /**
     * Display the simulator view.
     * @param form form object
     * @param model Spring model
     * @return view name
     */
    @GetMapping
    public String show(@ModelAttribute FormProposition form, Model model) {
        log.debug("show() - form={}", form);
        populate(form, model);
       
        model.addAttribute("regions", form.getRegions());
        model.addAttribute("communautesDeCommunes", form.getCommunautesDeCommunes());
        model.addAttribute("communes", form.getCommunes());
        model.addAttribute("typeCalcul", servicePropositionParc.getAvailableAlgorithms());
        
        // Trier listAnnee à l'envers
        List<Integer> anneeList = new ArrayList<>(List.of(applicationBusinessProperties.getInseeAnnees()));
        java.util.Collections.reverse(anneeList);
        model.addAttribute("listAnnee", anneeList);
        

        // Charger les propositions pour la commune sélectionnée
        loadPropositions(form, model);
        return VIEW;
    }	
    
    /**
     * Change region selection.
     */
    @PostMapping("/region")
    public String changeRegion(@ModelAttribute FormProposition form, Model model) {
        form.setIdCommunauteDeCommunes(null);
        form.setIdCommune(null);
        form.setCodeInsee(null);
        return show(form, model);
    }
    
    /**
     * Change EPCI selection.
     */
    @PostMapping("/commDeCo")
    public String changeEpci(@ModelAttribute FormProposition form, Model model) {
        form.setIdCommune(null);
        form.setCodeInsee(null);
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
            // Set INSEE code for proposal computation
            City city = serviceReadReferences.getCityById(form.getIdCommune());
            if (city != null) {
                form.setCodeInsee(city.getInseeCode());
            }
        }
        return show(form, model);
    }
    
    /**
     * Compute park proposal for the selected commune.
     */
    @PostMapping("/compute")
    public String compute(
            @ModelAttribute FormProposition form,
            Model model) {
        log.info("compute() - form={}, commune={}, algorithm={}", form, form.getCodeInsee(), form.getType());
        
        
        // Anti double-soumission côté serveur: validation du token temporel
        Boolean isValid = tokenManagement.isTokenValid(form.getToken());
        if (Boolean.FALSE.equals(isValid)) {
            log.warn("Token invalide ou expiré lors du POST /compute");
            model.addAttribute("tokenInvalid", true);
            return show(form, model);
        }
        
        // Validation des paramètres obligatoires
        if (form.getCodeInsee() == null || form.getCodeInsee().isEmpty()) {
            log.warn("Code INSEE manquant lors du calcul de proposition");
            model.addAttribute("computeError", "Erreur: Aucune commune sélectionnée");
            return show(form, model);
        }
        
        if (form.getAnnee() == null) {
            log.warn("Année manquante lors du calcul de proposition");
            model.addAttribute("computeError", "Erreur: Veuillez sélectionner une année");
            return show(form, model);
        }
        
        if (form.getType() == null) {
            log.warn("Algorithme manquant lors du calcul de proposition");
            model.addAttribute("computeError", "Erreur: Veuillez sélectionner un algorithme");
            return show(form, model);
        }
        
        try {
            // Launch proposal computation
            log.info("Lancement du calcul de proposition pour INSEE={}, année={}, algo={}", 
                    form.getCodeInsee(), form.getAnnee(), form.getType());
            servicePropositionParc.calculeProposition(form.getCodeInsee(), form.getAnnee(), form.getType());
            
            model.addAttribute("computeSuccess", "Calcul lancé avec succès pour " + form.getNameCommune());
            log.info("Calcul de proposition terminé pour INSEE={}", form.getCodeInsee());
        
        } catch (Exception e) {
            log.error("Erreur lors du calcul de proposition", e);
            model.addAttribute("computeError", "Erreur lors du calcul: " + e.getMessage());
        }
        
        return show(form, model);
    }
    
    /**
     * Clear all proposals for the selected commune.
     */
    @PostMapping("/clear")
    public String clearProposals(@ModelAttribute FormProposition form, Model model) {
        log.info("clearProposals() - form={}, commune={}", form, form.getCodeInsee());
        
        if (form.getCodeInsee() == null || form.getCodeInsee().isEmpty()) {
            log.warn("Code INSEE manquant lors de l'effacement des propositions");
            model.addAttribute("clearError", "Erreur: Aucune commune sélectionnée");
            return show(form, model);
        }
        
        try {
            // Delete all proposals for the commune
            List<ParkProposalMeta> propositions = parkProposalMetaRepository.findByInsee(form.getCodeInsee());
            parkProposalMetaRepository.deleteAll(propositions);
            
            log.info("Suppression de {} propositions pour INSEE={}", propositions.size(), form.getCodeInsee());
            model.addAttribute("clearSuccess", "Toutes les propositions ont été effacées pour " + form.getNameCommune());
        
        } catch (Exception e) {
            log.error("Erreur lors de l'effacement des propositions", e);
            model.addAttribute("clearError", "Erreur lors de l'effacement: " + e.getMessage());
        }
        
        return show(form, model);
    }
    
    
    
    /**
     * Populate form with reference data.
     */
    private void populate(FormProposition form, Model model) {
    	if (form == null) {
    		return;
    	}
        log.debug("populate() - idRegion={}, idCommunauteDeCommunes={}, idCommune={}",
            form.getIdRegion(), form.getIdCommunauteDeCommunes(), form.getIdCommune());
        
        // Générer un nouveau token pour chaque affichage
        form.setToken(tokenManagement.getValidToken());
        
        // Auto preset par contexte utilisateur si aucune région n'est encore sélectionnée
        if (form.getIdRegion() == null) {
            form.autoLocate();
            if (form.getIdCommune() != null) {
                if (form.getMapLng() == null && form.getMapLat() == null) {
                    setMapCoordinates(form);
                }
                // Set INSEE code if not already set
                if (form.getCodeInsee() == null || form.getCodeInsee().isEmpty()) {
                    City city = serviceReadReferences.getCityById(form.getIdCommune());
                    if (city != null) {
                        form.setCodeInsee(city.getInseeCode());
                    }
                }
            }
        }
        
        // Load regions
        form.setRegions(serviceReadReferences.getRegion());
        
        if (form.getIdRegion() != null) {
            loadRegionData(form);
        } else {
            form.setCommunautesDeCommunes(new ArrayList<>());
            form.setCommunes(new ArrayList<>());
        }
        
        // Set city and EPCI names
        setCityAndEpciNames(form);
        
        model.addAttribute(FORM_KEY, form);
    }

    /**
     * Set map coordinates for the selected city.
     */
    private void setMapCoordinates(FormProposition form) {
        Coordinate location = serviceReadReferences.getCoordinate(form.getIdCommune());
        if (location != null) {
            form.setMapLng(String.valueOf(location.getX()));
            form.setMapLat(String.valueOf(location.getY()));
        }
    }

    /**
     * Load region data (EPCI and communes).
     */
    private void loadRegionData(FormProposition form) {
        form.setCommunautesDeCommunes(serviceReadReferences.getCommunauteByRegionId(form.getIdRegion()));
        
        // if only one preselect it
        if (form.getCommunautesDeCommunes() != null && form.getCommunautesDeCommunes().size() == 1 && 
            form.getIdCommunauteDeCommunes() == null) {
            form.setIdCommunauteDeCommunes(form.getCommunautesDeCommunes().get(0).getId());
        }
        
        if (form.getIdCommunauteDeCommunes() != null) {
            form.setCommunes(serviceReadReferences.getCityByCommunauteCommuneId(form.getIdCommunauteDeCommunes()));
        } else {
            form.setCommunes(serviceReadReferences.getCityByRegionId(form.getIdRegion()));
        }
    }

    /**
     * Set city and EPCI names in the form.
     */
    private void setCityAndEpciNames(FormProposition form) {
        // Set city name if city is selected
        if (form.getIdCommune() != null) {
            City city = serviceReadReferences.getCityById(form.getIdCommune());
            form.setNameCommune(city != null ? city.getName() : "");
        }
        
        // Set EPCI name if EPCI is selected
        if (form.getIdCommunauteDeCommunes() != null) {
            com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune comm2co = 
                serviceReadReferences.getCommunauteCommuneById(form.getIdCommunauteDeCommunes());
            form.setNameCommunauteDeCommunes(comm2co != null ? comm2co.getName() : "");
        }
    }
    
    /**
     * Load proposals for the selected commune.
     */
    private void loadPropositions(FormProposition form, Model model) {
        if (form.getCodeInsee() != null && !form.getCodeInsee().isEmpty()) {
            List<ParkProposalMeta> propositions =  parkProposalMetaRepository.findByInsee(form.getCodeInsee());
            
            log.debug("Chargement de {} propositions pour INSEE={}", propositions.size(), form.getCodeInsee());
            model.addAttribute("propositions", propositions);
        } else {
            model.addAttribute("propositions", new ArrayList<>());
        }
    }
}
