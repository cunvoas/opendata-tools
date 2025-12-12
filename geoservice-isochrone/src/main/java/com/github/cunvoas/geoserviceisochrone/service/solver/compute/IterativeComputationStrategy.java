package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.service.solver.ServicePropositionParc;

/**
 * Iterative computation using calculeEtapeProposition over all squares.
 */
public class IterativeComputationStrategy implements ProposalComputationStrategy {

    private final ServicePropositionParc service;
    private final double minParkSurface;

    public IterativeComputationStrategy(ServicePropositionParc service, double minParkSurface) {
        this.service = service;
        this.minParkSurface = minParkSurface;
    }

    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
                                      Double minSquareMeterPerCapita,
                                      Double recoSquareMeterPerCapita,
                                      Integer urbanDistance) {
        List<ParkProposal> proposals = new ArrayList<>();
        for (int i = 0; i < carreMap.size(); i++) {
            ParkProposal proposal = service.calculeEtapeProposition(minParkSurface, carreMap,
                    minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);
            if (proposal != null) {
                proposals.add(proposal);
            }
        }
        return proposals;
    }
}
