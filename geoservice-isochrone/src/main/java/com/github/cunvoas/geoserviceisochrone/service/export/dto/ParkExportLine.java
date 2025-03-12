package com.github.cunvoas.geoserviceisochrone.service.export.dto;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;

/**
 * DTO.
 */
@Data
public class ParkExportLine implements Comparable<ParkExportLine> {
	
	
	private Long idRegion;
	private Long idCom2Co;
	
	private Long idCommune;
	private String commune;
	
	private Long idPark;
	private String parkName;
	
	private Long idTypePark;
	private String parkTypeName;
	private Boolean omsCustom;

	private Double surfaceOpendata;
	private Double surfaceContour; // saisie autmel
	
	private Long idParkArea;
	private Integer nbParkEntrance;
	private Date parkAreaComputedDate;

	@Override
	public int compareTo(ParkExportLine other) {
		CompareToBuilder ctb = new CompareToBuilder();
		ctb.append(this.idRegion, other.idRegion);
		ctb.append(this.idCom2Co, other.idCom2Co);
		ctb.append(this.idCommune, other.idCommune);
		ctb.append(this.parkName, other.parkName);
		return ctb.toComparison();
	}
	

	private static final NumberFormat NF = new DecimalFormat("#.##O");
	private static final DateFormat DF =new SimpleDateFormat("dd/MM/yyyy");
	
	public String[] map() {
		List<String> rec = new ArrayList<>(14);
		rec.add(String.valueOf(idRegion));
		rec.add(idCom2Co==null?String.valueOf(idCom2Co):"");
		rec.add(String.valueOf(idCommune));
		rec.add(String.valueOf(idPark));
		rec.add(idTypePark==null?String.valueOf(idTypePark):"");
		rec.add(idParkArea==null?String.valueOf(idParkArea):"");
		
		rec.add(commune);
		rec.add(parkName);
		rec.add(parkTypeName!=null?parkTypeName:"");
		rec.add(omsCustom!=null?String.valueOf(omsCustom):"");
		rec.add(surfaceOpendata!=null?NF.format(surfaceOpendata):"");
		rec.add(surfaceContour!=null?NF.format(surfaceContour):"");
		rec.add(nbParkEntrance!=null?String.valueOf(nbParkEntrance):"0");
		rec.add(parkAreaComputedDate!=null?DF.format(parkAreaComputedDate):"");
		
		return rec.toArray(new String[14]); 
	}
	
}
