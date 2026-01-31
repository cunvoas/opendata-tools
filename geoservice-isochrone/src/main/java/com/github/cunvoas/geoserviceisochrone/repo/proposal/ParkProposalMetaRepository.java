package com.github.cunvoas.geoserviceisochrone.repo.proposal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalMeta;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.ProposalComputationStrategyFactory;

/**
 * Spring JPA repository.
 */
@Repository 
public interface ParkProposalMetaRepository extends JpaRepository<ParkProposalMeta, Long>{

	public ParkProposalMeta findByAnneeAndInseeAndTypeAlgo(Integer annee, String insee, ProposalComputationStrategyFactory.TypeAlgo typeAlgo);

	public List<ParkProposalMeta> findByAnneeAndInsee(Integer annee, String insee);

	public List<ParkProposalMeta> findByInsee(String insee);
	
}
