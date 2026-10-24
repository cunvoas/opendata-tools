package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Bloque l'appel HTTP si le quota mensuel CDSE est épuisé.
 * La requête est refusée avant tout envoi : le plafond de 10 000 requêtes/mois est garanti.
 */
public class MonthlyQuotaInterceptor implements ClientHttpRequestInterceptor {

    private final NdviQuotaService quotaService;

    public MonthlyQuotaInterceptor(NdviQuotaService quotaService) {
        this.quotaService = quotaService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (!quotaService.tryAcquire()) {
            throw new NdviQuotaExceededException(quotaService.getMonthlyQuota());
        }
        return execution.execute(request, body);
    }
}
