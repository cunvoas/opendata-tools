package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionGeo;
import com.github.cunvoas.geoserviceisochrone.model.ignTopo.IgnTopoVegetal;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.MissingNode;


/**
 * Composant utilitaire pour la conversion de GeoJSON en objets Geometry (JTS).
 * Permet de parser différents formats GeoJSON pour obtenir des géométries utilisables.
 */
@Component
@Slf4j
public class GeoJson2GeometryHelper {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	private static final GeoJsonReader geoJsonReader = new GeoJsonReader(factory);

	// use the safe because is threadsafe
	// plus de ObjectMapper dédié aux geojson JTS
	private final ObjectMapper mapper;
	
	@Autowired
	public GeoJson2GeometryHelper(ObjectMapper objectMapper) {
		super();
		this.mapper = objectMapper;
	}
	
	/**
	 * Parse une chaîne GeoJSON et retourne la géométrie correspondante.
	 * @param geoJson chaîne GeoJSON à parser
	 * @return objet Geometry correspondant
	 * @ en cas d'erreur de parsing JSON
	 */
	public Geometry parse(String geoJson) throws ParseException {
		Geometry ret = geoJsonReader.read(geoJson);
		
		//JSonNode of root
		JsonNode rootNode = mapper.readTree(geoJson);
		
		//JSonNode of geometry
		JsonNode geometryNode = rootNode.findValue("geometry");
		
		// Vérifier que le nœud geometry existe et n'est pas null
		if (geometryNode == null || geometryNode instanceof MissingNode) {
			throw new ExceptionGeo("Aucune géométrie trouvée dans le GeoJSON fourni") {};
		}
		ret = geoJsonReader.read(geometryNode.toString());
		
		return ret;
	}
	
	/**
	 * Parse un GeoJSON de type Geoman et retourne la géométrie correspondante (Polygon ou MultiPolygon).
	 * @param geoJson chaîne GeoJSON à parser
	 * @return objet Geometry correspondant
	 * @ en cas d'erreur de parsing JSON
	 */
	public Geometry parseGeoman(String geoJson)  {
		Geometry ret = null;
		//JSonNode of root
		JsonNode rootNode = mapper.readTree(geoJson);
		
		//JSonNode of geometry
		List<JsonNode> geometryNodes = rootNode.findValues("geometry");
		
		if (!geometryNodes.isEmpty()) {
			int i = 0;
			boolean many = geometryNodes.size() > 1;
			Polygon[] polys = new Polygon[geometryNodes.size()];
			
			for (JsonNode geometryNode : geometryNodes) {
				// Vérifier que geometryNode n'est pas null
				if (geometryNode != null && !(geometryNode instanceof MissingNode)) {
					try {
						Polygon poly = (Polygon) geoJsonReader.read(geometryNode.toString());
						polys[i] = poly;
					} catch (ParseException e) {
						log.info(geometryNode.toString(), e);
					}
				}
				i++;
			}
			
			if (many) {
				ret = new MultiPolygon(polys, factory);
			} else {
				ret = polys[0];
			}
		} else {
			if (!(rootNode instanceof MissingNode)) {
				try {
					Polygon poly = (Polygon) geoJsonReader.read(rootNode.toString());
					ret = poly;
				} catch (ParseException e) {
					log.info(rootNode.toString(), e);
				}
			}
		}
		
		return ret;
	}
	
	
	/**
	 * Parse un GeoJSON au format Inspire et retourne un objet GeoJsonInspire.
	 * @param geoJson chaîne GeoJSON à parser
	 * @return objet GeoJsonInspire correspondant
	 * @ en cas d'erreur de parsing JSON
	 */
	@SuppressWarnings("deprecation")
	public GeoJsonInspire parseInspire(String geoJson)  {
		//JSonNode of root
		JsonNode rootNode = mapper.readTree(geoJson);
		

		//JSonNode of geometry
		JsonNode geometryNode = rootNode.findValue("geometry");
		
		// Vérifier que le nœud geometry existe et n'est pas null
		if (geometryNode == null || geometryNode instanceof MissingNode) {
			throw new ExceptionGeo("Aucune géométrie trouvée dans le GeoJSON fourni") {};
		}
		Geometry geo=null;
		try {
			geo = geoJsonReader.read(geometryNode.toString());
		} catch (ParseException e) {
			log.warn(rootNode.toString(), e);
		}
		
		GeoJsonInspire ret = new GeoJsonInspire();
		
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
	
	@SuppressWarnings("deprecation")
	public GeoJsonIris parseIris(String geoJson)  {
		//JSonNode of root
		JsonNode rootNode = mapper.readTree(geoJson);
		

		//JSonNode of geometry
		JsonNode geometryNode = rootNode.findValue("geometry");
		
		Geometry geo=null;
		try {
			geo = geoJsonReader.read(geometryNode.toString());
		} catch (ParseException e) {
			log.warn(rootNode.toString(), e);
		}
		
		
		GeoJsonIris ret = new GeoJsonIris();
		
		if (geo instanceof MultiPolygon) {
			MultiPolygon mp = (MultiPolygon)geo;
			ret.geometry = mp;
			
			//JSonNode of xxx
			JsonNode node = rootNode.findValue("fid");
			ret.fid = node.asInt();

			node = rootNode.findValue("cleabs");
			ret.cleabs = node.asText();

			node = rootNode.findValue("code_insee");
			ret.codeInsee = node.asText();

			node = rootNode.findValue("nom_commune");
			ret.nomCommune = node.asText();
			node = rootNode.findValue("iris");
			ret.irisCourt = node.asText();
			node = rootNode.findValue("code_iris");
			ret.codeIris = node.asText();
			node = rootNode.findValue("nom_iris");
			ret.nomIris = node.asText();
			node = rootNode.findValue("type_iris");
			ret.typeIris = node.asText();
			
		}
		
		return ret;
	}
	

	private static final DateFormat DF =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	@SuppressWarnings("deprecation")
	public IgnTopoVegetal parseIgnTopo(String geoJsonLine)  {
		//JSonNode of root
		JsonNode rootNode = mapper.readTree(geoJsonLine);
		

		//JSonNode of geometry
		JsonNode geometryNode = rootNode.findValue("geometry");
		
		Geometry geo=null;
		try {
			geo = geoJsonReader.read(geometryNode.toString());
		} catch (ParseException e) {
			log.warn(rootNode.toString(), e);
		}
		
		
		IgnTopoVegetal ret = new IgnTopoVegetal();
		
		if (geo instanceof MultiPolygon) {
			MultiPolygon mp = (MultiPolygon)geo;
			ret.setGeometry( mp );
		}
		if (geo instanceof Polygon) {
			Polygon mp = (Polygon)geo;
			ret.setGeometry( mp );
		}
		
		//JSonNode of xxx
		JsonNode node = rootNode.findValue("ID");
		ret.setId( node.asText() );

		node = rootNode.findValue("NATURE");
		ret.setNature(node.asText());
		
		node = rootNode.findValue("ACQU_PLANI");
		ret.setAcquPlani(node.asText());
		node = rootNode.findValue("PREC_PLANI");
		ret.setPrecPlani(node.asText());


		node = rootNode.findValue("ID_SOURCE");
		ret.setIdSource(node.asText());
		node = rootNode.findValue("SOURCE");
		ret.setSource(node.asText());

		
		
		node = rootNode.findValue("DATE_CREAT");
		try {
			ret.setDateCreated( DF.parse(node.asText()));
		} catch (java.text.ParseException ignore) {
		}
		node = rootNode.findValue("DATE_MAJ");
		try {
			ret.setDateUpdated( DF.parse(node.asText()));
		} catch (java.text.ParseException ignore) {
		}
		node = rootNode.findValue("DATE_APP");
		try {
			ret.setDateApp( DF.parse(node.asText()));
		} catch (java.text.ParseException ignore) {
		}
		node = rootNode.findValue("DATE_CONF");
		try {
			ret.setDateConf( DF.parse(node.asText()));
		} catch (java.text.ParseException ignore) {
		}
		
		return ret;
	}
	
	public class GeoJsonInspire {
		public String idInspire;
		public String id1km;
		public Polygon geometry;
	}
	
	/**
	 *  { 
	 *  "fid": 14683, 
	 *  "cleabs": "IRIS____0000000674822804", 
	 *  "code_insee": "67482", 
	 *  "nom_commune": "Strasbourg", 
	 *  "iris": "2804", 
	 *  "code_iris": 
	 *  "674822804", "nom_iris": "Polygone Ouest", 
	 *  "type_iris": "H"
	 *   }, 
	 */
	public class GeoJsonIris {
		public Integer fid;
		public String cleabs;
		public String codeInsee;
		public String nomCommune;
		public String irisCourt;
		public String codeIris;
		public String nomIris;
		public String typeIris;
		public Geometry geometry;
	}
}
