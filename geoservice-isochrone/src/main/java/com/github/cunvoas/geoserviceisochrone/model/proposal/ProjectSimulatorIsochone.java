package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.util.Date;

import org.apache.commons.lang3.builder.CompareToBuilder;
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
public class ProjectSimulatorIsochone implements Comparable<ProjectSimulatorIsochone> {


	@Id
	@Column(name="project_simulator_id")
	private Long idProjectSimulator;

	@Id
	private Point point;
	
	private Geometry isochrone;
	
	private Date updateDate;
	
	// only to easyly compute 
	private transient Boolean processed=false; 

	@Override
	public int compareTo(ProjectSimulatorIsochone other) {
		CompareToBuilder cbt=new CompareToBuilder();
		cbt.append(this.idProjectSimulator, other.idProjectSimulator);
		cbt.append(this.point, other.point);
		return cbt.toComparison();
	}
	
	
	
}