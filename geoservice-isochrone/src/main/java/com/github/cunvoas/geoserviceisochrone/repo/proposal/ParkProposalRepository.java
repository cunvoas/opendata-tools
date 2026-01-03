package com.github.cunvoas.geoserviceisochrone.repo.proposal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;

/**
 * Spring JPA repository.
 */
@Repository
public interface ParkProposalRepository extends JpaRepository<ParkProposal, InseeCarre200mComputedId>{

	/** 
	 * findParkProposalInMapArea.
	 * @param mapArea shape in string
	 * @return list of ParkArea
	 */
	@Query(nativeQuery = true, 
			   value = "SELECT pp.* FROM park_proposal pp WHERE annee=?1 and ST_Intersects(pp.centre, ?2)")
	public List<ParkProposal> findParkProposalInMapArea(Integer annee, String sPolygon);
}
