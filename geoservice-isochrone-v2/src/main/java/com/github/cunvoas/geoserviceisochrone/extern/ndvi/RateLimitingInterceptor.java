package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.google.common.util.concurrent.RateLimiter;

/**
 * Limitation d'appel pour repecter le contrat Copernicus.
 * @see https://documentation.dataspace.copernicus.eu/APIs/SentinelHub/Overview/RateLimiting.html
 */
public class RateLimitingInterceptor implements ClientHttpRequestInterceptor {

    // Autorise 4 requêtes par seconde (80% du max 5/s 10000/moins)
    private final RateLimiter rateLimiter;

    /** Seam de test : permet d'injecter un RateLimiter mocké. */
    RateLimitingInterceptor(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public RateLimitingInterceptor() {
        this(RateLimiter.create(4.0));
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 1. Première tentative d'exécution de la requête
        ClientHttpResponse response = execution.execute(request, body);
        
        // 2. Vérification si la réponse est un 429 (Too Many Requests)
        if ( HttpStatus.TOO_MANY_REQUESTS.equals(response.getStatusCode()) ) {
        	
            // Récupération de la valeur du header "retry-after"
            String retryAfterHeader = response.getHeaders().getFirst("retry-after");
            
            if (retryAfterHeader != null && !retryAfterHeader.isEmpty()) {
                try {
                    // Conversion de la valeur (secondes) en entier
                    int secondsToWait = Integer.parseInt(retryAfterHeader);
                    
                    if (secondsToWait > 0) {
                        // Fermeture de la réponse initiale pour libérer les ressources système
                        response.close();
                        
                        // Alimentation du rateLimiter (ou Thread.sleep selon votre implémentation)
                        // Note : rateLimiter.acquire(1) attend généralement pour 1 permis, 
                        // si vous devez attendre 'X' secondes, adaptez selon votre API (ex: Thread.sleep ou acquire de X permis)
                        this.rateLimiter.acquire(secondsToWait);
                        
                        // 3. Rejeu de la requête après l'attente
                        return execution.execute(request, body);
                    }
                } catch (NumberFormatException e) {
                    // Gestion au cas où le header n'est pas un entier valide (ex: une date GMT)
                    // Optionnel : appliquer une attente par défaut (ex: 5 secondes)
                }
            }
        }

        return response;
    }

}