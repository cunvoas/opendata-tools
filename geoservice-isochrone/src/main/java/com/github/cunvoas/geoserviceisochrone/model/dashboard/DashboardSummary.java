package com.github.cunvoas.geoserviceisochrone.model.dashboard;


import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
//@Entity(name = "dashboard")
public class DashboardSummary {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_dashboard")
	private Long id;

	@Column(name = "nb_contributeur")
	private Long nbContributeur;
	@Column(name = "nb_assos")
	private Long nbAssociation;
	@Column(name = "nb_commco")
	private Long nbCommunauteCommune;
	@Column(name = "nb_ville")
	private Long nbCommune;
	
	@Column(name = "nb_carreau")
	private Long nbCarreau;
	@Column(name = "nb_parc_ref")
	private Long nbParcReference;
	
	@Column(name = "nb_parc")
	private Long nbParc;
	@Column(name = "nb_parc_entrances")
	private Long nbParcEntance;
	@Column(name = "nb_parc_saisis")
	private Long nbParcIsochrone;
	
	private Date updateDate;

	
}
