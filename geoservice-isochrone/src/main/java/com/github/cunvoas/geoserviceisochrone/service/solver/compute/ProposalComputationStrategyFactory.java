package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.util.List;

/**
 * Abstract factory to create computation strategy (iterative or solver).
 */
public final class ProposalComputationStrategyFactory {

    private ProposalComputationStrategyFactory() {}

    
    public static List<ProposalComputationTypeAlgo> getAvailableTypes() {
        return List.of(
            ProposalComputationTypeAlgo.ITERATIVE_1,
            ProposalComputationTypeAlgo.SOLVER_1,
            ProposalComputationTypeAlgo.SOLVER_2,
            ProposalComputationTypeAlgo.SOLVER_3
        );
    }
    
    public static ProposalComputationStrategy create(ProposalComputationTypeAlgo type, double minParkSurface) {
        switch (type) {
            case ITERATIVE_1:
                return new IterativeComputationDeficitStrategy(minParkSurface);
            case SOLVER_1:
                return new Solver1ComputationStrategy();
            case SOLVER_2:
                return new Solver2ComputationStrategy();
            case SOLVER_3:
                return new Solver3ComputationStrategy();
            default:
                throw new IllegalArgumentException("Unknown computation strategy type: " + type);
        }
    }
}
