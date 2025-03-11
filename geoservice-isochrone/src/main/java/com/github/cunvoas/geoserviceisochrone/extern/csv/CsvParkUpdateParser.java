package com.github.cunvoas.geoserviceisochrone.extern.csv;

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

/**
 * CSV parser to import.
 * @author cunvoas
 * @see https://commons.apache.org/proper/commons-csv/user-guide.html
 */
@Component
public class CsvParkUpdateParser {
	
	/**
	 * CSV Header definition for easier mods.
	 * @author cunvoas
	 */
	public enum ParkUpdateCsvHeaders {
		cityId("cityId"),
		parkId("parkId"),
		nom("nom"),
		surface("surface"),
		nomE("nomE"),
		coord("coord");
		
		private String column;

		ParkUpdateCsvHeaders(String column) {
			this.column = column;
		}

		public String getColumn() {
			return column;
		}

		// Lookup table
		private static final Map<String, ParkUpdateCsvHeaders> lookup = new HashMap<>();

		// Populate the lookup table on loading time
		static {
			for (ParkUpdateCsvHeaders env : ParkUpdateCsvHeaders.values()) {
				lookup.put(env.getColumn(), env);
			}
		}

		// This method can be used for reverse lookup purpose
		public static ParkUpdateCsvHeaders get(String column) {
			return lookup.get(column);
		}

	}
	

	/**
	 * Parse CSV.
	 * @param csvFile
	 * @return
	 * @throws IOException
	 */
	public List<CsvParkUpdate> parseParkEntrance(File csvFile) throws IOException {
		List<CsvParkUpdate> contacts = new ArrayList<>();

		if (csvFile.isFile()) {
			CSVFormat format = CSVFormat.DEFAULT.builder()
					.setDelimiter(";")
					.setQuote('"')
					.setHeader(ParkUpdateCsvHeaders.class)
					.setSkipHeaderRecord(true)
					.build();

			try (Reader reader = new FileReader(csvFile)) {
				Iterable<CSVRecord> rows = format.parse(reader);

				for (CSVRecord row : rows) {
					CsvParkUpdate contact = new CsvParkUpdate();
					contact.setCityId(row.get(ParkUpdateCsvHeaders.cityId));
					contact.setParkId(row.get(ParkUpdateCsvHeaders.parkId));
					contact.setNom(row.get(ParkUpdateCsvHeaders.nom));
					contact.setSurface(row.get(ParkUpdateCsvHeaders.surface));
					contact.setNomE(row.get(ParkUpdateCsvHeaders.nomE));
					contact.setCoord(row.get(ParkUpdateCsvHeaders.coord));
					contacts.add(contact);
				}
			}
		}

		return contacts;
	}
}
