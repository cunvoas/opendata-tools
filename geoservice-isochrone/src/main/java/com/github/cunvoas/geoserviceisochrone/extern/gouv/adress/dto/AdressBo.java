package com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.locationtech.jts.geom.Point;

import lombok.Data;

/**
 * DTO.
 */
@Data
public class AdressBo implements Comparable<AdressBo>{
	
	private String id;
	private String label;  // rue, cp, commune
    private Float score;	// proba de validité vs requete
	private String citycode; // code insee de le commune
    
	private Point point; // geopoint

	@Override
	public int compareTo(AdressBo other) {
		return (new CompareToBuilder())
				.append(this.score, other.score)
				.toComparison();
	}
	

}
