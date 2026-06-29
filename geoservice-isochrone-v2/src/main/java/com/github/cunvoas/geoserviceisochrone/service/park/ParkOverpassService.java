package com.github.cunvoas.geoserviceisochrone.service.park;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParkOverpass;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkOverpassRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service de gestion des espaces verts Overpass (OpenStreetMap).
 *
 * <p>Fournit des méthodes de lecture des données brutes Overpass et d'intégration
 * dans le référentiel {@link ParcEtJardin}. L'intégration peut se faire par commune
 * (code INSEE), par EPCI ou pour les communes sans EPCI.
 * Les nouvelles entités sont créées avec {@link ParcSourceEnum#OSM_OVERPASS} et
 * {@link ParcStatusEnum#TO_QUALIFY}. Les entités existantes sont enrichies
 * (forme, surface) sans écraser les libellés saisis manuellement.</p>
 */
@Service
@Slf4j
public class ParkOverpassService {

    /** Accès aux données brutes Overpass (OSM). */
    private final ParkOverpassRepository parkOverpassRepository;

    /** Accès aux parcs et jardins du référentiel. */
    private final ParkJardinRepository parkJardinRepository;

    /** Accès aux communes pour la résidence INSEE -> City. */
    private final CityRepository cityRepository;

    /** Calcul de surface à partir d'une géométrie. */
    private final InseeCarre200mOnlyShapeRepository surfaceRepo;

    /**
     * Constructeur avec injection des dépendances.
     * @param parkOverpassRepository repository des données Overpass
     * @param parkJardinRepository    repository des parcs et jardins
     * @param cityRepository          repository des communes
     * @param surfaceRepo             repository de calcul de surface
     */
    @Autowired
    public ParkOverpassService(ParkOverpassRepository parkOverpassRepository,
                               ParkJardinRepository parkJardinRepository,
                               CityRepository cityRepository,
                               InseeCarre200mOnlyShapeRepository surfaceRepo) {
        this.parkOverpassRepository = parkOverpassRepository;
        this.parkJardinRepository = parkJardinRepository;
        this.cityRepository = cityRepository;
        this.surfaceRepo = surfaceRepo;
    }

    /**
     * Retourne la liste des espaces Overpass pour un code INSEE donné.
     * @param inseeCode code INSEE de la commune
     * @return liste des {@link ParkOverpass} correspondants
     */
    public List<ParkOverpass> getVegetal(String inseeCode) {
        return parkOverpassRepository.findByInsee(inseeCode);
    }

    /**
     * Retourne les parcs Overpass intersectant un polygone géographique.
     * @param polygon polygone définissant la zone de recherche
     * @return liste des {@link ParkOverpass} dans la zone
     */
    public List<ParkOverpass> findAllParkByArea(Polygon polygon) {
        return parkOverpassRepository.findByMapArea(GeometryQueryHelper.toText(polygon));
    }

    /**
     * Intègre les données Overpass dans le référentiel pour une commune.
     *
     * <p>Pour chaque {@link ParkOverpass} de la commune :
     * <ul>
     *   <li>si un {@link ParcEtJardin} correspond spatialement existe → mise à jour (forme, surface, enrichissement)</li>
     *   <li>sinon → création d'un nouveau {@link ParcEtJardin} avec source {@link ParcSourceEnum#OSM_OVERPASS}</li>
     * </ul>
     * Les libellés existants (nom, quartier, type) sont conservés lors des mises à jour.</p>
     *
     * @param inseeCode code INSEE de la commune à traiter
     */
    @Transactional
    public void integrateByInsee(String inseeCode) {
        log.info("integrateByInsee: {}", inseeCode);
        City city = cityRepository.findByInseeCode(inseeCode);
        if (city == null) {
            log.warn("integrateByInsee: city not found for INSEE {}", inseeCode);
            return;
        }

        List<ParkOverpass> overpassList = parkOverpassRepository.findByInsee(inseeCode);
        if (overpassList == null || overpassList.isEmpty()) {
            log.info("integrateByInsee: no Overpass data for INSEE {}", inseeCode);
            return;
        }

        List<ParcEtJardin> existingParks = parkJardinRepository.findByCityId(city.getId());
        log.info("integrateByInsee: {} Overpass entries, {} existing parks for INSEE {}",
                overpassList.size(), existingParks.size(), inseeCode);

        for (ParkOverpass overpass : overpassList) {
            ParcEtJardin matched = findMatching(existingParks, overpass.getShape());
            if (matched != null) {
                updateFromOverpass(matched, overpass);
            } else {
                createFromOverpass(overpass, city);
            }
        }
    }

    /**
     * Intègre les données Overpass pour toutes les communes d'un EPCI.
     * Délègue le traitement de chaque commune à {@link #integrateByInsee(String)}.
     * @param epciId identifiant de la communauté de communes (EPCI)
     */
    @Transactional
    public void integrateByEpci(Long epciId) {
        log.info("integrateByEpci: {}", epciId);
        List<City> cities = cityRepository.findByCommunauteCommuneId(epciId);
        for (City city : cities) {
            integrateByInsee(city.getInseeCode());
        }
    }

    /**
     * Intègre les données Overpass pour les communes sans EPCI.
     * Ces communes n'appartiennent à aucune communauté de communes.
     */
    @Transactional
    public void integrateCitiesWithoutEpci() {
        log.info("integrateCitiesWithoutEpci");
        List<City> cities = cityRepository.findByCommunauteCommuneIsNull();
        for (City city : cities) {
            integrateByInsee(city.getInseeCode());
        }
    }

    /**
     * Recherche un {@link ParcEtJardin} existant correspondant à une géométrie Overpass.
     * Le matching spatial vérifie :
     * <ul>
     *   <li>si le shape Overpass contient le point de coordonnées du parc</li>
     *   <li>si le shape Overpass intersecte le contour du parc</li>
     * </ul>
     * @param existingParks liste des parcs existants de la commune
     * @param overpassShape géométrie de l'élément Overpass
     * @return le {@link ParcEtJardin} correspondant, ou {@code null} si aucun
     */
    private ParcEtJardin findMatching(List<ParcEtJardin> existingParks, Geometry overpassShape) {
        if (overpassShape == null || existingParks == null || existingParks.isEmpty()) {
            return null;
        }
        for (ParcEtJardin pj : existingParks) {
            if (overpassShape.contains(pj.getCoordonnee())) {
                return pj;
            }
            if (pj.getContour() != null && overpassShape.intersects(pj.getContour())) {
                return pj;
            }
        }
        return null;
    }

    /**
     * Crée un nouveau {@link ParcEtJardin} à partir des données Overpass.
     * Les champs géométriques (contour, coordonnée) et descriptifs (nom, surface, horaires)
     * sont initialisés depuis l'entité Overpass. La source est fixée à {@link ParcSourceEnum#OSM_OVERPASS}.
     * @param overpass données source Overpass
     * @param city     commune de rattachement
     */
    private void createFromOverpass(ParkOverpass overpass, City city) {
        log.info("createFromOverpass: {} in {}", overpass.getName(), city.getInseeCode());
        ParcEtJardin pj = new ParcEtJardin();
        pj.setSource(ParcSourceEnum.OSM);
        pj.setStatus(ParcStatusEnum.TO_QUALIFY);
        pj.setCommune(city);
        pj.setName(overpass.getName());
        pj.setContour(overpass.getShape());
        if (overpass.getShape() != null) {
            pj.setCoordonnee(overpass.getShape().getCentroid());
        }
        pj.setSurface(overpass.getSurface());
        if (overpass.getShape() != null) {
            Long s = surfaceRepo.getSurface(overpass.getShape());
            if (s != null) {
                pj.setSurfaceContour(s.doubleValue());
            }
        }
        if (overpass.getOpeningHours() != null) {
            pj.setEtatOuverture(overpass.getOpeningHours());
        }
        parkJardinRepository.saveAndFlush(pj);
    }

    /**
     * Met à jour un {@link ParcEtJardin} existant avec les données Overpass.
     *
     * <p>Règles de mise à jour :
     * <ul>
     *   <li>si le parc n'a pas de contour (point seul) → recopie la forme Overpass</li>
     *   <li>si le parc n'a pas de surface → recopie la surface Overpass</li>
     *   <li>si le libellé contient "Parcs OSM (Overpass)" → passe le statut à {@link ParcStatusEnum#TO_QUALIFY}</li>
     *   <li>les autres libellés (nom, quartier, type) sont conservés</li>
     * </ul>
     * </p>
     * @param pj      le parc existant à mettre à jour
     * @param overpass les données Overpass sources
     */
    private void updateFromOverpass(ParcEtJardin pj, ParkOverpass overpass) {
        boolean changed = false;

        if (pj.getContour() == null && overpass.getShape() != null) {
            log.info("updateFromOverpass: adding contour to park {}", pj.getId());
            pj.setContour(overpass.getShape());
            pj.setCoordonnee(overpass.getShape().getCentroid());
            changed = true;
        }

        if (pj.getSource() ==null ) {
        	pj.setSource(ParcSourceEnum.OSM);
        }
        
        if (pj.getSurface() == null && overpass.getSurface() != null) {
            pj.setSurface(overpass.getSurface());
            changed = true;
        }

        if (changed && pj.getContour() != null) {
            Long s = surfaceRepo.getSurface(pj.getContour());
            if (s != null) {
                pj.setSurfaceContour(s.doubleValue());
            }
        }

        if (overpass.getOpeningHours() != null && pj.getEtatOuverture() == null) {
            pj.setEtatOuverture(overpass.getOpeningHours());
            changed = true;
        }

        if (pj.getName() != null && pj.getName().contains("Parcs OSM (Overpass)")) {
            pj.setStatus(ParcStatusEnum.TO_QUALIFY);
            changed = true;
        }

        if (changed) {
            log.info("updateFromOverpass: saving updated park {}", pj.getId());
            parkJardinRepository.saveAndFlush(pj);
        }
    }
}
