package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Modèle représentant les indicateurs calculés pour une zone d'accessibilité de parc.
 * Contient les résultats de calculs démographiques et surfaciques pour une zone donnée et une année.
 */
@Data
@Entity(name = "park_area_computed")
@IdClass(ParkAreaComputedId.class)
@EqualsAndHashCode(of = {"annee", "id"})
public class ParkAreaComputed {
    /** Identifiant de la zone de parc. */
    @Id
    @Column(name = "id")
    private Long id;
    /** Année de référence des données. */
    @Id
    @Column(name = "annee")
    private Integer annee;
    /** Indique si la zone respecte les critères OMS. */
    @Column(name = "oms")
    private Boolean oms;
    /** Surface totale de la zone (m²). */
    @Column(name = "surface", precision = 11, scale = 2)
    private BigDecimal surface;
    /** Population totale dans la zone. */
    @Column(name = "population", precision = 7, scale = 2)
    private BigDecimal population;
    /** Surface par habitant (m²/hab). */
    @Column(name = "surface_population", precision = 10, scale =2)
    private BigDecimal surfacePerInhabitant;
    /** Population IRIS dans la zone. */
    @Column(name = "population_iris", precision = 7, scale = 2)
    private BigDecimal populationIris;
    /** Surface par habitant IRIS (m²/hab). */
    @Column(name = "surface_population_iris", precision = 10, scale =2)
    private BigDecimal surfacePerInhabitantIris;
    /** Indique si la zone est considérée comme dense. */
    @Column(name = "dense")
    private Boolean isDense = Boolean.TRUE;
    /** Date de dernière mise à jour des calculs. */
    @DateTimeFormat
    @Column(name="updated")
    private Date updated;

}