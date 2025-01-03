package com.github.cunvoas.geoserviceisochrone.extern.mel;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusEnum;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;


/**
 * @author cunvoas
 * @see https://commons.apache.org/proper/commons-csv/user-guide.html
 * 
 * @see https://opendata.roubaix.fr/explore/dataset/liste-des-jardins-familiaux-et-partages-de-roubaix/api/
 */
@Component
public class CsvNantesParkJardinParser {
	
	@Autowired
	private CityRepository cityRepository;
	

	/**
	 * CSV Header definition for easier mods.
	 * @author cunvoas
	 */
	public enum CsvHeaders {
		identifiant("Identifiant"),
		nom("Nom"),
		type("Type"),
		adresse("Adresse"),
		codePostal("Code postal"),
		codeInsee("Code insee"),
		commune("Commune"),
		y("Y"),
		x("X");
		
		private String column;

		CsvHeaders(String column) {
			this.column = column;
		}

		public String geColumn() {
			return column;
		}

		// Lookup table
		private static final Map<String, CsvHeaders> lookup = new HashMap<>();

		// Populate the lookup table on loading time
		static {
			for (CsvHeaders env : CsvHeaders.values()) {
				lookup.put(env.geColumn(), env);
			}
		}

		// This method can be used for reverse lookup purpose
		public static CsvHeaders get(String column) {
			return lookup.get(column);
		}

	}
	

	/**
	 * Parse CSV.
	 * @param csvFile
	 * @return
	 * @throws IOException
	 */
	public List<ParcEtJardin> parseCsv(File csvFile) throws IOException {
		List<ParcEtJardin> parks = new ArrayList<>();

		if (csvFile.isFile()) {
			CSVFormat format = CSVFormat.DEFAULT.builder()
					.setDelimiter(";")
					.setQuote('"')
					.setHeader(CsvHeaders.class)
					.setSkipHeaderRecord(true)
					.build();

			try (Reader reader = new FileReader(csvFile)) {
				Iterable<CSVRecord> rows = format.parse(reader);

				for (CSVRecord row : rows) {
					ParcEtJardin park = new ParcEtJardin();
					park.setName(row.get(CsvHeaders.nom));
					park.setType(row.get(CsvHeaders.type));
					park.setAdresse(row.get(CsvHeaders.adresse));
					Double lng = Double.valueOf(row.get(CsvHeaders.x));
					Double lat = Double.valueOf(row.get(CsvHeaders.y));
					
					String insee = row.get(CsvHeaders.codeInsee);
					City c = cityRepository.findByInseeCode(insee);
					park.setCommune(c);
					park.setStatus(ParcStatusEnum.TO_QUALIFY);
					park.setSource(ParcSourceEnum.OPENDATA);
					park.setTypeId(1L);
					
					Point p = GeoShapeHelper.getPoint(lng, lat);
					park.setCoordonnee(p);
					parks.add(park);
				}
			}
		}

		return parks;
	}
}
