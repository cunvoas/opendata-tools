package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.math.BigDecimal;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entité représentant une proposition de localisation de nouveau parc pour un carreau de 200m.
 * <p>
 * Associée à une {@link ParkProposalMeta} (métadonnées de la simulation), elle fournit pour chaque
 * carreau un centre géographique et une surface de parc proposée, ainsi que l'indication de densité.
 * </p>
 */
@Data
@EqualsAndHashCode(of = {"annee", "idInspire"})
@Entity(name = "park_proposal")
@IdClass(ParkProposalId.class)
public class ParkProposal {

	/** Année de la donnée. */
	@Id
	@Column(name="annee",length=4)
    private Integer annee;
	
	/** Identifiant Inspire du carreau de 200m. */
	@Id
	@Column(name="idInspire",length=30)
	private String idInspire;

	/**
	 * Identifiant de la proposition : référence à {@link ParkProposalMeta}.
	 * Sans reverse collection pour éviter les cycles.
	 */
	@Id
	@Column(name="idMeta")
	private Long idMeta;

	/** Indique si le carreau est en zone dense. */
	@Column(name="dense")
	private Boolean isDense;

	/** Point central du carreau (centroïde proposé pour le nouveau parc). */
	@Column(name="centre")
	private Point centre;

	/** Surface proposée pour le nouveau parc (m²). */
	@Column(name="park_surface", precision = 12, scale = 2)
	private BigDecimal parkSurface;
	
	
	/**
	 * Calcule le rayon du cercle de surface équivalente à la surface proposée.
	 * @return rayon en mètres (arrondi à l'entier le plus proche)
	 */
	public Long getRadius() {
		double r = Math.sqrt(parkSurface.doubleValue()/Math.PI);
		return  Math.round(r);
	}
	
}