package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.web.multipart.MultipartFile;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

import lombok.Data;

@Data
public class FormParkNew extends AbstractFormLocate{

	private Long idPark;//parcEtJardin.id

	// parcs preselection
	private  List<ParkType> listTypePark;
	
	// data from DB
	private ParcEtJardin parcEtJardin;
	private ParcPrefecture parcPrefecture;
	
	// park photo
	private MultipartFile photo;
	
	//edit fields
	//parkArea
	private String description;
	private Long idParkType;	//ParkType
	
	//parkAreaCompute
	private Boolean oms;
	
	
	
	/////////////:
	private Long id; 
	private City commune;
	private String name;
	private String quartier;
	private String hierarchie;
	private String type;
	private String sousType;
	private Boolean ouverturePermanente = Boolean.TRUE;
	private Boolean aireJeux = Boolean.FALSE;
	
	private String adresse;
	private Double surface;
	private Point coordonnee;
	private ParcSourceEnum source = ParcSourceEnum.OPENDATA;
	
	// prefecture
	private String namePrefecture;
	private String status;
	
	public FormParkNew() {
		super();
	}
	/**
	 * Constucteur et Mapper.
	 * @param pj
	 */
	public FormParkNew(ParcEtJardin pj) {
		super();
		this.map(pj);
	}
	
	private void map(ParcEtJardin pj) {
		this.idPark = pj.getId();
		this.commune = pj.getCommune();
		this.name = pj.getName();
		this.quartier =  pj.getQuartier();
		this.hierarchie =  pj.getHierarchie();
		this.type =  pj.getType();
		this.sousType =  pj.getSousType();
		this.ouverturePermanente = "Permanente".equals(pj.getEtatOuverture());
		this.aireJeux =  "Oui".equals(pj.getAireJeux());
		this.adresse =  pj.getAdresse();
		this.surface =  pj.getSurface();
		this.coordonnee =  pj.getCoordonnee();
		this.source =  pj.getSource();
	}
	
	public ParcEtJardin map() {
		ParcEtJardin pj = new ParcEtJardin();
		pj.setId(this.idPark);
		pj.setCommune(this.commune);
		pj.setName(this.name);
		pj.setQuartier(this.quartier );
		pj.setHierarchie(this.hierarchie);
		pj.setType(this.type);
		pj.setSousType(this.sousType);
		pj.setEtatOuverture(this.ouverturePermanente?"Permanente":"Restreine");
		pj.setAireJeux(this.aireJeux?"Oui":"Non");
		pj.setAdresse(this.adresse);
		pj.setSurface(this.surface);
		pj.setCoordonnee(this.coordonnee);
		pj.setSource(this.source);
		return pj;
	}
	
}
