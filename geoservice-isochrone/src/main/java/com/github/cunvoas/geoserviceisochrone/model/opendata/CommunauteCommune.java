package com.github.cunvoas.geoserviceisochrone.model.opendata;


import java.util.Comparator;
import java.util.List;

import org.locationtech.jts.geom.Polygon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Représente une communauté de communes, structure intercommunale regroupant plusieurs villes.
 * Contient les informations principales, la région de rattachement, la liste des villes et la géométrie simplifiée.
 */
@Data
@Entity(name = "adm_com2commune")
@EqualsAndHashCode(of = {"id"})
public class CommunauteCommune implements Comparator<CommunauteCommune> {

	@Id
	@ToString.Include
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_com2com")
    @SequenceGenerator(
    		name="seq_com2com",
    		allocationSize=1,
    		initialValue = 1
    	)
	/**
	 * Identifiant unique de la communauté de communes.
	 */
	private Long id;

	@ToString.Include
	@Column(name = "name")
	/**
	 * Nom de la communauté de communes.
	 */
	private String name;
	 
	@ManyToOne
	@JoinColumn(name="id_region", nullable=true)
	/**
	 * Région à laquelle appartient la communauté de communes.
	 */
	private Region region;
	
	@OneToMany( targetEntity=City.class, mappedBy="communauteCommune" , fetch = FetchType.EAGER)
	/**
	 * Liste des villes appartenant à la communauté de communes.
	 */
	List<City> cities;
	
	@Column(name = "carre_carte")
	/**
	 * Géométrie simplifiée de la communauté de communes (polygone).
	 */
	private Polygon carreCarte;

	/**
	 * Compare deux communautés de communes par leur nom (ordre alphabétique).
	 *
	 * @param arg0 première communauté
	 * @param arg1 seconde communauté
	 * @return résultat de la comparaison des noms
	 */
	@Override
	public int compare(CommunauteCommune arg0, CommunauteCommune arg1) {
		if (arg0!=null && arg0.getName()!=null && arg1!=null) {
			return arg0.getName().compareTo(arg1.getName());
		}
		return 0;
	}

	
}