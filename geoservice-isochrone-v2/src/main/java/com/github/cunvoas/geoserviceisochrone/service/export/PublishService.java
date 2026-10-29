package com.github.cunvoas.geoserviceisochrone.service.export;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GitHelper;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.analytics.StatsSurfaceService;

import lombok.extern.slf4j.Slf4j;

/**
 * Service de publication asynchrone pour les données géographiques.
 * Produit les fichiers GeoJSON et statistiques, puis publie le résultat sur GitHub.
 * Le statut de production est consultable avant la publication Git (voir {@link #getStatus(Long, Integer)}).
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
    private GitHelper gitHelper;

    @Autowired
    private ApplicationBusinessProperties applicationBusinessProperties;

    private final Map<String, PublicationStatus> statuses = new ConcurrentHashMap<>();

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
        PublicationStatus status = new PublicationStatus();
        status.setCom2coId(c2cId);
        status.setYear(requestedYear);
        status.setPhase(PublicationStatus.Phase.PRODUCING);
        status.setStartedAt(LocalDateTime.now());
        status.setUpdatedAt(status.getStartedAt());
        statuses.put(key(c2cId, requestedYear), status);

        try {
            CommunauteCommune com2co = communauteCommuneRepository.findById(c2cId)
                .orElseThrow(() -> new IllegalArgumentException("CommunauteCommune not found with id: " + c2cId));

            // Phase 1 : production des fichiers, un fichier par étape
            // (séquence reprise de TestExporterApplication#export)
            // 1a. référentiels : régions, EPCI par région, villes par EPCI
            produce(status, () -> servicePublicationExporter.writeRegions(),
                    "referentiels", referentialPaths(com2co));
            // 1b. données géographiques et statistiques de l'EPCI
            produce(status, () -> servicePublicationExporter.writeGeoJsonParkOutline(com2co, requestedYear),
                    "parkOutline", parkOutlinePath(c2cId, requestedYear));
            produce(status, () -> servicePublicationExporter.writeGeoJsonIsochrone(com2co, requestedYear),
                    "isochrone", isochronePath(c2cId, requestedYear));
            produce(status, () -> servicePublicationExporter.writeGeoJsonCarreaux(com2co, requestedYear),
                    "carres", carrePath(c2cId, requestedYear));
            produce(status, () -> servicePublicationExporter.writeGeoJsonIris(com2co, requestedYear),
                    "iris", irisPath(c2cId, requestedYear));
            produce(status, () -> statsSurfaceService.writeStatsSurfaceByCom2CoIdAndAnnee(c2cId, requestedYear),
                    "stats-cities", statsCityPaths(com2co, requestedYear));
            produce(status, () -> statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllDense(requestedYear, c2cId),
                    "stats-urbans", statsPath(c2cId, requestedYear, "urbans"));
            produce(status, () -> statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllSuburbs(requestedYear, c2cId),
                    "stats-suburbs", statsPath(c2cId, requestedYear, "suburbs"));
            produce(status, () -> statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllV2(requestedYear, c2cId),
                    "stats-all", statsPath(c2cId, requestedYear, "all"));

            if (status.getPhase() == PublicationStatus.Phase.PRODUCTION_FAILED) {
                log.error("Publication {} stopped: production failed ({})", key(c2cId, requestedYear), status.getMessage());
                return;
            }

            status.setPhase(PublicationStatus.Phase.PRODUCED);
            status.setUpdatedAt(LocalDateTime.now());

            // Phase 2 : publication Git, uniquement si la production est complète
            publishGit(status, com2co);
            log.info("Finished async publication for com2co {} and year {}: {}", c2cId, requestedYear, status.getPhase());
        } catch (Exception e) {
            log.error("Error during async publication for com2co {} and year {}", c2cId, requestedYear, e);
            status.setPhase(PublicationStatus.Phase.PRODUCTION_FAILED);
            status.setMessage(e.getMessage());
            status.setUpdatedAt(LocalDateTime.now());
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
        synchronized (source) {
            PublicationStatus copy = new PublicationStatus();
            copy.setCom2coId(source.getCom2coId());
            copy.setYear(source.getYear());
            copy.setPhase(source.getPhase());
            copy.setFiles(new ArrayList<>(source.getFiles()));
            copy.setGitSha(source.getGitSha());
            copy.setMessage(source.getMessage());
            copy.setStartedAt(source.getStartedAt());
            copy.setUpdatedAt(source.getUpdatedAt());
            return copy;
        }
    }

    private void produce(PublicationStatus status, ThrowingRunnable step, String type, String... paths) {
        try {
            step.run();
            for (String path : paths) {
                record(status, type, path, null);
            }
        } catch (Exception e) {
            log.error("Production failed for {} ({} file(s))", type, paths.length, e);
            for (String path : paths) {
                record(status, type, path, e.getMessage());
            }
            if (paths.length == 0) {
                record(status, type, null, e.getMessage());
            }
            status.setPhase(PublicationStatus.Phase.PRODUCTION_FAILED);
            status.setMessage("Production en échec : " + type + " — " + e.getMessage());
            status.setUpdatedAt(LocalDateTime.now());
            throw new ProductionException(status.getMessage());
        }
    }

    private void record(PublicationStatus status, String type, String path, String error) {
        synchronized (status) {
            File file = path == null ? null : new File(path);
            boolean produced = file != null && file.isFile();
            status.addFile(type, path, produced,
                    produced ? file.length() : 0,
                    produced ? LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault()) : null,
                    error);
            status.setUpdatedAt(LocalDateTime.now());
        }
    }

    private void publishGit(PublicationStatus status, CommunauteCommune com2co) {
        if (!gitHelper.isConfigured()) {
            status.setPhase(PublicationStatus.Phase.PUSH_NOT_CONFIGURED);
            status.setMessage("Fichiers produits ; publication Git non configurée (username/token absents).");
            status.setUpdatedAt(LocalDateTime.now());
            return;
        }

        status.setPhase(PublicationStatus.Phase.PUBLISHING);
        status.setUpdatedAt(LocalDateTime.now());
        String repoRoot = gitHelper.getLocalPath();
        if (repoRoot == null || repoRoot.isBlank()) {
            repoRoot = applicationBusinessProperties.getJsonFileFolder();
        }

        try {
            List<String> folders = foldersToPublish(repoRoot, com2co);
            if (folders.isEmpty()) {
                throw new IllegalStateException("Aucun dossier produit à publier pour com2co " + status.getCom2coId());
            }
            String sha = gitHelper.addCommitPush(repoRoot, folders.toArray(new String[0]));
            status.setGitSha(sha);
            status.setPhase(PublicationStatus.Phase.PUBLISHED);
            status.setMessage("Fichiers produits et poussés (" + folders.size() + " dossier(s)).");
        } catch (Exception e) {
            log.error("Git publication failed for {}", key(status.getCom2coId(), status.getYear()), e);
            status.setPhase(PublicationStatus.Phase.PUSH_FAILED);
            status.setMessage("Fichiers produits ; push en échec : " + e.getMessage());
        } finally {
            status.setUpdatedAt(LocalDateTime.now());
        }
    }

    private List<String> foldersToPublish(String repoRoot, CommunauteCommune com2co) {
        Path root = Paths.get(repoRoot).toAbsolutePath().normalize();
        Path dataRoot = Paths.get(applicationBusinessProperties.getJsonFileFolder()).toAbsolutePath().normalize();
        Set<Path> folders = new LinkedHashSet<>();
        String c2cId = String.valueOf(com2co.getId());
        // référentiels : régions, EPCI, villes
        folders.add(dataRoot.resolve("data/regions.json"));
        folders.add(dataRoot.resolve("data/com2cos"));
        folders.add(dataRoot.resolve("data/cities"));
        // données géographiques de l'EPCI
        folders.add(dataRoot.resolve("geojson/parkOutline").resolve(c2cId));
        folders.add(dataRoot.resolve("geojson/isochrones").resolve(c2cId));
        folders.add(dataRoot.resolve("geojson/carres").resolve(c2cId));
        folders.add(dataRoot.resolve("geojson/iris").resolve(c2cId));
        // statistiques de l'EPCI et de ses villes
        folders.add(dataRoot.resolve("data/stats/com2co").resolve(c2cId));
        for (City city : cities(com2co)) {
            String insee = city.getInseeCode();
            folders.add(dataRoot.resolve("data/stats").resolve(insee.substring(0, 2)).resolve(insee));
        }

        Set<String> relative = new LinkedHashSet<>();
        for (Path folder : folders) {
            if (!Files.exists(folder)) {
                continue;
            }
            if (!folder.startsWith(root)) {
                throw new IllegalStateException("Dossier d'export hors du dépôt Git : " + folder);
            }
            relative.add(root.relativize(folder).toString().replace('\\', '/'));
        }
        return new ArrayList<>(relative);
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
