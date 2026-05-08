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
 * DTO représentant une ligne d'export pour un parc ou jardin.
 * Contient toutes les informations nécessaires à l'export CSV ou autre format tabulaire.
 */
@Data
public class ParkExportLine implements Comparable<ParkExportLine> {
	
	/** Identifiant de la région */
	private Long idRegion;
	/** Identifiant de la communauté de communes */
	private Long idCom2Co;
	/** Identifiant de la commune */
	private Long idCommune;
	/** Nom de la commune */
	private String commune;
	/** Identifiant du parc */
	private Long idPark;
	/** Nom du parc */
	private String parkName;
	/** Identifiant du type de parc */
	private Long idTypePark;
	/** Nom du type de parc */
	private String parkTypeName;
	/** Indique si le parc est customisé OMS */
	private Boolean omsCustom;
	/** Surface open data */
	private Double surfaceOpendata;
	/** Surface du contour (saisie automatique) */
	private Double surfaceContour;
	/** Identifiant de la zone de parc */
	private Long idParkArea;
	/** Nombre d'entrées du parc */
	private Integer nbParkEntrance;
	/** Date de calcul de la zone de parc */
	private java.util.Date parkAreaComputedDate;

	/**
	 * Compare cette ligne d'export à une autre pour le tri.
	 *
	 * @param other autre ligne d'export
	 * @return résultat de la comparaison
	 */
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
	
	/**
	 * Transforme la ligne d'export en tableau de chaînes pour l'écriture CSV.
	 * @return tableau de valeurs correspondant aux colonnes CSV
	 */
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