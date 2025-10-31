package com.github.cunvoas.geoserviceisochrone.model.analytics;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.CompareToBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity(name = "z_stats_surface")
@IdClass(StatsSurfaceId.class)
@EqualsAndHashCode(of = {"annee", "surfaceMin", "surfaceMax"})
public class StatsSurface implements Comparable<StatsSurface> {

	@Id
	@Column(name="annee",length=4)
	private Integer annee;
	@Id
	@Column(name="surface_min",length=5)
	private Integer surfaceMin;
	@Id
	@Column(name="surface_max",length=5)
	private Integer surfaceMax;

	
	/**  population avec parc.*/
	@Column(name="pop_inc", precision = 16, scale = 4)
	private BigDecimal populationInclue;
	
	/**  population sans parc.*/
	@Column(name="pop_exc", precision = 16, scale = 4)
	private BigDecimal populationExclue;
	
	@Override
	public int compareTo(StatsSurface other) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.annee, other.annee);
		builder.append(this.surfaceMin, other.surfaceMin);
		builder.append(this.surfaceMax, other.surfaceMax);
		return builder.toComparison();
	}
	
}
