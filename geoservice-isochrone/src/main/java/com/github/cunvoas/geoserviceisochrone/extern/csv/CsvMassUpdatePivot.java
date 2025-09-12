package com.github.cunvoas.geoserviceisochrone.extern.csv;

import java.util.ArrayList;
import java.util.List;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Objet de transfert (DTO) pour la mise à jour massive des parcs et jardins.
 * Contient les informations de la région, de la communauté de communes, de la commune,
 * du parc et de ses entrées associées.
 */
@Data
@NoArgsConstructor
public class CsvMassUpdatePivot {
	
	private Region region;
	private CommunauteCommune comm2co;
	private City commune;
	
	private ParcEtJardin parcEtJardin;
	private List<ParkEntrance> entrances = new ArrayList<>();

}