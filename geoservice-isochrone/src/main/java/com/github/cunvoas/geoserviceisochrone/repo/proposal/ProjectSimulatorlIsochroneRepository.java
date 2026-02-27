package com.github.cunvoas.geoserviceisochrone.repo.proposal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorIsochone;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorIsochroneId;

/**
 * Spring JPA repository.
 */
@Repository
public interface ProjectSimulatorlIsochroneRepository extends JpaRepository<ProjectSimulatorIsochone, ProjectSimulatorIsochroneId>{

	/**
	 * Trouve tous les travaux de simulation pour un projet de simulation donné.
	 * @param idProjectSimulator
	 * @return
	 */
	List<ProjectSimulatorIsochone> findByIdProjectSimulator(Long idProjectSimulator);
	
	/**
	 * @param idProjectSimulator
	 */
	void deleteByIdProjectSimulator(Long idProjectSimulator);
}
