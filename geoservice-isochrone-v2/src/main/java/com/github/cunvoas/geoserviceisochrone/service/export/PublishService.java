package com.github.cunvoas.geoserviceisochrone.service.export;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.analytics.StatsSurfaceService;

import lombok.extern.slf4j.Slf4j;

/**
 * Service de publication asynchrone pour les données géographiques.
 * Traite l'exportation GeoJSON et la génération de statistiques en arrière-plan.
 */
@Service
@Slf4j
public class PublishService {

    @Autowired
    private ServicePublicationExporter servicePublicationExporter;

    @Autowired
    private StatsSurfaceService statsSurfaceService;

    @Autowired
    private CommunauteCommuneRepository communauteCommuneRepository;

    /**
     * Exécute les tâches de publication de manière asynchrone.
     * 
     * @param c2cId L'identifiant de la communauté de communes concernée
     * @param requestedYear L'année de référence pour le calcul
     */
    @Async
    @Transactional(readOnly = true)
    public void publishAsync(Long c2cId, Integer requestedYear) {
        log.info("Starting async publication for com2co {} and year {}", c2cId, requestedYear);
        try {
            CommunauteCommune com2co = communauteCommuneRepository.findById(c2cId)
                .orElseThrow(() -> new IllegalArgumentException("CommunauteCommune not found with id: " + c2cId));
            
            // Exportation des carreaux GeoJSON
            servicePublicationExporter.writeGeoJsonCarreaux(com2co, requestedYear);
            
            // Génération des statistiques de surface
            statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllDense(requestedYear, c2cId);
            statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllSuburbs(requestedYear, c2cId);
            statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllV2(requestedYear, c2cId);
            
            log.info("Finished async publication for com2co {} and year {}", c2cId, requestedYear);
        } catch (Exception e) {
            log.error("Error during async publication for com2co {} and year {}", c2cId, requestedYear, e);
        }
    }
}