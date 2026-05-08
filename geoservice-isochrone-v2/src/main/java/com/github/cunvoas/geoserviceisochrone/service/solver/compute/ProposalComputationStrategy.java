package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.util.List;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

/**
 * Strategy interface to compute park proposals.
 */
public interface ProposalComputationStrategy {
	
    /**
     * @param carreMap
     * @param minSquareMeterPerCapita
     * @param recoSquareMeterPerCapita
     * @param urbanDistance
     * @return
     */
    List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
                               Double minSquareMeterPerCapita,
                               Double recoSquareMeterPerCapita,
                               Integer urbanDistance);
    
    
    
}
