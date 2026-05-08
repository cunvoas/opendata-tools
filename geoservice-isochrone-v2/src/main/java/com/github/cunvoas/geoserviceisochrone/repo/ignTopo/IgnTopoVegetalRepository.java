package com.github.cunvoas.geoserviceisochrone.repo.ignTopo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.ignTopo.IgnTopoVegetal;

@Repository
public interface IgnTopoVegetalRepository  extends JpaRepository<IgnTopoVegetal, Long>{

	List<IgnTopoVegetal> findByInseeId(String inseeId);

	@Query(nativeQuery = true, 
		value="SELECT itv.* FROM public.ign_topo_vegetal itv "
				+ " WHERE ST_Intersects(itv.shape, :mapArea)" 
		)
	public List<IgnTopoVegetal> findTopoVegetalByMapArea(@Param("mapArea") String mapArea);

	
	@Query(nativeQuery = true, 
		value="SELECT itv.* FROM public.ign_topo_vegetal itv "
				+ " WHERE itv.insee_id in (SELECT insee_code FROM public.city where id_comm2co=:com2coId)"
				+ "   and ST_Intersects(itv.shape, :mapArea)" 
		)
	public List<IgnTopoVegetal> findTopoVegetalByCom2CoAndMapArea(@Param("com2coId") Long com2coId, @Param("mapArea") String mapArea);

	@Query(nativeQuery = true, 
			value="SELECT itv.* FROM public.ign_topo_vegetal itv "
					+ " WHERE itv.insee_id in (SELECT insee_proche FROM public.cadastre_proche WHERE id_insee=:insee)"
					+ "   and ST_Intersects(itv.shape, :mapArea)" 
			)
	public List<IgnTopoVegetal> findTopoVegetalByVilleProcheMapArea(@Param("insee") String insee, @Param("mapArea") String mapArea);

	
}
