package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.service.solver.ServicePropositionParc;

/**
 * Global computation using Choco solver.
 * Updates the provided map in place; returns an empty list
 * since propositions are applied directly on the map.
 */
public class SolverComputationStrategy implements ProposalComputationStrategy {

    private final ServicePropositionParc service;

    public SolverComputationStrategy(ServicePropositionParc service) {
        this.service = service;
    }

    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
                                      Double minSquareMeterPerCapita,
                                      Double recoSquareMeterPerCapita,
                                      Integer urbanDistance) {
        service.calculePropositionSolver(carreMap, recoSquareMeterPerCapita, urbanDistance);
        return Collections.emptyList();
    }
}
