package com.github.cunvoas.geoserviceisochrone.model.opendata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Représente la relation de proximité entre deux entités cadastrales (communes).
 * Permet d'identifier les communes voisines.
 */
@Data
@Entity(name = "cadastre_proche")
@IdClass(CadastreProcheId.class)
@EqualsAndHashCode(of = {"idInsee", "idInseeProche"})
public class CadastreProche {
	
	@Id
	@Column(name="id_insee", length=5)
	/**
	 * Code INSEE de la commune principale.
	 */
	private String idInsee;
	
	@Id
	@Column(name="insee_proche", length=5)
	/**
	 * Code INSEE de la commune proche (voisine).
	 */
	private String idInseeProche;
}