package com.github.cunvoas.geoserviceisochrone.service.solver.helper;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

import com.github.cunvoas.geoserviceisochrone.extern.helper.DistanceHelper;

/**
 * Implémentation de {@link DistanceMeasure} pour Apache Commons Math utilisant
 * la formule de Haversine via {@link DistanceHelper#crowFlyDistance}.
 * <p>
 * Les coordonnées sont exprimées en degrés décimaux (WGS84) au format {@code [lon, lat]}.
 * Le résultat est en mètres, compatible avec {@code DBSCANClusterer}.
 * </p>
 * 
 * @see DBSCANClusterer
 * @see DistanceHelper
 */
public class HaversineDistance implements DistanceMeasure {

    private static final long serialVersionUID = 1L;

    /**
     * Calcule la distance en mètres entre deux points géographiques.
     * @param a coordonnées du premier point {@code [longitude, latitude]}
     * @param b coordonnées du second point {@code [longitude, latitude]}
     * @return distance en mètres
     */
    @Override
    public double compute(double[] a, double[] b) {
        double lon1 = a[0];
        double lat1 = a[1];
        double lon2 = b[0];
        double lat2 = b[1];

        return DistanceHelper.crowFlyDistance(lat1, lon1, lat2, lon2) * 1000;
    }
}
