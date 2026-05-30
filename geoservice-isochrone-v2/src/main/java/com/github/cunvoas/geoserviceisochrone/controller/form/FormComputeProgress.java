package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobProgressStat;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Formulaire pour la page d'avancement des calculs.
 * Hérite de la sélection territoriale (Région / EPCI / Commune) et ajoute un filtre sur l'année.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FormComputeProgress extends AbstractFormLocate {

    /** Année de filtre (null = toutes les années). */
    private Integer annee;

    /** Liste des années disponibles (depuis ApplicationBusinessProperties). */
    private List<Integer> annees;

    /** Résultats de la requête après application des filtres. */
    private List<ComputeJobProgressStat> stats;
}
