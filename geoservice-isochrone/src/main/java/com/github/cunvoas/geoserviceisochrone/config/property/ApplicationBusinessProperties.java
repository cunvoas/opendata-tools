package com.github.cunvoas.geoserviceisochrone.config.property;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class ApplicationBusinessProperties {

	@Value("${application.business.oms.urban.duration}")
	private String omsUrbanDistance;
	@Value("${application.business.oms.urban.area_min}")
	private Double minUrbSquareMeterPerCapita;
	@Value("${application.business.oms.urban.area_reco}")
	private Double recoUrbSquareMeterPerCapita;

	@Value("${application.business.oms.suburban.duration}")
	private String omsSubUrbanDistance;
	@Value("${application.business.oms.suburban.area_min}")
	private Double minSubUrbSquareMeterPerCapita;
	@Value("${application.business.oms.suburban.area_reco}")
	private Double recoSubUrbSquareMeterPerCapita;
	

	@Value("${application.business.oms.min_size}")
	private Double recoAtLeastParkSurface;

	@Value("${application.business.insee.densite}")
	private String inseeCodeDensite;

	@Value("${application.business.insee.annees}")
	private Integer[] inseeAnnees;

	@Value("${application.admin.export-path}")
	private String exportPath;

	@Value("${application.admin.export-data-path}")
	private String jsonFileFolder;
	
	@Value("${application.mailjet.apiToken}")
	private String mailjetToken;
	@Value("${application.mailjet.apiSecret}")
	private String mailjetSecret;
	@Value("${application.mailjet.senderEmail}")
	private String mailjetSenderEmail;
	@Value("${application.mailjet.senderName}")
	private String mailjetSenderName;
	@Value("${application.mailjet.attachementPath}")
	private String  mailjetAttachementPath;
	
	
	
	@Value("${ISOCHRONE_SITE_GESTION}")
	private String siteGestion;
	@Value("${ISOCHRONE_SITE_PUBLIQUE}")
	private String sitePublique;
	

	public Integer getDerniereAnnee() {
		List<Integer> annes = List.of(inseeAnnees);
		Integer annee=null;
		for (Integer i : annes) {
			if (annee==null || annee<i) {
				annee = i;
			}
		}
		return annee;
	}
}
