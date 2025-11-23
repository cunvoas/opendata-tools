package com.github.cunvoas.geoserviceisochrone.model.dashboard;


import java.util.Date;

import lombok.Data;

/**
 * DTO représentant le résumé global du tableau de bord.
 * Contient les principaux indicateurs agrégés et la date de mise à jour.
 */
@Data
public class DashboardSummary {
    /** Nombre de contributeurs. */
    private Long nbContributeur;
    /** Nombre d'associations. */
    private Long nbAssociation;
    /** Nombre de communautés de communes. */
    private Long nbCommunauteCommune;
    /** Nombre de communes. */
    private Long nbCommune;
    /** Nombre de jeux de données Filosofil. */
    private Long nbFilosofil;
    /** Nombre de carreaux. */
    private Long nbCarreau;
    /** Nombre d'années. */
    private Long nbAnnee;
    /** Nombre de parcs de référence. */
    private Long nbParcReference;
    /** Nombre total de parcs. */
    private Long nbParc;
    /** Nombre d'entrées de parcs. */
    private Long nbParcEntance;
    /** Nombre d'isochrones de parcs. */
    private Long nbParcIsochrone;
        /** Nombre de ComputeJob en attente de calcul. */
        private Long nbComputeJobPending;
    /** Date de mise à jour des données. */
    private java.util.Date updateDate;
}