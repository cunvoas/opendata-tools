package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;

/**
 * Spring JPA repository.
 */
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
	List<Cadastre> findMyCadastre(@Param("lng")Double lng, @Param("lat")Double lat);
	
	/**
	 * warn, very slow request !
	 * @param location
	 * @return
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT ca.* FROM cadastre ca WHERE ST_Intersects(:location, ca.geo_shape::geography)",
			countQuery = "SELECT count(1) FROM cadastre) WHERE ST_Intersects(:location, ca.geo_shape::geography)")
	Cadastre findMyCadastre(Point location);
	

	/**
	 * warn, very slow request !
	 * @param location
	 * @return
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT ca.* FROM cadastre ca WHERE id_insee in (SELECT insee_code FROM public.city where id_region=:idRegion) AND ST_Intersects(:location, ca.geo_shape::geography)",
			countQuery = "SELECT count(1) FROM cadastre) WHERE id_insee in (SELECT insee_code FROM public.city where id_region=:idRegion) AND ST_Intersects(:location, ca.geo_shape::geography)")
	Cadastre findMyCadastreWithRegion(Point location, Long idRegion);


	/**
	 * warn, very slow request !
	 * @param location
	 * @return
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT ca.* FROM cadastre ca WHERE id_insee in (SELECT insee_code FROM public.city where id_comm2co=:idCom2Co) AND ST_Intersects(:location, ca.geo_shape::geography)",
			countQuery = "SELECT count(1) FROM cadastre) WHERE id_insee in (SELECT insee_code FROM public.city where id_comm2co=:idCom2Co) AND ST_Intersects(:location, ca.geo_shape::geography)")
	Cadastre findMyCadastreWithComm2Co(Point location, Long idCom2Co);
}
