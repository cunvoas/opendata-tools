package com.github.cunvoas.geoserviceisochrone.model.dashboard;

import lombok.Data;

/**
 * DTO représentant les statistiques en temps réel du tableau de bord.
 * Agrège les activités et le nombre d'utilisateurs uniques sur une période donnée.
 */
@Data
public class DashboardLive {
    /** Nombre d'utilisateurs uniques sur la période. */
    private long nbUniqueUser;
    /** Nombre d'activités sur les parcs. */
    private long nbParcActivity;
    /** Nombre d'activités sur les entrées. */
    private long nbEntranceActivity;
    /** Nombre d'activités sur les isochrones. */
    private long nbIsochroneActivity;
    /** Nombre d'activités administrateur. */
    private long nbAdminActivity;

    /**
     * Ajoute les valeurs d'un item à l'agrégat courant.
     * @param item item à agréger
     */
    public void add(DashboardLiveItem item) {
        this.nbUniqueUser += item.getNbUniqueUser();
        this.nbAdminActivity += item.getNbAdminActivity();
        this.nbParcActivity += item.getNbParcActivity();
        this.nbIsochroneActivity += item.getNbIsochroneActivity();
        this.nbEntranceActivity += item.getNbEntranceActivity();
    }
}