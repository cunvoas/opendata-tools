package com.github.cunvoas.geoserviceisochrone.extern.gouv.adress;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

import com.bedatadriven.jackson.datatype.jts.parsers.GenericGeometryParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto.AdressBo;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoCoordinate;

@Component
public class AdressGeoJsonParser {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	private GenericGeometryParser geometryParser = new GenericGeometryParser(factory);
	
	// possible because is threadsafe
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public AdressGeoJsonParser() {
		super();
		objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, DtoCoordinate.class);
		objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	}
	
	public Set<AdressBo> parse(String geoJson) throws JsonProcessingException {
		Set<AdressBo> set = new HashSet<>();
		
		
		//JSonNode of root
		JsonNode rootNode = objectMapper.readTree(geoJson);
		
		//JSonNode of geometry
		JsonNode features = rootNode.findValue("features");
		
		for (JsonNode jsonNode : features) {
			AdressBo adressItem = new AdressBo();
			set.add(adressItem);
			
			JsonNode geometryNode = jsonNode.findValue("geometry");
			Geometry geom= geometryParser.geometryFromJson(geometryNode);
			adressItem.setPoint((Point)geom);  // cast check if API evolves
			
			JsonNode props = jsonNode.findValue("properties");
			
			adressItem.setId(props.findValue("id").asText());
			adressItem.setLabel(props.findValue("label").asText());
			adressItem.setCitycode(props.findValue("citycode").asText());
			String sScore = props.findValue("score").asText();
			adressItem.setScore(Float.valueOf(sScore));
			
		}
		
		return set;
	}
}
