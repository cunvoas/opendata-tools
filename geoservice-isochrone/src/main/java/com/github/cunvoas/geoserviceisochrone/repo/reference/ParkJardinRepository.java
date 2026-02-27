package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;

/**
 * Repository Spring Data JPA pour l'accès aux parcs et jardins.
 * Permet de manipuler les géométries associées.
 */
@Repository
public interface ParkJardinRepository extends JpaRepository<ParcEtJardin, Long> {

	/**
	 * getGeometryFromText.
	 * @param txt shape as string
	 * @return geolatte, to convert in locationtech, use GeometryQueryHelper.cast()
	 */
	@Query(nativeQuery = true,
			value="select ST_GeomFromText(?)")
	org.geolatte.geom.Geometry<?> getGeometryFromText(String txt);
	
	/**
	 * findByName.
	 * @param name name case sensitive
	 * @return ParcEtJardin
	 */
	ParcEtJardin findByName(String name);
	/**
	 * findByName.
	 * @param name name case INSensitive
	 * @return ParcEtJardin
	 */
	ParcEtJardin findByNameIgnoreCase(String name);
	
	
	/**
	 * findByCityId.
	 * @param id city
	 * @return list ParcEtJardin
	 */
	@Query(nativeQuery = true,
			value="select pj.* from parc_jardin pj where pj.id_city=:id",
			countQuery="select count(1) from parc_jardin pj where pj.id_city=:id")
	List<ParcEtJardin> findByCityId(@Param("id")Long id);
	
	
	/**
	 * findByComm2CoId.
	 * @param id com2co 
	 * @param pageable page
	 * @return list ParcEtJardin
	 */
	@Query(nativeQuery = true,
	value="select pj.* from parc_jardin pj "
			+ " inner join city c on pj.id_city=c.id "
			+ " inner join adm_com2commune c2c on c2c.id=c.id_comm2co "
			+ " inner join park_area pa on pj.identifiant=pa.id_parc_jardin "
			+ " inner join park_entrance pe on pa.id=pe.area_id "
			+ " where c2c.id=:id"
			+ " and (pe.update_date>pa.updated or pa.updated isnull)",
	countQuery="select count(1) from parc_jardin pj inner join city c on c.id=pj.id_city inner join adm_com2commune c2c on c2c.id=c.id_comm2co where c2c.id=:id")
	Page<ParcEtJardin> findByComm2CoId(@Param("id")Long id, Pageable pageable);
	
	/**
	 * findByComm2CoIdToMerge.
	 * @param id com2co
	 * @param pageable page
	 * @return list ParcEtJardin
	 */
	@Query(nativeQuery = true,
	value="select pj.* from parc_jardin pj "
			+ " inner join city c on pj.id_city=c.id "
			+ " inner join adm_com2commune c2c on c2c.id=c.id_comm2co "
			+ " inner join park_area pa on pj.identifiant=pa.id_parc_jardin "
			+ " inner join park_entrance pe on pa.id=pe.area_id "
			+ " where c2c.id=:id"
			+ " and (pe.update_date>pa.updated or pa.updated isnull)")
	Page<ParcEtJardin> findByComm2CoIdToMerge(@Param("id")Long id, Pageable pageable);
	
	/**
	 * findByComm2CoIdToCompute.
	 * @param id com2co 
	 * @param pageable page 
	 * @return list ParcEtJardin
	 */
	@Query(nativeQuery = true,
	value="select pj.* from parc_jardin pj "
			+ " inner join city c on pj.id_city=c.id "
			+ " inner join adm_com2commune c2c on c2c.id=c.id_comm2co "
			+ " inner join park_area pa on pj.identifiant=pa.id_parc_jardin "
			+ " inner join park_area_computed pac on pa.id=pac.id "
			+ " where c2c.id=:id"
			+ " and (pa.updated>pac.updated or pac.updated isnull)"
			)
	Page<ParcEtJardin> findByComm2CoIdToCompute(@Param("id")Long id, Pageable pageable);
	
	
	/**
	 * findByCityId.
	 * @param id city 
	 * @param pageable page
	 * @return list ParcEtJardin
	 */
	@Query(nativeQuery = true,
			value="select pj.* from parc_jardin pj where pj.id_city=:id",
			countQuery="select count(1) from parc_jardin pj where pj.id_city=:id")
	Page<ParcEtJardin> findByCityId(@Param("id")Long id, Pageable pageable);
	
	/**
	 * findByCityIdToMerge.
	 * @param id city
	 * @param pageable page
	 * @return list ParcEtJardin
	 */
	@Query(nativeQuery = true,
			value="select distinct pj.* from parc_jardin pj "
					+ "inner join park_area pa on pj.identifiant=pa.id_parc_jardin "
					+ "inner join park_entrance pe on pa.id=pe.area_id "
					+ "where pj.id_city=:id "
					+ "and (pe.update_date>pa.updated or pa.updated isnull)"
			)
	Page<ParcEtJardin> findByCityIdToMerge(@Param("id")Long id, Pageable pageable);
	
	/**
	 * findByCityIdToCompute.
	 * @param id city
	 * @param pageable page
	 * @return list ParcEtJardin
	 */
	@Query(nativeQuery = true,
			value="select distinct pj.* from parc_jardin pj "
					+ "inner join park_area pa on pj.identifiant=pa.id_parc_jardin "
					+ "inner join park_area_computed pac on pa.id=pac.id "
					+ "where pj.id_city=:id "
					+ "and (pa.updated>pac.updated or pac.updated isnull)"
			)
	Page<ParcEtJardin> findByCityIdToCompute(@Param("id")Long id, Pageable pageable);
	
	
	/**
	 * findByAreaAndCityId.
	 * @param cityId city 
	 * @param searchArea shape
	 * @return list ParcEtJardin
	 */
	@Query(nativeQuery = true, 
			   value = "SELECT pj.* FROM parc_jardin pj "
			   		+ "WHERE pj.id_city=:cityId and ST_Contains(pj.coordonnee, :searchArea)")
	List<ParcEtJardin> findByAreaAndCityId(@Param("cityId")Long cityId, @Param("searchArea")Geometry searchArea);
	
	
	
	/**
	 * findNearWithinDistance.
	 * @param p point
	 * @param distanceM distance
	 * @return list ParcEtJardin
	 */
	@Query(value="SELECT * from parc_jardin where ST_Distance(coordonnee, :p) < :distanceM order by ST_Distance(coordonnee, :p) asc", nativeQuery = true)
	List<ParcEtJardin> findNearWithinDistance(@Param("p")Point p, @Param("distanceM")double distanceM);
	
	
	/**
	 * findByAreaAndYear.
	 * @param annee year
	 * @param searchArea shape
	 * @return list ParcEtJardin
	 */
	@Query(nativeQuery = true, 
		   value = "SELECT pj.* FROM parc_jardin pj "
		   		+ " WHERE date_part('year',COALESCE(pj.date_debut, TO_DATE('20100101','YYYYMMDD'))) <=:annee "
		   		+ "   AND :annee <= date_part('year',COALESCE(pj.date_fin, TO_DATE('21991231','YYYYMMDD'))) "
		   		+ "   AND ST_Intersects(pj.contour, :searchArea) OR ST_Intersects(pj.coordonnee, :searchArea)")
	List<ParcEtJardin> findByAreaAndYear(@Param("annee")Integer annee, @Param("searchArea")Polygon searchArea);

	
	/**
	 * findByArea.
	 * @param searchArea shape
	 * @return list ParcEtJardin
	 */
	@Query(nativeQuery = true, 
		   value = "SELECT pj.* FROM parc_jardin pj "
		   		+ "WHERE ST_Intersects(pj.contour, :searchArea) OR ST_Intersects(pj.coordonnee, :searchArea)")
	List<ParcEtJardin> findByArea(@Param("searchArea")Geometry searchArea); //ST_Intersects ST_Within
	

	/**
	 * findByArea..
	 * @param searchArea shape as string
	 * @return list ParcEtJardin

	 */
	@Query(nativeQuery = true, 
			   value = "SELECT pj.* FROM parc_jardin pj WHERE pj.coordonnee is not null and ST_Intersects(pj.coordonnee, ST_GeomFromText(:searchArea, 4326))")
	List<ParcEtJardin> findByArea(@Param("searchArea")String searchArea);
	
	
}