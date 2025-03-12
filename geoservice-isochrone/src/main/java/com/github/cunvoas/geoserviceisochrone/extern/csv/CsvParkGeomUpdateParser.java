package com.github.cunvoas.geoserviceisochrone.extern.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.extern.csv.CsvParkUpdateParser.ParkUpdateCsvHeaders;

/**
 * CSV parser to import.
 * @author cunvoas
 * @see https://commons.apache.org/proper/commons-csv/user-guide.html
 */
@Component
public class CsvParkGeomUpdateParser {
	
	/**
	 * CSV Header definition for easier mods.
	 * @author cunvoas
	 * objectid,id,nom,quartier,nom_liste,adresse,surface,geom
	 */
	public enum CsvParkGeomUpdateCsvHeaders {
		objectid("objectid"),
		id("id"),
		nom("nom"),
		quartier("quartier"),
		nomListe("nom_liste"),
		adresse("adresse"),
		surface("surface"),
		geom("geom");
		
		private String column;

		CsvParkGeomUpdateCsvHeaders(String column) {
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
	
	public void write(File csvFile, List<CsvParkGeomUpdate> rows) throws IOException {
		Writer writer = new BufferedWriter(new FileWriter(csvFile));
		for (CsvParkGeomUpdate row : rows) {
			writer.write("update public.parc_jardin set contour=ST_GeomFromText('");
			writer.write(row.getGeom());
			writer.write("') where nom_parc='");
			writer.write(row.getNom().replaceAll("'", "''"));
			writer.write("';\n");
			writer.flush();
			System.out.println(row.getNom());
		}

		writer.close();
	}

	/**
	 * Parse CSV.
	 * @param csvFile
	 * @return
	 * @throws IOException
	 */
	public List<CsvParkGeomUpdate> parseParkGeom(File csvFile) throws IOException {
		List<CsvParkGeomUpdate> contacts = new ArrayList<>();

		if (csvFile.isFile()) {
			CSVFormat format = CSVFormat.DEFAULT.builder()
					.setDelimiter(",")
					.setQuote('"')
					.setHeader(CsvParkGeomUpdateCsvHeaders.class)
					.setSkipHeaderRecord(true)
					.build();

			try (Reader reader = new FileReader(csvFile)) {
				Iterable<CSVRecord> rows = format.parse(reader);

				for (CSVRecord row : rows) {
					CsvParkGeomUpdate contact = new CsvParkGeomUpdate();
					contact.setNom(row.get(CsvParkGeomUpdateCsvHeaders.nom));
					contact.setSurface(row.get(CsvParkGeomUpdateCsvHeaders.surface));
					contact.setGeom(row.get(CsvParkGeomUpdateCsvHeaders.geom));
					contacts.add(contact);
				}
			}
		}

		return contacts;
	}
}
