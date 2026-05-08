package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;

/**
 * Repository Spring Data JPA pour l'accès aux entités CommunauteCommune.
 * Permet de récupérer les communautés de communes d'une région.
 */
@Repository
public interface CommunauteCommuneRepository extends JpaRepository<CommunauteCommune, Long>{
	/**
	 * findByRegionId.
	 * @param id region
	 * @return list of com2co
	 */
	@Query(	nativeQuery = true,
			value = "select * from adm_com2commune where id_region=:id order by name",
			countQuery = "select count(1) from adm_com2commune where id_region=:id")
	List<CommunauteCommune> findByRegionId(@Param("id")Long id);
	
	
	/**
	 * updateGeoShape.
	 * @param id community of communes
	 */
	@Modifying
	@Query(nativeQuery = true,
			value = "UPDATE adm_com2commune acc " +
					"SET carre_carte = sub.union_geom, " +
					"    geo_shape_low = sub.final_geom " +
					"FROM ( " +
					"    WITH raw_unions AS ( " +
					"        SELECT " +
					"            ci.id_comm2co, " +
					"            ST_Multi(ST_Union(ca.geo_shape)) as union_geom " +
					"        FROM city ci " +
					"        JOIN cadastre ca ON ci.insee_code = ca.id_insee " +
					"        WHERE ci.id_comm2co IS NOT NULL " +
					"        GROUP BY ci.id_comm2co " +
					"    ), " +
					"    filled_rings AS ( " +
					"        SELECT " +
					"            id_comm2co, " +
					"            union_geom, " +
					"            ST_Collect( " +
					"                ARRAY( " +
					"                    SELECT ST_MakePolygon( " +
					"                        ST_ExteriorRing(poly.geom), " +
					"                        ARRAY( " +
					"                            SELECT ST_InteriorRingN(poly.geom, n) " +
					"                            FROM generate_series(1, ST_NumInteriorRings(poly.geom)) AS n " +
					"                            WHERE ST_Area(ST_MakePolygon(ST_InteriorRingN(poly.geom, n))) >= 0.0001 " +
					"                        ) " +
					"                    ) " +
					"                    FROM ST_Dump(union_geom) AS poly " +
					"                ) " +
					"            ) AS geometry_filled " +
					"        FROM raw_unions " +
					"    ), " +
					"    filtered_polygons AS ( " +
					"        SELECT " +
					"            id_comm2co, " +
					"            union_geom, " +
					"            ST_Multi(ST_Union(dumped.geom)) as final_geom " +
					"        FROM ( " +
					"            SELECT id_comm2co, union_geom, (ST_Dump(geometry_filled)).geom as geom " +
					"            FROM filled_rings " +
					"        ) dumped " +
					"        WHERE ST_Area(dumped.geom) >= 0.0001 " +
					"        GROUP BY id_comm2co, union_geom " +
					"    ) " +
					"    SELECT id_comm2co, union_geom, ST_MakeValid(ST_Simplify(final_geom, 0.0001)) as final_geom " +
					"    FROM filtered_polygons " +
					") AS sub " +
					"WHERE acc.id = sub.id_comm2co AND acc.id=:id")
	void updateGeoShape(@Param("id")Long id);

}