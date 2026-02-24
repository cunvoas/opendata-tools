package com.github.cunvoas.geoserviceisochrone.repo.proposal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.ProposalComputationTypeAlgo;

/**
 * Spring JPA repository.
 */
@Repository
public interface ParkProposalRepository extends JpaRepository<ParkProposal, InseeCarre200mComputedId>{

//	/** 
//	 * findParkProposalInMapArea.
//	 * @param mapArea shape in string
//	 * @return list of ParkArea
//	 */
//	@Query(nativeQuery = true, 
//			   value = "SELECT pp.* FROM park_proposal pp WHERE annee=?1 and id_meta=?2  and ST_Intersects(pp.centre, ?3)")
//	public List<ParkProposal> findParkProposalInMapArea(Integer annee, Long meta, String sPolygon);
	

	/** 
	 * findParkProposalInMapArea.
	 * @param mapArea shape in string
	 * @return list of ParkArea
	 */
	public List<ParkProposal> findParkProposalByIdMeta(Long meta);
	
	/** 
	 * findParkProposalInMapArea.
	 * @param mapArea shape in string
	 * @return list of ParkArea
	 */
	@Query(nativeQuery = true, 
			   value = "SELECT pp.* FROM park_proposal pp WHERE annee=?1 and ST_Intersects(pp.centre, ?2) and id_meta in (SELECT pm.id from park_proposal_meta WHERE pa.annne=?1 AND pa.type_algo=?3)")
	public List<ParkProposal> findParkProposalInMapArea(Integer annee, String sPolygon, ProposalComputationTypeAlgo meta );
	
}
