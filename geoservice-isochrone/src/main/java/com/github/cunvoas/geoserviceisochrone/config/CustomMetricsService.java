package com.github.cunvoas.geoserviceisochrone.config;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * custom metric for prometheus monitoring.
 */
@Component
public class CustomMetricsService {

    private final Counter customMetricCounter;

    /**
     * @param meterRegistry the registry.
     */
    public CustomMetricsService(MeterRegistry meterRegistry) {
        customMetricCounter = Counter.builder("custom_metric_name")
          .description("Description of custom metric")
          .tags("environment", "development")
          .register(meterRegistry);
    }

    public void incrementCustomMetric() {
        customMetricCounter.increment();
    }
    
    
}