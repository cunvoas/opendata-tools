package com.github.cunvoas.geoserviceisochrone.config;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Service de métrique personnalisée pour la surveillance Prometheus.
 */
@Component
public class CustomMetricsService {

    /**
     * Compteur personnalisé pour la métrique.
     */
    private final Counter customMetricCounter;

    /**
     * Constructeur du service de métrique personnalisée.
     * @param meterRegistry le registre de métriques
     */
    public CustomMetricsService(MeterRegistry meterRegistry) {
        customMetricCounter = Counter.builder("custom_metric_name")
          .description("Description of custom metric")
          .tags("environment", "development")
          .register(meterRegistry);
    }

    /**
     * Incrémente la métrique personnalisée.
     */
    public void incrementCustomMetric() {
        customMetricCounter.increment();
    }
    
    
}