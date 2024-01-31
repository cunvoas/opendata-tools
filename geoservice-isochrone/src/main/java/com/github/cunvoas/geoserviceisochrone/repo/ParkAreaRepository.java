package com.github.cunvoas.geoserviceisochrone.repo;

import java.util.List;

import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;

@Repository
public interface ParkAreaRepository extends JpaRepository<ParkArea, Long>{
	
	List<ParkArea> polygonNull();
	List<ParkArea> findByPolygon(Polygon polygon);
	List<ParkArea> findByBlock(String block);
	
	ParkArea findByName(String name);
	
	
	public static final String FIND_POLYGONS = 
	  "SELECT ca.id,ca.id_inspire,ca.idk, ca.ind_c, cs.geo_point_2d,cs.geo_shape, cs.commune,cs.departement,cs.region, pa.description, pa.polygon "
	+ "FROM park_area pa, carre200shape cs inner join carre200 ca on cs.id_carre_hab=ca.id "
	+ "WHERE ST_Intersects(pa.polygon, cs.geo_shape) and ST_Intersects(pa.polygon, :mapArea) "
	+ "ORDER BY pa.id";
// 'SRID=4326;POLYGON((3.10903 50.62347,3.1090273 50.6240298,3.1088697 50.6249568,3.1088697 50.6249568,3.10903 50.62347))'

	@Query(value = FIND_POLYGONS, nativeQuery = true)
	public List<Object[]> findForAnalysis(@Param("mapArea") String mapArea);
	
	@Query(nativeQuery = true, 
			   value = "SELECT pa.* FROM park_area pa WHERE ST_Intersects(pa.polygon, ?1)")
	public List<ParkArea> findParkInMapArea(String mapArea);
	
	public ParkArea findByIdParcEtJardin(Long id);
	

}
