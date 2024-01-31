package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionParseUrl;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

@Component
public class UrlPointParser {
	
	private static final Pattern pattern = Pattern.compile("([0-9]+[.][0-9]+),([0-9]+[.][0-9]+)");
	
	
	public Coordinate parse (String genericUrl) {
		Coordinate coord=null;
		
		if (genericUrl.toLowerCase().indexOf("google.com")>0) {
			coord = parseGoogle(genericUrl);
			
		} else if (genericUrl.toLowerCase().indexOf("geoportail.gouv.fr")>0) {
			coord = parseGeoportail(genericUrl);
			
		} else {
			String sub = genericUrl.substring(20);
			throw new ExceptionParseUrl("Unsupported Site: "+sub);
		}
		
		return coord;
	}
	
	/**
	 * Parse URL from point on GoogleMap
	 * @param gmapUrl https://www.google.com/maps/@50.1234567,3.1234567,18z
	 * @return
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
	 * Parse URL from point on GeoPortail
	 * @param geopUrl https://www.geoportail.gouv.fr/carte?c=2.473994493484497,48.85187488786221&z=17&l0=ORTHOIMAGERY.ORTHOPHOTOS::GEOPORTAIL:OGC:WMTS(1)&l1=GEOGRAPHICALNAMES.NAMES::GEOPORTAIL:OGC:WMTS(1)&l2=UTILITYANDGOVERNMENTALSERVICES.IGN.POI.ENSEIGNEMENTPRIMAIRE::GEOPORTAIL:OGC:WMS(1)&l3=UTILITYANDGOVERNMENTALSERVICES.IGN.POI.ENSEIGNEMENTMATERNELLES::GEOPORTAIL:OGC:WMS(1)&permalink=yes
	 * @return
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
