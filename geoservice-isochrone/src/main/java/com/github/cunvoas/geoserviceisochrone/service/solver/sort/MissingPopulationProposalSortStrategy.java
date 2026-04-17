package com.github.cunvoas.geoserviceisochrone.service.solver.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

/**
 * Trie les propositions par impact humain total decroissant.
 *
 * <p>Le critere de priorite est : <strong>manque de surface × population</strong>
 * = {@code newMissingSurface * accessingPopulation} (en m² × habitants).</p>
 *
 * <p>Ce critere privilegie les carreaux ou le deficit est a la fois important
 * en surface et concerne beaucoup d'habitants, plutot que de traiter uniquement
 * le deficit surfacique (comme {@link DeficitProposalSortStrategy}) ou la densite
 * par habitant (comme {@link PersonaProposalSortStrategy}).</p>
 */
public class MissingPopulationProposalSortStrategy implements ProposalSortStrategy {

    @Override
    public List<ParkProposalWork> sort(Map<String, ParkProposalWork> carreMap) {
        List<ParkProposalWork> proposals = new ArrayList<>(carreMap.values());
        proposals.sort((p1, p2) -> {
            double missing1 = p1.getNewMissingSurface() != null ? p1.getNewMissingSurface().doubleValue() : 0d;
            double pop1     = p1.getAccessingPopulation() != null ? p1.getAccessingPopulation().doubleValue() : 0d;

            double missing2 = p2.getNewMissingSurface() != null ? p2.getNewMissingSurface().doubleValue() : 0d;
            double pop2     = p2.getAccessingPopulation() != null ? p2.getAccessingPopulation().doubleValue() : 0d;

            // tri decroissant : le plus grand impact humain en premier
            return Double.compare(missing2 * pop2, missing1 * pop1);
        });
        return proposals;
    }
}
