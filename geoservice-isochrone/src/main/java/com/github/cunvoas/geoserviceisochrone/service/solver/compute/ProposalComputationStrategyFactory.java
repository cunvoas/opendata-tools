package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.util.List;

/**
 * Abstract factory to create computation strategy (iterative or solver).
 */
public final class ProposalComputationStrategyFactory {

    private ProposalComputationStrategyFactory() {}

    public enum Type {
        ITERATIVE_1("Itératif par déficit"),
        SOLVER_1("Solveur 1"),
        SOLVER_2("Solveur 2"),
        SOLVER_3("Solveur 3");
    	
    	private String displayName;

    	Type(String displayName) {
    		this.displayName = displayName;
    	}

    	public String getDisplayName() {
    		return displayName;
    	}
    }
	
    
    public static List<ProposalComputationStrategyFactory.Type> getAvailableTypes() {
    	return List.of(
			ProposalComputationStrategyFactory.Type.ITERATIVE_1,
			ProposalComputationStrategyFactory.Type.SOLVER_1,
			ProposalComputationStrategyFactory.Type.SOLVER_2,
			ProposalComputationStrategyFactory.Type.SOLVER_3
    	);
    }
    
    public static ProposalComputationStrategy create(Type type, double minParkSurface) {
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
