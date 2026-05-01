package com.github.cunvoas.geoserviceisochrone.extern.gouv.adress;

import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto.AdressBo;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Parseur GeoJSON pour les réponses de l'API adresse.data.gouv.fr.
 * <p>
 * Cette classe transforme les réponses JSON en objets métier utilisables dans l'application.
 */
@Component
public class AdressGeoJsonParser {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	
	// possible because is threadsafe
	private final ObjectMapper objectMapper;
	private final GeoJsonReader geoJsonReader = new GeoJsonReader();
	 
	@Autowired
	public AdressGeoJsonParser(ObjectMapper objectMapper) {
		super();
		this.objectMapper=objectMapper;
	}
	
	/**
     * Parse une chaîne GeoJSON et retourne un ensemble d'adresses.
     *
     * @param geoJson la chaîne JSON à parser
     * @return un ensemble d'adresses extraites du GeoJSON
     * @throws JsonProcessingException en cas d'erreur de parsing
     */
    public Set<AdressBo> parse(String geoJson) {
		Set<AdressBo> set = new HashSet<>();
		
		
		
		//JSonNode of root
		JsonNode rootNode = objectMapper.readTree(geoJson);
		
		//JSonNode of geometry
		JsonNode features = rootNode.findValue("features");
		
		if (features!=null) {
			for (JsonNode jsonNode : features) {
				AdressBo adressItem = new AdressBo();
				set.add(adressItem);
				
				JsonNode geometryNode = jsonNode.findValue("geometry");
				
				
				try {
					//Geometry geom= geometryParser.geometryFromJson(geometryNode.);
					Geometry geom= geoJsonReader.read(geometryNode.asText());
					adressItem.setPoint((Point)geom);  // cast check if API evolves
					
					JsonNode props = jsonNode.findValue("properties");
					
					adressItem.setId(props.findValue("id").asText());
					adressItem.setLabel(props.findValue("label").asText());
					adressItem.setCitycode(props.findValue("citycode").asText());
					String sScore = props.findValue("score").asText();
					adressItem.setScore(Float.valueOf(sScore));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
			}
		}
		return set;
	}
}