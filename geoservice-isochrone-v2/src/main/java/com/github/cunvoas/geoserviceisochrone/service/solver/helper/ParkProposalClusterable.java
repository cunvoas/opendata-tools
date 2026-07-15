package com.github.cunvoas.geoserviceisochrone.service.solver.helper;

import org.apache.commons.math3.ml.clustering.Clusterable;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

/**
 * Adaptateur {@link Clusterable} pour {@link ParkProposalWork}, permettant
 * d'utiliser les algorithmes de clustering d'Apache Commons Math (DBSCAN)
 * sur les carreaux de 200m.
 * <p>
 * Le point est représenté par les coordonnées WGS84 du centre du carreau
 * au format {@code [longitude, latitude]}.
 * </p>
 * 
 * @see DBSCANClusterer
 * @see ParkProposalWork#getCentre()
 */
public class ParkProposalClusterable implements Clusterable {

    private final ParkProposalWork work;

    /**
     * Construit un wrapper clusterable pour un carreau.
     * @param work carreau à envelopper
     */
    public ParkProposalClusterable(ParkProposalWork work) {
        this.work = work;
    }

    /**
     * Retourne les coordonnées du centre du carreau.
     * @return tableau {@code [longitude, latitude]} en degrés WGS84
     */
    @Override
    public double[] getPoint() {
        return new double[] { work.getCentre().getX(), work.getCentre().getY() };
    }

    /**
     * Retourne le carreau sous-jacent.
     * @return instance {@link ParkProposalWork}
     */
    public ParkProposalWork getWork() {
        return work;
    }
}
