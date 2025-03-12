package com.github.cunvoas.geoserviceisochrone.extern.csv;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;


/**
 * CSV parser to import.
 * @author cunvoas
 * @see https://commons.apache.org/proper/commons-csv/user-guide.html
 */
@Component
@Deprecated
public class CsvCarre200ShapeParser {
	
	/**
	 * CSV Header definition for easier mods.
	 * @author cunvoas
	 */
	public enum ParkEntranceCsvHeaders {
		idInspire("Identifiant INSPIRE du carreau habité"),
		idCarreHab("Identifiant du carreau habité"),
		idRectHab("Identifiant du rectangle d’appartenance du carreau habité"),
		nbHabCarre("Nombre d’individus résidant dans le carreau"),
		nbCarRect("Nombre de carreaux habités du rectangle d’appartenance"),
		geoPoint2d("geo_point_2d"),
		geoShape("geo_shape"),
		code("code"),
		epci("EPCI"),
		commune("Commune"),
		region("region"),
		departement("departement");
		
		
		
		private String column;

		ParkEntranceCsvHeaders(String column) {
			this.column = column;
		}

		public String geColumn() {
			return column;
		}

		// Lookup table
		private static final Map<String, ParkEntranceCsvHeaders> lookup = new HashMap<>();

		// Populate the lookup table on loading time
		static {
			for (ParkEntranceCsvHeaders env : ParkEntranceCsvHeaders.values()) {
				lookup.put(env.geColumn(), env);
			}
		}

		// This method can be used for reverse lookup purpose
		public static ParkEntranceCsvHeaders get(String column) {
			return lookup.get(column);
		}

	}
	
	
	private Double parseDouble(String val) {
		Double ret = Double.valueOf(0);
		
		if (val!=null && val.trim().length()>0) {
			ret = Double.valueOf(val);
		}
		return ret;
	}
	
}
