package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Statistiques NDVI agrégées sur le polygone d'un parc.
 *
 * <p>La période est celle retournée par Copernicus. Elle est conservée dans
 * l'historique {@code ndvi_observation} : le satellite ne repasse pas tous les
 * ans au même endroit, et une observation doit pouvoir être rattachée à la
 * bonne période lors d'une reventilation ultérieure.</p>
 *
 * @param mean            NDVI moyen
 * @param min             NDVI minimal
 * @param max             NDVI maximal
 * @param stdDev          écart-type du NDVI
 * @param acquisitionDate fin de la fenêtre d'observation du fournisseur
 * @param periodStart     début de la fenêtre d'agrégation retournée
 * @param sampleCount     nombre de pixels valides utilisés par Copernicus
 */
public record NdviStatistics(
    BigDecimal mean,
    BigDecimal min,
    BigDecimal max,
    BigDecimal stdDev,
    Date acquisitionDate,
    Date periodStart,
    Integer sampleCount
) {}
