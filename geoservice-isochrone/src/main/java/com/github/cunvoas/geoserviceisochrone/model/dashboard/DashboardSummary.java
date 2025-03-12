package com.github.cunvoas.geoserviceisochrone.model.dashboard;


import java.util.Date;

import lombok.Data;

/**
 * DTO DashboardSummary.
 */
@Data
public class DashboardSummary {

	private Long nbContributeur;
	private Long nbAssociation;
	private Long nbCommunauteCommune;
	private Long nbCommune;

	private Long nbFilosofil;
	private Long nbCarreau;
	private Long nbAnnee;
	private Long nbParcReference;
	
	private Long nbParc;
	private Long nbParcEntance;
	private Long nbParcIsochrone;
	
	private Date updateDate;

	
}
