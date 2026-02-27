package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.util.List;

import com.github.cunvoas.geoserviceisochrone.service.solver.compute.ProposalComputationTypeAlgo;

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
@Entity(name = "park_proposal_meta")
public class ParkProposalMeta {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	// id tech
	private Long id;
	
	//id business
    private Integer annee;
    private String insee;
    private ProposalComputationTypeAlgo typeAlgo;
    
    //data
    private Integer numberOfParks = 0;
    private Integer totalSurfaceOfParks = 0;
    
    @OneToMany(mappedBy = "idMeta", fetch = FetchType.LAZY)
    private List<ParkProposal> proposals;
    
}
