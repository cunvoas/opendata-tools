package com.github.cunvoas.geoserviceisochrone.repo;

import java.util.List;

import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;

/**
 * Spring JPA repository.
 */
@Repository
public interface ParkAreaRepository extends JpaRepository<ParkArea, Long>{
	
	/**
	 * polygonNull (with null poly).
	 * @return list ParkArea
	 */
	List<ParkArea> polygonNull();
	
	/**
	 * findByPolygon.
	 * @param polygon
	 * @return
	 */
	List<ParkArea> findByPolygon(Polygon polygon);
	
	/**
	 * findByBlock.
	 * @param block quartier
	 * @return list ParkArea
	 */
	List<ParkArea> findByBlock(String block);
	
	/**
	 * findByName.
	 * @param name name
	 * @return ParkArea
	 */
	ParkArea findByName(String name);
	
	/**
	 * fin polygonToUpdate.
	 * @return list ParkArea
	 */
	@Query(nativeQuery = true, 
			   value = "SELECT DISTINCT pa.* FROM park_area pa "
			   		+ " INNER JOIN park_entrance pe on pa.id=pe.area_id"
			   		+ " WHERE pe.updated>pa.update_date")
	List<ParkArea> polygonToUpdate();
	
	
	/**
	 * to long query to set in method.
	 */
	public static final String FIND_POLYGONS = 
	  "SELECT ca.id,ca.id_inspire,ca.idk, ca.ind_c, cs.geo_point_2d,cs.geo_shape, cs.commune,cs.departement,cs.region, pa.description, pa.polygon "
	+ "FROM park_area pa, carre200shape cs inner join carre200 ca on cs.id_carre_hab=ca.id "
	+ "WHERE ST_Intersects(pa.polygon, cs.geo_shape) and ST_Intersects(pa.polygon, :mapArea) "
	+ "ORDER BY pa.id";
// 'SRID=4326;POLYGON((3.10903 50.62347,3.1090273 50.6240298,3.1088697 50.6249568,3.1088697 50.6249568,3.10903 50.62347))'

	/**
	 * findForAnalysis.
	 * @param mapArea shape as string
	 * @return List of array of Object
	 */
	@Query(value = FIND_POLYGONS, nativeQuery = true)
	public List<Object[]> findForAnalysis(@Param("mapArea") String mapArea);
	
	/** 
	 * findParkInMapArea.
	 * @param mapArea shape in string
	 * @return list of ParkArea
	 */
	@Query(nativeQuery = true, 
			   value = "SELECT pa.* FROM park_area pa WHERE ST_Intersects(pa.polygon, ?1)")
	public List<ParkArea> findParkInMapArea(@Param("mapArea") String mapArea);
	
	/**
	 * findByIdParcEtJardin.
	 * @param id ParcEtJardin
	 * @return ParkArea
	 */
	public ParkArea findByIdParcEtJardin(Long id);
	

}
