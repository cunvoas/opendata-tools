package com.github.cunvoas.geoserviceisochrone.model.opendata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Model Cadastre.
 */
@Data
@Entity(name = "cadastre_proche")
@IdClass(CadastreProcheId.class)
@EqualsAndHashCode(of = {"idInsee", "idInseeProche"})
public class CadastreProche {
	
	@Id
	@Column(name="id_insee", length=5)
	private String idInsee;
	
	@Id
	@Column(name="insee_proche", length=5)
	private String idInseeProche;
}
