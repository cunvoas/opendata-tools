package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.math.BigDecimal;
import java.util.Date;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 */
@Data
@EqualsAndHashCode(of = {"annee", "idInspire"})
@Entity(name = "carre200_computed_v2")
@IdClass(InseeCarre200mComputedId.class)
public class InseeCarre200mComputedV2 {
	
	/**
	 * Année de la donnée.
	 */
	@Id
	@Column(name="annee",length=4)
    private Integer annee;
	
	@Id
	@Column(name="idInspire",length=30)
	private String idInspire;

	@Column(name="id",length=21)
	private String idCarre200;
	
	
	/**
	 * population in the square.
	 */
	@Column(name="pop_all", precision = 12, scale = 2)
	private BigDecimal popAll;
	
	/**
	 * population with park (prorata surface)
	 */
	@Column(name="pop_inc", precision = 12, scale = 2)
	private BigDecimal popIncluded;
	
	/**
	 * population without park (prorata surface)
	 */
	@Column(name="pop_exc", precision = 12, scale = 2)
	private BigDecimal popExcluded;

	/**
	 * surface of the parks that is accessible.
	 */
	@Column(name="surface_access_park", precision = 12, scale = 2)
	private BigDecimal surfaceTotalPark;
	
	/**
	 * population that share the parks.
	 */
	@Column(name="pop_park_share", precision = 12, scale = 2)
	private BigDecimal populationInIsochrone;
	
	/**
	 * surface of the parks per inhabitant
	 */
	@Column(name="surface_park_pcapita", precision = 12, scale = 2)
	private BigDecimal surfaceParkPerCapita = BigDecimal.ZERO;
	
	
	
	
	// OMS Conditions

	/**
	 * population with OMS park (prorata surface)
	 */
	@Column(name="pop_inc_oms", precision = 12, scale = 2)
	private BigDecimal popIncludedOms;
	
	/**
	 * population without OMS park (prorata surface)
	 */
	@Column(name="pop_exc_oms", precision = 12, scale = 2)
	private BigDecimal popExcludedOms;
	
	/**
	 * surface of the parks OMS that is accessible.
	 */
	@Column(name="surface_access_park_oms", precision = 12, scale = 2)
	private BigDecimal surfaceTotalParkOms = BigDecimal.ZERO;

	/**
	 * population that share the parks OMS.
	 */
	@Column(name="pop_park_share_oms", precision = 12, scale = 2)
	private BigDecimal populationInIsochroneOms;

	/**
	 * surface of the parks OMS per inhabitant
	 */
	@Column(name="surface_park_pcapita_oms", precision = 12, scale = 2)
	private BigDecimal surfaceParkPerCapitaOms = BigDecimal.ZERO;
	

	@Column(name="is_dense")
	private Boolean isDense =Boolean.TRUE;

	@Column(name="updated")
	private Date updated;
	
	@Column(name="parc_comment",length=500)
	private String comments;
	
	
	// Réf : Tarzia V. European  Common Indicators - Towars a Local Sustainability Profile. Milan, Italie: Ambiante Italia Research Institute; 2003.
	// au moins un parc 5000 m² à 300m en zone dense

	@Column(name = "sustainable_park_is")
	private Boolean isSustainablePark = Boolean.FALSE;
	
	@Column(name="sustainable_park_pop", precision = 12, scale = 2)
	private BigDecimal populationWithSustainablePark;
	
}
