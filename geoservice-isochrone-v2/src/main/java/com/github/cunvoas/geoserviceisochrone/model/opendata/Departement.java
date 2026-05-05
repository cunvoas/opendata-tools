package com.github.cunvoas.geoserviceisochrone.model.opendata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(of = {"code"})
@Entity(name = "adm_departement")
public class Departement {
	
	@Id
	@ToString.Include
	@Column(name = "code", length = 3)
	private String code;
	
	@Column(name = "nom", length = 100)
	private String nom;
	
	/**
	 * Région à laquelle appartient le département.
	 */
	@ManyToOne
	@JoinColumn(name="id_region", nullable=true)
	private Region region;
}
