package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import com.github.cunvoas.geoserviceisochrone.service.solver.ServicePropositionParc;

/**
 * Abstract factory to create computation strategy (iterative or solver).
 */
public final class ProposalComputationStrategyFactory {

    private ProposalComputationStrategyFactory() {}

    public enum Type {
        ITERATIVE,
        SOLVER
    }

    public static ProposalComputationStrategy create(Type type, ServicePropositionParc service,
                                                     double minParkSurface) {
        switch (type) {
            case ITERATIVE:
                return new IterativeComputationStrategy(service, minParkSurface);
            case SOLVER:
                return new SolverComputationStrategy(service);
            default:
                throw new IllegalArgumentException("Unknown computation strategy type: " + type);
        }
    }
}
