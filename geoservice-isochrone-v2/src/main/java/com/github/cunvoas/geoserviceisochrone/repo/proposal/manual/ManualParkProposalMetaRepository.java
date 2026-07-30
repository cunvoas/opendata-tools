package com.github.cunvoas.geoserviceisochrone.repo.proposal.manual;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualParkProposalMeta;

@Repository
public interface ManualParkProposalMetaRepository extends JpaRepository<ManualParkProposalMeta, Long> {

    ManualParkProposalMeta findByAnneeAndInsee(Integer annee, String insee);

    ManualParkProposalMeta findByInsee(String insee);
}
