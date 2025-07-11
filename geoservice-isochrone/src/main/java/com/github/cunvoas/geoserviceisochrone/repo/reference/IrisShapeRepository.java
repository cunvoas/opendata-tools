package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisId;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;

/**
 * Spring JPA repository.
 * Repo for IrisShape
 */
@Repository
public interface IrisShapeRepository extends JpaRepository< IrisShape,String> {
	

	/**
	 * findByFootprintIsNull.
	 * @return List<IrisShape>
	 */
	List<IrisShape> findByFootprintIsNull();
	
	
	/**
	 * findByCodeInsee.
	 * @param codeInsee code
	 * @return List<IrisShape>
	 */
	List<IrisShape> findByCodeInsee(String codeInsee);

	
	/**
	 * findByAnneeAndIdInspire.
	 * @param annee annee
	 * @param idIris code Iris
	 * @return IrisShape
	 */
//	IrisShape findByAnneeAndIris(Integer annee, String idIris);
	
	/*
	 * getAllCarreInMap.
	 * exemple: ST_Intersects(pa.polygon,'SRID=4326;POLYGON((3.10903 50.62347,3.1090273 50.6240298,3.1088697 50.6249568,3.1088697 50.6249568,3.10903 50.62347))')
	 * @param polygon polygon
	 * @param annee annee
	 * @return list Filosofil200m
	 *
	@Query(nativeQuery = true, 
			   value = "SELECT fi.* FROM carre200shape cs inner join filosofi_200m fi on fi.annee=:annee and cs.id_carre_hab=fi.idcar_200m "
				+ "WHERE ST_Intersects(cs.geo_shape, :polygon)")
	List<Filosofil200m> getAllCarreInMap(@Param("polygon")String polygon, @Param("annee")Integer annee);
	
	*/


	
	
	/**
	 * findCarreInMapArea.
	 * @param geometry shape
	 * @return list InseeCarre200mOnlyShape
	 */
	@Query(nativeQuery = true, value =  "SELECT * FROM iris_shape WHERE ST_Intersects(contour, ?1)")
	List<IrisShape> findIrisInMapArea(String geometry);
	
	/**
	 * getSurface.
	 * @param polygon shape
	 * @return surface of shape
	 */
	@Query(nativeQuery = true,  value = "SELECT ST_Area(?1, true)")
	Long getSurface(Geometry polygon);
}