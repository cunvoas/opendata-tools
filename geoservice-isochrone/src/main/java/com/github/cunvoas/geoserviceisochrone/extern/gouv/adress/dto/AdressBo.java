package com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.locationtech.jts.geom.Point;

import lombok.Data;

/**
 * DTO.
 * Objet de transfert représentant une adresse issue de l'API adresse.data.gouv.fr.
 * <p>
 * Comparable selon le score de correspondance à la requête.
 */
@Data
public class AdressBo implements Comparable<AdressBo>{
	
	private String id;
	private String label;  // rue, cp, commune
    private Float score;	// proba de validité vs requete
	private String citycode; // code insee de le commune
    
	private Point point; // geopoint

	/**
	 * Compare deux adresses selon leur score de pertinence.
	 *
	 * @param other autre objet AdressBo à comparer
	 * @return résultat de la comparaison
	 */
	@Override
	public int compareTo(AdressBo other) {
		return (new CompareToBuilder())
				.append(this.score, other.score)
				.toComparison();
	}
	

}