package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;

/**
 * Repo pour only geographic squares.
 */
@Repository
public interface InseeCarre200mOnlyShapeRepository extends JpaRepository<InseeCarre200mOnlyShape, String>{
	
	
	@Query(nativeQuery = true, value =  "SELECT * FROM carre200onlyshape WHERE ST_Intersects(geo_shape, ?1)")
	List<InseeCarre200mOnlyShape> findCarreInMapArea(String geometry);

	@Query(nativeQuery = true, value =  "SELECT * FROM carre200onlyshape WHERE ST_Intersects(geo_shape, ?1)")
	List<InseeCarre200mOnlyShape> findCarreInMapArea(Geometry geometry);
	
	@Query(nativeQuery = true, value =  "SELECT * FROM carre200onlyshape WHERE ST_Intersects(geo_shape, ?1) and avec_pop=?2")
	List<InseeCarre200mOnlyShape> findCarreInMapArea(String geometry, Boolean withPop);

	@Query(nativeQuery = true, value =  "SELECT * FROM carre200onlyshape WHERE code_insee=?1 and avec_pop=?2")
	List<InseeCarre200mOnlyShape> findCarreByInseeCode(String codeInsee, Boolean withPop);
	
	@Query(nativeQuery = true, value =  "SELECT * FROM carre200onlyshape WHERE ST_Intersects(geo_shape, ?1) and avec_pop=?2")
	List<InseeCarre200mOnlyShape> findCarreInMapArea(Geometry geometry, Boolean withPop);

	@Query(nativeQuery = true,  value = "SELECT ST_Area(?1, true)")
	Long getSurface(Geometry polygon);
	
}
