package com.github.cunvoas.geoserviceisochrone.repo.proposal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWorkId;
import com.github.cunvoas.geoserviceisochrone.service.solver.helper.ProposalComputationTypeAlgo;

/**
 * Spring JPA repository.
 */
@Repository
public interface ParkProposalWorkRepository extends JpaRepository<ParkProposalWork, ParkProposalWorkId>{

	@Query(nativeQuery = true, value = "SELECT pw.* FROM park_proposal_work pw "
			+ "JOIN carre200onlyshape c ON pw.id_inspire = c.id_inspire "
			+ "WHERE pw.annee = ?1 AND c.code_insee = ?2 AND pw.type_algo = ?3")
	List<ParkProposalWork> findByAnneeAndCodeInseeAndTypeAlgo(Integer annee, String codeInsee, String typeAlgo);

}
