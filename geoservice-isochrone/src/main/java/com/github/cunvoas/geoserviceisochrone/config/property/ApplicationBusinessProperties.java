package com.github.cunvoas.geoserviceisochrone.config.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class ApplicationBusinessProperties {

	@Value("${application.business.oms.urban.distance}")
	private String omsUrbanDistance;
	@Value("${application.business.oms.urban.area_min}")
	private Double minUrbSquareMeterPerCapita;
	@Value("${application.business.oms.urban.area}")
	private Double recoUrbSquareMeterPerCapita;

	@Value("${application.business.oms.suburban.distance}")
	private String omsSubUrbanDistance;
	@Value("${application.business.oms.suburban.area_min}")
	private Double minSubUrbSquareMeterPerCapita;
	@Value("${application.business.oms.suburban.area}")
	private Double recoSubUrbSquareMeterPerCapita;

	@Value("${application.business.insee.densite}")
	private String inseeCodeDensite;

	@Value("${application.mailjet.apiToken}")
	private String mailjetToken;
	@Value("${application.mailjet.apiSecret}")
	private String mailjetSecret;
	@Value("${application.mailjet.senderEmail}")
	private String mailjetSenderEmail;
	@Value("${application.mailjet.senderName}")
	private String mailjetSenderName;
	@Value("${ISOCHRONE_SITE_GESTION}")
	private String siteGestion;
	@Value("${ISOCHRONE_SITE_PUBLIQUE}")
	private String sitePublique;

}
