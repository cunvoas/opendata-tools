package com.github.cunvoas.geoserviceisochrone.model.admin;

import java.util.Date;

import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Model ComputeIrisJob.
 */
@Data
@EqualsAndHashCode(of = {"annee", "iris"})
@Entity(name = "compute_iris_job")
@IdClass(IrisId.class)
public class ComputeIrisJob {

	/**
	 * Année de la donnée.
	 */
	@Id
	@Column(name="annee",length=4)
    private Integer annee;
	
	@Id
	@Column(name="iris",length=30)
	private String iris;
	
	@Column(name="demand")
	private Date demand = new Date();

	@Column(name="processed")
	private Date processed;

	@Column(name="status")
	private ComputeJobStatusEnum status = ComputeJobStatusEnum.TO_PROCESS;

	@Column(name="insee",length=5)
	private String codeInsee;
}
