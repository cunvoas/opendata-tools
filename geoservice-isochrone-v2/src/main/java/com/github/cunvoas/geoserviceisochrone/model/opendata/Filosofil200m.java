package com.github.cunvoas.geoserviceisochrone.model.opendata;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Représente les données démographiques et sociales issues de la base Filosofi pour un carreau de 200m.
 * Contient de nombreux indicateurs sur la population, les ménages et les logements à une maille fine.
 * Model Filosofil200m.
 */
@Data
@Entity(name = "filosofi_200m")
@IdClass(Filosofil200mId.class)
@EqualsAndHashCode(of = {"annee", "idInspire"})
public class Filosofil200m {
	
	/**
	 * Année de la donnée.
	 */
	@Id
	@Column(name="annee",length=4)
    private Integer annee;
	
	/**
	 *  Identifiant Inspire du carreau de 200 m.
	 */
	@Id
	@Column(name="idcar_200m",length=30)
    private String idInspire;
	
	/**
	 * Vaut 1 si le carreau est imputé par une valeur approchée, 0 sinon.
	 */
	@Column(name="i_est_200",length=1)
    private Integer approche200m;
	
	/**
	 * Identifiant Inspire du carreau de 1 km auquel appartient le carreau de 200 m.
	 */
	@Column(name="idcar_1km",length=30)
    private String idCarreau1000m;
	/**
	 * Vaut 1 si le carreau est imputé par une valeur approchée, 0 sinon.
	 */
	@Column(name="i_est_1km",length=1)
    private Double approche1000m;

	/**
	 * Identifiant Inspire du carreau de niveau naturel auquel appartient le carreau de 200 m.
	 */
	@Column(name="idcar_nat",length=30)
    private String idCarreauNaturel;

	/**
	 *  Code officiel géographique.
	 */
	@Column(name="lcog_geo",length=50)
    private String codeOfficielGeo;
	
	
	/**
	 * Numéro du groupe auquel appartient le carreau.
	 */
	@Column(name="groupe", precision = 16, scale = 4)
    private Integer groupe;
	
	
	/**
	 * Nombre d’individus.
	 */
	@Column(name="ind", precision = 16, scale = 4)
	private BigDecimal nbIndividus;
	
	/**
	 * Nombre d’individus de 0 à 3 ans.
	 */
	@Column(name="ind_0_3", precision = 16, scale = 4)
	private BigDecimal nbIndividus0a3ans;
	
	/**
	 * Nombre d’individus de 4 à 5 ans
	 */
	@Column(name="ind_4_5", precision = 16, scale = 4)
	private BigDecimal nbIndividus4a5ans;
	
	/**
	 * Nombre d’individus de 6 à 10 ans.
	 */
	@Column(name="ind_6_10", precision = 16, scale = 4)
	private BigDecimal nbIndividus6a10ans;
	
	/**
	 * Nombre d’individus de 11 à 17 ans.
	 */
	@Column(name="ind_11_17", precision = 16, scale = 4)
	private BigDecimal nbIndividus11a17ans;
	
	/**
	 * Nombre d’individus de 18 à 24 ans.
	 */
	@Column(name="ind_18_24", precision = 16, scale = 4)
	private BigDecimal nbIndividus18a24ans;
	
	/**
	 * Nombre d’individus de 25 à 39 ans.
	 */
	@Column(name="ind_25_39", precision = 16, scale = 4)
	private BigDecimal nbIndividus25a39ans;
	
	/**
	 * Nombre d’individus de 40 à 54 ans.
	 */
	@Column(name="ind_40_54", precision = 16, scale = 4)
	private BigDecimal nbIndividus40a54ans;
	
	/**
	 * Nombre d’individus de 55 à 64 ans.
	 */
	@Column(name="ind_55_64", precision = 16, scale = 4)
	private BigDecimal nbIndividus55a64ans;
	
	/**
	 * Nombre d’individus de 65 à 79 ans.
	 */
	@Column(name="ind_65_79", precision = 16, scale = 4)
	private BigDecimal nbIndividus65a79ans;
	
	/**
	 *  Nombre d’individus de 80 ans ou plus.
	 */
	@Column(name="ind_80p", precision = 16, scale = 4)
	private BigDecimal nbIndividus80ansPlus;
	
	/**
	 *  Nombre d’individus dont l’âge est inconnu.
	 */
	@Column(name="ind_inc", precision = 16, scale = 4)
	private BigDecimal nbIndividusAgeInconnu;
	

	/**
	 *  Nombre de ménages.
	 */
	@Column(name="men", precision = 16, scale = 4)
	private BigDecimal nbMenages;
	
	/**
	 *  Nombre de ménages pauvres.
	 */
	@Column(name="men_pauv", precision = 16, scale = 4)
	private BigDecimal nbMenagePauvre;
	
	/**
	 *  Nombre de ménages d’un seul individu.
	 */
	@Column(name="men_1ind", precision = 16, scale = 4)
	private BigDecimal nbMenage1pers;
	
	/**
	 *  Nombre de ménages de 5 individus ou plus.
	 */
	@Column(name="men_5ind", precision = 16, scale = 4)
	private BigDecimal nbMenage5persPlus;
	
	/**
	 *  Nombre de ménages propriétaires.
	 */
	@Column(name="men_prop", precision = 16, scale = 4)
	private BigDecimal nbMenageProprio;
	
	/**
	 *  Nombre de ménages monoparentaux.
	 */
	@Column(name="men_fmp", precision = 16, scale = 4)
	private BigDecimal nbMenageMonoparent;
	
	/**
	 *  Somme de la surface des logements du carreau.
	 */
	@Column(name="men_surf", precision = 16, scale = 4)
	private BigDecimal sommeSurfaceLogement;
	
	/**
	 *  Nombre de ménages en logements collectifs.
	 */
	@Column(name="men_coll", precision = 16, scale = 4)
	private BigDecimal nbMenageLogementCollectif;
	
	/**
	 *  Nombre de ménages en maison.
	 */
	@Column(name="men_mais", precision = 16, scale = 4)
	private BigDecimal nbMenageMaison;
	
	/**
	 *  Somme des niveaux de vie winsorisés des individus.
	 *  Winsorisation des niveaux de vie La winsorisation est une technique statistique de traitement des valeurs extrêmes d'une distribution, qui consiste à ramener à un seuil donné toutes les valeurs situées au-delà, ou en deçà, de ce seuil. Ces seuils peuvent être des quantiles particuliers de la distribution.
	 * @see https://fr.wikipedia.org/wiki/Winsorisation
	 */
	@Column(name="ind_snv", precision = 16, scale = 4)
	private BigDecimal sommeNivVieWinsorise;


	/**
	 *  Nombre de logements construits avant 1945.
	 */
	@Column(name="log_av45", precision = 16, scale = 4)
	private BigDecimal nbLogementAv1945;
	
	/**
	 *  Nombre de logements construits entre 1945 et 1969.
	 */
	@Column(name="log_45_70", precision = 16, scale = 4)
	private BigDecimal nbLogement1945a1969;
	
	/**
	 *  Nombre de logements construits entre 1970 et 1989.
	 */
	@Column(name="log_70_90", precision = 16, scale = 4)
	private BigDecimal nbLogement1970a1990;
	
	/**
	 *  Nombre de logements construits depuis 1990.
	 */
	@Column(name="log_ap90", precision = 16, scale = 4)
	private BigDecimal nbLogementAp1990;
	
	/**
	 * Nombre de logements dont la date de construction est inconnue.
	 */
	@Column(name="log_inc", precision = 16, scale = 4)
	private BigDecimal nbLogementDateInconnu;
	
	/**
	 * Nombre de logements sociaux.
	 */
	@Column(name="log_soc", precision = 16, scale = 4)
	private BigDecimal nbLogementSociaux;
	

}