package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Historique d'une observation NDVI retournée par Copernicus pour un parc.
 *
 * <p>Chaque réponse contenant des statistiques est conservée telle quelle :
 * période retournée, date d'acquisition satellite et valeurs. Cela permet de
 * reventiler les valeurs a posteriori sur les bonnes années, sans dépendre du
 * seul résultat recopié dans {@code park_area_computed}.</p>
 *
 * <p>Le NDVI Copernicus est agrégé sur une fenêtre de quelques jours et le
 * satellite ne repasse pas tous les ans au même endroit : l'historique garde
 * donc la trace de chaque observation réellement disponible.</p>
 */
@Data
@Entity(name = "ndvi_observation")
public class NdviObservation {

	/** Identifiant technique de l'observation. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	/** Identifiant de la zone de parc ({@code park_area.id}). */
	@Column(name = "park_area_id", nullable = false)
	private Long parkAreaId;

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
