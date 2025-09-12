package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Data;

/**
 * Modèle représentant le type d'un parc ou jardin.
 * Permet de catégoriser les zones selon leur usage, leur conformité OMS, etc.
 * Les traductions sont dans le fichier messages.properties (clé park.type.*).
 */
@Data
@Entity(name = "park_type")
public class ParkType {
    /** Identifiant du type de parc. */
    @Id
    @Column(name="id")
    private Long id;
    /** Libellé technique du type. */
    @Column(name="park_type")
    private String type;
    /** Clé de traduction i18n. */
    @Column(name="i18n")
    private String i18n;
    /** Indique si le type est conforme aux critères OMS. */
    @Column(name="oms")
    private Boolean oms;
    /** Indique si le type est strictement conforme. */
    @Column(name="strict")
    private Boolean strict;
    /** Libellé affiché (non persisté, pour l'affichage). */
    @Transient
    private String label;
}