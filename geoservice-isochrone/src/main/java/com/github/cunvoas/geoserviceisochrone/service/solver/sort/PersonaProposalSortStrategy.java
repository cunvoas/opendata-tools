package com.github.cunvoas.geoserviceisochrone.service.solver.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

/**
 * Sorts proposals by surface per capita ascending.
 */
public class PersonaProposalSortStrategy implements ProposalSortStrategy {
    @Override
    public List<ParkProposalWork> sort(Map<String, ParkProposalWork> carreMap) {
        List<ParkProposalWork> proposals = new ArrayList<>(carreMap.values());
        proposals.sort((p1, p2) -> {
            Double v1 = p1.getNewSurfacePerCapita() != null ? p1.getNewSurfacePerCapita().doubleValue() : 0;
            Double v2 = p2.getNewSurfacePerCapita() != null ? p2.getNewSurfacePerCapita().doubleValue() : 0;
            return Double.compare(v1, v2);
        });
        return proposals;
    }
}
