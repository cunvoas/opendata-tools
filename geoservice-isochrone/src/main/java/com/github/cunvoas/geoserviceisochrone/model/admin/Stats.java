package com.github.cunvoas.geoserviceisochrone.model.admin;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * Représente une statistique d'activité sur la plateforme.
 * Permet de tracer les actions réalisées par les utilisateurs (ajout, modification, etc.).
 */
@Data
@EqualsAndHashCode(of = {"id"})
@Entity(name = "adm_activity_stats")
@Table(indexes = {
		  @Index(name = "idx_activity_action", columnList = "action"),
		  @Index(name = "idx_activity_userId", columnList = "userId")
		})
public class Stats {
	
	/**
	 * Constante pour l'événement "entrée".
	 */
	public static final String EVT_ENTRANCE="EVT_ENTRANCE";
	/**
	 * Constante pour l'événement "isochrone".
	 */
	public static final String EVT_ISOCHRONE="EVT_ISOCHRONE";
	/**
	 * Constante pour l'événement "parc".
	 */
	public static final String EVT_PARK="EVT_PARK";
	/**
	 * Constante pour l'événement "administration".
	 */
	public static final String EVT_ADMIN="EVT_ADMIN";
	/**
	 * Constante pour le mode "ajout".
	 */
	public static final String MODE_ADD="ADD";
	/**
	 * Constante pour le mode "modification".
	 */
	public static final String MODE_UPD="UPD";
	
	/**
	 * Constructeur avec action.
	 * @param action nom de l'action
	 */
	public Stats(String action) {
		super();
		this.action=action;
	}

	/**
	 * Identifiant unique de la statistique.
	 */
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_activity_stats")
    @SequenceGenerator(
    		name="seq_activity_stats",
    		allocationSize=1,
    		initialValue = 1
    	)
	private Long id;
	
	/**
	 * Identifiant de l'utilisateur concerné.
	 */
	private Long userId;
	/**
	 * Type d'action réalisée.
	 */
	private String action;
	/**
	 * Mode de l'action (ajout, modification, etc.).
	 */
	private String mode;
	/**
	 * Date de la dernière mise à jour.
	 */
	private Date update=new Date();

}