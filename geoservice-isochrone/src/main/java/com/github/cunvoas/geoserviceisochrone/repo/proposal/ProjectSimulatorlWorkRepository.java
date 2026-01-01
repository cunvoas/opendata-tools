package com.github.cunvoas.geoserviceisochrone.repo.proposal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorWork;

/**
 * Spring JPA repository.
 */
@Repository
public interface ProjectSimulatorlWorkRepository extends JpaRepository<ProjectSimulatorWork, InseeCarre200mComputedId>{

}
