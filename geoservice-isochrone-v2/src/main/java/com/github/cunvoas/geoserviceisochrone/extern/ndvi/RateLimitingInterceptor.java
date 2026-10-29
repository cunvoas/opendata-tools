package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.google.common.util.concurrent.RateLimiter;

/**
 * Limitation d'appel pour respecter le contrat Copernicus.
 *
 * <p>Deux mécanismes sont appliqués à chaque requête :</p>
 * <ol>
 *   <li><b>Débit</b> : un permis {@link RateLimiter} est consommé avant l'envoi,
 *       ce qui plafonne à {@value #PERMITS_PER_SECOND} requêtes par seconde et
 *       bloque le thread appelant le temps nécessaire. Le limiteur est unique
 *       par instance de client HTTP (donc partagé par tous les parcs).</li>	
 *   <li><b>Rejeu sur 429</b> : si le fournisseur répond {@code 429 Too Many Requests},
 *       le header {@code Retry-After} est lu, la réponse est fermée, puis la requête
 *       est rejouée <b>une seule fois</b> après l'attente demandée.</li>
 * </ol>
 *
 * <p>Le header {@code Retry-After} de Sentinel Hub est exprimé en
 * <b>millisecondes</b> (et non en secondes comme le prévoit la RFC HTTP).</p>
 *
 * @see <a href="https://documentation.dataspace.copernicus.eu/APIs/SentinelHub/Overview/RateLimiting.html">CDSE - Rate limiting</a>
 */
public class RateLimitingInterceptor implements ClientHttpRequestInterceptor {

    // 2 req/s : une requête plafonnée 512×512 coûte au plus 2 PU (area 1 × samples 2),
    // soit 240 PU/min, sous la limite CDSE de 300 PU/min (et 300 req/min).
    static final double PERMITS_PER_SECOND = 2.0;

    // Partage entre tous les appels du client : le débit est global, pas par parc
    private final RateLimiter rateLimiter;

    /**
     * Seam de test : permet d'injecter un RateLimiter mocké.
     * @param rateLimiter limiteur à utiliser
     */
    RateLimitingInterceptor(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    /** Limiteur de production : {@value #PERMITS_PER_SECOND} permis par seconde. */
    public RateLimitingInterceptor() {
        this(RateLimiter.create(PERMITS_PER_SECOND));
    }

    /**
     * Débit effectif, exposé aux tests.
     * @return nombre de permis par seconde
     */
    double getRate() {
        return rateLimiter.getRate();
    }

    /**
     * Applique le débit puis exécute la requête HTTP.
     *
     * <p>En cas de 429 avec un {@code Retry-After} valide, attend le délai indiqué
     * (millisecondes) et rejoue la requête une seule fois. Le rejeu ne consomme pas
     * de nouveau permis : le {@code Retry-After} fait office de temporisation.</p>
     *
     * @param request requête HTTP sortante
     * @param body corps de la requête
     * @param execution chaîne d'exécution Spring
     * @return la réponse finale (celle du rejeu si un 429 a été rejoué)
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 1. Consommation d'un permis avant chaque appel : plafonne à 2 req/s
        rateLimiter.acquire();

        // 2. Exécution de la requête
        ClientHttpResponse response = execution.execute(request, body);
        
        // 3. Vérification si la réponse est un 429 (Too Many Requests)
        if ( HttpStatus.TOO_MANY_REQUESTS.equals(response.getStatusCode()) ) {
        	
            // Récupération de la valeur du header "retry-after"
            String retryAfterHeader = response.getHeaders().getFirst("retry-after");
            
            if (retryAfterHeader != null && !retryAfterHeader.isEmpty()) {
                try {
                    // Sentinel Hub fournit "retry-after" en millisecondes (ex: 3398 = 3,398 s)
                    int millisToWait = Integer.parseInt(retryAfterHeader);
                    
                    if (millisToWait > 0) {
                        // Fermeture de la réponse initiale pour libérer les ressources système
                        response.close();
                        
                        // Respect du délai demandé par le fournisseur avant rejeu
                        Thread.sleep(millisToWait+5);
                        
                        // 4. Rejeu de la requête après l'attente
                        return execution.execute(request, body);
                    }
                } catch (NumberFormatException e) {
                    // Gestion au cas où le header n'est pas un entier valide (ex: une date GMT)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return response;
    }

}