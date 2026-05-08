package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;

/**
 * Repository Spring Data JPA pour l'accès aux entités City.
 * Permet de rechercher des villes par coordonnées, distance ou code INSEE.
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long>{

	/**
	 * findNearWithinDistance.
	 * @param p point 
	 * @param distanceM distance in meters
	 * @return list of City 
	 */
	@Query(value="SELECT * from city where ST_Distance(coordinate, :p) < :distanceM order by ST_Distance(coordinate, :p) asc", nativeQuery = true)
	List<City> findNearWithinDistance(@Param("p")Point p, @Param("distanceM")double distanceM);
	
	/**
	 * findByInseeCode.
	 * @param insee code
	 * @return City
	 */
	City findByInseeCode(String insee);
	
	/**
	 * findByCommunauteCommune_Id.
	 * @param id com2co
	 * @return list of City
	 */
	List<City> findByCommunauteCommune_Id(Long id);
	
	
	/**
	 * findByRegionId.
	 * @param id region
	 * @return list of City
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT * FROM city where id_region=:id order by name",
			countQuery = "SELECT count(1) FROM city where id_region=:id")
	List<City> findByRegionId(@Param("id")Long id);
	
	/**
	 * findByRegionIdAndName.
	 * @param id region
	 * @param txt search 
	 * @return list of City
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT * FROM city where id_region=:id and name like upper(:txt) order by name",
			countQuery = "SELECT count(1) FROM city where id_region=:id")
	List<City> findByRegionIdAndName(@Param("id")Long id, @Param("txt")String txt);
	
	/**
	 * findByCommunauteCommuneId.
	 * @param id com2co
	 * @return list of City
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT * FROM city where id_comm2co=:id order by name",
			countQuery = "SELECT count(1) FROM city where id_comm2co=:id")
	List<City> findByCommunauteCommuneId(@Param("id")Long id);

	/**
	 * findByRegionIdAndCommunauteCommuneId.
	 * @param idR region
	 * @param idC com2co
	 * @return list of City
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT * FROM city where id_region=:idR and id_comm2co=:idC order by name",
			countQuery = "SELECT count(1) FROM city where id_region=:idR and id_comm2co=:idC")
	List<City> findByRegionIdAndCommunauteCommuneId(@Param("idR")Long idR, @Param("idC")Long idC);
	
	
	/**
	 * findNearMeCities.
	 * @param lng longitude
	 * @param lat latitude
	 * @return list of City
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT c.* FROM city c ORDER BY ST_Distance(c.coordinate::geography,ST_MakePoint(:lng,:lat)::geography) limit 10",
			countQuery = "SELECT count(1) FROM city)")
	List<City> findNearMeCities(@Param("lng")Double lng, @Param("lat")Double lat);
	
}