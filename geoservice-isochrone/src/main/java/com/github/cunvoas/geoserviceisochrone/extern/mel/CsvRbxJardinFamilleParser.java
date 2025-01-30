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
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.extern.csv.CsvCarre200ShapeParser.ParkEntranceCsvHeaders;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;


/**
 * @author cunvoas
 * @see https://commons.apache.org/proper/commons-csv/user-guide.html
 * 
 * @see https://opendata.roubaix.fr/explore/dataset/liste-des-jardins-familiaux-et-partages-de-roubaix/api/
 */
@Component
public class CsvRbxJardinFamilleParser {
	

	/**
	 * CSV Header definition for easier mods.
	 * @author cunvoas
	 */
	public enum ParkJardinCsvHeaders {
	//	Identifiant;Nom;Adresse;Surface (ha);geo_shape;geo_point_2d;lat;long

		nom("nom"),
		adresse("adresse"),
		id("OBJECTID"),
		geo_shape("geo_shape"),
		coord("geo_point_2d"),
		lon("latitude"),
		lat("longitude");
		
		private String column;

		ParkJardinCsvHeaders(String column) {
			this.column = column;
		}

		public String geColumn() {
			return column;
		}

		// Lookup table
		private static final Map<String, ParkEntranceCsvHeaders> lookup = new HashMap<>();

		// Populate the lookup table on loading time
		static {
			for (ParkEntranceCsvHeaders env : ParkEntranceCsvHeaders.values()) {
				lookup.put(env.geColumn(), env);
			}
		}

		// This method can be used for reverse lookup purpose
		public static ParkEntranceCsvHeaders get(String column) {
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
					.setHeader(ParkJardinCsvHeaders.class)
					.setSkipHeaderRecord(true)
					.build();

			try (Reader reader = new FileReader(csvFile)) {
				Iterable<CSVRecord> rows = format.parse(reader);

				for (CSVRecord row : rows) {
					ParcEtJardin park = new ParcEtJardin();
					park.setName(row.get(ParkJardinCsvHeaders.nom));
					park.setAdresse(row.get(ParkJardinCsvHeaders.adresse));
					park.setType("Parc");
					park.setCoordonnee(GeoShapeHelper.parsePointLatLng(row.get(ParkJardinCsvHeaders.coord)));
					parks.add(park);
				}
			}
		}

		return parks;
	}
}
