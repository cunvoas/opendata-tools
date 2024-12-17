package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;

@Repository
public interface CadastreRepository extends JpaRepository<Cadastre, String>{
	@Query(nativeQuery = true, 
			   value = "SELECT ca.* FROM cadastre ca WHERE ST_Intersects(ca.geo_shape, ?1)")
	public List<Cadastre> findCadastreInMapArea(String mapArea);
	
	/**
	 * warn, very slow request !
	 * @param lng
	 * @param lat
	 * @return
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT ca.* FROM cadastre ca WHERE ST_Intersects(ST_MakePoint(:lng,:lat)::geography, ca.geo_shape::geography)",
			countQuery = "SELECT count(1) FROM cadastre) WHERE ST_Intersects(ST_MakePoint(:lng,:lat)::geography, ca.geo_shape::geography)")
	List<City> findMyCadastre(@Param("lng")Double lng, @Param("lat")Double lat);
}
