package com.github.cunvoas.geoserviceisochrone.service.solver.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

/**
 * Sorts proposals by surface per capita ascending.
 */
public class AccessingSurfaceSortStrategy implements ProposalSortStrategy {
    @Override
    public List<ParkProposalWork> sort(Map<String, ParkProposalWork> carreMap) {
        List<ParkProposalWork> proposals = new ArrayList<>(carreMap.values());
        proposals.sort((p1, p2) -> {
            Double accs1 = p1.getAccessingSurface() != null ? p1.getAccessingSurface().doubleValue() : 0;
            Double accs2 = p2.getAccessingSurface() != null ? p2.getAccessingSurface().doubleValue() : 0;
            // croissant
            return Double.compare(accs1, accs2);
        });
        return proposals;
    }
}
