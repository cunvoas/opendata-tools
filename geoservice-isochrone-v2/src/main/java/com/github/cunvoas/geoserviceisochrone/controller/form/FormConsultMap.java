package com.github.cunvoas.geoserviceisochrone.controller.form;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Form simple pour la page de consultation cartographique (lecture seule).
 * Reprend uniquement les sélections territoriales et la localisation carte.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FormConsultMap extends AbstractFormLocate {
    /** Nom logique du formulaire (utilisé dans le modèle Thymeleaf). */
    private String formName = "territoire";

    // Ajout des propriétés lat/lon pour Thymeleaf
    public Double getLat() {
        try {
            return mapLat != null ? Double.valueOf(mapLat) : null;
        } catch (Exception e) {
            return null;
        }
    }
    public Double getLon() {
        try {
            return mapLng != null ? Double.valueOf(mapLng) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
