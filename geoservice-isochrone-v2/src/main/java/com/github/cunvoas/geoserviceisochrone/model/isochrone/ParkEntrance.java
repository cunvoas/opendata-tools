package com.github.cunvoas.geoserviceisochrone.model.isochrone;



import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;

/**
 * Modèle représentant une entrée d'accès à une zone de parc.
 * Contient les informations géographiques et descriptives d'une entrée, ainsi que les liens avec la zone de parc associée.
 */
@Data
@Entity(name = "park_entrance")
public class ParkEntrance {
    /** Identifiant unique de l'entrée. */
    @Id
    @Column(name="id")
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "seq_park_entrance"
    )
    @SequenceGenerator(
        name="seq_park_entrance",
        allocationSize=1,
        initialValue = 1
    )
    private long id;
    /** Référence à la zone de parc associée. */
    @ManyToOne
    @JoinColumn(name="area_id", nullable=false)
    private ParkArea parkArea;
    /** Réponse JSON de l'API IGN (optionnelle). */
    @Column(name="ign_response", length=10000)
    private String ignReponse;
    /** Date de mise à jour de l'entrée. */
    @Column(name="update_date")
    private java.util.Date updateDate;
    /** Date de la donnée IGN. */
    @Column(name="ign_date")
    private java.util.Date ignDate;
    /** Description textuelle de l'entrée. */
    @Column(name="description")
    private String description;
    /** Lien externe vers l'entrée (optionnel). */
    @Column(name="entrance_link", length=1000)
    private String entranceLink;
    /** Point d'entrée (coordonnées GPS). */
    @Column(name="entrance_point", columnDefinition = "geometry(Point,4326)")
    private Point entrancePoint;
    /** Polygone isochrone IGN associé à l'entrée. */
    @Column(columnDefinition = "geometry(Polygon,4326)")
    private Polygon polygon;
    /**
     * Retourne la longitude de l'entrée si disponible.
     * @return longitude en chaîne de caractères, vide sinon
     */
    public String getEntryLng() {
        if (entrancePoint!=null && entrancePoint.getCoordinate()!=null) {
            return String.valueOf(entrancePoint.getCoordinates()[0].x);
        }
        return "";
    }
    /**
     * Retourne la latitude de l'entrée si disponible.
     * @return latitude en chaîne de caractères, vide sinon
     */
    public String getEntryLat() {
        if (entrancePoint!=null && entrancePoint.getCoordinate()!=null) {
            return String.valueOf(entrancePoint.getCoordinates()[0].y);
        }
        return "";
    }
}