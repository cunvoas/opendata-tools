package com.github.cunvoas.geoserviceisochrone.service.export;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.analytics.StatsSurfaceService;

import lombok.extern.slf4j.Slf4j;

/**
 * Service de production asynchrone des données géographiques.
 * Produit les fichiers GeoJSON et statistiques ; le commit final est délégué au dashboard.
 * Le statut de production est consultable via {@link #getStatus(Long, Integer)}.
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

    @Autowired
    private ApplicationBusinessProperties applicationBusinessProperties;

    private final Map<String, PublicationStatus> statuses = new ConcurrentHashMap<>();

    private PublicationStatus newStatus(Long c2cId, Integer requestedYear) {
        PublicationStatus status = new PublicationStatus();
        status.setCom2coId(c2cId);
        status.setYear(requestedYear);
        status.setPhase(PublicationStatus.Phase.PRODUCING);
        status.setStartedAt(LocalDateTime.now());
        status.setUpdatedAt(status.getStartedAt());
        status.setSteps(new ArrayList<>(List.of(
                new PublicationStatus.StepStatus("referentiel", "Référentiel", 8),
                new PublicationStatus.StepStatus("stats-cities", "Stats par ville", 27),
                new PublicationStatus.StepStatus("stats-epci", "Stats EPCI", 10),
                new PublicationStatus.StepStatus("parkOutline", "Park outline", 10),
                new PublicationStatus.StepStatus("isochrone", "Isochrones", 15),
                new PublicationStatus.StepStatus("carres", "Carreaux", 15),
                new PublicationStatus.StepStatus("iris", "IRIS", 5),
                new PublicationStatus.StepStatus("ndvi", "NDVI", 5),
                new PublicationStatus.StepStatus("territoires", "Territoires", 5)
        )));
        return status;
    }

    /**
     * Crée le statut avant le lancement asynchrone, pour que le premier /status ne renvoie pas null.
     * @return copie du statut créé
     */
    public PublicationStatus prepare(Long c2cId, Integer requestedYear) {
        PublicationStatus status = statuses.compute(key(c2cId, requestedYear), (k, existing) -> {
            if (existing != null && (existing.getPhase() == PublicationStatus.Phase.PRODUCING
                    || existing.getPhase() == PublicationStatus.Phase.PUBLISHING)) {
                return existing;
            }
            return newStatus(c2cId, requestedYear);
        });
        status.touch();
        return status.copy();
    }

    /**
     * @return true si une production ou une publication est déjà en cours pour ce couple EPCI/année
     */
    public boolean isRunning(Long c2cId, Integer requestedYear) {
        PublicationStatus status = statuses.get(key(c2cId, requestedYear));
        return status != null && (status.getPhase() == PublicationStatus.Phase.PRODUCING
                || status.getPhase() == PublicationStatus.Phase.PUBLISHING);
    }

    /**
     * Exécute les tâches de publication de manière asynchrone.
     * 
     * @param c2cId L'identifiant de la communauté de communes concernée
     * @param requestedYear L'année de référence pour le calcul
     */
    @Async
    public void publishAsync(Long c2cId, Integer requestedYear) {
        PublicationStatus status = statuses.computeIfAbsent(key(c2cId, requestedYear),
                k -> newStatus(c2cId, requestedYear));
        run(status);
    }

    @Async
    public void publishAsync(PublicationStatus status) {
        run(status);
    }

    private void run(PublicationStatus status) {
        Long c2cId = status.getCom2coId();
        Integer requestedYear = status.getYear();
        log.info("Starting async publication for com2co {} and year {}", c2cId, requestedYear);

        try {
            CommunauteCommune com2co = communauteCommuneRepository.findById(c2cId)
                .orElseThrow(() -> new IllegalArgumentException("CommunauteCommune not found with id: " + c2cId));

            if (!StringUtils.hasText(com2co.getSiren())) {
                status.setPhase(PublicationStatus.Phase.PRODUCTION_FAILED);
                status.setMessage("Publication refusée : l'intercommunalité " + com2co.getName()
                        + " n'a pas de numéro Siren.");
                status.touch();
                log.error("Publication refused for com2co {}: missing Siren", c2cId);
                return;
            }

            // 1a. référentiels : régions, EPCI par région, villes par EPCI
            produce(status, "referentiel", () -> servicePublicationExporter.writeRegions(),
                    referentialPaths(com2co));

            // 1b. statistiques
            produce(status, "stats-cities", () -> statsSurfaceService.writeStatsSurfaceByCom2CoIdAndAnnee(
                            c2cId, requestedYear,
                            (index, total, city) -> {
                                status.city(index, total, city.getInseeCode(), city.getName());
                                status.objects("villes", 1);
                            }),
                    statsCityPaths(com2co, requestedYear));
            produce(status, "stats-epci", () -> {
                statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllDense(requestedYear, c2cId);
                statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllSuburbs(requestedYear, c2cId);
                statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllV2(requestedYear, c2cId);
            },
                    statsPath(c2cId, requestedYear, "urbans"),
                    statsPath(c2cId, requestedYear, "suburbs"),
                    statsPath(c2cId, requestedYear, "all"));

            // 1c. données géographiques
            produce(status, "parkOutline", () -> servicePublicationExporter.writeGeoJsonParkOutline(com2co, requestedYear),
                    parkOutlinePath(c2cId, requestedYear));
            produce(status, "isochrone", () -> servicePublicationExporter.writeGeoJsonIsochrone(com2co, requestedYear),
                    isochronePath(c2cId, requestedYear));
            produce(status, "carres", () -> servicePublicationExporter.writeGeoJsonCarreaux(com2co, requestedYear),
                    carrePath(c2cId, requestedYear));
            produce(status, "iris", () -> servicePublicationExporter.writeGeoJsonIris(com2co, requestedYear),
                    irisPath(c2cId, requestedYear));
            produce(status, "ndvi", () -> servicePublicationExporter.writeNdviJson(com2co, requestedYear),
                    ndviPath(c2cId, requestedYear));

            // 1d. index des territoires publiés, consommé par le front (routes, sitemap, pré-rendu)
            produce(status, "territoires", () -> servicePublicationExporter.writeTerritoires(),
                    territoiresPath());

            if (status.hasProductionError()) {
                status.setPhase(PublicationStatus.Phase.PRODUCTION_FAILED);
                status.setMessage("Production incomplète : un ou plusieurs fichiers attendus sont absents.");
                status.touch();
                log.error("Publication {} stopped: production incomplete", key(c2cId, requestedYear));
                return;
            }

            status.setPhase(PublicationStatus.Phase.PRODUCED);
            status.setMessage("Fichiers produits ; en attente du dashboard et du commit final.");
            status.touch();
            log.info("Finished async production for com2co {} and year {}: {}", c2cId, requestedYear, status.getPhase());
        } catch (Exception e) {
            log.error("Error during async publication for com2co {} and year {}", c2cId, requestedYear, e);
            status.setPhase(PublicationStatus.Phase.PRODUCTION_FAILED);
            status.setMessage(e.getMessage());
            status.touch();
        }
    }

    /**
     * @return statut de la dernière publication pour un EPCI et une année, ou null si aucune publication
     */
    public PublicationStatus getStatus(Long c2cId, Integer requestedYear) {
        PublicationStatus source = statuses.get(key(c2cId, requestedYear));
        if (source == null) {
            return null;
        }
        return source.copy();
    }

    private void produce(PublicationStatus status, String stepCode, ThrowingRunnable step, String... paths) {
        status.startStep(stepCode);
        try {
            step.run();
            for (String path : paths) {
                record(status, stepCode, path, null);
            }
            status.endStep(stepCode);
        } catch (Exception e) {
            log.error("Production failed for {} ({} file(s))", stepCode, paths.length, e);
            for (String path : paths) {
                record(status, stepCode, path, e.getMessage());
            }
            if (paths.length == 0) {
                record(status, stepCode, null, e.getMessage());
            }
            status.failStep(stepCode);
            status.setPhase(PublicationStatus.Phase.PRODUCTION_FAILED);
            status.setMessage("Production en échec : " + stepCode + " — " + e.getMessage());
            throw new ProductionException(status.getMessage());
        }
    }

    private void record(PublicationStatus status, String type, String path, String error) {
        File file = path == null ? null : new File(path);
        boolean produced = file != null && file.isFile();
        status.addFile(type, path, produced,
                produced ? file.length() : 0,
                produced ? LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault()) : null,
                error);
    }

    /**
     * Produit le seul référentiel (régions, EPCI, villes, index des territoires).
     * Le commit est délégué au dashboard d'état des lieux.
     */
    @Async
    public void publishReferentialAsync() {
        try {
            servicePublicationExporter.writeRegions();
            servicePublicationExporter.writeTerritoires();
            log.info("Référentiel écrit ; en attente du dashboard et du commit final");
        } catch (Exception e) {
            log.error("Référentiel : production en échec", e);
        }
    }

    private String[] referentialPaths(CommunauteCommune com2co) {
        String base = applicationBusinessProperties.getJsonFileFolder();
        List<String> paths = new ArrayList<>();
        paths.add(base + "/data/regions.json");
        paths.add(base + "/data/cities/densite.json");
        if (com2co.getRegion() != null) {
            String regId = String.valueOf(com2co.getRegion().getId());
            paths.add(base + "/data/com2cos/" + regId + "/com2cos_" + regId + ".json");
        }
        paths.add(base + "/data/cities/com2co/cities_" + com2co.getId() + ".json");
        return paths.toArray(new String[0]);
    }

    private List<City> cities(CommunauteCommune com2co) {
        return com2co.getCities() == null ? List.of() : com2co.getCities();
    }

    private String parkOutlinePath(Long c2cId, Integer year) {
        return applicationBusinessProperties.getJsonFileFolder()
                + "/geojson/parkOutline/" + c2cId + "/parkOutline_" + year + "_" + c2cId + ".json";
    }

    private String isochronePath(Long c2cId, Integer year) {
        return applicationBusinessProperties.getJsonFileFolder()
                + "/geojson/isochrones/" + c2cId + "/isochrone_" + year + "_" + c2cId + ".json";
    }

    private String carrePath(Long c2cId, Integer year) {
        return applicationBusinessProperties.getJsonFileFolder()
                + "/geojson/carres/" + c2cId + "/carre_" + year + "_" + c2cId + ".json";
    }

    private String irisPath(Long c2cId, Integer year) {
        return applicationBusinessProperties.getJsonFileFolder()
                + "/geojson/iris/" + c2cId + "/iris_" + year + "_" + c2cId + ".json";
    }

    private String[] statsCityPaths(CommunauteCommune com2co, Integer year) {
        return cities(com2co).stream()
                .map(city -> {
                    String insee = city.getInseeCode();
                    return applicationBusinessProperties.getJsonFileFolder()
                            + "/data/stats/" + insee.substring(0, 2) + "/" + insee
                            + "/stats_" + insee + "_" + year + ".json";
                })
                .toArray(String[]::new);
    }

    private String statsPath(Long c2cId, Integer year, String suffix) {
        return applicationBusinessProperties.getJsonFileFolder()
                + "/data/stats/com2co/" + c2cId + "/stats_c2c_" + c2cId + "_" + suffix + "_" + year + ".json";
    }

    private String ndviPath(Long c2cId, Integer year) {
        return applicationBusinessProperties.getJsonFileFolder()
                + "/data/ndvi/" + c2cId + "/ndvi_" + year + "_" + c2cId + ".json";
    }

    private String territoiresPath() {
        return applicationBusinessProperties.getJsonFileFolder() + "/data/territoires.json";
    }

    private String key(Long c2cId, Integer year) {
        return c2cId + ":" + year;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static class ProductionException extends RuntimeException {
        ProductionException(String message) {
            super(message);
        }
    }
}
