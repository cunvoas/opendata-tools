package com.github.cunvoas.geoserviceisochrone.model.dashboard;

import lombok.Data;

/**
 * DTO représentant une statistique d'activité pour un segment horaire.
 */
@Data
public class DashboardLiveItem {
    /** Segment horaire (ex : heure de la journée). */
    private int segment;
    /** Nombre d'utilisateurs uniques pour ce segment. */
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
     * Constructeur avec segment horaire.
     * @param segment segment horaire
     */
    public DashboardLiveItem(int segment) {
        this.segment=segment;
    }
    /**
     * Réinitialise le nombre d'utilisateurs uniques à 0.
     */
    public void resetUniqueUser() {
        nbUniqueUser=0;
    }
    /**
     * Réinitialise tous les compteurs à 0.
     */
    public void clear() {
        nbUniqueUser=0;
        nbParcActivity=0;
        nbEntranceActivity=0;
        nbIsochroneActivity=0;
        nbAdminActivity=0;
    }
    /**
     * Incrémente le nombre d'utilisateurs uniques.
     */
    public void addNbUniqueUser() {
        this.nbUniqueUser++;
    }
    /**
     * Incrémente le nombre d'activités sur les parcs.
     */
    public void addNbParkActivity() {
        this.nbParcActivity++;
    }
    /**
     * Incrémente le nombre d'activités sur les entrées.
     */
    public void addNbEntranceActivity() {
        this.nbEntranceActivity++;
    }
    /**
     * Incrémente le nombre d'activités sur les isochrones.
     */
    public void addNbIsochroneActivity() {
        this.nbIsochroneActivity++;
    }
    /**
     * Incrémente le nombre d'activités administrateur.
     */
    public void addNbAdminActivity() {
        this.nbAdminActivity++;
    }
}