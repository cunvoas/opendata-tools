package com.github.cunvoas.geoserviceisochrone.repo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputedId;

/**
 * Spring JPA repository.
 */
@Repository
public interface ParkAreaComputedRepository extends JpaRepository<ParkAreaComputed, ParkAreaComputedId>{
	
	/**
	 * Projection des mesures NDVI d'un parc pour une année.
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
	 * Mesures NDVI des parcs d'une année, triées par NDVI moyen décroissant.
	 * @param annee année de référence
	 * @return summaries
	 */
	@Query(nativeQuery = true,
			value = "SELECT pac.id AS \"parkId\", pa.name AS \"name\", pac.surface AS \"surface\","
					+ " pac.ndvi_mean AS \"ndviMean\", pac.ndvi_min AS \"ndviMin\","
					+ " pac.ndvi_max AS \"ndviMax\", pac.ndvi_stddev AS \"ndviStddev\","
					+ " pac.ndre_mean AS \"ndreMean\", pac.ndre_min AS \"ndreMin\","
					+ " pac.ndre_max AS \"ndreMax\", pac.ndre_stddev AS \"ndreStddev\","
					+ " pac.gndvi_mean AS \"gndviMean\", pac.gndvi_min AS \"gndviMin\","
					+ " pac.gndvi_max AS \"gndviMax\", pac.gndvi_stddev AS \"gndviStddev\","
					+ " pac.ndvi_acquisition_date AS \"acquisitionDate\", pac.annee AS \"annee\""
					+ " FROM park_area_computed pac"
					+ " JOIN park_area pa ON pa.id = pac.id"
					+ " WHERE pac.annee = :annee AND pac.ndvi_mean IS NOT NULL"
					+ " ORDER BY pac.ndvi_mean DESC")
	List<NdviSummaryProjection> findNdviSummariesByAnnee(@Param("annee") Integer annee);

	/**
	 * Mesures NDVI des parcs d'une année pour un EPCI, triées par NDVI moyen décroissant.
	 * @param annee année de référence
	 * @param com2coId identifiant de la communauté de communes
	 * @return summaries
	 */
	@Query(nativeQuery = true,
			value = "SELECT pac.id AS \"parkId\", pa.name AS \"name\", pac.surface AS \"surface\","
					+ " pac.ndvi_mean AS \"ndviMean\", pac.ndvi_min AS \"ndviMin\","
					+ " pac.ndvi_max AS \"ndviMax\", pac.ndvi_stddev AS \"ndviStddev\","
					+ " pac.ndre_mean AS \"ndreMean\", pac.ndre_min AS \"ndreMin\","
					+ " pac.ndre_max AS \"ndreMax\", pac.ndre_stddev AS \"ndreStddev\","
					+ " pac.gndvi_mean AS \"gndviMean\", pac.gndvi_min AS \"gndviMin\","
					+ " pac.gndvi_max AS \"gndviMax\", pac.gndvi_stddev AS \"gndviStddev\","
					+ " pac.ndvi_acquisition_date AS \"acquisitionDate\", pac.annee AS \"annee\""
					+ " FROM park_area_computed pac"
					+ " JOIN park_area pa ON pa.id = pac.id"
					+ " JOIN parc_jardin pj ON pj.identifiant = pa.id_parc_jardin"
					+ " JOIN city ci ON ci.id = pj.id_city"
					+ " WHERE pac.annee = :annee AND ci.id_comm2co = :com2coId AND pac.ndvi_mean IS NOT NULL"
					+ " ORDER BY pac.ndvi_mean DESC")
	List<NdviSummaryProjection> findNdviSummariesByAnneeAndCom2co(@Param("annee") Integer annee,
			@Param("com2coId") Long com2coId);

	long countByAnnee(Integer annee);

	/**
	 * Nombre de lignes calculées d'une année pour un EPCI.
	 * @param annee année de référence
	 * @param com2coId identifiant de la communauté de communes
	 * @return nombre de lignes
	 */
	@Query(nativeQuery = true,
			value = "SELECT count(1) FROM park_area_computed pac"
					+ " JOIN park_area pa ON pa.id = pac.id"
					+ " JOIN parc_jardin pj ON pj.identifiant = pa.id_parc_jardin"
					+ " JOIN city ci ON ci.id = pj.id_city"
					+ " WHERE pac.annee = :annee AND ci.id_comm2co = :com2coId")
	long countByAnneeAndCom2co(@Param("annee") Integer annee, @Param("com2coId") Long com2coId);

	/**
	 * Mesures NDVI des parcs d'une année pour une commune, triées par NDVI moyen décroissant.
	 * @param annee année de référence
	 * @param inseeCode code INSEE de la commune
	 * @return summaries
	 */
	@Query(nativeQuery = true,
			value = "SELECT pac.id AS \"parkId\", pa.name AS \"name\", pac.surface AS \"surface\","
					+ " pac.ndvi_mean AS \"ndviMean\", pac.ndvi_min AS \"ndviMin\","
					+ " pac.ndvi_max AS \"ndviMax\", pac.ndvi_stddev AS \"ndviStddev\","
					+ " pac.ndre_mean AS \"ndreMean\", pac.ndre_min AS \"ndreMin\","
					+ " pac.ndre_max AS \"ndreMax\", pac.ndre_stddev AS \"ndreStddev\","
					+ " pac.gndvi_mean AS \"gndviMean\", pac.gndvi_min AS \"gndviMin\","
					+ " pac.gndvi_max AS \"gndviMax\", pac.gndvi_stddev AS \"gndviStddev\","
					+ " pac.ndvi_acquisition_date AS \"acquisitionDate\", pac.annee AS \"annee\""
					+ " FROM park_area_computed pac"
					+ " JOIN park_area pa ON pa.id = pac.id"
					+ " JOIN parc_jardin pj ON pj.identifiant = pa.id_parc_jardin"
					+ " JOIN city ci ON ci.id = pj.id_city"
					+ " WHERE pac.annee = :annee AND ci.insee_code = :inseeCode AND pac.ndvi_mean IS NOT NULL"
					+ " ORDER BY pac.ndvi_mean DESC")
	List<NdviSummaryProjection> findNdviSummariesByAnneeAndCity(@Param("annee") Integer annee,
			@Param("inseeCode") String inseeCode);

	/**
	 * Nombre de lignes calculées d'une année pour une commune.
	 * @param annee année de référence
	 * @param inseeCode code INSEE de la commune
	 * @return nombre de lignes
	 */
	@Query(nativeQuery = true,
			value = "SELECT count(1) FROM park_area_computed pac"
					+ " JOIN park_area pa ON pa.id = pac.id"
					+ " JOIN parc_jardin pj ON pj.identifiant = pa.id_parc_jardin"
					+ " JOIN city ci ON ci.id = pj.id_city"
					+ " WHERE pac.annee = :annee AND ci.insee_code = :inseeCode")
	long countByAnneeAndCity(@Param("annee") Integer annee, @Param("inseeCode") String inseeCode);

	/**
	 * Identifiants des EPCI disposant de lignes calculées.
	 * @return identifiants des communautés de communes
	 */
	@Query(nativeQuery = true,
			value = "SELECT DISTINCT ci.id_comm2co FROM park_area_computed pac"
					+ " JOIN park_area pa ON pa.id = pac.id"
					+ " JOIN parc_jardin pj ON pj.identifiant = pa.id_parc_jardin"
					+ " JOIN city ci ON ci.id = pj.id_city"
					+ " WHERE ci.id_comm2co IS NOT NULL")
	List<Long> findCom2coIdsWithComputedParks();

	/**
	 * Identifiants des parcs calculés d'un EPCI pour une année.
	 * @param annee année de référence
	 * @param com2coId identifiant de la communauté de communes
	 * @return identifiants {@code park_area.id}
	 */
	@Query(nativeQuery = true,
			value = "SELECT DISTINCT pac.id FROM park_area_computed pac"
					+ " JOIN park_area pa ON pa.id = pac.id"
					+ " JOIN parc_jardin pj ON pj.identifiant = pa.id_parc_jardin"
					+ " JOIN city ci ON ci.id = pj.id_city"
					+ " WHERE pac.annee = :annee AND ci.id_comm2co = :com2coId")
	List<Long> findParkIdsByAnneeAndCom2co(@Param("annee") Integer annee,
			@Param("com2coId") Long com2coId);

	/**
	 * findByIdAndAnnee.
	 * @param id ParkAreaComputed
	 * @param annee yeay
	 * @return ParkAreaComputed
	 */
	Optional<ParkAreaComputed> findByIdAndAnnee(Long id, Integer annee);

	/**
	 * Toutes les lignes calculées d'un parc, toutes années confondues.
	 * Le NDVI Copernicus étant lissé sur plusieurs années, un seul appel
	 * alimente toutes ces lignes.
	 * @param id identifiant de la zone de parc
	 * @return lignes {@code park_area_computed} du parc
	 */
	@Query(nativeQuery = true,
			value = "select * from public.park_area_computed where id = :id")
	List<ParkAreaComputed> findAllByIdPark(@Param("id") Long id);

	/**
	 * isComputedForCity.
	 * Indique si au moins un parc de la commune possède déjà un calcul isochrone
	 * (park_area_computed). Si oui, on ne modifie plus la géométrie des parcs.
	 * @param cityId id de la commune
	 * @return true si la commune a déjà des parcs calculés
	 */
	@Query(nativeQuery = true,
			value="SELECT EXISTS( "
					+ " SELECT 1 FROM park_area pa "
					+ "   JOIN park_area_computed pac ON pa.id = pac.id "
					+ "   JOIN parc_jardin pj ON pj.identifiant = pa.id_parc_jardin "
					+ " WHERE pj.id_city = :cityId)")
	boolean isComputedForCity(@Param("cityId") Long cityId);
	
}
