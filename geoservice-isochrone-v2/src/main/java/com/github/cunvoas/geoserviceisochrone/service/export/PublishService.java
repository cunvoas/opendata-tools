package com.github.cunvoas.geoserviceisochrone.service.export;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
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

    /**
     * Exécute les tâches de publication de manière asynchrone.
     * 
     * @param com2co La communauté de communes concernée
     * @param requestedYear L'année de référence pour le calcul
     */
    @Async
    public void publishAsync(CommunauteCommune com2co, Integer requestedYear) {
        log.info("Starting async publication for com2co {} and year {}", com2co.getId(), requestedYear);
        try {
            Long c2cId = com2co.getId();
            
            // Exportation des carreaux GeoJSON
            servicePublicationExporter.writeGeoJsonCarreaux(com2co, requestedYear);
            
            // Génération des statistiques de surface
            statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllDense(requestedYear, c2cId);
            statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllSuburbs(requestedYear, c2cId);
            statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllV2(requestedYear, c2cId);
            
            log.info("Finished async publication for com2co {} and year {}", com2co.getId(), requestedYear);
        } catch (Exception e) {
            log.error("Error during async publication for com2co {} and year {}", com2co.getId(), requestedYear, e);
        }
    }
}
