package com.github.cunvoas.geoserviceisochrone.repo.proposal.manual;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualEvalWork;

@Repository
public interface ManualEvalWorkRepository extends JpaRepository<ManualEvalWork, Long> {

    List<ManualEvalWork> findByIdMetaOrderByIdInspire(Long idMeta);

    void deleteByIdMeta(Long idMeta);
}
