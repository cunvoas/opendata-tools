package com.github.cunvoas.geoserviceisochrone.model.admin;

import java.util.Date;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Model ComputeJob.
 */
@Data
@EqualsAndHashCode(of = {"annee", "idInspire"})
@Entity(name = "compute_job")
@IdClass(InseeCarre200mComputedId.class)
public class ComputeJob {

	/**
	 * Année de la donnée.
	 */
	@Id
	@Column(name="annee",length=4)
    private Integer annee;
	
	@Id
	@Column(name="idInspire",length=30)
	private String idInspire;
	
	@Column(name="demand")
	private Date demand = new Date();

	@Column(name="processed")
	private Date processed;

	@Column(name="status")
	private ComputeJobStatusEnum status = ComputeJobStatusEnum.TO_PROCESS;

	@Column(name="insee",length=5)
	private String codeInsee;
}
