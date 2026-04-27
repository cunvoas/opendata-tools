package com.github.cunvoas.geoserviceisochrone.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.IrisDataComputed;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisId;

/**
 * Spring JPA repository.
 * Repo for IrisData
 */
@Repository
public interface IrisDataComputedRepository extends JpaRepository< IrisDataComputed,IrisId> {
	
	/**
	 * findByAnneeAndIdInspire.
	 * @param annee annee
	 * @param idIris code Iris
	 * @return IrisShape
	 */
	IrisDataComputed findByAnneeAndIris(Integer annee, String idIris);
	
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


}