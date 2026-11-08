package com.github.cunvoas.geoserviceisochrone.repo.admin;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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

	/**
	 * Statistiques d'avancement NDVI agrégées au niveau ville.
	 * @param idCommune filtre commune, facultatif
	 * @param idEpci filtre EPCI, facultatif
	 * @param idRegion filtre région, facultatif
	 * @param type filtre type NDVI (PARC, IRIS, CADASTRE, CARRE), facultatif
	 * @return lignes brutes epciName, cityName, type, toProcess, inProcess, processed, inError
	 */
	@Query(nativeQuery = true,
			value = "SELECT epci.name AS epciName, c.name AS cityName,"
					+ " COALESCE(rt.ndvi_type, 'PARC') AS objectType,"
					+ " COUNT(CASE WHEN j.status = 0 THEN 1 END) AS toProcess,"
					+ " COUNT(CASE WHEN j.status = 1 THEN 1 END) AS inProcess,"
					+ " COUNT(CASE WHEN j.status = 2 THEN 1 END) AS processed,"
					+ " COUNT(CASE WHEN j.status = 3 THEN 1 END) AS inError"
					+ " FROM public.ndvi_job j"
					+ " JOIN public.park_area pa ON pa.id = j.id"
					+ " JOIN public.parc_jardin pj ON pj.identifiant = pa.id_parc_jardin"
					+ " JOIN public.city c ON c.id = pj.id_city"
					+ " LEFT JOIN public.adm_com2commune epci ON c.id_comm2co = epci.id"
					+ " LEFT JOIN ("
					+ "   SELECT park_area_computed_id,"
					+ "          MIN(CASE WHEN object_type IN ('PARK_AREA_COMPUTED', 'PARC_ET_JARDIN') THEN 'PARC'"
					+ "                   WHEN object_type IN ('CARRE_200M', 'CARRE') THEN 'CARRE'"
					+ "                   ELSE object_type END) AS ndvi_type"
					+ "   FROM public.ndvi_result"
					+ "   WHERE park_area_computed_id IS NOT NULL"
					+ "   GROUP BY park_area_computed_id"
					+ " ) rt ON rt.park_area_computed_id = j.id"
					+ " WHERE (:idCommune IS NULL OR c.id = :idCommune)"
					+ "   AND (:idEpci IS NULL OR c.id_comm2co = :idEpci)"
					+ "   AND (:idRegion IS NULL OR epci.id_region = :idRegion)"
					+ "   AND (:type IS NULL OR COALESCE(rt.ndvi_type, 'PARC') = :type)"
					+ " GROUP BY epci.name, c.name, COALESCE(rt.ndvi_type, 'PARC')"
					+ " ORDER BY epci.name, c.name, COALESCE(rt.ndvi_type, 'PARC')")
	List<Object[]> getProgressStatsCityLevel(@Param("idCommune") Long idCommune,
			@Param("idEpci") Long idEpci,
			@Param("idRegion") Long idRegion,
			@Param("type") String type);

	/**
	 * Statistiques d'avancement NDVI agrégées au niveau EPCI.
	 * @param idEpci filtre EPCI, facultatif
	 * @param idRegion filtre région, facultatif
	 * @param type filtre type NDVI (PARC, IRIS, CADASTRE, CARRE), facultatif
	 * @return lignes brutes epciName, cityName null, type, toProcess, inProcess, processed, inError
	 */
	@Query(nativeQuery = true,
			value = "SELECT epci.name AS epciName, NULL AS cityName,"
					+ " COALESCE(rt.ndvi_type, 'PARC') AS objectType,"
					+ " COUNT(CASE WHEN j.status = 0 THEN 1 END) AS toProcess,"
					+ " COUNT(CASE WHEN j.status = 1 THEN 1 END) AS inProcess,"
					+ " COUNT(CASE WHEN j.status = 2 THEN 1 END) AS processed,"
					+ " COUNT(CASE WHEN j.status = 3 THEN 1 END) AS inError"
					+ " FROM public.ndvi_job j"
					+ " JOIN public.park_area pa ON pa.id = j.id"
					+ " JOIN public.parc_jardin pj ON pj.identifiant = pa.id_parc_jardin"
					+ " JOIN public.city c ON c.id = pj.id_city"
					+ " LEFT JOIN public.adm_com2commune epci ON c.id_comm2co = epci.id"
					+ " LEFT JOIN ("
					+ "   SELECT park_area_computed_id,"
					+ "          MIN(CASE WHEN object_type IN ('PARK_AREA_COMPUTED', 'PARC_ET_JARDIN') THEN 'PARC'"
					+ "                   WHEN object_type IN ('CARRE_200M', 'CARRE') THEN 'CARRE'"
					+ "                   ELSE object_type END) AS ndvi_type"
					+ "   FROM public.ndvi_result"
					+ "   WHERE park_area_computed_id IS NOT NULL"
					+ "   GROUP BY park_area_computed_id"
					+ " ) rt ON rt.park_area_computed_id = j.id"
					+ " WHERE (:idEpci IS NULL OR epci.id = :idEpci)"
					+ "   AND (:idRegion IS NULL OR epci.id_region = :idRegion)"
					+ "   AND (:type IS NULL OR COALESCE(rt.ndvi_type, 'PARC') = :type)"
					+ " GROUP BY epci.name, COALESCE(rt.ndvi_type, 'PARC')"
					+ " ORDER BY epci.name, COALESCE(rt.ndvi_type, 'PARC')")
	List<Object[]> getProgressStatsEpciLevel(@Param("idEpci") Long idEpci,
			@Param("idRegion") Long idRegion,
			@Param("type") String type);

	/**
	 * Dernier traitement connu pour un parc, toutes années confondues.
	 * @param id identifiant de la zone de parc
	 * @return dernier job traité
	 */
	Optional<NdviJob> findFirstByIdAndProcessedIsNotNullOrderByProcessedDesc(Long id);
}
