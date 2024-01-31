package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200m;

@Repository
public interface InseeCarre200mRepository extends JpaRepository<InseeCarre200m, String>{
	
	
	InseeCarre200m findByIdInspire(String idInspire);
	/**
	 * ST_Intersects(pa.polygon,'SRID=4326;POLYGON((3.10903 50.62347,3.1090273 50.6240298,3.1088697 50.6249568,3.1088697 50.6249568,3.10903 50.62347))')
	 * @param polygon
	 * @return
	 */
	@Query(nativeQuery = true, 
			   value = "SELECT ca.* FROM carre200shape cs inner join carre200 ca on cs.id_carre_hab=ca.id "
				+ "WHERE ST_Intersects(cs.geo_shape, ?1)")
	List<InseeCarre200m> getAllCarreInMap(String polygon);
	
	
	
	@Query(nativeQuery = true,  value = "SELECT ST_Area(?1, true)")
	Long getSurface(Geometry polygon);
	
	
	/*

SELECT ROUND(ST_Area('SRID=4326;POLYGON ((3.0951315121007243 50.62456086590594, 3.097878664457651 50.62472604472621, 3.098142665865518 50.622937200496004, 3.0972874504550307 50.622885805776264, 3.097409157 50.623377206, 3.097415385 50.623375537, 3.097410536 50.623382773, 3.0975504848608737 50.62394782866446, 3.0951315121007243 50.62456086590594))'))
	 */
}