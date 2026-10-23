package com.github.cunvoas.geoserviceisochrone.repo;

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
