package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

public enum ProposalComputationTypeAlgo {
    ITERATIVE_1("Itératif par déficit"),
    SOLVER_1("Solveur 1"),
    SOLVER_2("Solveur 2"),
    SOLVER_3("Solveur 3");

    private String displayName;

    ProposalComputationTypeAlgo(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
