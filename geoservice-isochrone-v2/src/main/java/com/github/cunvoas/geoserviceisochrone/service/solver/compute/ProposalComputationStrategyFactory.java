package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.util.List;

/**
 * Abstract factory to create computation strategy (iterative or solver).
 */
public final class ProposalComputationStrategyFactory {

    private ProposalComputationStrategyFactory() {}

    
    public static List<ProposalComputationTypeAlgo> getAvailableTypes() {
        return List.of(
//            ProposalComputationTypeAlgo.ITERATIVE_1,
            ProposalComputationTypeAlgo.ITERATIVE_2,
            ProposalComputationTypeAlgo.ITERATIVE_3,
//            ProposalComputationTypeAlgo.PPC_1,
//            ProposalComputationTypeAlgo.PPC_2,
            ProposalComputationTypeAlgo.PPC_3,
//            ProposalComputationTypeAlgo.CHI2_5,
            ProposalComputationTypeAlgo.CHI2_6,
            ProposalComputationTypeAlgo.GENETIC_7
        );
    }
    
    public static ProposalComputationStrategy create(ProposalComputationTypeAlgo type, double minParkSurface) {
        switch (type) {
            case ITERATIVE_1:
                return new IterativeComputationDeficit1Strategy(minParkSurface);
            case ITERATIVE_2:
                return new IterativeComputationDeficit2Strategy(minParkSurface);
            case ITERATIVE_3:
                return new IterativeComputationPopulation1Strategy(minParkSurface);
             case PPC_1:
                return new Solver1ComputationStrategy();
            case PPC_2:
                return new Solver2ComputationStrategy();
            case PPC_3:
                return new Solver3ComputationStrategy();
            case CHI2_5:
                return new LeastSquaresNeigbour1Strategy();
            case CHI2_6:
                return new LeastSquaresNeighbour2Strategy();
            case GENETIC_7:
                return new Genetic1Strategy(minParkSurface);
             default:
                throw new IllegalArgumentException("Unknown computation strategy type: " + type);
        }
    }
}
