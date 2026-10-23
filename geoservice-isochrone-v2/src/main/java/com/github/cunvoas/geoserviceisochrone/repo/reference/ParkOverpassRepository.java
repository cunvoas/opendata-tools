package com.github.cunvoas.geoserviceisochrone.repo.reference;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.ParkOverpass;

/**
 * repository de ParkOverpass.
 *  
 */
@Repository
public interface ParkOverpassRepository extends JpaRepository<ParkOverpass, Long> {
	
	List<ParkOverpass> findByInsee(String inseeCode);

	/**
	 * findByInseeContains.
	 * Recherche les parcs dont le code INSEE composite contient le code donné.
	 * Un parc peut s'étendre sur plusieurs communes : le champ {@code insee}
	 * contient alors les codes triés séparés par des virgules.
	 * @param inseeCode code INSEE de la commune
	 * @return list of ParkOverpass
	 */
	@Query(nativeQuery = true,
			value="SELECT po.* FROM public.park_overpass po "
					+ " WHERE :inseeCode = ANY(string_to_array(po.insee, ','))"
			)
	List<ParkOverpass> findByInseeContains(@Param("inseeCode")String inseeCode);

	@Query(nativeQuery = true, 
			value="SELECT po.* FROM public.park_overpass po "
					+ " WHERE ST_Intersects(po.shape, :mapArea)" 
			)
	List<ParkOverpass> findByMapArea(@Param("mapArea")String sPolygon);

	/**
	 * findByAreaGeo.
	 * Parcs dont la forme correspond à la commune : centroïde contenu par le cadastre,
	 * ou au moins {@code minRatio} de la surface géodésique (m²) du parc dans le cadastre.
	 * @param mapArea zone au format "SRID=4326;..."
	 * @param minRatio ratio minimal de couverture (0-1)
	 * @return list of ParkOverpass
	 */
	@Query(nativeQuery = true,
			value="SELECT po.* FROM public.park_overpass po "
					+ " WHERE po.shape IS NOT NULL "
					+ "   AND ST_Intersects(po.shape, :mapArea) "
					+ "   AND ( ST_Contains(:mapArea, ST_Centroid(po.shape)) "
					+ "         OR ( ST_Area(po.shape, true) > 0 "
					+ "              AND ST_Area(ST_Intersection(po.shape, :mapArea), true) "
					+ "                  / ST_Area(po.shape, true) >= :minRatio ) )")
	List<ParkOverpass> findByAreaGeo(@Param("mapArea")String sPolygon,
									 @Param("minRatio")double minRatio);

	/**
	 * findByCornerSouthWestInArea.
	 * Recherche les parcs sans forme (shape NULL) dont le coin sud-ouest
	 * de la bounding box est contenu dans la zone.
	 * @param mapArea zone au format "SRID=4326;..."
	 * @return list of ParkOverpass
	 */
	@Query(nativeQuery = true,
			value="SELECT po.* FROM public.park_overpass po "
					+ " WHERE po.shape IS NULL AND ST_Contains(:mapArea, po.corner_south_west)"
			)
	List<ParkOverpass> findByCornerSouthWestInArea(@Param("mapArea")String sPolygon);
}
