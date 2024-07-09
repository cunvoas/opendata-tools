package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200mId;

/**
 * Repo for INSEE stats (population, ages, poor...)
 */
@Repository
public interface Filosofil200mRepository extends JpaRepository< Filosofil200m,  Filosofil200mId>{
	
	Filosofil200m findByAnneeAndIdInspire(Integer annee, String idInspire);
	
	List<Filosofil200m> findByIdInspire(String idInspire);
	
	/**
	 * ST_Intersects(pa.polygon,'SRID=4326;POLYGON((3.10903 50.62347,3.1090273 50.6240298,3.1088697 50.6249568,3.1088697 50.6249568,3.10903 50.62347))')
	 * @param polygon
	 * @return
	 */
	@Query(nativeQuery = true, 
			   value = "SELECT fi.* FROM carre200shape cs inner join filosofi_200m fi on fi.annee=:annee and cs.id_carre_hab=fi.idcar_200m "
				+ "WHERE ST_Intersects(cs.geo_shape, :polygon)")
	List<Filosofil200m> getAllCarreInMap(String polygon, Integer annee);
	
	


}