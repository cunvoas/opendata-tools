package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

/**
 * Abstract factory to create computation strategy (iterative or solver).
 */
public final class ProposalComputationStrategyFactory {

    private ProposalComputationStrategyFactory() {}

    public enum Type {
        ITERATIVE,
        SOLVER_1,
        SOLVER_2
    }

    public static ProposalComputationStrategy create(Type type, double minParkSurface) {
        switch (type) {
            case ITERATIVE:
                return new IterativeComputationStrategy(minParkSurface);
            case SOLVER_1:
                return new Solver1ComputationStrategy();
            case SOLVER_2:
                return new Solver2ComputationStrategy();
            default:
                throw new IllegalArgumentException("Unknown computation strategy type: " + type);
        }
    }
}
