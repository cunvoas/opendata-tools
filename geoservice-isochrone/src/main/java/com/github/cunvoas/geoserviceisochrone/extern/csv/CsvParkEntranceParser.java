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
public class CsvParkEntranceParser {
	
	/**
	 * CSV Header definition for easier mods.
	 * @author cunvoas
	 */
	public enum ParkEntranceCsvHeaders {
		reserved01("Champ_Reservé_1"),
		reserved02("Champ_Reservé 2"),
		city("Ville"),
		block("Quartier"),
		park_name("Parc"),
		entrance_name("Nom_Entrée"),
		entrance_url("Lien_entrée");
		
		private String column;

		ParkEntranceCsvHeaders(String column) {
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
	public List<CsvParkLine> parseParkEntrance(File csvFile) throws IOException {
		List<CsvParkLine> contacts = new ArrayList<>();

		if (csvFile.isFile()) {
			CSVFormat format = CSVFormat.DEFAULT.builder()
					.setDelimiter(",")
					.setQuote('"')
					.setHeader(ParkEntranceCsvHeaders.class)
					.setSkipHeaderRecord(true)
					.build();

			try (Reader reader = new FileReader(csvFile)) {
				Iterable<CSVRecord> rows = format.parse(reader);

				for (CSVRecord row : rows) {
					CsvParkLine contact = new CsvParkLine();
					contact.setReserved1(row.get(ParkEntranceCsvHeaders.reserved01));
					contact.setReserved2(row.get(ParkEntranceCsvHeaders.reserved02));
					contact.setCity(row.get(ParkEntranceCsvHeaders.city));
					contact.setBlock(row.get(ParkEntranceCsvHeaders.block));
					contact.setPark(row.get(ParkEntranceCsvHeaders.park_name));
					contact.setEntrance(row.get(ParkEntranceCsvHeaders.entrance_name));
					contact.setUrl(row.get(ParkEntranceCsvHeaders.entrance_url));
					contacts.add(contact);
				}
			}
		}

		return contacts;
	}
}
