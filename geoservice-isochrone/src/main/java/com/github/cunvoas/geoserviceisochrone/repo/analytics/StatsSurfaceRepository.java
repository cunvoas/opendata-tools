package com.github.cunvoas.geoserviceisochrone.repo.analytics;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.analytics.StatsSurface;
import com.github.cunvoas.geoserviceisochrone.model.analytics.StatsSurfaceId;
import com.github.cunvoas.geoserviceisochrone.repo.extend.ReadOnlyRepository;

@Repository
public interface StatsSurfaceRepository extends ReadOnlyRepository<StatsSurface, StatsSurfaceId> {
	
	final String QRY_CITY = "WITH surface_range AS (\n"
			+ "	select 0 as surface_min, 3 as surface_max, 0 as seuil UNION\n"
			+ "	select 3 as surface_min, 7 as surface_max, 0 as seuil UNION\n"
			+ "	select 7 as surface_min, 10 as surface_max, 0 as seuil UNION\n"
			+ "	select 10 as surface_min, 12 as surface_max, 1 as seuil UNION\n"
			+ "	select 12 as surface_min, 10000 as surface_max, 2 as seuil \n"
			+ "), stats AS (\n"
			+ "    SELECT \n"
			+ "	  cc.annee, f.lcog_geo, coalesce(round(surface_park_pcapita, 2), 0) as surface_park_pcapita,\n"
			+ "	  coalesce(round(sum(cc.pop_inc), 0), 0) as pop_inc,  coalesce(round(sum(cc.pop_exc), 0), 0) as pop_exc\n"
			+ "    FROM public.carre200_computed_v2 cc \n"
			+ "    INNER JOIN public.filosofi_200m f  ON cc.annee=f.annee AND cc.id_inspire=idcar_200m\n"
			+ "    GROUP BY cc.annee, f.lcog_geo, coalesce(round(surface_park_pcapita, 2), 0)\n"
			+ ")\n"
			+ "SELECT annee, surface_min, surface_max, seuil ,sum(pop_inc) as pop_inc,sum(pop_exc) as pop_exc \n"
			+ "FROM surface_range r , stats \n"
			+ "WHERE  r.surface_min  <= surface_park_pcapita AND surface_park_pcapita < r.surface_max AND\n"
			+ "    stats.annee=:annee AND  lcog_geo like :insee\n"
			+ "GROUP BY annee, surface_min, surface_max, seuil \n"
			+ "order by r.surface_min";	
	
	final String QRY_SUBURBS = "WITH surface_range AS (\n"
			+ "	select 0 as surface_min, 8 as surface_max, 0 as seuil UNION\n"
			+ "	select 8 as surface_min, 17 as surface_max, 0 as seuil UNION\n"
			+ "	select 17 as surface_min, 25 as surface_max, 0 as seuil UNION\n"
			+ "	select 25 as surface_min, 45 as surface_max, 1 as seuil UNION\n"
			+ "	select 45 as surface_min, 10000 as surface_max, 2 as seuil \n"
			+ "), stats AS (\n"
			+ "    SELECT \n"
			+ "	  cc.annee, f.lcog_geo, coalesce(round(surface_park_pcapita, 2), 0) as surface_park_pcapita,\n"
			+ "	  coalesce(round(sum(cc.pop_inc), 0), 0) as pop_inc,  coalesce(round(sum(cc.pop_exc), 0), 0) as pop_exc \n"
			+ "    FROM public.carre200_computed_v2 cc \n"
			+ "    INNER JOIN public.filosofi_200m f  ON cc.annee=f.annee AND cc.id_inspire=idcar_200m\n"
			+ "    GROUP BY cc.annee, f.lcog_geo, coalesce(round(surface_park_pcapita, 2), 0)\n"
			+ ")\n"
			+ "SELECT annee, surface_min, surface_max, seuil ,sum(pop_inc) as pop_inc,sum(pop_exc) as pop_exc\n"
			+ "FROM surface_range r , stats \n"
			+ "WHERE  r.surface_min  <= surface_park_pcapita AND surface_park_pcapita < r.surface_max AND\n"
			+ "    stats.annee=:annee AND  lcog_geo like :insee\n"
			+ "GROUP BY annee, surface_min, surface_max, seuil \n"
			+ "order by r.surface_min";
	@Query(value = QRY_CITY, nativeQuery = true)
	public List<StatsSurface> getStatsForCity(@Param("annee")Integer annee, @Param("insee") String insee);

	@Query(value = QRY_SUBURBS, nativeQuery = true)
	public List<StatsSurface> getStatsForSuburbs(@Param("annee")Integer annee, @Param("insee") String insee);


	final String QRY_COM2CO_DENSE = "WITH \n"
			+ "surface_range_dense AS (\n"
			+ "    select 0 as surface_min, 3 as surface_max, 0 as seuil UNION\n"
			+ "    select 3 as surface_min, 7 as surface_max, 0 as seuil UNION\n"
			+ "    select 7 as surface_min, 10 as surface_max, 0 as seuil UNION\n"
			+ "    select 10 as surface_min, 12 as surface_max, 1 as seuil UNION\n"
			+ "    select 12 as surface_min, 10000 as surface_max, 2 as seuil ),\n"
			+ "densite_ville AS (\n"
			+ "	SELECT insee_code, dens<='2' as is_dense, id_comm2co\n"
			+ "	FROM public.insee_densite_city d\n"
			+ "	INNER JOIN public.city c ON d.codgeo=c.insee_code ),\n"
			+ "stats AS (\n"
			+ "    SELECT \n"
			+ "        cc.annee, f.lcog_geo, coalesce(round(surface_park_pcapita, 2), 0) as surface_park_pcapita,\n"
			+ "        coalesce(round(sum(cc.pop_inc), 0), 0) as pop_inc, coalesce(round(sum(cc.pop_exc), 0), 0) as pop_exc,\n"
			+ "         count(cc.id_inspire) AS nb_inspire\n"
			+ "    FROM public.carre200_computed_v2 cc \n"
			+ "    INNER JOIN public.filosofi_200m f \n"
			+ "        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m\n"
			+ "    group by\n"
			+ "        cc.annee,\n"
			+ "        f.lcog_geo,\n"
			+ "        coalesce(round(surface_park_pcapita, 2), 0) )\n"
			+ "\n"
			+ "SELECT \n"
			+ "    annee, surface_min, surface_max,seuil,\n"
			+ "    sum(pop_inc) as pop_inc, sum(pop_exc) as pop_exc, count(nb_inspire)\n"
			+ "FROM surface_range_dense r, stats, densite_ville d\n"
			+ "WHERE \n"
			+ "    stats.annee=:annee AND lcog_geo like '%'||d.insee_code||'%'\n"
			+ "	AND d.id_comm2co=:com2coId AND d.is_dense is true\n"
			+ "    AND surface_park_pcapita >= r.surface_min AND surface_park_pcapita < r.surface_max\n"
			+ "GROUP BY annee, surface_min, surface_max, seuil\n";
	@Query(value = QRY_COM2CO_DENSE, nativeQuery = true)
	public List<StatsSurface> getStatsForCom2CoDense(@Param("annee")Integer annee, @Param("com2coId") Long com2coId);


	final String QRY_COM2CO_SUBURBS = "WITH \n"
			+ "surface_range_peri AS (\n"
			+ "	select 0 as surface_min, 8 as surface_max, 0 as seuil UNION\n"
			+ "	select 8 as surface_min, 17 as surface_max, 0 as seuil UNION\n"
			+ "	select 17 as surface_min, 25 as surface_max, 0 as seuil UNION\n"
			+ "	select 25 as surface_min, 45 as surface_max, 1 as seuil UNION\n"
			+ "	select 45 as surface_min, 10000 as surface_max, 2 as seuil ),\n"
			+ "densite_ville AS (\n"
			+ "	SELECT insee_code, dens<='2' as is_dense, id_comm2co\n"
			+ "	FROM public.insee_densite_city d\n"
			+ "	INNER JOIN public.city c ON d.codgeo=c.insee_code ),\n"
			+ "stats AS (\n"
			+ "    SELECT \n"
			+ "        cc.annee, f.lcog_geo, coalesce(round(surface_park_pcapita, 2), 0) as surface_park_pcapita,\n"
			+ "        coalesce(round(sum(cc.pop_inc), 0), 0) as pop_inc, coalesce(round(sum(cc.pop_exc), 0), 0) as pop_exc,\n"
			+ "         count(cc.id_inspire) AS nb_inspire\n"
			+ "    FROM public.carre200_computed_v2 cc \n"
			+ "    INNER JOIN public.filosofi_200m f \n"
			+ "        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m\n"
			+ "    group by\n"
			+ "        cc.annee,\n"
			+ "        f.lcog_geo,\n"
			+ "        coalesce(round(surface_park_pcapita, 2), 0) )\n"
			+ "\n"
			+ "SELECT \n"
			+ "    annee, surface_min, surface_max,seuil,\n"
			+ "    sum(pop_inc) as pop_inc,sum(pop_exc) as pop_exc,count(nb_inspire)\n"
			+ "FROM surface_range_peri r, stats, densite_ville d\n"
			+ "WHERE \n"
			+ "    stats.annee=:annee AND lcog_geo like '%'||d.insee_code||'%'\n"
			+ "	AND d.id_comm2co=:com2coId AND d.is_dense is false\n"
			+ "    AND surface_park_pcapita >= r.surface_min AND surface_park_pcapita < r.surface_max\n"
			+ "GROUP BY annee, surface_min, surface_max, seuil";
	
	@Query(value = QRY_COM2CO_SUBURBS, nativeQuery = true)
	public List<StatsSurface> getStatsForCom2CoSubUrbs(@Param("annee")Integer annee, @Param("com2coId") Long com2coId);

	
	
	final String QRY_COM2CO = "WITH \n"
			+ "surface_range_dense AS (\n"
			+ "    select 0 as surface_min, 3 as surface_max, 0 as seuil UNION\n"
			+ "    select 3 as surface_min, 7 as surface_max, 0 as seuil UNION\n"
			+ "    select 7 as surface_min, 10 as surface_max, 0 as seuil UNION\n"
			+ "    select 10 as surface_min, 12 as surface_max, 1 as seuil UNION\n"
			+ "    select 12 as surface_min, 10000 as surface_max, 2 as seuil ),\n"
			+ "surface_range_peri AS (\n"
			+ "    select 0 as surface_min, 8 as surface_max, 0 as seuil UNION\n"
			+ "    select 8 as surface_min, 17 as surface_max, 0 as seuil UNION\n"
			+ "    select 17 as surface_min, 25 as surface_max, 0 as seuil UNION\n"
			+ "    select 25 as surface_min, 45 as surface_max, 1 as seuil UNION\n"
			+ "    select 45 as surface_min, 10000 as surface_max, 2 as seuil ),\n"
			+ "densite_ville AS (\n"
			+ "    SELECT insee_code, dens<='2' as is_dense, id_comm2co\n"
			+ "    FROM public.insee_densite_city d\n"
			+ "    INNER JOIN public.city c ON d.codgeo=c.insee_code ),\n"
			+ "stats AS (\n"
			+ "    SELECT \n"
			+ "        cc.annee, f.lcog_geo, coalesce(round(surface_park_pcapita, 2), 0) as surface_park_pcapita,\n"
			+ "        coalesce(round(sum(cc.pop_inc), 0), 0) as pop_inc, coalesce(round(sum(cc.pop_exc), 0), 0) as pop_exc,\n"
			+ "         count(cc.id_inspire) AS nb_inspire\n"
			+ "    FROM public.carre200_computed_v2 cc \n"
			+ "    INNER JOIN public.filosofi_200m f \n"
			+ "        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m\n"
			+ "    group by\n"
			+ "        cc.annee,\n"
			+ "        f.lcog_geo,\n"
			+ "        coalesce(round(surface_park_pcapita, 2), 0) )\n"
			+ "\n"
			+ "SELECT annee, 0 as surface_min, 0 as surface_max, seuil,sum(pop_inc) as pop_inc, sum(pop_exc) as pop_exc from (\n"
			+ "SELECT \n"
			+ "    annee, seuil,\n"
			+ "    sum(pop_inc) as pop_inc, sum(pop_exc) as pop_exc\n"
			+ "FROM surface_range_dense r, stats, densite_ville d\n"
			+ "WHERE \n"
			+ "    stats.annee=:annee AND lcog_geo like '%'||d.insee_code||'%'\n"
			+ "    AND d.id_comm2co=:com2coId AND d.is_dense is true\n"
			+ "    AND surface_park_pcapita >= r.surface_min AND surface_park_pcapita < r.surface_max\n"
			+ "GROUP BY annee, seuil\n"
			+ "UNION\n"
			+ "SELECT \n"
			+ "    annee, seuil,\n"
			+ "    sum(pop_inc) as pop_inc,sum(pop_exc) as pop_exc\n"
			+ "FROM surface_range_peri r, stats, densite_ville d\n"
			+ "WHERE \n"
			+ "    stats.annee=:annee AND lcog_geo like '%'||d.insee_code||'%'\n"
			+ "    AND d.id_comm2co=:com2coId AND d.is_dense is false\n"
			+ "    AND surface_park_pcapita >= r.surface_min AND surface_park_pcapita < r.surface_max\n"
			+ "GROUP BY annee, seuil\n"
			+ ") GROUP BY annee, seuil";
	
	@Query(value = QRY_COM2CO, nativeQuery = true)
	@Deprecated
	public List<StatsSurface> getStatsForCom2Co(@Param("annee")Integer annee, @Param("com2coId") Long com2coId);

	
}
