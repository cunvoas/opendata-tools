package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

public enum ProposalComputationTypeAlgo {
    ITERATIVE_1("Itératif par déficit"),
    PPC_1("Solveur PPC 1"),
    PPC_2("Solveur PPC 2"),
    PPC_3("Solveur PPC 3"),
    CHI2_4("χ²");

    private String displayName;

    ProposalComputationTypeAlgo(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
