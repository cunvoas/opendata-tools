import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

import com.bedatadriven.jackson.datatype.jts.parsers.GenericGeometryParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class MainQuartier {


	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	private static GenericGeometryParser parser = new GenericGeometryParser(factory);

	// use the safe because is threadsafe
	private static ObjectMapper mapper = new ObjectMapper();
	
	public static void main(String[] args) {
		lireFichierLigneParLigne(
				"/work/PERSO/github/opendata-tools/geoservice-isochrone/doc/lille_geo_quartier_shape.csv");
	}

	public static void lireFichierLigneParLigne(String filePath) {
		List<String> quartiers = new ArrayList<>();
		quartiers.add("Lille-Moulins");
		quartiers.add("Faubourg de Béthune");
		quartiers.add("Wazemmes");
		quartiers.add("Lille-Sud");
		quartiers.add("Vieux-Lille");
		quartiers.add("Fives");
		quartiers.add("Lomme");
		quartiers.add("Bois-Blancs");
		quartiers.add("Hellemmes");
		quartiers.add("Vauban-Esquermes");
		quartiers.add("Saint-Maurice Pellevoisin");
		quartiers.add("Lille-Centre");

		
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			FileWriter fw = new FileWriter(filePath+".sql");
			String line;
			int lineNumber = 0;
			while ((line = br.readLine()) != null) {
				
				String geoJsonStr = line;
//				System.out.println("\n\t--"+lineNumber+" - "+quartiers.get(lineNumber));
//				System.out.println("\n\t\t--"+geoJsonStr);
				
				Geometry geom = parseGeom(geoJsonStr);
				
				
				String out = "\n\n insert into quartier (nom, contour) values ('"+quartiers.get(lineNumber)+"', ST_GeomFromText('"+geom.toText()+"', 4326));";
				fw.write(out);
				System.out.println(out);
				lineNumber++;
			}
			
			fw.close();
		} catch (IOException e) {
			System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
		}
	}

	
	public static Geometry parseGeom(String geoJson) throws JsonProcessingException {
		List<Coordinate> shape = new ArrayList<>();
		
		
		//JSonNode of root
		JsonNode rootNode = mapper.readTree(geoJson);
		
		//JSonNode of geometry
		List<JsonNode> geometryNodes = rootNode.findValues("coordinates");
		
		geometryNodes.forEach(node -> {
			
			ArrayNode aryNode1 = (ArrayNode) node;
			aryNode1.forEach(node1 -> {
				
				ArrayNode aryNode2 = (ArrayNode) node1;
				aryNode2.forEach(node3 -> {
//					System.out.println(node3.toString());
					
					ArrayNode aryNode3 = (ArrayNode) node3;
					Double lat = aryNode3.get(0).asDouble();
					Double lon = aryNode3.get(1).asDouble();
					shape.add(new Coordinate(lat, lon));
				});
			});
			
		});
		

		
		Polygon polygon = factory.createPolygon(shape.toArray(new Coordinate[0]));
		
		return polygon;
	}
}
