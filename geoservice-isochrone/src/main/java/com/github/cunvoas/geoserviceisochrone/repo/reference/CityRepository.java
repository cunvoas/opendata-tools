package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;

@Repository
public interface CityRepository extends JpaRepository<City, Long>{

	@Query(value="SELECT * from city where ST_Distance(coordinate, :p) < :distanceM order by ST_Distance(coordinate, :p) asc", nativeQuery = true)
	List<City> findNearWithinDistance(@Param("p")Point p, @Param("distanceM")double distanceM);
	
	City findByInseeCode(String insee);
	
	List<City> findByCommunauteCommune_Id(Long id);
	
	
	@Query(	nativeQuery = true, 
			value = "SELECT * FROM city where id_region=:id order by name",
			countQuery = "SELECT count(1) FROM city where id_region=:id")
	List<City> findByRegionId(@Param("id")Long id);
	
	@Query(	nativeQuery = true, 
			value = "SELECT * FROM city where id_region=:id and name like upper(:txt) order by name",
			countQuery = "SELECT count(1) FROM city where id_region=:id")
	List<City> findByRegionIdAndName(@Param("id")Long id, @Param("txt")String txt);
	
	@Query(	nativeQuery = true, 
			value = "SELECT * FROM city where id_comm2co=:id order by name",
			countQuery = "SELECT count(1) FROM city where id_comm2co=:id")
	List<City> findByCommunauteCommuneId(@Param("id")Long id);

	@Query(	nativeQuery = true, 
			value = "SELECT * FROM city where id_region=:idR and id_comm2co=:idC order by name",
			countQuery = "SELECT count(1) FROM city where id_region=:idR and id_comm2co=:idC")
	List<City> findByRegionIdAndCommunauteCommuneId(@Param("idR")Long idR, @Param("idC")Long idC);
	
	
	@Query(	nativeQuery = true, 
			value = "SELECT c.* FROM city c ORDER BY ST_Distance(c.coordinate::geography,ST_MakePoint(:lng,:lat)::geography) limit 10",
			countQuery = "SELECT count(1) FROM city)")
	List<City> findNearMeCities(@Param("lng")Double lng, @Param("lat")Double lat);
	
}
