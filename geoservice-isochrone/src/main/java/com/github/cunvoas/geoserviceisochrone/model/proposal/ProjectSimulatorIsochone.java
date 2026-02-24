package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.util.Date;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"idProjectSimulator", "point"})
@Entity(name = "project_simul_isochrone")
@IdClass(ProjectSimulatorIsochroneId.class)
public class ProjectSimulatorIsochone {


	@Id
	@Column(name="project_simulator_id")
	private Long idProjectSimulator;

	@Id
	private Point point;
	
	private Geometry isochrone;
	
	private Date updateDate;
	
}