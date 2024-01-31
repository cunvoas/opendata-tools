package com.github.cunvoas.geoserviceisochrone.extern.helper;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

import com.bedatadriven.jackson.datatype.jts.parsers.GenericGeometryParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GeoJson2GeometryHelper {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

	
	/**
	 * @param geoJson
	 * @return Geometry
	 * @throws JsonProcessingException
	 */
	public Geometry parse(String geoJson) throws JsonProcessingException {
		Geometry ret = null;
		
		//JSonNode of root
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(geoJson);
		
		//JSonNode of geometry
		JsonNode geometryNode = rootNode.findValue("geometry");
		
		GenericGeometryParser parser = new GenericGeometryParser(factory);
		ret = parser.geometryFromJson(geometryNode);
		
		return ret;
	}
}
