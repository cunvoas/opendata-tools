package com.github.cunvoas.geoserviceisochrone.repo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.NdviResult;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.NdviResultObjectType;

/**
 * Mesures de végétation Copernicus.
 */
@Repository
public interface NdviResultRepository extends JpaRepository<NdviResult, Long> {

	/**
	 * Projection des mesures NDVI d'un parc pour une année de calcul.
	 */
	interface NdviSummaryProjection {
		Long getParkId();
		String getName();
		BigDecimal getSurface();
		BigDecimal getNdviMean();
		BigDecimal getNdviMin();
		BigDecimal getNdviMax();
		BigDecimal getNdviStddev();
		BigDecimal getNdreMean();
		BigDecimal getNdreMin();
		BigDecimal getNdreMax();
		BigDecimal getNdreStddev();
		BigDecimal getGndviMean();
		BigDecimal getGndviMin();
		BigDecimal getGndviMax();
		BigDecimal getGndviStddev();
		Date getAcquisitionDate();
		Integer getAnnee();
	}

	/**
	 * Indique si une mesure existe déjà pour une zone de parc.
	 * @param objectType type d'objet
	 * @param parkAreaComputedId identifiant de la zone
	 * @return vrai si au moins une mesure est présente
	 */
	boolean existsByObjectTypeAndParkAreaComputedId(NdviResultObjectType objectType,
			Long parkAreaComputedId);

	/**
	 * Dernière mesure d'un parc, pour l'historique ou les contrôles.
	 * @param objectType type d'objet
	 * @param parkAreaComputedId identifiant de la zone
	 * @return mesures triées de la plus ancienne à la plus récente
	 */
	List<NdviResult> findByObjectTypeAndParkAreaComputedIdOrderByPeriodEndAsc(
			NdviResultObjectType objectType, Long parkAreaComputedId);

	/**
	 * Mesures NDVI des parcs d'une année, dernière période connue par parc.
	 * @param annee année de référence
	 * @return summaries
	 */
	@Query(nativeQuery = true,
			value = "SELECT * FROM ("
					+ " SELECT DISTINCT ON (pac.id) pac.id AS \"parkId\", pa.name AS \"name\","
					+ " pac.surface AS \"surface\","
					+ " m.ndvi_mean AS \"ndviMean\", m.ndvi_min AS \"ndviMin\","
					+ " m.ndvi_max AS \"ndviMax\", m.ndvi_stddev AS \"ndviStddev\","
					+ " m.ndre_mean AS \"ndreMean\", m.ndre_min AS \"ndreMin\","
					+ " m.ndre_max AS \"ndreMax\", m.ndre_stddev AS \"ndreStddev\","
					+ " m.gndvi_mean AS \"gndviMean\", m.gndvi_min AS \"gndviMin\","
					+ " m.gndvi_max AS \"gndviMax\", m.gndvi_stddev AS \"gndviStddev\","
					+ " m.period_end AS \"acquisitionDate\", pac.annee AS \"annee\""
					+ " FROM ndvi_result m"
					+ " JOIN park_area_computed pac ON pac.id = m.park_area_computed_id"
					+ " JOIN park_area pa ON pa.id = pac.id"
					+ " WHERE m.object_type = 'PARK_AREA_COMPUTED'"
					+ "   AND pac.annee = :annee AND m.ndvi_mean IS NOT NULL"
					+ " ORDER BY pac.id, m.period_end DESC NULLS LAST, m.id DESC"
					+ ") latest ORDER BY \"ndviMean\" DESC")
	List<NdviSummaryProjection> findNdviSummariesByAnnee(@Param("annee") Integer annee);

	/**
	 * Mesures NDVI des parcs d'une année pour un EPCI, dernière période connue par parc.
	 * @param annee année de référence
	 * @param com2coId identifiant de la communauté de communes
	 * @return summaries
	 */
	@Query(nativeQuery = true,
			value = "SELECT * FROM ("
					+ " SELECT DISTINCT ON (pac.id) pac.id AS \"parkId\", pa.name AS \"name\","
					+ " pac.surface AS \"surface\","
					+ " m.ndvi_mean AS \"ndviMean\", m.ndvi_min AS \"ndviMin\","
					+ " m.ndvi_max AS \"ndviMax\", m.ndvi_stddev AS \"ndviStddev\","
					+ " m.ndre_mean AS \"ndreMean\", m.ndre_min AS \"ndreMin\","
					+ " m.ndre_max AS \"ndreMax\", m.ndre_stddev AS \"ndreStddev\","
					+ " m.gndvi_mean AS \"gndviMean\", m.gndvi_min AS \"gndviMin\","
					+ " m.gndvi_max AS \"gndviMax\", m.gndvi_stddev AS \"gndviStddev\","
					+ " m.period_end AS \"acquisitionDate\", pac.annee AS \"annee\""
					+ " FROM ndvi_result m"
					+ " JOIN park_area_computed pac ON pac.id = m.park_area_computed_id"
					+ " JOIN park_area pa ON pa.id = pac.id"
					+ " JOIN parc_jardin pj ON pj.identifiant = pa.id_parc_jardin"
					+ " JOIN city ci ON ci.id = pj.id_city"
					+ " WHERE m.object_type = 'PARK_AREA_COMPUTED'"
					+ "   AND pac.annee = :annee AND ci.id_comm2co = :com2coId"
					+ "   AND m.ndvi_mean IS NOT NULL"
					+ " ORDER BY pac.id, m.period_end DESC NULLS LAST, m.id DESC"
					+ ") latest ORDER BY \"ndviMean\" DESC")
	List<NdviSummaryProjection> findNdviSummariesByAnneeAndCom2co(@Param("annee") Integer annee,
			@Param("com2coId") Long com2coId);

	/**
	 * Mesures NDVI des parcs d'une année pour une commune, dernière période connue par parc.
	 * @param annee année de référence
	 * @param inseeCode code INSEE de la commune
	 * @return summaries
	 */
	@Query(nativeQuery = true,
			value = "SELECT * FROM ("
					+ " SELECT DISTINCT ON (pac.id) pac.id AS \"parkId\", pa.name AS \"name\","
					+ " pac.surface AS \"surface\","
					+ " m.ndvi_mean AS \"ndviMean\", m.ndvi_min AS \"ndviMin\","
					+ " m.ndvi_max AS \"ndviMax\", m.ndvi_stddev AS \"ndviStddev\","
					+ " m.ndre_mean AS \"ndreMean\", m.ndre_min AS \"ndreMin\","
					+ " m.ndre_max AS \"ndreMax\", m.ndre_stddev AS \"ndreStddev\","
					+ " m.gndvi_mean AS \"gndviMean\", m.gndvi_min AS \"gndviMin\","
					+ " m.gndvi_max AS \"gndviMax\", m.gndvi_stddev AS \"gndviStddev\","
					+ " m.period_end AS \"acquisitionDate\", pac.annee AS \"annee\""
					+ " FROM ndvi_result m"
					+ " JOIN park_area_computed pac ON pac.id = m.park_area_computed_id"
					+ " JOIN park_area pa ON pa.id = pac.id"
					+ " JOIN parc_jardin pj ON pj.identifiant = pa.id_parc_jardin"
					+ " JOIN city ci ON ci.id = pj.id_city"
					+ " WHERE m.object_type = 'PARK_AREA_COMPUTED'"
					+ "   AND pac.annee = :annee AND ci.insee_code = :inseeCode"
					+ "   AND m.ndvi_mean IS NOT NULL"
					+ " ORDER BY pac.id, m.period_end DESC NULLS LAST, m.id DESC"
					+ ") latest ORDER BY \"ndviMean\" DESC")
	List<NdviSummaryProjection> findNdviSummariesByAnneeAndCity(@Param("annee") Integer annee,
			@Param("inseeCode") String inseeCode);
}
