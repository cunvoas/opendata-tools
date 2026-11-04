package com.github.cunvoas.geoserviceisochrone.repo.admin;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.admin.NdviJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputedId;

/**
 * File d'attente persistante des rafraîchissements NDVI.
 */
@Repository
public interface NdviJobRepository extends JpaRepository<NdviJob, ParkAreaComputedId> {

	/**
	 * Jobs à traiter, dans l'ordre de demande.
	 * @param status statut recherché
	 * @param page pagination
	 * @return liste de jobs
	 */
	List<NdviJob> findByStatusOrderByDemandAsc(ComputeJobStatusEnum status, Pageable page);

	/**
	 * Jobs restés {@code IN_PROCESS} avant un crash ou redémarrage.
	 * @param processed date limite
	 * @return liste de jobs à rejouer
	 */
	@Query(nativeQuery = true,
			value = "select * from public.ndvi_job where status=1 and processed <= :processed")
	List<NdviJob> findOnStartUnfinishedProcessed(@Param("processed") Date processed);

	/**
	 * Jobs en erreur depuis un moment.
	 * @param processed date limite
	 * @return liste de jobs à rejouer
	 */
	@Query(nativeQuery = true,
			value = "select * from public.ndvi_job where status=3 and processed <= :processed")
	List<NdviJob> findOnErrorAndProcessed(@Param("processed") Date processed);

	/**
	 * Compte les jobs NDVI dans un statut donné.
	 * @param status statut recherché
	 * @return nombre de jobs
	 */
	Long countByStatus(ComputeJobStatusEnum status);
}
