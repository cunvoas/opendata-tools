package com.github.cunvoas.geoserviceisochrone.repo.analytics;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.analytics.StatsSurface;
import com.github.cunvoas.geoserviceisochrone.model.analytics.StatsSurfaceId;
import com.github.cunvoas.geoserviceisochrone.repo.extend.ReadOnlyRepository;

/**
 * Répartition de la population par tranche de m² de parc OMS accessible par habitant.
 * <p>
 * Règles communes à toutes les requêtes :
 * <ul>
 *   <li>la tranche est choisie avec le ratio <b>OMS</b> ({@code surface_park_pcapita_oms}), cohérent avec
 *       l'accès {@code pop_inc} calculé sur les seuls parcs OMS ;</li>
 *   <li>un carreau à cheval sur plusieurs communes ({@code lcog_geo} de la forme {@code INSEE1_INSEE2}) est
 *       réparti entre elles au prorata de sa surface dans le cadastre de chacune, la somme des parts valant 1 :
 *       le total d'un EPCI est ainsi la somme exacte de ses communes ;</li>
 *   <li>bornes de tranche : minimum inclus, maximum exclu ; lignes triées par borne minimum.</li>
 * </ul>
 * Limite : le prorata suppose les habitants répartis uniformément dans le carreau, et un code commune de
 * {@code lcog_geo} absent du cadastre actuel (fusion de communes) reçoit une part nulle.
 */
@Repository
public interface StatsSurfaceRepository extends ReadOnlyRepository<StatsSurface, StatsSurfaceId> {

	/** Tranches des communes denses. */
	final String RANGE_DENSE = "SELECT 0 AS surface_min, 3 AS surface_max, 0 AS seuil UNION "
			+ "SELECT 3, 7, 0 UNION SELECT 7, 10, 0 UNION SELECT 10, 12, 1 UNION SELECT 12, 10000, 2";

	/** Tranches des communes périurbaines. */
	final String RANGE_PERI = "SELECT 0 AS surface_min, 8 AS surface_max, 0 AS seuil UNION "
			+ "SELECT 8, 17, 0 UNION SELECT 17, 25, 0 UNION SELECT 25, 45, 1 UNION SELECT 45, 10000, 2";

	/** Densité INSEE des communes et leur EPCI. */
	final String CTE_DENSITE = "densite_ville AS (SELECT insee_code, dens<='2' AS is_dense, id_comm2co "
			+ "FROM public.insee_densite_city d INNER JOIN public.city c ON d.codgeo=c.insee_code)";

	/**
	 * Parts de chaque carreau Filosofi par commune ({@code part}). Attend une CTE {@code communes(insee_code)}.
	 * Un carreau d'une seule commune vaut 1 ; un carreau à cheval est réparti au prorata de la surface
	 * d'intersection carreau/cadastre, normalisée sur les communes listées (partage égal si aucune surface).
	 */
	final String CTE_PART = "cand AS (SELECT f.annee, f.idcar_200m AS id_inspire, f.lcog_geo "
			+ "FROM public.filosofi_200m f WHERE f.annee = :annee "
			+ "AND string_to_array(f.lcog_geo, '_') && (SELECT array_agg(insee_code) FROM communes)), "
			+ "part AS (SELECT w.annee, w.id_inspire, w.insee, "
			+ "CAST(COALESCE(w.aire / NULLIF(SUM(w.aire) OVER (PARTITION BY w.annee, w.id_inspire), 0), 1.0 / w.nb) AS numeric) AS part "
			+ "FROM (SELECT cand.annee, cand.id_inspire, u.insee, "
			+ "count(*) OVER (PARTITION BY cand.annee, cand.id_inspire) AS nb, "
			+ "CASE WHEN position('_' IN cand.lcog_geo) = 0 THEN 1.0 "
			+ "ELSE COALESCE(ST_Area(ST_Intersection(s.geo_shape, k.geo_shape)), 0) END AS aire "
			+ "FROM cand CROSS JOIN LATERAL unnest(string_to_array(cand.lcog_geo, '_')) AS u(insee) "
			+ "LEFT JOIN public.carre200onlyshape s ON position('_' IN cand.lcog_geo) > 0 AND s.id_inspire = cand.id_inspire "
			+ "LEFT JOIN public.cadastre k ON position('_' IN cand.lcog_geo) > 0 AND k.id_insee = u.insee) w)";

	/** Agrégation finale. Attend les CTE {@code ranges}, {@code communes} et {@code part}. */
	final String SELECT_STATS = "SELECT p.annee, r.surface_min, r.surface_max, r.seuil, "
			+ "COALESCE(ROUND(SUM(cc.pop_inc * p.part), 0), 0) AS pop_inc, "
			+ "COALESCE(ROUND(SUM(cc.pop_exc * p.part), 0), 0) AS pop_exc "
			+ "FROM part p JOIN communes c ON c.insee_code = p.insee "
			+ "JOIN public.carre200_computed_v2 cc ON cc.annee = p.annee AND cc.id_inspire = p.id_inspire "
			+ "JOIN ranges r ON r.surface_min <= COALESCE(ROUND(cc.surface_park_pcapita_oms, 2), 0) "
			+ "AND COALESCE(ROUND(cc.surface_park_pcapita_oms, 2), 0) < r.surface_max "
			+ "GROUP BY p.annee, r.surface_min, r.surface_max, r.seuil "
			+ "ORDER BY r.surface_min";

	final String QRY_CITY = "WITH ranges AS (" + RANGE_DENSE + "), "
			+ "communes AS (SELECT CAST(:insee AS text) AS insee_code), "
			+ CTE_PART + " " + SELECT_STATS;

	final String QRY_SUBURBS = "WITH ranges AS (" + RANGE_PERI + "), "
			+ "communes AS (SELECT CAST(:insee AS text) AS insee_code), "
			+ CTE_PART + " " + SELECT_STATS;

	final String QRY_COM2CO_DENSE = "WITH ranges AS (" + RANGE_DENSE + "), "
			+ CTE_DENSITE + ", "
			+ "communes AS (SELECT CAST(insee_code AS text) AS insee_code FROM densite_ville "
			+ "WHERE id_comm2co = :com2coId AND is_dense IS TRUE), "
			+ CTE_PART + " " + SELECT_STATS;

	final String QRY_COM2CO_SUBURBS = "WITH ranges AS (" + RANGE_PERI + "), "
			+ CTE_DENSITE + ", "
			+ "communes AS (SELECT CAST(insee_code AS text) AS insee_code FROM densite_ville "
			+ "WHERE id_comm2co = :com2coId AND is_dense IS FALSE), "
			+ CTE_PART + " " + SELECT_STATS;

	/**
	 * Statistiques d'une commune dense.
	 * @param annee année Filosofi
	 * @param insee code INSEE de la commune
	 * @return une ligne par tranche non vide, triée par borne minimum
	 */
	@Query(value = QRY_CITY, nativeQuery = true)
	public List<StatsSurface> getStatsForCity(@Param("annee")Integer annee, @Param("insee") String insee);

	/**
	 * Statistiques d'une commune périurbaine.
	 * @param annee année Filosofi
	 * @param insee code INSEE de la commune
	 * @return une ligne par tranche non vide, triée par borne minimum
	 */
	@Query(value = QRY_SUBURBS, nativeQuery = true)
	public List<StatsSurface> getStatsForSuburbs(@Param("annee")Integer annee, @Param("insee") String insee);

	/**
	 * Statistiques des communes denses d'un EPCI.
	 * @param annee année Filosofi
	 * @param com2coId identifiant de l'EPCI
	 * @return une ligne par tranche non vide, triée par borne minimum
	 */
	@Query(value = QRY_COM2CO_DENSE, nativeQuery = true)
	public List<StatsSurface> getStatsForCom2CoDense(@Param("annee")Integer annee, @Param("com2coId") Long com2coId);

	/**
	 * Statistiques des communes périurbaines d'un EPCI.
	 * @param annee année Filosofi
	 * @param com2coId identifiant de l'EPCI
	 * @return une ligne par tranche non vide, triée par borne minimum
	 */
	@Query(value = QRY_COM2CO_SUBURBS, nativeQuery = true)
	public List<StatsSurface> getStatsForCom2CoSubUrbs(@Param("annee")Integer annee, @Param("com2coId") Long com2coId);

}
