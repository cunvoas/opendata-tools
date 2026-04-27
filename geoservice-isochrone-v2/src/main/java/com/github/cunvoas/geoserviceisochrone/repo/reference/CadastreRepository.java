package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;

/**
 * Repository Spring Data JPA pour l'accès aux entités Cadastre.
 * Permet de rechercher des parcelles cadastrales par zone ou coordonnées.
 */
@Repository
public interface CadastreRepository extends JpaRepository<Cadastre, String>{
	/**
	 * findCadastreInMapArea.
	 * @param mapArea area as string
	 * @return list of Cadastre
	 */
	@Query(nativeQuery = true, 
			   value = "SELECT ca.* FROM cadastre ca WHERE ST_Intersects(ca.geo_shape, ?1)")
	public List<Cadastre> findCadastreInMapArea(String mapArea);
	
	/**
	 * findMyCadastre.
	 * @FIXME warn, very slow request !
	 * @param lng longitude
	 * @param lat latitude
	 * @return  list of Cadastre
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT ca.* FROM cadastre ca WHERE ST_Intersects(ST_MakePoint(:lng,:lat)::geography, ca.geo_shape::geography)",
			countQuery = "SELECT count(1) FROM cadastre) WHERE ST_Intersects(ST_MakePoint(:lng,:lat)::geography, ca.geo_shape::geography)")
	List<Cadastre> findMyCadastre(@Param("lng")Double lng, @Param("lat")Double lat);
	
	/**
	 * findMyCadastre.
	 * @FIXME warn, very slow request !
	 * @param location point
	 * @return Cadastre
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT ca.* FROM cadastre ca WHERE ST_Intersects(:location, ca.geo_shape::geography)",
			countQuery = "SELECT count(1) FROM cadastre) WHERE ST_Intersects(:location, ca.geo_shape::geography)")
	Cadastre findMyCadastre(Point location);
	

	/**
	 * findMyCadastreWithRegion.
	 * @FIXME warn, very slow request !
	 * @param location point
	 * @param idRegion region
	 * @return Cadastre
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT ca.* FROM cadastre ca WHERE id_insee in (SELECT insee_code FROM public.city where id_region=:idRegion) AND ST_Intersects(:location, ca.geo_shape::geography)",
			countQuery = "SELECT count(1) FROM cadastre) WHERE id_insee in (SELECT insee_code FROM public.city where id_region=:idRegion) AND ST_Intersects(:location, ca.geo_shape::geography)")
	Cadastre findMyCadastreWithRegion(Point location, Long idRegion);


	/**
	 * findMyCadastreWithComm2Co.
	 * @FIXME warn, very slow request !
	 * @param location Point
	 * @param idCom2Co com2co
	 * @return Cadastre
	 */
	@Query(	nativeQuery = true, 
			value = "SELECT ca.* FROM cadastre ca WHERE id_insee in (SELECT insee_code FROM public.city where id_comm2co=:idCom2Co) AND ST_Intersects(:location, ca.geo_shape::geography)",
			countQuery = "SELECT count(1) FROM cadastre) WHERE id_insee in (SELECT insee_code FROM public.city where id_comm2co=:idCom2Co) AND ST_Intersects(:location, ca.geo_shape::geography)")
	Cadastre findMyCadastreWithComm2Co(Point location, Long idCom2Co);
}