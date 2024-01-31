package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "carre200_computed")
public class InseeCarre200mComputed {
	
	@Id
	@Column(name="id",length=21)
	private String idCarre200;

	
	// surface dont les personnes ont accès un parc à 200m (ou 2km en péri-urbain)
	@Column(name="surface_access_park", precision = 7, scale = 2)
	private BigDecimal surfaceWithPark;

	@Column(name="pop_all", precision = 7, scale = 2)
	private BigDecimal popAll;
	// prorata avec parc
	@Column(name="pop_inc", precision = 12, scale = 2)
	private BigDecimal popIncluded;
	// prorata sans parc
	@Column(name="pop_exc", precision = 12, scale = 2)
	private BigDecimal popExcluded;


	@Column(name="updated")
	private Date updated;
	
}
