package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mShape;

@Repository
public interface InseeCarre200mOnlyShapeRepository extends JpaRepository<InseeCarre200mOnlyShape, String>{
	
	
//	@Query(nativeQuery = true, value =  "SELECT * FROM carre200onlyshape WHERE ST_Intersects(geo_shape, ?1)")
//	List<InseeCarre200mOnlyShape> findCarreInMapArea(String polygon);
//
//	
//	public static final String FIND_PARK_AREAS = 
//			"SELECT s.id_inspire, s.id_carre_1km, c.geo_point_2d, s.geo_shape, p.code_insee, p.name "+
//			"FROM park_area p, carre200onlyshape s INNER JOIN carre200 c ON s.id_inspire=c.id_inspire " +
//			"WHERE ST_Intersects(s.geo_shape, :mapArea) AND ST_intersects(p.polygon, s.geo_shape) " +
//			"ORDER BY s.id_carre_hab";
//	@Query(value = FIND_PARK_AREAS, nativeQuery = true)
//	public List<Object[]> findAreasInMapArea(@Param("annee") Integer annee, @Param("mapArea") String mapArea);

}
