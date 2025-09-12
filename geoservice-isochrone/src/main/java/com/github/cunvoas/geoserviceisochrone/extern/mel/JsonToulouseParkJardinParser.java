package com.github.cunvoas.geoserviceisochrone.extern.mel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoCoordinate;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusEnum;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Analyseur de fichiers JSON pour les parcs et jardins de Toulouse.
 * Permet d'extraire les informations des espaces verts à partir d'un fichier JSON.
 * 
 * @author cunvoas
 * @see https://commons.apache.org/proper/commons-csv/user-guide.html
 * 
*/
@Component
@Slf4j
public class JsonToulouseParkJardinParser {

	@Autowired
	private CityRepository cityRepository;

	private ObjectMapper objectMapper = new ObjectMapper();

	public JsonToulouseParkJardinParser() {
		super();
		objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, DtoCoordinate.class);
		objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	}

	/**
	 * CSV Header definition for easier mods.
	 * 
	 * @author cunvoas
	 */
	// uid nom num numvoie voie codepost commune code_insee surf_tot_m2 clos

	/**
	 * Analyse le fichier JSON fourni et retourne la liste des parcs et jardins extraits.
	 * @param csvFile Fichier JSON à analyser
	 * @return Liste des objets ParcEtJardin extraits
	 * @throws IOException en cas d'erreur de lecture du fichier
	 */
	public List<ParcEtJardin> parseJson(File csvFile) throws IOException {
		List<ParcEtJardin> parks = new ArrayList<>();

		if (csvFile.isFile()) {
			
			byte[] cnt = java.nio.file.Files.readAllBytes(Paths.get(csvFile.getAbsolutePath()));
			
			//JSonNode of root
			JsonNode rootNode = objectMapper.readTree(cnt);
			JsonNode features = rootNode.get("locations");
			
			
			if (features!=null) {
				for (JsonNode jsonNode : features) {

					ParcEtJardin park = new ParcEtJardin();
					
					park.setName(jsonNode.findValue("title").asText());
					park.setAdresse(jsonNode.findValue("address").asText());					
					String insee = "31555";
					City c = cityRepository.findByInseeCode(insee);
					park.setCommune(c);
					
					park.setStatus(ParcStatusEnum.TO_QUALIFY);
					park.setSource(ParcSourceEnum.OPENDATA);
					park.setTypeId(1L);
					
					Double lat = Double.valueOf(jsonNode.findValue("latitude").asText());
					Double lng = Double.valueOf(jsonNode.findValue("longitude").asText());
					Point p = GeoShapeHelper.getPoint(lng, lat);
					park.setCoordonnee(p);

					log.error("JsonToulouse {}", park);
					parks.add(park);
				}
				
			}
		}

		return parks;
	}
}