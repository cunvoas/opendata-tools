package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;

/**
 * Spring JPA repository.
 * @see https://postgis.net/docs/ST_Distance.html
 */
@Repository
public interface ParcPrefectureRepository extends JpaRepository<ParcPrefecture, Long> {

	ParcPrefecture findByNamePrefecture(String name);
	
	@Query(nativeQuery = true, 
			   value = "SELECT pp.* FROM parc_prefecture pp WHERE pp.id_city=?")
	List<ParcPrefecture> findByCity(Long id);
	
	/**
	 * @param id cityId
	 * @param radius ion meters
	 * @return
	 */
//	@Query(nativeQuery = true, 
//			   value = "SELECT pp.* FROM parc_prefecture pp WHERE ST_Distance(pp.point,(select c.coordinate from city c where c.id=:cityId), true)<:radius")
//	List<ParcPrefecture> findNearCityAndRadius(Long id, Long radius);
	
	@Query(nativeQuery = true, 
			   value = "SELECT pp.* FROM parc_prefecture pp WHERE ST_Intersects(area, :searchArea")
	List<ParcPrefecture> findByArea(@Param("searchArea")Polygon searchArea);
	
	@Query(nativeQuery = true, 
			   value = "SELECT pp.* FROM parc_prefecture pp WHERE ST_Intersects(pp.area, ?1)")
	List<ParcPrefecture> findByArea(String searchArea);
	
	@Query(nativeQuery = true, 
			   value = "SELECT pp.* FROM parc_prefecture pp WHERE pp.id_parc=? limit 2")
	List<ParcPrefecture> findByParcEtJardinId(Long id);

	@Query(nativeQuery = true, 
			   value = "SELECT ST_Distance(pp.point ,ST_MakePoint(ST_X(pp.point)+ST_MaxDistance(pp.point ,pp.area), ST_Y(pp.point)), true) as maxi FROM parc_prefecture pp WHERE pp.identifiant=?")
	Double getMaxDistanceFromCentroid(Long id);
	
	@Query(nativeQuery = true, 
			   value = "SELECT ST_Area(pp.area, true), sqrt(ST_Area(pp.area, true)/pi()) as mini FROM parc_prefecture pp WHERE pp.identifiant=?")
	Double getCircleDistance(Long id);

	
	
/* calcul distance max entre centroide et péréphérie

SELECT identifiant, nom_pref, id_city, surface,
	ST_Distance(point ,ST_MakePoint(ST_X(point)+ST_MaxDistance(point ,area), ST_Y(point)), true),

	ST_MaxDistance(point ,area), ST_X(point), ST_Y(point)
FROM public.parc_prefecture limit 5

	 */
}
