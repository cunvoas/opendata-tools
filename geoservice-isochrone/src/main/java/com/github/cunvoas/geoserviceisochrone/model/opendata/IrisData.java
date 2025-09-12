package com.github.cunvoas.geoserviceisochrone.model.opendata;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Représente les données démographiques et sociales d'un IRIS (Ilots Regroupés pour l'Information Statistique).
 * Permet de décrire la population, sa structure par âge et sexe, ainsi que des indicateurs socio-économiques à l'échelle fine.
 * @see https://www.insee.fr/fr/statistiques/7704076#dictionnaire
 */
@Data
@Entity(name = "iris_data")
@IdClass(IrisId.class)
@EqualsAndHashCode(of = {"annee", "iris"})
public class IrisData {
	
/*
 IRIS;COM;TYP_IRIS;LAB_IRIS;POP;POP0002;POP0305;POP0610;POP1117;POP1824;POP2539;POP4054;POP5564;POP6579;POP80P;POP0014;POP1529;POP3044;POP4559;POP6074;POP75P;POP0019;POP2064;POP65P;POPH;H0014;H1529;H3044;H4559;H6074;H75P;H0019;H2064;H65P;POPF;F0014;F1529;F3044;F4559;F6074;F75P;F0019;F2064;F65P;POP15P;POP15P_CS1;POP15P_CS2;POP15P_CS3;POP15P_CS4;POP15P_CS5;POP15P_CS6;POP15P_CS7;POP15P_CS8;H15P;H15P_CS1;H15P_CS2;H15P_CS3;H15P_CS4;H15P_CS5;H15P_CS6;H15P_CS7;H15P_CS8;F15P;F15P_CS1;F15P_CS2;F15P_CS3;F15P_CS4;F15P_CS5;F15P_CS6;F15P_CS7;F15P_CS8;POP_FR;POP_ETR;POP_IMM;PMEN;PHORMEN
 iris;com;typ_iris;lab_iris;pop;pop0002;pop0305;pop0610;pop1117;pop1824;pop2539;pop4054;pop5564;pop6579;pop80p;pop0014;pop1529;pop3044;pop4559;pop6074;pop75p;pop0019;pop2064;pop65p;poph;h0014;h1529;h3044;h4559;h6074;h75p;h0019;h2064;h65p;popf;f0014;f1529;f3044;f4559;f6074;f75p;f0019;f2064;f65p;pop15p;pop15p_cs1;pop15p_cs2;pop15p_cs3;pop15p_cs4;pop15p_cs5;pop15p_cs6;pop15p_cs7;pop15p_cs8;h15p;h15p_cs1;h15p_cs2;h15p_cs3;h15p_cs4;h15p_cs5;h15p_cs6;h15p_cs7;h15p_cs8;f15p;f15p_cs1;f15p_cs2;f15p_cs3;f15p_cs4;f15p_cs5;f15p_cs6;f15p_cs7;f15p_cs8;pop_fr;pop_etr;pop_imm;pmen;phormen
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
	@Column(name="pop", precision = 16, scale = 4)
	private BigDecimal pop;
	/**  population 0-2 ans.*/
	@Column(name="pop0002", precision = 16, scale = 4)
	private BigDecimal pop0002;
	/**  population 3-5 ans.*/
	@Column(name="pop0305", precision = 16, scale = 4)
	private BigDecimal pop0305;
	/**  population 6-10 ans.*/
	@Column(name="pop0610", precision = 16, scale = 4)
	private BigDecimal pop0610;
	/**  population 11-17 ans.*/
	@Column(name="pop1117", precision = 16, scale = 4)
	private BigDecimal pop1117;
	/**  population 18-24 ans.*/
	@Column(name="pop1824", precision = 16, scale = 4)
	private BigDecimal pop1824;
	/**  population 25-39 ans.*/
	@Column(name="pop2539", precision = 16, scale = 4)
	private BigDecimal pop2539;
	/**  population 40-55 ans.*/
	@Column(name="pop4054", precision = 16, scale = 4)
	private BigDecimal pop4054;
	/** population 55-64 ans .*/
	@Column(name="pop5564", precision = 16, scale = 4)
	private BigDecimal pop5564;
	/**  population 65-79 ans.*/
	@Column(name="pop6579", precision = 16, scale = 4)
	private BigDecimal pop6579;
	/**  population 80+ ans.*/
	@Column(name="pop80p", precision = 16, scale = 4)
	private BigDecimal pop80p;
	/**  nombre de personnes de 0 à 14 ans .*/
	@Column(name="pop0014", precision = 16, scale = 4)
	private BigDecimal pop0014;
	/**   nombre de personnes de 15 à 29 ans.*/
	@Column(name="pop1529", precision = 16, scale = 4)
	private BigDecimal pop1529;
	/**  nombre de personnes de 30 à 44 ans .*/
	@Column(name="pop3044", precision = 16, scale = 4)
	private BigDecimal pop3044;
	/**   nombre de personnes de 45 à 49 ans.*/
	@Column(name="pop4559", precision = 16, scale = 4)
	private BigDecimal pop4559;
	/**   nombre de personnes de 60 à 74 ans.*/
	@Column(name="pop6074", precision = 16, scale = 4)
	private BigDecimal pop6074;
	/**   nombre de personnes de 75+ ans.*/
	@Column(name="pop75p", precision = 16, scale = 4)
	private BigDecimal pop75p;
	/**  nombre de personnes de 0 à 19 ans.*/
	@Column(name="pop0019", precision = 16, scale = 4)
	private BigDecimal pop0019;
	/**  nombre de personnes de 20 à 64 ans.*/
	@Column(name="pop2064", precision = 16, scale = 4)
	private BigDecimal pop2064;
	/**  nombre de personnes de 65+ ans.*/
	@Column(name="pop65p", precision = 16, scale = 4)
	private BigDecimal pop65p;

	/**  nombre d'hommes.*/
	@Column(name="poph", precision = 16, scale = 4)
	private BigDecimal poph;
	/**  nombre d'hommes de 0 à 14 ans.*/
	@Column(name="h0014", precision = 16, scale = 4)
	private BigDecimal h0014;
	/**  nombre d'hommes de 15 à 29 ans.*/
	@Column(name="h1529", precision = 16, scale = 4)
	private BigDecimal h1529;
	/**  nombre d'hommes de 30 à 44 ans.*/
	@Column(name="h3044", precision = 16, scale = 4)
	private BigDecimal h3044;
	/**  nombre d'hommes de 45 à 59 ans.*/
	@Column(name="h4559", precision = 16, scale = 4)
	private BigDecimal h4559;
	/**  nombre d'hommes de 60 à 74 ans.*/
	@Column(name="h6074", precision = 16, scale = 4)
	private BigDecimal h6074;
	/**  nombre d'hommes de 75+ ans.*/
	@Column(name="h75p", precision = 16, scale = 4)
	private BigDecimal h75p;
	/** nombre d'hommes de 0 à 19 ans.*/
	@Column(name="h0019", precision = 16, scale = 4)
	private BigDecimal h0019;
	/**  nombre d'hommes de 20 à 64 ans.*/
	@Column(name="h2064", precision = 16, scale = 4)
	private BigDecimal h2064;
	/**  nombre d'hommes de 65+ ans.*/
	@Column(name="h65p", precision = 16, scale = 4)
	private BigDecimal h65p;
	

	/**  nombre de femmes.*/
	@Column(name="popf", precision = 16, scale = 4)
	private BigDecimal popf;
	/**  nombre de femmes de 0 à 14 ans.*/
	@Column(name="f0014", precision = 16, scale = 4)
	private BigDecimal f0014;
	/**  nombre de femmes de 15 à 29 ans.*/
	@Column(name="f1529", precision = 16, scale = 4)
	private BigDecimal f1529;
	/**  nombre de femmes de 30 à 44 ans.*/
	@Column(name="f3044", precision = 16, scale = 4)
	private BigDecimal f3044;
	/**  nombre de femmes de 20 à 44 ans.*/
	@Column(name="f4559", precision = 16, scale = 4)
	private BigDecimal f4559;
	/**  nombre de femmes de 60 à 74 ans.*/
	@Column(name="f6074", precision = 16, scale = 4)
	private BigDecimal f6074;
	/**  nombre de femmes de 75+ ans.*/
	@Column(name="f75p", precision = 16, scale = 4)
	private BigDecimal f75p;
	/**  nombre de femmes de 0 à 19 ans.*/
	@Column(name="f0019", precision = 16, scale = 4)
	private BigDecimal f0019;
	/**  nombre de femmes de 20 à 64 ans.*/
	@Column(name="f2064", precision = 16, scale = 4)
	private BigDecimal f2064;
	/**  nombre de femmes de 65+ ans.*/
	@Column(name="f65p", precision = 16, scale = 4)
	private BigDecimal f65p;

	/**  nombre de personnes de 15 ans ou plus.*/
	@Column(name="pop15p", precision = 16, scale = 4)
	private BigDecimal pop15p;
	/**  nombre de personnes de 15 ans ou plus Agriculteurs exploitants.*/
	@Column(name="pop15p_cs1", precision = 16, scale = 4)
	private BigDecimal pop15p_cs1;
	/**  nombre de personnes de 15 ans ou plus Artisans, Commerçants, Chefs d'entreprise.*/
	@Column(name="pop15p_cs2", precision = 16, scale = 4)
	private BigDecimal pop15p_cs2;
	/**  nombre de personnes de 15 ans ou plus Cadres et Professions intellectuelles supérieures.*/
	@Column(name="pop15p_cs3", precision = 16, scale = 4)
	private BigDecimal pop15p_cs3;
	/**  nombre de personnes de 15 ans ou plus Professions intermédiaires.*/
	@Column(name="pop15p_cs4", precision = 16, scale = 4)
	private BigDecimal pop15p_cs4;
	/**  nombre de personnes de 15 ans ou plus Employés.*/
	@Column(name="pop15p_cs5", precision = 16, scale = 4)
	private BigDecimal pop15p_cs5;
	/**  nombre de personnes de 15 ans ou plus Ouvriers.*/
	@Column(name="pop15p_cs6", precision = 16, scale = 4)
	private BigDecimal pop15p_cs6;
	/**   nombre de personnes de 15 ans ou plus Retraités.*/
	@Column(name="pop15p_cs7", precision = 16, scale = 4)
	private BigDecimal pop15p_cs7;
	/**  nombre de personnes de 15 ans ou plus Autres sans activité professionnelle.*/
	@Column(name="pop15p_cs8", precision = 16, scale = 4)
	private BigDecimal pop15p_cs8;
	/**  nombre d'hommes de 15 ans ou plus.*/
	@Column(name="h15p", precision = 16, scale = 4)
	private BigDecimal h15p;
	/**  nombre d'hommes de 15 ans ou plus Agriculteurs exploitants.*/
	@Column(name="h15p_cs1", precision = 16, scale = 4)
	private BigDecimal h15p_cs1;
	/**  nombre d'hommes de 15 ans ou plus Artisans, Commerçants, Chefs d'entreprise.*/
	@Column(name="h15p_cs2", precision = 16, scale = 4)
	private BigDecimal h15p_cs2;
	/**  nombre d'hommes de 15 ans ou plus Cadres et Professions intellectuelles supérieures.*/
	@Column(name="h15p_cs3", precision = 16, scale = 4)
	private BigDecimal h15p_cs3;
	/**  nombre d'hommes de 15 ans ou plus Professions intermédiaires.*/
	@Column(name="h15p_cs4", precision = 16, scale = 4)
	private BigDecimal h15p_cs4;
	/**  nombre d'hommes de 15 ans ou plus Employés.*/
	@Column(name="h15p_cs5", precision = 16, scale = 4)
	private BigDecimal h15p_cs5;
	/**  nombre d'hommes de 15 ans ou plus Ouvriers.*/
	@Column(name="h15p_cs6", precision = 16, scale = 4)
	private BigDecimal h15p_cs6;
	/**  nombre d'hommes de 15 ans ou plus Retraités.*/
	@Column(name="h15p_cs7", precision = 16, scale = 4)
	private BigDecimal h15p_cs7;
	/**  nombre d'hommes de 15 ans ou plus Autres sans activité professionnelle.*/
	@Column(name="h15p_cs8", precision = 16, scale = 4)
	private BigDecimal h15p_cs8;
	/**  nombre de femmes de 15 ans ou plus.*/
	@Column(name="f15p", precision = 16, scale = 4)
	private BigDecimal f15p;
	/**  nombre de femmes de 15 ans ou plus Agriculteurs exploitants.*/
	@Column(name="f15p_cs1", precision = 16, scale = 4)
	private BigDecimal f15p_cs1;
	/**  nombre de femmes de 15 ans ou plus Artisans, Commerçants, Chefs d'entreprise.*/
	@Column(name="f15p_cs2", precision = 16, scale = 4)
	private BigDecimal f15p_cs2;
	/**   nombre de femmes de 15 ans ou plus Cadres et Professions intellectuelles supérieures.*/
	@Column(name="f15p_cs3", precision = 16, scale = 4)
	private BigDecimal f15p_cs3;
	/**  nombre de femmes de 15 ans ou plus Professions intermédiaires.*/
	@Column(name="f15p_cs4", precision = 16, scale = 4)
	private BigDecimal f15p_cs4;
	/**  nombre de femmes de 15 ans ou plus Employés.*/
	@Column(name="f15p_cs5", precision = 16, scale = 4)
	private BigDecimal f15p_cs5;
	/**  nombre de femmes de 15 ans ou plus Ouvriers.*/
	@Column(name="f15p_cs6", precision = 16, scale = 4)
	private BigDecimal f15p_cs6;
	/**  nombre de femmes de 15 ans ou plus Retraités.*/
	@Column(name="f15p_cs7", precision = 16, scale = 4)
	private BigDecimal f15p_cs7;
	/**  nombre de femmes de 15 ans ou plus Autres sans activité professionnelle.*/
	@Column(name="f15p_cs8", precision = 16, scale = 4)
	private BigDecimal f15p_cs8;

	/**  nombre de personnes de nationalité française.*/
	@Column(name="pop_fr", precision = 16, scale = 4)
	private BigDecimal pop_fr;
	/**  nombre de personnes étrangères.*/
	@Column(name="pop_etr", precision = 16, scale = 4)
	private BigDecimal pop_etr;
	/**  nombres de personnes immigrées .*/
	@Column(name="pop_imm", precision = 16, scale = 4)
	private BigDecimal pop_imm;
	/**  population des ménages.*/
	@Column(name="pmen", precision = 16, scale = 4)
	private BigDecimal pmen;
	/**  population hors ménages.*/
	@Column(name="phormen", precision = 16, scale = 4)
	private BigDecimal phormen;
	

}