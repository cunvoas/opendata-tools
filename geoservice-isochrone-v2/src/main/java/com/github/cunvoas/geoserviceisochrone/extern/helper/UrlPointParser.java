package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionParseUrl;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

/**
 * Composant utilitaire pour extraire des coordonnées à partir d'URL ou de chaînes de texte.
 * Permet de parser différents formats d'URL (Google Maps, Geoportail, etc.) pour obtenir des coordonnées géographiques.
 */
@Component
public class UrlPointParser {
	
	private static final Pattern pattern = Pattern.compile("([0-9]+[.][0-9]+),([0-9]+[.][0-9]+)");
	
	/**
	 * Extrait les coordonnées d'une URL ou d'une chaîne générique.
	 * @param genericUrl URL ou chaîne à parser
	 * @return objet Coordinate extrait
	 * @throws ExceptionParseUrl si le format n'est pas supporté
	 */
	public Coordinate parse (String genericUrl) {
		Coordinate coord=null;
		
		if (genericUrl.toLowerCase().indexOf("google.com")>0) {
			coord = parseGoogle(genericUrl);
			
		} else if (genericUrl.toLowerCase().indexOf("geoportail.gouv.fr")>0) {
			coord = parseGeoportail(genericUrl);
			
		} else if (genericUrl.matches("[0-9.,]+")) {
			coord = parseLatLng(genericUrl);
			
		} else {
			String sub = genericUrl.substring(20);
			throw new ExceptionParseUrl("Unsupported Site: "+sub);
		}
		
		return coord;
	}
	
	/**
	 * Extrait les coordonnées d'une chaîne de type "lat,lon".
	 * @param latLng chaîne de coordonnées
	 * @return objet Coordinate extrait ou null
	 */
	protected Coordinate parseLatLng (String latLng) {
		Coordinate coord=null;
		
		Matcher m = pattern.matcher(latLng);
		if (m.find()) {
			String y = m.group(1);
			String x = m.group(2);
			
			coord = new Coordinate(Double.parseDouble(x), Double.parseDouble(y));
	    }
		
		return coord;
		
	}
	
	/**
	 * Extrait les coordonnées d'une URL Google Maps.
	 * @param gmapUrl URL Google Maps
	 * @return objet Coordinate extrait ou null
	 */
	protected Coordinate parseGoogle (String gmapUrl) {
		Coordinate coord=null;
		
		Matcher m = pattern.matcher(gmapUrl);
		if (m.find()) {
			String y = m.group(1);
			String x = m.group(2);
			
			coord = new Coordinate(Double.parseDouble(x), Double.parseDouble(y));
	    }
		
		return coord;
	}
	/**
	 * Extrait les coordonnées d'une URL Geoportail.
	 * @param geopUrl URL Geoportail
	 * @return objet Coordinate extrait ou null
	 */
	protected Coordinate parseGeoportail (String geopUrl) {
		Coordinate coord=null;
		
		Matcher m = pattern.matcher(geopUrl);
		if (m.find()) {
			String x = m.group(1);
			String y = m.group(2);
			
			coord = new Coordinate(Double.parseDouble(x), Double.parseDouble(y));
	    }
		
		return coord;
	}
}