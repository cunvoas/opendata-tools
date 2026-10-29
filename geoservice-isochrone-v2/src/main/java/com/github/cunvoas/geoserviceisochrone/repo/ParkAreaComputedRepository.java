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
					+ " pac.ndvi_acquisition_date AS \"acquisitionDate\", pac.annee AS \"annee\""
					+ " FROM park_area_computed pac"
					+ " JOIN park_area pa ON pa.id = pac.id"
					+ " WHERE pac.annee = :annee AND pac.ndvi_mean IS NOT NULL"
					+ " ORDER BY pac.ndvi_mean DESC")
	List<NdviSummaryProjection> findNdviSummariesByAnnee(@Param("annee") Integer annee);

	long countByAnnee(Integer annee);
	/**
	 * findByIdAndAnnee.
	 * @param id ParkAreaComputed
	 * @param annee yeay
	 * @return ParkAreaComputed
	 */
	Optional<ParkAreaComputed> findByIdAndAnnee(Long id, Integer annee);

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
