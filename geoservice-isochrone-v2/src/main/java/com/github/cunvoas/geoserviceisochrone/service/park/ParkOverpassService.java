package com.github.cunvoas.geoserviceisochrone.service.park;

import java.util.List;

import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.opendata.ParkOverpass;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkOverpassRepository;

/**
 * Service dédié à la gestion des espaces verts et parcs issus de la source Overpass (OpenStreetMap).
 *
 * <p>Ce service expose des méthodes de lecture simples pour récupérer les parcs
 * liés à une commune (par code INSEE) ou à une zone géographique (polygone).
 * Les méthodes retournent des entités JPA/Domain et ne modifient pas l'état de
 * la base de données.</p>
 *
 * <p>Documentation courte et non invasive : modifications limitées au niveau des
 * commentaires pour améliorer la lisibilité du code.</p>
 */
@Service
public class ParkOverpassService {

    /** Dépôt d'accès aux données des parcs Overpass. */
    private final ParkOverpassRepository parkOverpassRepository;

    /**
     * Constructeur avec injection de dépendance.
     *
     * @param parkOverpassRepository le repository permettant l'accès aux données {@link ParkOverpass}
     */
    @Autowired
    public ParkOverpassService(ParkOverpassRepository parkOverpassRepository) {
        this.parkOverpassRepository = parkOverpassRepository;
    }

    /**
     * Retourne la liste des espaces végétaux (parcs, jardins, etc.) associés à une commune
     * identifiée par son code INSEE.
     *
     * @param inseeCode le code INSEE de la commune recherchée
     * @return la liste des {@link ParkOverpass} correspondant au code INSEE
     */
    public List<ParkOverpass> getVegetal(String inseeCode) {
        return parkOverpassRepository.findByInsee(inseeCode);
    }

    /**
     * Retourne la liste des parcs dont la géométrie est contenue ou intersecte
     * le polygone géographique fourni.
     * <p>
     * Le polygone est converti en représentation texte (WKT) avant d'être transmis
     * au repository pour la requête spatiale.
     * </p>
     *
     * @param polygon le polygone géographique définissant la zone de recherche
     * @return la liste des {@link ParkOverpass} situés dans la zone délimitée par le polygone
     */
    public List<ParkOverpass> findAllParkByArea(Polygon polygon) {
        // Conversion du polygone JTS en texte WKT pour la requête spatiale
        return parkOverpassRepository.findByMapArea(GeometryQueryHelper.toText(polygon));
    }
}
