package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;

/**
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
	List<ParcPrefecture> findByArea(Polygon searchArea);
	
	
	
}
