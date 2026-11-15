package com.github.cunvoas.geoserviceisochrone.model.opendata;


import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

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

	private static final Pattern FRENCH_PARTICLES =
			Pattern.compile("(?<=[\\s-])(De|Des|Du|Le|La|Les|En|Et|Aux|Sur|Sous)(?=[\\s-])");

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
	 * Nom de la ville en casse mixte pour l'affichage (les données sont en majuscules).
	 * @return nom formaté ("LILLE" -> "Lille", "SAINT-DENIS" -> "Saint-Denis")
	 */
	public String getDisplayName() {
		return toTitleCase(name);
	}

	/**
	 * Met en majuscule la première lettre de chaque mot.
	 * @param value texte à formater
	 * @return texte en casse mixte, null si value null
	 */
	public static String toTitleCase(String value) {
		if (value == null || value.isBlank()) {
			return value;
		}
		String lower = value.toLowerCase(Locale.FRENCH);
		StringBuilder sb = new StringBuilder(lower.length());
		boolean capitalizeNext = true;
		for (char c : lower.toCharArray()) {
			if (capitalizeNext && Character.isLetter(c)) {
				sb.append(Character.toUpperCase(c));
			} else {
				sb.append(c);
			}
			capitalizeNext = !Character.isLetter(c);
		}
		// Particules françaises en minuscules ("Aix-En-Provence" -> "Aix-en-Provence").
		return FRENCH_PARTICLES.matcher(sb).replaceAll(m -> m.group(1).toLowerCase(Locale.FRENCH));
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