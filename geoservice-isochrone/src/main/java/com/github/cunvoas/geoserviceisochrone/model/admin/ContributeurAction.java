package com.github.cunvoas.geoserviceisochrone.model.admin;


import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Entity(name = "adm_contrib_action")
public class ContributeurAction {
	
	@Id
	@Column(name = "id")
	private Long idContributor = 0L;
	
	private Long nbUserAdd = 0L;
	private Long nbUserUpd = 0L;
	private Long nbUserDel = 0L;
	
	private Long nbAssoAdd = 0L;
	private Long nbAssoUpd = 0L;
	private Long nbAssoDel = 0L;

	private Long nbParkAdd = 0L;
	private Long nbParkUpd = 0L;
	private Long nbParkDel = 0L;
	
	private Long nbEntranceAdd = 0L;
	private Long nbEntranceUpd = 0L;
	private Long nbEntranceDel= 0L;
	
	private Long nbIsochroneComputed = 0L;
	private Long nbIsochroneValidated = 0L;
	private Long nbIsochronePublished = 0L;
	private Long nbIsochroneAllCityPublished = 0L;
	
	private Date firstDate;
	private Date lastDate;
	
	public void merge(ContributeurAction ca) {
		this.nbUserAdd += ca.nbUserAdd;
		this.nbUserUpd += ca.nbUserUpd;
		this.nbUserDel += ca.nbUserDel;
		
		this.nbAssoAdd += ca.nbAssoAdd;
		this.nbAssoUpd += ca.nbAssoUpd;
		this.nbAssoDel += ca.nbAssoDel;
		
		this.nbParkAdd += ca.nbParkAdd;
		this.nbParkUpd += ca.nbParkUpd;
		this.nbParkDel += ca.nbParkDel;
		
		this.nbEntranceAdd += ca.nbEntranceAdd;
		this.nbEntranceUpd += ca.nbEntranceUpd;
		this.nbEntranceDel += ca.nbEntranceDel;

		this.nbIsochroneComputed += ca.nbIsochroneComputed;
		this.nbIsochroneValidated += ca.nbIsochroneValidated;
		this.nbIsochronePublished += ca.nbIsochronePublished;
		this.nbIsochroneAllCityPublished += ca.nbIsochroneAllCityPublished;
		
		this.lastDate=new Date();
	}
	
}
