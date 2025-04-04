package com.github.cunvoas.geoserviceisochrone.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
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
	
	
	
	
}
