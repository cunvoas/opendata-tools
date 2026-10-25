package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Bloque l'appel HTTP si le quota mensuel CDSE est épuisé.
 * La requête est refusée avant tout envoi : le plafond de 10 000 requêtes/mois est garanti.
 *
 * <p>Une unité est réservée avant l'envoi puis remboursée si la réponse n'est pas 2XX :
 * CDSE ne facture que les requêtes exécutées avec succès. L'intercepteur est placé
 * <b>avant</b> le limiteur de débit pour qu'un rejeu 429 reste un seul appel facturé.</p>
 *
 * <p>Le token OAuth n'est pas un appel Sentinel Hub et n'est pas décompté.</p>
 */
public class MonthlyQuotaInterceptor implements ClientHttpRequestInterceptor {

    private final NdviQuotaService quotaService;

    /**
     * @param quotaService compteur persistant du quota mensuel
     */
    public MonthlyQuotaInterceptor(NdviQuotaService quotaService) {
        this.quotaService = quotaService;
    }

    /**
     * Réserve une unité de quota, exécute la requête et rembourse si elle échoue.
     *
     * @param request requête HTTP sortante
     * @param body corps de la requête
     * @param execution chaîne d'exécution Spring
     * @return la réponse HTTP
     * @throws NdviQuotaExceededException si le quota mensuel est épuisé (aucun envoi)
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (!quotaService.tryAcquire()) {
            throw new NdviQuotaExceededException(quotaService.getMonthlyQuota());
        }
        ClientHttpResponse response;
        try {
            response = execution.execute(request, body);
        } catch (IOException | RuntimeException e) {
            // timeout ou erreur réseau : aucune requête facturée par CDSE
            quotaService.release();
            throw e;
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            // CDSE ne débite que les 2XX : on annule la réservation des 4xx/5xx
            quotaService.release();
        }
        return response;
    }
}
