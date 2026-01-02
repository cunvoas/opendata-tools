package com.github.cunvoas.geoserviceisochrone.repo.proposal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorWork;

/**
 * Spring JPA repository.
 */
@Repository
public interface ProjectSimulatorlWorkRepository extends JpaRepository<ProjectSimulatorWork, InseeCarre200mComputedId>{

	/**
	 * Trouve tous les travaux de simulation pour une année donnée.
	 * 
	 * @param annee Année de la simulation
	 * @return Liste des travaux pour cette année
	 */
	List<ProjectSimulatorWork> findByAnnee(Integer annee);
	
	/**
	 * Trouve tous les travaux de simulation pour un projet de simulation donné.
	 * @param idProjectSimulator
	 * @return
	 */
	List<ProjectSimulatorWork> findByIdProjectSimulator(Long idProjectSimulator);
	
}
