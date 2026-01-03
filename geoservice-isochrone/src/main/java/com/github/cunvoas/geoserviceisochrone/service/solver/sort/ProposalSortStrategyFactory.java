package com.github.cunvoas.geoserviceisochrone.service.solver.sort;

/**
 * Abstract factory to obtain a sorting strategy for park proposals.
 */
public final class ProposalSortStrategyFactory {

    private ProposalSortStrategyFactory() {}

    public enum Type {
        DEFICIT,
        PERSONA,
        ACCESSING_SURFACE
    }

    public static ProposalSortStrategy create(Type type) {
        switch (type) {
            case DEFICIT:
                return new DeficitProposalSortStrategy();
            case PERSONA:
                return new PersonaProposalSortStrategy();
            case ACCESSING_SURFACE:
                return new AccessingSurfaceSortStrategy();
                
            default:
                throw new IllegalArgumentException("Unknown strategy type: " + type);
        }
    }
}
