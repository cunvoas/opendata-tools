package com.github.cunvoas.geoserviceisochrone.repo.admin;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeIrisJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;

/**
 * Spring JPA repository.
 */
@Repository
public interface ComputeJobIrisRepository extends JpaRepository<ComputeIrisJob, InseeCarre200mComputedId>{

    /**
     * findByStatusOrderByDemandAsc.
     * @param status enum
     * @param page page
     * @return list ComputeIrisJob
     */
    List<ComputeIrisJob> findByStatusOrderByDemandAsc(ComputeJobStatusEnum status, Pageable page);
    
    
    /**
     * findByStatusOrderByDemandDesc.
     * @param status enum
     * @param page page
     * @return list ComputeIrisJob
     */
    List<ComputeIrisJob> findByStatusOrderByDemandDesc(ComputeJobStatusEnum status, Pageable page);
    
    /**
     * findByStatusAndProcessed.
     * @param status enum
     * @param processed a date before
     * @return list ComputeIrisJob
     */
    @Query(nativeQuery=true, 
    		value="select * from public.compute_iris_job where status=3 and processed <= :processed")
    List<ComputeIrisJob> findOnErrorAndProcessed(@Param("processed")  Date processed);
    
    @Query(nativeQuery=true, 
    		value="SELECT count(iris), status FROM public.compute_iris_job group by status order by  status" )
    List<Object[]> getGlobalStats();
    
    @Query(nativeQuery=true, 
    		value="SELECT count(iris), status, insee FROM public.compute_iris_job WHERE insee=:codeInsee group by status, insee order by status" )
    List<Object[]> getStatsByCodeInsee(@Param("codeInsee") String codeInsee);
}
