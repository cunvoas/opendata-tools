package com.github.cunvoas.geoserviceisochrone.model.opendata;


import java.util.Comparator;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Représente une ville avec ses informations principales, coordonnées et rattachements administratifs.
 * Permet également la comparaison et l'affichage formaté pour la recherche.
 */
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(of = {"id"})
@Entity(name = "city")
public class City implements Comparator<City> {

	@Id
	@ToString.Include
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_city")
    @SequenceGenerator(
    		name="seq_city",
    		allocationSize=1,
    		initialValue = 1
    	)
	/**
	 * Identifiant unique de la ville.
	 */
	private Long id;

	@ToString.Include
	@Column(name = "name")
	/**
	 * Nom de la ville.
	 */
	private String name;

	@Column(name = "postal_code")
	/**
	 * Code postal de la ville.
	 */
	private String postalCode;

	@Column(name = "insee_code")
	/**
	 * Code INSEE de la ville.
	 */
	private String inseeCode;

	@Column(columnDefinition = "geometry(Point,4326)")
	/**
	 * Coordonnées géographiques de la ville (Point).
	 */
	private Point coordinate;

	@ManyToOne  
	@JoinColumn( name="id_comm2co" )
	/**
	 * Communauté de communes à laquelle appartient la ville.
	 */
	private CommunauteCommune communauteCommune;
	 
	@ManyToOne
	@JoinColumn(name="id_region", nullable=true)
	/**
	 * Région à laquelle appartient la ville.
	 */
	private Region region;

	
	/**
	 * city label for search.
	 * @return
	 * Retourne un libellé formaté pour la recherche (code postal et nom).
	 *
	 * @return libellé de la ville pour affichage
	 */
	public String getDisplay() {
		return postalCode+" - "+name;
	}

	/**
	 * @see java.lang.Comparable.compareTo(java.lang.Object)
	 * Compare deux villes par leur nom (ordre alphabétique).
	 *
	 * @param arg0 première ville
	 * @param arg1 seconde ville
	 * @return résultat de la comparaison des noms
	 */
	@Override
	public int compare(City arg0, City arg1) {
		if (arg0!=null && arg0.getName()!=null && arg1!=null) {
			return arg0.getName().compareTo(arg1.getName());
		}
		return 0;
	}
}