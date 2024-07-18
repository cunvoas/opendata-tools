package com.github.cunvoas.geoserviceisochrone.model.dashboard;


import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class DashboardSummary {

	private Long nbContributeur;
	private Long nbAssociation;
	private Long nbCommunauteCommune;
	private Long nbCommune;
	
	private Long nbCarreau;
	private Long nbAnnee;
	private Long nbParcReference;
	
	private Long nbParc;
	private Long nbParcEntance;
	private Long nbParcIsochrone;
	
	private Date updateDate;

	
}
