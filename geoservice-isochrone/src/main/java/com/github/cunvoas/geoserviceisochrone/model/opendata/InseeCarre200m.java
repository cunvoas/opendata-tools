package com.github.cunvoas.geoserviceisochrone.model.opendata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "carre200")
public class InseeCarre200m {
	
	@Id
	@Column(name="id",length=21)
	private String id;
	
	@Column(name="id_inspire", length=30)
	private String idInspire;
	
	@Column(name="idk",length=25)
	private String idk;

	@Column(name="ind_c",length=25)
	private String population;

	@Column(name="nbcar",length=25)
	private String nbcar;
	

}
