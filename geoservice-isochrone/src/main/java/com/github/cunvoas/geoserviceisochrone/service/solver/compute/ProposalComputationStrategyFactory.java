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
            ProposalComputationTypeAlgo.PPC_1,
            ProposalComputationTypeAlgo.PPC_2,
            ProposalComputationTypeAlgo.PPC_3,
            ProposalComputationTypeAlgo.CHI2_4
        );
    }
    
    public static ProposalComputationStrategy create(ProposalComputationTypeAlgo type, double minParkSurface) {
        switch (type) {
            case ITERATIVE_1:
                return new IterativeComputationDeficitStrategy(minParkSurface);
            case PPC_1:
                return new Solver1ComputationStrategy();
            case PPC_2:
                return new Solver2ComputationStrategy();
            case PPC_3:
                return new Solver3ComputationStrategy();
            case CHI2_4:
                return new LeastSquaresStrategy();
            default:
                throw new IllegalArgumentException("Unknown computation strategy type: " + type);
        }
    }
}
