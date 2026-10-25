package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Statistiques NDVI agrégées sur le polygone d'un parc.
 *
 * @param mean            NDVI moyen
 * @param min             NDVI minimal
 * @param max             NDVI maximal
 * @param stdDev          écart-type du NDVI
 * @param acquisitionDate fin de la fenêtre d'observation du fournisseur
 */
public record NdviStatistics(
    BigDecimal mean,
    BigDecimal min,
    BigDecimal max,
    BigDecimal stdDev,
    Date acquisitionDate
) {}
