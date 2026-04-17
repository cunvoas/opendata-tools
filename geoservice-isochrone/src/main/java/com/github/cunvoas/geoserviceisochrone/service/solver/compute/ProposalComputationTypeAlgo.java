package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

public enum ProposalComputationTypeAlgo {
    ITERATIVE_1("Itératif par déficit v1"),
    ITERATIVE_2("Itératif par déficit v2"),
    ITERATIVE_3("Itératif par population v2"),
    PPC_1("Solveur PPC v1"),
    PPC_2("Solveur PPC v2"),
    PPC_3("Solveur PPC v3"),
    CHI2_5("χ² avec voisins v1"),
    CHI2_6("χ² avec voisins v2"),
    GENETIC_7("Genetique v1");

    private String displayName;

    ProposalComputationTypeAlgo(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
