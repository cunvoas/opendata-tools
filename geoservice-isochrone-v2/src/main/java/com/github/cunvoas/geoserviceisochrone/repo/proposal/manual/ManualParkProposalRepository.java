package com.github.cunvoas.geoserviceisochrone.repo.proposal.manual;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualParkProposal;

@Repository
public interface ManualParkProposalRepository extends JpaRepository<ManualParkProposal, Long> {

    List<ManualParkProposal> findByIdMetaOrderByCreatedDateDesc(Long idMeta);

    List<ManualParkProposal> findByContourNotNull();

    List<ManualParkProposal> findByContourIsNull();
}
