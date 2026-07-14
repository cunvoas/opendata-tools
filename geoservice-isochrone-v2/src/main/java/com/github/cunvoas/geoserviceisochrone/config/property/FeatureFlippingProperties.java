package com.github.cunvoas.geoserviceisochrone.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * Propriétés pour l’activation ou la désactivation de fonctionnalités (feature flags).
 * Permet de contrôler dynamiquement les services proposés par l’application.
 * 
 * setup vars for Feature flag.
 * @author cunvoas
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.feature-flipping")
public class FeatureFlippingProperties {

	/**
	 * activate services for parks.
	 */
	private boolean parkAnalysisEnabled = false;

	/**
	 * activate services for salary (gentrification).
	 */
	private boolean salaryAnalysisEnabled = false;

	/**
	 * activate services for pollution (air).
	 */
	private boolean pollutionAnalysisEnabled = false;
	

	/**
	 * activate services sendEmail.
	 */
	private boolean sendEmail = false;

	/**
	 * CPU temperature threshold (°C) for batch inhibition.
	 * When the metrics endpoint reports cpu_temperature above this value,
	 * processShapes and processCarres are skipped.
	 */
	private double cpuTempThreshold = 75.0;

	/**
	 * Prometheus metrics endpoint URL for CPU temperature check.
	 */
	private String cpuMetricsUrl = "http://192.168.1.30:1081/metrics";

	/**
	 * Timeout in milliseconds for the CPU metrics HTTP call.
	 */
	private int cpuMetricsTimeoutMs = 2000;

}