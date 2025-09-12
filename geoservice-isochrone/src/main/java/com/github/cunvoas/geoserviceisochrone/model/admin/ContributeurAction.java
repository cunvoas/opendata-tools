package com.github.cunvoas.geoserviceisochrone.model.admin;


import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * Représente les actions réalisées par un contributeur sur la plateforme.
 * Permet de suivre les ajouts, modifications et suppressions d'utilisateurs, d'associations, de parcs, d'entrées et d'isochrones.
 */
@Data
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
@Entity(name = "adm_contrib_action")
public class ContributeurAction {
	
	/**
	 * Identifiant du contributeur.
	 */
	@Id
	@Column(name = "id")
	private Long idContributor = 0L;
	
	/**
	 * Nom du contributeur (transient).
	 */
	@Transient
	private String nomContributeur;
	
	/**
	 * Nombre d'utilisateurs ajoutés.
	 */
	private Long nbUserAdd = 0L;
	/**
	 * Nombre d'utilisateurs modifiés.
	 */
	private Long nbUserUpd = 0L;
	/**
	 * Nombre d'utilisateurs supprimés.
	 */
	private Long nbUserDel = 0L;
	
	/**
	 * Retourne le nombre total d'actions sur les utilisateurs.
	 * @return total des ajouts, modifications et suppressions d'utilisateurs
	 */
	public Long getUserActions() {
		return nbUserAdd+nbUserUpd+nbUserDel;
	}
	
	/**
	 * Nombre d'associations ajoutées.
	 */
	private Long nbAssoAdd = 0L;
	/**
	 * Nombre d'associations modifiées.
	 */
	private Long nbAssoUpd = 0L;
	/**
	 * Nombre d'associations supprimées.
	 */
	private Long nbAssoDel = 0L;
	
	/**
	 * Retourne le nombre total d'actions sur les associations.
	 * @return total des ajouts, modifications et suppressions d'associations
	 */
	public Long getAssoActions() {
		return nbAssoAdd+nbAssoUpd+nbAssoDel;
	}

	/**
	 * Nombre de parcs ajoutés.
	 */
	private Long nbParkAdd = 0L;
	/**
	 * Nombre de parcs modifiés.
	 */
	private Long nbParkUpd = 0L;
	/**
	 * Nombre de parcs supprimés.
	 */
	private Long nbParkDel = 0L;
	
	/**
	 * Retourne le nombre total d'actions sur les parcs.
	 * @return total des ajouts, modifications et suppressions de parcs
	 */
	public Long getParkActions() {
		return nbParkAdd+nbParkUpd+nbParkDel;
	}
	
	/**
	 * Nombre d'entrées ajoutées.
	 */
	private Long nbEntranceAdd = 0L;
	/**
	 * Nombre d'entrées modifiées.
	 */
	private Long nbEntranceUpd = 0L;
	/**
	 * Nombre d'entrées supprimées.
	 */
	private Long nbEntranceDel= 0L;
	
	/**
	 * Retourne le nombre total d'actions sur les entrées.
	 * @return total des ajouts, modifications et suppressions d'entrées
	 */
	public Long getEntranceActions() {
		return nbEntranceAdd+nbEntranceUpd+nbEntranceDel;
	}
	
	/**
	 * Nombre d'isochrones calculés.
	 */
	private Long nbIsochroneComputed = 0L;
	/**
	 * Nombre d'isochrones validés.
	 */
	private Long nbIsochroneValidated = 0L;
	/**
	 * Nombre d'isochrones publiés.
	 */
	private Long nbIsochronePublished = 0L;
	/**
	 * Nombre d'isochrones publiés pour toute la ville.
	 */
	private Long nbIsochroneAllCityPublished = 0L;
	
	/**
	 * Retourne le nombre total d'actions sur les isochrones.
	 * @return total des calculs, validations et publications d'isochrones
	 */
	public Long getIsochroneActions() {
		return nbIsochroneComputed+nbIsochroneValidated+nbIsochronePublished+nbIsochroneAllCityPublished;
	}
	
	/**
	 * Date de la première action.
	 */
	private Date firstDate;
	/**
	 * Date de la dernière action.
	 */
	private Date lastDate;
	
	/**
	 * Fusionne les actions d'un autre objet ContributeurAction dans celui-ci.
	 * @param ca autre objet ContributeurAction à fusionner
	 */
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