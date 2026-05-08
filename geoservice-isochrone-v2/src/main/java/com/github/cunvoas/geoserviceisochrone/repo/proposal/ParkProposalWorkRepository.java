package com.github.cunvoas.geoserviceisochrone.repo.proposal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

/**
 * Spring JPA repository.
 */
@Repository
public interface ParkProposalWorkRepository extends JpaRepository<ParkProposalWork, InseeCarre200mComputedId>{

}
