package com.github.cunvoas.geoserviceisochrone.extern.helper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

import com.bedatadriven.jackson.datatype.jts.parsers.GenericGeometryParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GeoJson2GeometryHelper {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

	// use the safe because is threadsafe
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * @param geoJson
	 * @return Geometry
	 * @throws JsonProcessingException
	 */
	public Geometry parse(String geoJson) throws JsonProcessingException {
		Geometry ret = null;
		
		//JSonNode of root
		JsonNode rootNode = mapper.readTree(geoJson);
		
		//JSonNode of geometry
		JsonNode geometryNode = rootNode.findValue("geometry");
		
		GenericGeometryParser parser = new GenericGeometryParser(factory);
		ret = parser.geometryFromJson(geometryNode);
		
		return ret;
	}
	
	
	public GeoJsonTmp parseInspire(String geoJson) throws JsonProcessingException {
		//JSonNode of root
		JsonNode rootNode = mapper.readTree(geoJson);
		

		//JSonNode of geometry
		JsonNode geometryNode = rootNode.findValue("geometry");
		
		GenericGeometryParser parser = new GenericGeometryParser(factory);
		Geometry geo = parser.geometryFromJson(geometryNode);
		
		GeoJsonTmp ret = new GeoJsonTmp();
		
		if (geo instanceof MultiPolygon) {
			MultiPolygon mp = (MultiPolygon)geo;
			Coordinate[] coords = mp.getCoordinates();
			
			Polygon polygon = (Polygon)factory.createPolygon(coords).getEnvelope();
			ret.geometry = polygon;
			

			//JSonNode of idINSPIRE
			JsonNode idInspireNode = rootNode.findValue("idINSPIRE");
			ret.idInspire = idInspireNode.asText();
			

			//JSonNode of idINSPIRE
			JsonNode id1kNode = rootNode.findValue("id_carr_1k");
			ret.id1km = id1kNode.asText();
		}
		
		return ret;
	}
	
	public class GeoJsonTmp {
		public String idInspire;
		public String id1km;
		public Polygon geometry;
	}
}
