package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.math.BigDecimal;

import org.locationtech.jts.geom.Point;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 */
@Data
@EqualsAndHashCode(of = {"annee", "idInspire"})
@Entity(name = "park_proposal")
@IdClass(ParkProposalId.class)
public class ParkProposal {

	/**
	 * Année de la donnée.
	 */
	@Id
	@Column(name="annee",length=4)
    private Integer annee;
	
	@Id
	@Column(name="idInspire",length=30)
	private String idInspire;

	/**
	 *  Identifiant de la proposition: ParkProposalMeta.
	 *   sans reverse collection.
	 */
	@Id
	@Column(name="idMeta")
	private Long idMeta;
	
	@Column(name="dense")
	private Boolean isDense;

	@Column(name="centre")
	private Point centre;

	@Column(name="park_surface", precision = 12, scale = 2)
	private BigDecimal parkSurface;
	
	
	/**
	 * @return radius in meters.
	 */
	public Long getRadius() {
		double r = Math.sqrt(parkSurface.doubleValue()/Math.PI);
		return  Math.round(r);
	}
	
}
