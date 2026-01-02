package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.math.BigDecimal;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"annee", "idInspire"})
@Entity(name = "project_simul_work")
@IdClass(InseeCarre200mComputedId.class)
public class ProjectSimulatorWork {

	/**
	 * Année de la donnée.
	 */
	@Id
	@Column(name="annee",length=4)
    private Integer annee;
	
	@Id
	@Column(name="idInspire",length=30)
	private String idInspire;
	
	@Column(name="project_simulator_id")
	private Long idProjectSimulator;
	
	
	/**
	 * ( Seuil OMS – MAX (0, surface disponible  - seuil OMS) ) * Nb Habitant qui ont accès
	 */
	@Column(name="miss_surf", precision = 12, scale = 2)
	private BigDecimal missingSurface;
	@Column(name="surf_per_capita", precision = 12, scale = 2)
	private BigDecimal surfacePerCapita;

	@Column(name="access_pop", precision = 12, scale = 2)
	private BigDecimal accessingPopulation;
	@Column(name="local_pop", precision = 12, scale = 2)
	private BigDecimal localPopulation;
	
	@Column(name="access_surf", precision = 12, scale = 2)
	private BigDecimal accessingSurface;
	
	@Column(name="dense")
	private Boolean isDense;

	@Column(name="centre")
	private Point centre;

	@Column(name="geo_shape", columnDefinition = "geometry(Polygon,4326)")
	private Polygon geoShape;
	
	@Column(name="new_surf", precision = 12, scale = 2)
	private BigDecimal newSurface;
	@Column(name="new_surf_per_capita", precision = 12, scale = 2)
	private BigDecimal newSurfacePerCapita;
	@Column(name="new_miss_surf", precision = 12, scale = 2)
	private BigDecimal newMissingSurface;
	
}