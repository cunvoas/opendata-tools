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
    
    @Query(nativeQuery=true,
            value = "SELECT " +
                    "  epci.name as epciName, " +
                    "  c.name as cityName, " +
                    "  all_jobs.annee, " +
                    "  COUNT(CASE WHEN all_jobs.status = 0 THEN 1 END) as toProcess, " +
                    "  COUNT(CASE WHEN all_jobs.status = 1 THEN 1 END) as inProcess, " +
                    "  COUNT(CASE WHEN all_jobs.status = 2 THEN 1 END) as processed, " +
                    "  COUNT(CASE WHEN all_jobs.status = 3 THEN 1 END) as inError " +
                    "FROM ( " +
                    "  SELECT annee, status, insee FROM public.compute_job " +
                    "  UNION ALL " +
                    "  SELECT annee, status, insee FROM public.compute_iris_job " +
                    ") all_jobs " +
                    "JOIN public.city c ON all_jobs.insee = c.insee_code " +
                    "LEFT JOIN public.adm_com2commune epci ON c.id_comm2co = epci.id " +
                    "GROUP BY epci.name, c.name, all_jobs.annee " +
                    "ORDER BY epci.name, c.name, all_jobs.annee")
    List<Object[]> getGroupedProgressStats();

    /**
     * countByStatus.
     * @param status enum
     * @return count
     */
    Long countByStatus(ComputeJobStatusEnum status);
}
