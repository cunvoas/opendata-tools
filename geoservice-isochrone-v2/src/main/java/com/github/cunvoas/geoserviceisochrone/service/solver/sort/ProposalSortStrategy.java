package com.github.cunvoas.geoserviceisochrone.service.solver.sort;

import java.util.List;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

/**
 * Strategy interface to sort park proposals.
 */
public interface ProposalSortStrategy {
    List<ParkProposalWork> sort(Map<String, ParkProposalWork> carreMap);
}
