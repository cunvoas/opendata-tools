package com.github.cunvoas.geoserviceisochrone.service.compute;

import java.time.Duration;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.github.cunvoas.geoserviceisochrone.config.property.FeatureFlippingProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Service de contrôle de la température CPU via un endpoint Prometheus.
 *
 * <p>Interroge le endpoint de métriques ({@code /metrics}) pour lire la
 * température du processeur. Si l'appel HTTP échoue (timeout, refus de
 * connexion, DNS injoignable), la méthode {@link #isCpuOverheated()}
 * retourne {@code false} afin de ne pas bloquer le traitement par lots.
 *
 * <p>La température est comparée au seuil configuré via la propriété
 * {@code application.feature-flipping.cpu-temp-threshold}.
 * L'URL du endpoint et le timeout de l'appel HTTP sont également
 * paramétrables ({@code cpu-metrics-url}, {@code cpu-metrics-timeout-ms}).
 *
 * <p>Utilisé par {@link BatchJobService} pour inhiber
 * {@code processShapes} et {@code processCarres} en cas de surchauffe.
 *
 * @see BatchJobService
 * @see FeatureFlippingProperties
 */
@Service
@Slf4j
public class CpuTemperatureService {

    /** Client HTTP dédié avec timeout court pour l'appel Prometheus. */
    private final RestClient metricsRestClient;

    /** Propriétés de configuration (seuil, URL, timeout). */
    private final FeatureFlippingProperties featureFlippingProperties;

    /**
     * Constructeur — initialise le client HTTP avec le timeout configuré.
     *
     * @param featureFlippingProperties propriétés de feature-flipping
     */
    public CpuTemperatureService(FeatureFlippingProperties featureFlippingProperties) {
        this.featureFlippingProperties = featureFlippingProperties;
        this.metricsRestClient = buildMetricsRestClient();
    }

    /**
     * Construit un {@link RestClient} avec timeout de connexion paramétrable.
     *
     * <p>Utilise {@link JdkClientHttpRequestFactory} (Java 11+ HttpClient)
     * avec un timeout configuré via {@code cpu-metrics-timeout-ms}.
     *
     * @return client HTTP restreint aux appels métriques CPU
     */
    private RestClient buildMetricsRestClient() {
        int timeout = featureFlippingProperties.getCpuMetricsTimeoutMs();
        return RestClient.builder()
            .requestFactory(new JdkClientHttpRequestFactory(
                java.net.http.HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeout))
                    .build()
            ))
            .build();
    }

    /**
     * Vérifie si la température CPU dépasse le seuil configuré.
     *
     * <p>Interroge le endpoint Prometheus ({@code /metrics}) et extrait
     * la valeur de la métrique {@code cpu_temperature}. Compare au seuil
     * défini par {@code application.feature-flipping.cpu-temp-threshold}.
     *
     * <p>Cas d'échec (endpoint injoignable, timeout, réponse invalide) :
     * retourne {@code false} pour ne pas bloquer le traitement par lots.
     *
     * @return {@code true} si la température CPU dépasse le seuil
     */
    public boolean isCpuOverheated() {
        try {
            String metrics = metricsRestClient.get()
                .uri(featureFlippingProperties.getCpuMetricsUrl())
                .retrieve()
                .body(String.class);
            if (metrics == null) return false;
            for (String line : metrics.split("\n")) {
                line = line.trim();
                if (line.contains("cpu_temperature") && !line.startsWith("#")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        double temp = Double.parseDouble(parts[parts.length - 1]);
                        double threshold = featureFlippingProperties.getCpuTempThreshold();
                        if (temp > threshold) {
                            log.warn("CPU temp {}°C > threshold {}°C, inhibiting batch", temp, threshold);
                            return true;
                        }
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Metrics endpoint unavailable: {}", e.getMessage());
        }
        return false;
    }
}
