package com.github.cunvoas.geoserviceisochrone.model.proposal.manual;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"id"})
@Entity(name = "manual_park_proposal_meta")
public class ManualParkProposalMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer annee;

    private String insee;

    private String typeAlgo = "MANUAL";

    private Integer numberOfParks = 0;

    private Integer totalSurfaceOfParks = 0;

    @OneToMany(mappedBy = "idMeta", fetch = FetchType.LAZY)
    private List<ManualParkProposal> proposals;
}
