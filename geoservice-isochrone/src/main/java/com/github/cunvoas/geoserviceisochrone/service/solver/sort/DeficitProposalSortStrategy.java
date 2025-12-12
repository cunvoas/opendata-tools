package com.github.cunvoas.geoserviceisochrone.service.solver.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

/**
 * Sorts proposals by missing surface deficit descending.
 */
public class DeficitProposalSortStrategy implements ProposalSortStrategy {
    @Override
    public List<ParkProposalWork> sort(Map<String, ParkProposalWork> carreMap) {
        List<ParkProposalWork> proposals = new ArrayList<>(carreMap.values());
        proposals.sort((p1, p2) -> {
            Double deficit1 = p1.getNewMissingSurface() != null ? p1.getNewMissingSurface().doubleValue() : 0;
            Double deficit2 = p2.getNewMissingSurface() != null ? p2.getNewMissingSurface().doubleValue() : 0;
            return Double.compare(deficit2, deficit1);
        });
        return proposals;
    }
}
