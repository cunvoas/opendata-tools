package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

/**
 * Quota mensuel d'appels à l'API Statistical CDSE épuisé.
 */
public class NdviQuotaExceededException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NdviQuotaExceededException(int monthlyQuota) {
        super("Quota mensuel NDVI atteint (" + monthlyQuota + " requêtes)");
    }
}
