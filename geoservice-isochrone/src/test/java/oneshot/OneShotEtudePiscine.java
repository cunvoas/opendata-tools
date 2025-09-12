package oneshot;

import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.Polygon;

import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.MapperIsoChrone;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.ClientIsoChroneApiV2;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.DtoIsoChroneParser;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoIsoChrone;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;

public class OneShotEtudePiscine {
	
	static Map<String, Coordinate> piscines = new HashMap<>();
	
	public static void main(String[] args) {
		ClientIsoChroneApiV2 isochroneApi = new ClientIsoChroneApiV2();
		DtoIsoChroneParser parser =  new DtoIsoChroneParser();
		MapperIsoChrone mapper = new MapperIsoChrone();
		
		
		// Piscine Plein Sud Lille
		piscines.put("Piscine Plein Sud Lille", new Coordinate(3.0332178, 50.6075634));
		// Piscine Tournesol d'Hellemmes
		piscines.put(" Piscine Tournesol d'Hellemmes", new Coordinate(3.117698, 50.6300478));
		// Piscine de Fives 
		piscines.put("Piscine de Fives ", new Coordinate(3.0866306, 50.629051));
		// Piscine Marx Dormoy
		piscines.put("Piscine Marx Dormoy", new Coordinate(3.0314587, 50.6362478));
		// Piscine Municipale de Lomme
		piscines.put("Piscine Municipale de Lomme", new Coordinate(3.0094718, 50.6434367));
//		// Piscine Municipale de Mons-en-Baroeul
//		piscines.put("Piscine Municipale de Mons-en-Baroeul", new Coordinate(3.1021699, 50.6398747));
//		// Piscine Municipale de La Madeleine
//		piscines.put("Piscine Municipale de La Madeleine", new Coordinate(3.0685061, 50.6548446));
//		// Piscine municipale Jean Guérécheau Lambersard
//		piscines.put("Piscine municipale Lambersard", new Coordinate(3.0226427, 50.650891));
//		// Piscine du Triolo
//		piscines.put("Piscine du Triolo", new Coordinate(3.1430421,50.6138699));
//		// Centre Nautique Babylone
//		piscines.put("Centre Nautique Babylone", new Coordinate(3.1390985,50.6519962));
//		// Centre aquatique O'Santéa à Saint-André-lez-Lille
//		piscines.put(" Centre aquatique O'Santéa", new Coordinate(3.025516, 50.6655346));
		
		for (Map.Entry<String, Coordinate> entry: piscines.entrySet()) {
			String resp = isochroneApi.getIsoChrone(entry.getValue(), "300");
			try {
				DtoIsoChrone dto = parser.parseIsoChrone(resp);
				ParkEntrance pe = mapper.map(null, dto);
				Polygon polygon = pe.getPolygon();
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}
		
	}
}
