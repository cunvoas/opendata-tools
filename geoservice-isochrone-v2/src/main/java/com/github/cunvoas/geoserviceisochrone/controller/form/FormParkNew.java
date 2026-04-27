package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;
import org.springframework.web.multipart.MultipartFile;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Form for Park page (new and current impl).
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class FormParkNew extends AbstractFormLocate{
	
	private static final DateFormat DF =new SimpleDateFormat("dd/MM/yyyy");

	private Long idPark;//parcEtJardin.id

	// parcs preselection
	private  List<ParkType> listTypePark;
	
	// data from DB
	private ParcEtJardin parcEtJardin;
	private ParcPrefecture parcPrefecture;
	
	// park photo
	private MultipartFile fileupload;
	
	//edit fields
	//parkArea
	private String description;
	private Long idParkType;	//ParkType
	
	
	/////////////
	private Long id;
	private City commune;
	private String name;
	private String quartier;
	private String hierarchie;
	private String type;
	private String sousType;
	private String dateDebut;
	private String dateFin;
	
	private Boolean ouverturePermanente = Boolean.TRUE;
	private Boolean aireJeux = Boolean.FALSE;
	
	private String adresse;
	@NumberFormat(pattern = "#,##0.0", style=Style.NUMBER)
	private Double surface;
	@NumberFormat(pattern = "#,##0.0", style=Style.NUMBER)
	private Double surfaceContour;
	
	private Point coordonnee;

	private Boolean hadGeometry;
	private String sGeometry;
	private String etatAction;
	private String etat;
	private Long typeId;
	private Boolean omsCustom;
	
	private ParcSourceEnum source = ParcSourceEnum.OPENDATA;
	
	// prefecture
	private String namePrefecture;
	private String status;
	
	/**
	 * constructor.
	 */
	public FormParkNew() {
		super();
	}
	/**
	 * Constucteur et Mapper.
	 * @param pj from model
	 */
	public FormParkNew(ParcEtJardin pj) {
		super();
		this.mapper(pj);
	}
	
	private void mapper(ParcEtJardin pj) {
		this.idPark = pj.getId();
		this.commune = pj.getCommune();
		this.name = pj.getName();
		this.quartier =  pj.getQuartier();
		this.hierarchie =  pj.getHierarchie();
		this.type =  pj.getType();
		this.typeId =  pj.getTypeId();
		this.sousType =  pj.getSousType();
		this.ouverturePermanente = "Permanente".equals(pj.getEtatOuverture());
		this.aireJeux =  "Oui".equals(pj.getAireJeux());
		this.adresse =  pj.getAdresse();
		this.surface =  pj.getSurface();
		this.coordonnee =  pj.getCoordonnee();
		this.source =  pj.getSource();
		this.surfaceContour = pj.getSurfaceContour();

		pj.setTypeId(this.typeId);
		pj.setOmsCustom(this.omsCustom);
		if (pj.getDateDebut()!=null) {
			this.setDateDebut(DF.format(pj.getDateDebut()));
		}
		if (pj.getDateFin()!=null) {
			this.setDateFin(DF.format(pj.getDateFin()));
		}
		
	}
	
	/**
	 * gen mapped Model.
	 * @return model
	 * @throws ParseException execption on parsing
	 */
	public ParcEtJardin mapper() throws ParseException {
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
		pj.setSurfaceContour(this.surfaceContour);
		pj.setCoordonnee(this.coordonnee);
		pj.setSource(this.source);

		pj.setTypeId(this.typeId);
		pj.setOmsCustom(this.omsCustom);
		
		if (StringUtils.isNoneBlank(this.getDateDebut())) {
			pj.setDateDebut(DF.parse(this.getDateDebut()));
		}
		if (StringUtils.isNoneBlank(this.getDateFin())) {
			pj.setDateFin(DF.parse(this.getDateFin()));
		}
		
		return pj;
	}
	
}
