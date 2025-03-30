package com.github.cunvoas.geoserviceisochrone.model.opendata;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Model Iris.
 * @see https://www.insee.fr/fr/statistiques/7704076#dictionnaire
 */
@Data
@Entity(name = "iris_data")
@IdClass(IrisId.class)
@EqualsAndHashCode(of = {"annee", "iris"})
public class IrisData {
	
/*
 IRIS;COM;TYP_IRIS;LAB_IRIS;P20_POP;P20_POP0002;P20_POP0305;P20_POP0610;P20_POP1117;P20_POP1824;P20_POP2539;P20_POP4054;P20_POP5564;P20_POP6579;P20_POP80P;P20_POP0014;P20_POP1529;P20_POP3044;P20_POP4559;P20_POP6074;P20_POP75P;P20_POP0019;P20_POP2064;P20_POP65P;P20_POPH;P20_H0014;P20_H1529;P20_H3044;P20_H4559;P20_H6074;P20_H75P;P20_H0019;P20_H2064;P20_H65P;P20_POPF;P20_F0014;P20_F1529;P20_F3044;P20_F4559;P20_F6074;P20_F75P;P20_F0019;P20_F2064;P20_F65P;C20_POP15P;C20_POP15P_CS1;C20_POP15P_CS2;C20_POP15P_CS3;C20_POP15P_CS4;C20_POP15P_CS5;C20_POP15P_CS6;C20_POP15P_CS7;C20_POP15P_CS8;C20_H15P;C20_H15P_CS1;C20_H15P_CS2;C20_H15P_CS3;C20_H15P_CS4;C20_H15P_CS5;C20_H15P_CS6;C20_H15P_CS7;C20_H15P_CS8;C20_F15P;C20_F15P_CS1;C20_F15P_CS2;C20_F15P_CS3;C20_F15P_CS4;C20_F15P_CS5;C20_F15P_CS6;C20_F15P_CS7;C20_F15P_CS8;P20_POP_FR;P20_POP_ETR;P20_POP_IMM;P20_PMEN;P20_PHORMEN
 iris;com;typ_iris;lab_iris;p20_pop;p20_pop0002;p20_pop0305;p20_pop0610;p20_pop1117;p20_pop1824;p20_pop2539;p20_pop4054;p20_pop5564;p20_pop6579;p20_pop80p;p20_pop0014;p20_pop1529;p20_pop3044;p20_pop4559;p20_pop6074;p20_pop75p;p20_pop0019;p20_pop2064;p20_pop65p;p20_poph;p20_h0014;p20_h1529;p20_h3044;p20_h4559;p20_h6074;p20_h75p;p20_h0019;p20_h2064;p20_h65p;p20_popf;p20_f0014;p20_f1529;p20_f3044;p20_f4559;p20_f6074;p20_f75p;p20_f0019;p20_f2064;p20_f65p;c20_pop15p;c20_pop15p_cs1;c20_pop15p_cs2;c20_pop15p_cs3;c20_pop15p_cs4;c20_pop15p_cs5;c20_pop15p_cs6;c20_pop15p_cs7;c20_pop15p_cs8;c20_h15p;c20_h15p_cs1;c20_h15p_cs2;c20_h15p_cs3;c20_h15p_cs4;c20_h15p_cs5;c20_h15p_cs6;c20_h15p_cs7;c20_h15p_cs8;c20_f15p;c20_f15p_cs1;c20_f15p_cs2;c20_f15p_cs3;c20_f15p_cs4;c20_f15p_cs5;c20_f15p_cs6;c20_f15p_cs7;c20_f15p_cs8;p20_pop_fr;p20_pop_etr;p20_pop_imm;p20_pmen;p20_phormen
 010010000;01001;Z;5;806;31.4843129955518;26.9117464342402;53.7541896229158;77.4388345607983;28.293681022153;142.61682829573;166.360708502483;121.071226734928;119.551506796836;38.5169650343634;155.306149757562;97.1693796265234;148.761022415785;191.372049239565;143.616249071237;69.7751498893276;201.926627623821;446.00490054498;158.0684718312;410.480214921234;90.9886259824167;46.3339750677837;72.8218295695954;96.274332257511;70.0216194544927;34.0398325894342;114.880833542247;222.006255536212;73.5931258427748;395.519785078766;64.3175237751449;50.8354045587396;75.93919284619;95.0977169820541;73.5946296167442;35.7353172998934;87.045794081574;223.998645008767;84.4753459884248;658.136164144124;4.69227195834885;42.4917108710302;61.7742384237732;102.402052938306;98.4662824580651;122.293902369099;183.694112883898;42.3215922416032;345.543436022606;4.69227195834885;23.5509899461356;33.1056524922602;55.8119983033107;18.7706022954676;94.2146183877055;101.147311757299;14.2499908820791;312.592728121518;0;18.9407209248946;28.668585931513;46.5900546349957;79.6956801625975;28.0792839813934;82.5468011265994;28.0716013595241;796.562592158603;9.43740784139693;17.7835410427381;806;0
*/
	/** Année de la donnée. */
	@Id
	@Column(name="annee",length=4)
    private Integer annee;

	/** id iris. */
	@Id
	@Column(name = "iris", length = 9)
	private String iris;
	
	/** inse commune. */
	@Column(name = "com", length = 5)
	private String codeInsee;

	/** type d'IRIS : habitat (H), activité (A), divers (D), Autre (Z) */
	@Column(name = "typ_iris", length = 1)
	private String typIris;
	
	/** label de qualité de l'IRIS. */
	@Column(name = "lab_iris", length = 1)
	private String labIris;
	
	/**  population.*/
	@Column(name="p20_pop", precision = 16, scale = 4)
	private BigDecimal p20pop;
	/**  population 0-2 ans.*/
	@Column(name="p20_pop0002", precision = 16, scale = 4)
	private BigDecimal p20pop0002;
	/**  population 3-5 ans.*/
	@Column(name="p20_pop0305", precision = 16, scale = 4)
	private BigDecimal p20pop0305;
	/**  population 6-10 ans.*/
	@Column(name="p20_pop0610", precision = 16, scale = 4)
	private BigDecimal p20pop0610;
	/**  population 11-17 ans.*/
	@Column(name="p20_pop1117", precision = 16, scale = 4)
	private BigDecimal p20pop1117;
	/**  population 18-24 ans.*/
	@Column(name="p20_pop1824", precision = 16, scale = 4)
	private BigDecimal p20pop1824;
	/**  population 25-39 ans.*/
	@Column(name="p20_pop2539", precision = 16, scale = 4)
	private BigDecimal p20pop2539;
	/**  population 40-55 ans.*/
	@Column(name="p20_pop4054", precision = 16, scale = 4)
	private BigDecimal p20pop4054;
	/** population 55-64 ans .*/
	@Column(name="p20_pop5564", precision = 16, scale = 4)
	private BigDecimal p20pop5564;
	/**  population 65-79 ans.*/
	@Column(name="p20_pop6579", precision = 16, scale = 4)
	private BigDecimal p20pop6579;
	/**  population 80+ ans.*/
	@Column(name="p20_pop80p", precision = 16, scale = 4)
	private BigDecimal p20pop80p;
	/**  nombre de personnes de 0 à 14 ans .*/
	@Column(name="p20_pop0014", precision = 16, scale = 4)
	private BigDecimal p20pop0014;
	/**   nombre de personnes de 15 à 29 ans.*/
	@Column(name="p20_pop1529", precision = 16, scale = 4)
	private BigDecimal p20pop1529;
	/**  nombre de personnes de 30 à 44 ans .*/
	@Column(name="p20_pop3044", precision = 16, scale = 4)
	private BigDecimal p20pop3044;
	/**   nombre de personnes de 45 à 49 ans.*/
	@Column(name="p20_pop4559", precision = 16, scale = 4)
	private BigDecimal p20pop4559;
	/**   nombre de personnes de 60 à 74 ans.*/
	@Column(name="p20_pop6074", precision = 16, scale = 4)
	private BigDecimal p20pop6074;
	/**   nombre de personnes de 75+ ans.*/
	@Column(name="p20_pop75p", precision = 16, scale = 4)
	private BigDecimal p20pop75p;
	/**  nombre de personnes de 0 à 19 ans.*/
	@Column(name="p20_pop0019", precision = 16, scale = 4)
	private BigDecimal p20pop0019;
	/**  nombre de personnes de 20 à 64 ans.*/
	@Column(name="p20_pop2064", precision = 16, scale = 4)
	private BigDecimal p20pop2064;
	/**  nombre de personnes de 65+ ans.*/
	@Column(name="p20_pop65p", precision = 16, scale = 4)
	private BigDecimal p20pop65p;

	/**  nombre d'hommes.*/
	@Column(name="p20_poph", precision = 16, scale = 4)
	private BigDecimal p20poph;
	/**  nombre d'hommes de 0 à 14 ans.*/
	@Column(name="p20_h0014", precision = 16, scale = 4)
	private BigDecimal p20h0014;
	/**  nombre d'hommes de 15 à 29 ans.*/
	@Column(name="p20_h1529", precision = 16, scale = 4)
	private BigDecimal p20h1529;
	/**  nombre d'hommes de 30 à 44 ans.*/
	@Column(name="p20_h3044", precision = 16, scale = 4)
	private BigDecimal p20h3044;
	/**  nombre d'hommes de 45 à 59 ans.*/
	@Column(name="p20_h4559", precision = 16, scale = 4)
	private BigDecimal p20h4559;
	/**  nombre d'hommes de 60 à 74 ans.*/
	@Column(name="p20_h6074", precision = 16, scale = 4)
	private BigDecimal p20h6074;
	/**  nombre d'hommes de 75+ ans.*/
	@Column(name="p20_h75p", precision = 16, scale = 4)
	private BigDecimal p20h75p;
	/** nombre d'hommes de 0 à 19 ans.*/
	@Column(name="p20_h0019", precision = 16, scale = 4)
	private BigDecimal p20h0019;
	/**  nombre d'hommes de 20 à 64 ans.*/
	@Column(name="p20_h2064", precision = 16, scale = 4)
	private BigDecimal p20h2064;
	/**  nombre d'hommes de 65+ ans.*/
	@Column(name="p20_h65p", precision = 16, scale = 4)
	private BigDecimal p20h65p;
	

	/**  nombre de femmes.*/
	@Column(name="p20_popf", precision = 16, scale = 4)
	private BigDecimal p20popf;
	/**  nombre de femmes de 0 à 14 ans.*/
	@Column(name="p20_f0014", precision = 16, scale = 4)
	private BigDecimal p20f0014;
	/**  nombre de femmes de 15 à 29 ans.*/
	@Column(name="p20_f1529", precision = 16, scale = 4)
	private BigDecimal p20f1529;
	/**  nombre de femmes de 30 à 44 ans.*/
	@Column(name="p20_f3044", precision = 16, scale = 4)
	private BigDecimal p20f3044;
	/**  nombre de femmes de 20 à 44 ans.*/
	@Column(name="p20_f4559", precision = 16, scale = 4)
	private BigDecimal p20f4559;
	/**  nombre de femmes de 60 à 74 ans.*/
	@Column(name="p20_f6074", precision = 16, scale = 4)
	private BigDecimal p20f6074;
	/**  nombre de femmes de 75+ ans.*/
	@Column(name="p20_f75p", precision = 16, scale = 4)
	private BigDecimal p20f75p;
	/**  nombre de femmes de 0 à 19 ans.*/
	@Column(name="p20_f0019", precision = 16, scale = 4)
	private BigDecimal p20f0019;
	/**  nombre de femmes de 20 à 64 ans.*/
	@Column(name="p20_f2064", precision = 16, scale = 4)
	private BigDecimal p20f2064;
	/**  nombre de femmes de 65+ ans.*/
	@Column(name="p20_f65p", precision = 16, scale = 4)
	private BigDecimal p20f65p;

	/**  nombre de personnes de 15 ans ou plus.*/
	@Column(name="c20_pop15p", precision = 16, scale = 4)
	private BigDecimal c20pop15p;
	/**  nombre de personnes de 15 ans ou plus Agriculteurs exploitants.*/
	@Column(name="c20_pop15p_cs1", precision = 16, scale = 4)
	private BigDecimal c20pop15p_cs1;
	/**  nombre de personnes de 15 ans ou plus Artisans, Commerçants, Chefs d'entreprise.*/
	@Column(name="c20_pop15p_cs2", precision = 16, scale = 4)
	private BigDecimal c20pop15p_cs2;
	/**  nombre de personnes de 15 ans ou plus Cadres et Professions intellectuelles supérieures.*/
	@Column(name="c20_pop15p_cs3", precision = 16, scale = 4)
	private BigDecimal c20pop15p_cs3;
	/**  nombre de personnes de 15 ans ou plus Professions intermédiaires.*/
	@Column(name="c20_pop15p_cs4", precision = 16, scale = 4)
	private BigDecimal c20pop15p_cs4;
	/**  nombre de personnes de 15 ans ou plus Employés.*/
	@Column(name="c20_pop15p_cs5", precision = 16, scale = 4)
	private BigDecimal c20pop15p_cs5;
	/**  nombre de personnes de 15 ans ou plus Ouvriers.*/
	@Column(name="c20_pop15p_cs6", precision = 16, scale = 4)
	private BigDecimal c20pop15p_cs6;
	/**   nombre de personnes de 15 ans ou plus Retraités.*/
	@Column(name="c20_pop15p_cs7", precision = 16, scale = 4)
	private BigDecimal c20pop15p_cs7;
	/**  nombre de personnes de 15 ans ou plus Autres sans activité professionnelle.*/
	@Column(name="c20_pop15p_cs8", precision = 16, scale = 4)
	private BigDecimal c20pop15p_cs8;
	/**  nombre d'hommes de 15 ans ou plus.*/
	@Column(name="c20_h15p", precision = 16, scale = 4)
	private BigDecimal c20h15p;
	/**  nombre d'hommes de 15 ans ou plus Agriculteurs exploitants.*/
	@Column(name="c20_h15p_cs1", precision = 16, scale = 4)
	private BigDecimal c20h15p_cs1;
	/**  nombre d'hommes de 15 ans ou plus Artisans, Commerçants, Chefs d'entreprise.*/
	@Column(name="c20_h15p_cs2", precision = 16, scale = 4)
	private BigDecimal c20h15p_cs2;
	/**  nombre d'hommes de 15 ans ou plus Cadres et Professions intellectuelles supérieures.*/
	@Column(name="c20_h15p_cs3", precision = 16, scale = 4)
	private BigDecimal c20h15p_cs3;
	/**  nombre d'hommes de 15 ans ou plus Professions intermédiaires.*/
	@Column(name="c20_h15p_cs4", precision = 16, scale = 4)
	private BigDecimal c20h15p_cs4;
	/**  nombre d'hommes de 15 ans ou plus Employés.*/
	@Column(name="c20_h15p_cs5", precision = 16, scale = 4)
	private BigDecimal c20h15p_cs5;
	/**  nombre d'hommes de 15 ans ou plus Ouvriers.*/
	@Column(name="c20_h15p_cs6", precision = 16, scale = 4)
	private BigDecimal c20h15p_cs6;
	/**  nombre d'hommes de 15 ans ou plus Retraités.*/
	@Column(name="c20_h15p_cs7", precision = 16, scale = 4)
	private BigDecimal c20h15p_cs7;
	/**  nombre d'hommes de 15 ans ou plus Autres sans activité professionnelle.*/
	@Column(name="c20_h15p_cs8", precision = 16, scale = 4)
	private BigDecimal c20h15p_cs8;
	/**  nombre de femmes de 15 ans ou plus.*/
	@Column(name="c20_f15p", precision = 16, scale = 4)
	private BigDecimal c20f15p;
	/**  nombre de femmes de 15 ans ou plus Agriculteurs exploitants.*/
	@Column(name="c20_f15p_cs1", precision = 16, scale = 4)
	private BigDecimal c20f15p_cs1;
	/**  nombre de femmes de 15 ans ou plus Artisans, Commerçants, Chefs d'entreprise.*/
	@Column(name="c20_f15p_cs2", precision = 16, scale = 4)
	private BigDecimal c20f15p_cs2;
	/**   nombre de femmes de 15 ans ou plus Cadres et Professions intellectuelles supérieures.*/
	@Column(name="c20_f15p_cs3", precision = 16, scale = 4)
	private BigDecimal c20f15p_cs3;
	/**  nombre de femmes de 15 ans ou plus Professions intermédiaires.*/
	@Column(name="c20_f15p_cs4", precision = 16, scale = 4)
	private BigDecimal c20f15p_cs4;
	/**  nombre de femmes de 15 ans ou plus Employés.*/
	@Column(name="c20_f15p_cs5", precision = 16, scale = 4)
	private BigDecimal c20f15p_cs5;
	/**  nombre de femmes de 15 ans ou plus Ouvriers.*/
	@Column(name="c20_f15p_cs6", precision = 16, scale = 4)
	private BigDecimal c20f15p_cs6;
	/**  nombre de femmes de 15 ans ou plus Retraités.*/
	@Column(name="c20_f15p_cs7", precision = 16, scale = 4)
	private BigDecimal c20f15p_cs7;
	/**  nombre de femmes de 15 ans ou plus Autres sans activité professionnelle.*/
	@Column(name="c20_f15p_cs8", precision = 16, scale = 4)
	private BigDecimal c20f15p_cs8;

	/**  nombre de personnes de nationalité française.*/
	@Column(name="p20_pop_fr", precision = 16, scale = 4)
	private BigDecimal p20pop_fr;
	/**  nombre de personnes étrangères.*/
	@Column(name="p20_pop_etr", precision = 16, scale = 4)
	private BigDecimal p20pop_etr;
	/**  nombres de personnes immigrées .*/
	@Column(name="p20_pop_imm", precision = 16, scale = 4)
	private BigDecimal p20pop_imm;
	/**  population des ménages.*/
	@Column(name="p20_pmen", precision = 16, scale = 4)
	private BigDecimal p20pmen;
	/**  population hors ménages.*/
	@Column(name="p20_phormen", precision = 16, scale = 4)
	private BigDecimal p20phormen;
	

}
