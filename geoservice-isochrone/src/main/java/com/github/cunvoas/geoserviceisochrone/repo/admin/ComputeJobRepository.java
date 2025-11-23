package com.github.cunvoas.geoserviceisochrone.repo.admin;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    
    /**
     * findByStatusOrderByDemandDesc.
     * @param status enum
     * @param page page
     * @return list ComputeJob
     */
    List<ComputeJob> findByStatusOrderByDemandDesc(ComputeJobStatusEnum status, Pageable page);
    
    /**
     * findByStatusAndProcessed.
     * @param status enum
     * @param processed a date before
     * @return list ComputeJob
     */
    @Query(nativeQuery=true, 
    		value="select * from public.compute_job where status=1 and processed <= :processed")
    List<ComputeJob> findOnStartUnfinishedProcessed(@Param("processed")  Date processed);
    
    
    /**
     * findByStatusAndProcessed.
     * @param status enum
     * @param processed a date before
     * @return list ComputeJob
     */
    @Query(nativeQuery=true, 
    		value="select * from public.compute_job where status=3 and processed <= :processed")
    List<ComputeJob> findOnErrorAndProcessed(@Param("processed")  Date processed);
    
    @Query(nativeQuery=true, 
    		value="SELECT count(id_inspire), status FROM public.compute_job group by status order by  status" )
    List<Object[]> getGlobalStats();
    
    @Query(nativeQuery=true, 
    		value="SELECT count(id_inspire), status, insee FROM public.compute_job WHERE insee=:codeInsee group by status, insee order by status" )
    List<Object[]> getStatsByCodeInsee(@Param("codeInsee") String codeInsee);
    
    /**
     * countByStatus.
     * @param status enum
     * @return count
     */
    Long countByStatus(ComputeJobStatusEnum status);
}
