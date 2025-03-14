package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;

/**
 * Spring JPA repository.
 * Repo pour only geographic squares.
 */
@Repository
public interface InseeCarre200mOnlyShapeRepository extends JpaRepository<InseeCarre200mOnlyShape, String>{
	
	
	/**
	 * findCarreInMapArea.
	 * @param geometry shape
	 * @return list InseeCarre200mOnlyShape
	 */
	@Query(nativeQuery = true, value =  "SELECT * FROM carre200onlyshape WHERE ST_Intersects(geo_shape, ?1)")
	List<InseeCarre200mOnlyShape> findCarreInMapArea(String geometry);

	/**
	 * findCarreInMapArea.
	 * @param geometry shape
	 * @return list InseeCarre200mOnlyShape
	 */
	@Query(nativeQuery = true, value =  "SELECT * FROM carre200onlyshape WHERE ST_Intersects(geo_shape, ?1)")
	List<InseeCarre200mOnlyShape> findCarreInMapArea(Geometry geometry);
	
	/**
	 * findCarreInMapArea.
	 * @param geometry shape
	 * @param withPop bool
	 * @return list InseeCarre200mOnlyShape
	 */
	@Query(nativeQuery = true, value =  "SELECT * FROM carre200onlyshape WHERE ST_Intersects(geo_shape, ?1) and avec_pop=?2")
	List<InseeCarre200mOnlyShape> findCarreInMapArea(String geometry, Boolean withPop);

	/**
	 * findCarreByInseeCode.
	 * @param codeInsee code
	 * @param withPop bool
	 * @return list InseeCarre200mOnlyShape
	 */
	@Query(nativeQuery = true, value =  "SELECT * FROM carre200onlyshape WHERE code_insee=?1 and avec_pop=?2")
	List<InseeCarre200mOnlyShape> findCarreByInseeCode(String codeInsee, Boolean withPop);
	
	/**
	 * findCarreInMapArea.
	 * @param geometry shape
	 * @param withPop bool
	 * @return list InseeCarre200mOnlyShape
	 */
	@Query(nativeQuery = true, value =  "SELECT * FROM carre200onlyshape WHERE ST_Intersects(geo_shape, ?1) and avec_pop=?2")
	List<InseeCarre200mOnlyShape> findCarreInMapArea(Geometry geometry, Boolean withPop);

	/**
	 * getSurface.
	 * @param polygon shape
	 * @return surface of shape
	 */
	@Query(nativeQuery = true,  value = "SELECT ST_Area(?1, true)")
	Long getSurface(Geometry polygon);
	
}
