package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.util.Date;
import java.util.List;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

/**
 * Modèle représentant une zone d'accessibilité d'un parc.
 * Contient les informations géographiques et descriptives d'une zone accessible autour d'un parc ou jardin.
 */
@Data
@ToString(onlyExplicitlyIncluded = true)
@Entity(name = "park_area")
@Table(indexes = {
    @Index(name = "idx_parkarea_point", columnList = "point"),
    @Index(name = "idx_parkarea_polygon", columnList = "polygon")
})
public class ParkArea {
    /** Identifiant unique de la zone. */
    @Id
    @ToString.Include
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_park")
    @SequenceGenerator(
        name="seq_park",
        allocationSize=1,
        initialValue = 1
    )
    private Long id;

    /** Identifiant du parc ou jardin d'origine. */
    @Column(name = "id_parc_jardin")
    private Long idParcEtJardin;

//        @ManyToOne
//        @JoinColumn(name="city_id", nullable=false)
//        private City city;

    /** Liste des entrées associées à cette zone. */
    @OneToMany(mappedBy = "parkArea", fetch = FetchType.EAGER)
//	@OneToMany(mappedBy = "parkArea")
	private List<ParkEntrance> entrances;

    /** Nom de la zone ou du parc. */
    @ToString.Include
    @Column(name = "name")
    private String name;
    /** Bloc ou secteur administratif. */
    @Column(name = "block")
    private String block;
    /** Description textuelle de la zone. */
    @Column(name = "description")
    private String description;
    /** Point central de la zone (coordonnées GPS). */
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point point;
    /** Polygone isochrone représentant la zone accessible. */
    @Column(columnDefinition = "geometry(Polygon,4326)")
    private Polygon polygon;
    /** Date de dernière mise à jour. */
    @ToString.Include
    @DateTimeFormat
    @Column(name="updated")
    private Date updated;
    /** Type de parc (référence à ParkType). */
    @ManyToOne
    @JoinColumn( name="type_id", nullable = true)
    private ParkType type;
    /** Indique si la zone est personnalisée selon les critères OMS. */
    @Column(name = "oms_custom")
    private Boolean omsCustom;
    /** Indique si la zone doit être recalculée. */
    @Column(name = "to_compute")
    private Boolean toCompute;
}