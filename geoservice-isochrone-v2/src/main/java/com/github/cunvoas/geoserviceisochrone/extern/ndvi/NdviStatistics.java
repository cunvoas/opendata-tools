package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Statistiques des trois indices de végétation Sentinel-2 agrégées sur le polygone d'un parc.
 *
 * <p>Le NDVI mesure la vigueur et la biomasse, le NDRE la chlorophylle des couverts
 * denses (il sature moins que le NDVI) et le GNDVI la concentration en chlorophylle.
 * Les trois sont calculés dans le même appel Copernicus.</p>
 *
 * <p>La période est celle retournée par Copernicus. Elle est conservée dans
 * la table {@code ndvi_result} : le satellite ne repasse pas tous les
 * ans au même endroit, et une observation doit pouvoir être rattachée à la
 * bonne période lors d'une reventilation ultérieure.</p>
 *
 * @param ndvi            statistiques NDVI (B08/B04)
 * @param ndre            statistiques NDRE (B08/B05)
 * @param gndvi           statistiques GNDVI (B08/B03)
 * @param acquisitionDate fin de la fenêtre d'observation du fournisseur
 * @param periodStart     début de la fenêtre d'agrégation retournée
 * @param sampleCount     nombre de pixels valides utilisés par Copernicus
 */
public record NdviStatistics(
    IndexStatistics ndvi,
    IndexStatistics ndre,
    IndexStatistics gndvi,
    Date acquisitionDate,
    Date periodStart,
    Integer sampleCount
) {
    /**
     * Statistiques d'un indice : moyenne, min, max et écart-type.
     *
     * @param mean   valeur moyenne
     * @param min    valeur minimale
     * @param max    valeur maximale
     * @param stdDev écart-type
     */
    public record IndexStatistics(
        BigDecimal mean,
        BigDecimal min,
        BigDecimal max,
        BigDecimal stdDev
    ) {}
}
