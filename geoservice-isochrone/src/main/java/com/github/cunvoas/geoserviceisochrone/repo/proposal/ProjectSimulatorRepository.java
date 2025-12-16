package com.github.cunvoas.geoserviceisochrone.repo.proposal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;

/**
 * Repo for ProjectSimulator.
 */
public interface ProjectSimulatorRepository extends JpaRepository<ProjectSimulator, Long> {

	@Query("SELECT p FROM project_simul p WHERE p.idCommune = :idCommune ORDER BY p.annee DESC, p.id DESC")
	List<ProjectSimulator> findByIdCommune(@Param("idCommune") Long idCommune);

}
