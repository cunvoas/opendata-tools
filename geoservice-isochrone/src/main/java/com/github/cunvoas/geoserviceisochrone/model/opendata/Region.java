package com.github.cunvoas.geoserviceisochrone.model.opendata;


import java.util.Comparator;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Modèle représentant une région administrative.
 * <p>
 * Cette entité contient :
 * <ul>
 *   <li>Identifiant et nom de la région</li>
 *   <li>Liste des communautés de communes associées</li>
 *   <li>Liste des villes associées</li>
 * </ul>
 * Fournit une méthode de comparaison basée sur le nom de la région.
 */
@Data
@Entity(name = "adm_region")
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(of = {"id"})
public class Region implements Comparator<Region> {

	@Id
	@ToString.Include
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_region")
    @SequenceGenerator(
    		name="seq_region",
    		allocationSize=1,
    		initialValue = 1
    	)
	private Long id;

	@Column(name = "name")
	@ToString.Include
	private String name;
	
	@OneToMany( targetEntity=CommunauteCommune.class, mappedBy="region" )
	private List<CommunauteCommune> communauteCommunes;
	
	@OneToMany( targetEntity=City.class, mappedBy="region" )
	private List<City> cities;
	
	/**
	 * @see java.lang.Comparable.compareTo(java.lang.Object)
	 */
	@Override
	public int compare(Region arg0, Region arg1) {
		if (arg0!=null && arg0.getName()!=null && arg1!=null) {
			return arg0.getName().compareTo(arg1.getName());
		}
		return 0;
	}
	

}