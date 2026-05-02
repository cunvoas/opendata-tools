package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Modèle représentant un carreau de 200m calculé (version 2).
 * <p>
 * Contient les informations démographiques et de surface liées à l'accès aux parcs,
 * ainsi que des indicateurs OMS et de durabilité (surface accessible, population incluse/exclue, etc.).
 * Permet d'analyser la couverture en espaces verts à l'échelle fine et le respect des recommandations OMS.
 * </p>
 *
 * @author cunvoas
 */
@Data
@EqualsAndHashCode(of = {"annee", "idInspire"})
@Entity(name = "carre200_computed_v2")
@IdClass(InseeCarre200mComputedId.class)
public class InseeCarre200mComputedV2 {
	/** Année de la donnée. */
	@Id
	@Column(name="annee",length=4)
    private Integer annee;
	/** Identifiant Inspire du carreau de 200m. */
	@Id
	@Column(name="idInspire",length=30)
	private String idInspire;
	/** Population totale du carreau. */
	@Column(name="pop_all", precision = 12, scale = 2)
	private BigDecimal popAll;
	/** Population ayant accès à un parc (prorata surface). */
	@Column(name="pop_inc", precision = 12, scale = 2)
	private BigDecimal popIncluded;
	/** Population sans accès à un parc (prorata surface). */
	@Column(name="pop_exc", precision = 12, scale = 2)
	private BigDecimal popExcluded;
	/** Surface totale des parcs accessibles (m²). */
	@Column(name="surface_access_park", precision = 12, scale = 2)
	private BigDecimal surfaceTotalPark;
	/** Population partageant les parcs accessibles. */
	@Column(name="pop_park_share", precision = 12, scale = 2)
	private BigDecimal populationInIsochrone;
	/** Surface de parc accessible par habitant (m²/hab). */
	@Column(name="surface_park_pcapita", precision = 12, scale = 2)
	private BigDecimal surfaceParkPerCapita = BigDecimal.ZERO;
	// OMS Conditions
	/** Population ayant accès à un parc respectant le seuil OMS (prorata surface). */
	@Column(name="pop_inc_oms", precision = 12, scale = 2)
	private BigDecimal popIncludedOms;
	/** Population sans accès à un parc respectant le seuil OMS (prorata surface). */
	@Column(name="pop_exc_oms", precision = 12, scale = 2)
	private BigDecimal popExcludedOms;
	/** Surface totale des parcs OMS accessibles (m²). */
	@Column(name="surface_access_park_oms", precision = 12, scale = 2)
	private BigDecimal surfaceTotalParkOms = BigDecimal.ZERO;
	/** Population partageant les parcs OMS accessibles. */
	@Column(name="pop_park_share_oms", precision = 12, scale = 2)
	private BigDecimal populationInIsochroneOms;
	/** Surface de parc OMS accessible par habitant (m²/hab). */
	@Column(name="surface_park_pcapita_oms", precision = 12, scale = 2)
	private BigDecimal surfaceParkPerCapitaOms = BigDecimal.ZERO;
	/** Surface manquante pour atteindre le minimum OMS (10m²/hab). */
	@Column(name="miss_surface_mini", precision = 12, scale = 2)
	private BigDecimal missingSurfaceMini = BigDecimal.ZERO;
	/** Surface manquante pour atteindre le conseillé OMS (12m²/hab). */
	@Column(name="miss_surface_adv", precision = 12, scale = 2)
	private BigDecimal missingSurfaceAdvised = BigDecimal.ZERO;
	/** Indique si le carreau est en zone dense. */
	@Column(name="is_dense")
	private Boolean isDense =Boolean.TRUE;
	/** Date de dernière mise à jour. */
	@Column(name="updated")
	private Date updated;
	/** Commentaires éventuels sur le carreau. */
	@Column(name="parc_comment",length=2500)
	private String comments;
	// Réf : Tarzia V. European  Common Indicators - Towars a Local Sustainability Profile. Milan, Italie: Ambiante Italia Research Institute; 2003.
	// au moins un parc 5000 m² à 300m en zone dense
	/** Indique si le carreau dispose d'un parc durable (au moins 5000m² à 300m). */
	@Column(name = "sustainable_park_is")
	private Boolean isSustainablePark = Boolean.FALSE;
	/** Population ayant accès à un parc durable. */
	@Column(name="sustainable_park_pop", precision = 12, scale = 2)
	private BigDecimal populationWithSustainablePark;
}