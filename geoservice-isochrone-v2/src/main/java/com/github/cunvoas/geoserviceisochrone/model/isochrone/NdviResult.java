package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Mesure de végétation retournée par Copernicus.
 *
 * <p>Une mesure correspond à une période d'observation (mois/jour), pas à une
 * année de calcul. Elle porte les identifiants des objets auxquels elle se
 * rattache ainsi que le type d'objet : parc calculé, parc brut, IRIS, cadastre
 * ou carreau 200 m.</p>
 *
 * <p>Les identifiants sont stockés sans association Hibernate : les lectures
 * NDVI sont fréquentes et ne doivent pas déclencher de jointures implicites.</p>
 */
@Data
@Entity(name = "ndvi_result")
@Table(indexes = {
		@Index(name = "idx_ndvi_result_park_period",
				columnList = "object_type, park_area_computed_id, period_end"),
		@Index(name = "idx_ndvi_result_parc", columnList = "parc_et_jardin_id")
})
public class NdviResult {

	/** Identifiant technique de la mesure. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	/** Type d'objet portant la mesure. */
	@Enumerated(EnumType.STRING)
	@Column(name = "object_type", nullable = false, length = 30)
	private NdviResultObjectType objectType;

	/** Identifiant de la ligne {@code park_area_computed.id}. */
	@Column(name = "park_area_computed_id")
	private Long parkAreaComputedId;

	/** Identifiant du parc ou jardin d'origine ({@code parc_jardin.identifiant}). */
	@Column(name = "parc_et_jardin_id")
	private Long parcEtJardinId;

	/** Code IRIS de contexte ({@code iris_shape.code_iris}). */
	@Column(name = "iris_id", length = 9)
	private String irisId;

	/** Code INSEE du cadastre de contexte ({@code cadastre.id_insee}). */
	@Column(name = "cadastre_id", length = 5)
	private String cadastreId;

	/** Identifiant Inspire du carreau 200m de contexte. */
	@Column(name = "carre200m_id", length = 30)
	private String carre200mId;

	/** Date de l'appel Copernicus. */
	@Column(name = "requested_at", nullable = false)
	private Date requestedAt;

	/** Début de la fenêtre d'agrégation retournée par Copernicus. */
	@Column(name = "period_start")
	private Date periodStart;

	/** Fin de la fenêtre d'agrégation retournée par Copernicus. */
	@Column(name = "period_end")
	private Date periodEnd;

	/** Nombre de pixels valides utilisés par Copernicus. */
	@Column(name = "sample_count")
	private Integer sampleCount;

	/** NDVI moyen observé. */
	@Column(name = "ndvi_mean", precision = 6, scale = 4)
	private BigDecimal ndviMean;

	/** NDVI minimal observé. */
	@Column(name = "ndvi_min", precision = 6, scale = 4)
	private BigDecimal ndviMin;

	/** NDVI maximal observé. */
	@Column(name = "ndvi_max", precision = 6, scale = 4)
	private BigDecimal ndviMax;

	/** Écart-type du NDVI observé. */
	@Column(name = "ndvi_stddev", precision = 6, scale = 4)
	private BigDecimal ndviStddev;

	/** NDRE moyen observé (B08/B05). */
	@Column(name = "ndre_mean", precision = 6, scale = 4)
	private BigDecimal ndreMean;

	/** NDRE minimal observé. */
	@Column(name = "ndre_min", precision = 6, scale = 4)
	private BigDecimal ndreMin;

	/** NDRE maximal observé. */
	@Column(name = "ndre_max", precision = 6, scale = 4)
	private BigDecimal ndreMax;

	/** Écart-type du NDRE observé. */
	@Column(name = "ndre_stddev", precision = 6, scale = 4)
	private BigDecimal ndreStddev;

	/** GNDVI moyen observé (B08/B03). */
	@Column(name = "gndvi_mean", precision = 6, scale = 4)
	private BigDecimal gndviMean;

	/** GNDVI minimal observé. */
	@Column(name = "gndvi_min", precision = 6, scale = 4)
	private BigDecimal gndviMin;

	/** GNDVI maximal observé. */
	@Column(name = "gndvi_max", precision = 6, scale = 4)
	private BigDecimal gndviMax;

	/** Écart-type du GNDVI observé. */
	@Column(name = "gndvi_stddev", precision = 6, scale = 4)
	private BigDecimal gndviStddev;
}
