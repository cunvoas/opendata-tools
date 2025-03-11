package com.github.cunvoas.geoserviceisochrone.model.dashboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Model DashboardCache.
 */
@Data
@EqualsAndHashCode(of = {"code"})
@Entity(name = "dashboard_cache")
public class DashboardCache {
	
	public static final String ASSOS="ASSOS";
	public static final String CONTRIB="CONTRIB";
	public static final String CARREAUX="CARREAUX";
	public static final String FILOSOFIL="FILOSOFIL";
	public static final String ANNES="ANNES";
	public static final String COM2CO="COM2CO";
	public static final String COMMUNE="COMMUNE";
	public static final String PARC_ENTREE="PARC_ENTREE";
	public static final String PARC_REF="PARC_REF";
	public static final String PARC_CHCK="PARC_CHCK";
	public static final String PARC_CALC="PARC_CALC";
	
	@Id
	@Column(name = "id", length = 50)
	private String code;
	@Column(name = "indicator_value")
	private Long indicator;
	
	public DashboardCache() {
		super();
	}
	
	public DashboardCache(String code, Long nb) {
		super();
		this.code=code;
		this.indicator=nb;
	}
	public DashboardCache(String code, Integer nb) {
		super();
		this.code=code;
		this.indicator=Long.valueOf(nb);
	}

}
