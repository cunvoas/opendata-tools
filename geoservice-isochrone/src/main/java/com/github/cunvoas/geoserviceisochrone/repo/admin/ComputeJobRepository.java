package com.github.cunvoas.geoserviceisochrone.repo.admin;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;

/**
 * Spring JPA repository.
 */
@Repository
public interface ComputeJobRepository extends JpaRepository<ComputeJob, InseeCarre200mComputedId>{

    /**
     * findByStatusOrderByDemandAsc.
     * @param status enum
     * @param page page
     * @return list ComputeJob
     */
    List<ComputeJob> findByStatusOrderByDemandAsc(ComputeJobStatusEnum status, Pageable page);
    
}
