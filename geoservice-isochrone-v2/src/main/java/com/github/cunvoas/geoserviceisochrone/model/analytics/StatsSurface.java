package com.github.cunvoas.geoserviceisochrone.model.analytics;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.CompareToBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entité de statistiques sur la surface de parcs accessibles par tranche de surface.
 * <p>
 * Permet d'analyser la répartition de la population (incluse ou exclue) selon la surface
 * d'espaces verts accessibles et le seuil OMS atteint, pour une année et des bornes de surface données.
 * </p>
 */
@Data
@Entity(name = "z_stats_surface")
@IdClass(StatsSurfaceId.class)
@EqualsAndHashCode(of = {"annee", "surfaceMin", "surfaceMax"})
public class StatsSurface implements Comparable<StatsSurface> {

	/** Année de la donnée. */
	@Id
	@Column(name="annee",length=4)
	private Integer annee;

	/** Borne inférieure de la tranche de surface (m²). */
	@Id
	@Column(name="surface_min",length=5)
	private Integer surfaceMin;

	/** Borne supérieure de la tranche de surface (m²). */
	@Id
	@Column(name="surface_max",length=5)
	private Integer surfaceMax;

	/**  population avec parc.*/
	@Column(name="pop_inc", precision = 16, scale = 4)
	private BigDecimal populationInclue;
	
	/**  population sans parc.*/
	@Column(name="pop_exc", precision = 16, scale = 4)
	private BigDecimal populationExclue;

	/**  niveau de seuil OMS.*/
	@Column(name="seuil")
	private StatsSeuilOmsEnum seuil;

	/**
	 * Compare deux entrées de statistiques de surface selon l'année, la borne min puis la borne max.
	 * @param other autre instance à comparer
	 * @return résultat de la comparaison
	 */
	@Override
	public int compareTo(StatsSurface other) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.annee, other.annee);
		builder.append(this.surfaceMin, other.surfaceMin);
		builder.append(this.surfaceMax, other.surfaceMax);
		return builder.toComparison();
	}
	
}