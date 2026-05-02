package com.github.cunvoas.geoserviceisochrone.repo.reference;


import java.util.List;

import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.ParkOverpass;

/**
 * repository de ParkOverpass.
 *  
 */
@Repository
public interface ParkOverpassRepository extends JpaRepository<ParkOverpass, Long> {
	
	List<ParkOverpass> findByInsee(String inseeCode);
	
	@Query(nativeQuery = true, 
			value="SELECT po.* FROM public.park_overpass po "
					+ " WHERE ST_Intersects(po.shape, :mapArea)" 
			)
	List<ParkOverpass> findByMapArea(String sPolygon);
}
