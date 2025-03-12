package com.github.cunvoas.geoserviceisochrone.service.export.dto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

/**
 * CSV Exporter.
 * @author cunvoas
 * @see https://commons.apache.org/proper/commons-csv/user-guide.html
 */
@Component
public class ParkExportCsv {
	
	/**
	 * CSV Header definition for easier mods.
	 * @author cunvoas
	 */
	public enum ParkExportCsvHeaders {
		idRegion("idRegion"),
		idCom2Co("idCom2Co"),
		idCommune("idCommune"),
		idPark("idPark"),
		idTypePark("idTypePark"),
		idParkArea("idTyidParkAreapePark"),
		
		commune("commune"),
		parkName("parkName"),
		parkTypeName("parkTypeName"),
		omsCustom("omsCustom"),
		surfaceOpendata("surfaceOpendata"),
		surfaceContour("surfaceContour"),
		nbParkEntrance("nbParkEntrance"),
		parkAreaComputedDate("parkAreaComputedDate")
		;
		
		private String column;

		ParkExportCsvHeaders(String column) {
			this.column = column;
		}

		public String getColumn() {
			return column;
		}

		// Lookup table
		private static final Map<String, ParkExportCsvHeaders> lookup = new LinkedHashMap<>();

		// Populate the lookup table on loading time
		static {
			for (ParkExportCsvHeaders env : ParkExportCsvHeaders.values()) {
				lookup.put(env.getColumn(), env);
			}
		}

		// This method can be used for reverse lookup purpose
		public static ParkExportCsvHeaders get(String column) {
			return lookup.get(column);
		}
		
		public static List<String> header() {
			List<String> ret = new ArrayList<>();
			ret.addAll(lookup.keySet());
			return ret;
		}

	}
	
	private CSVFormat initCSVFormat() {
		return  CSVFormat.DEFAULT.builder()
		        .setHeader(ParkExportCsvHeaders.class)
	        	.build();
	}
	
	public void write(File csvFile, List<ParkExportLine> rows) throws IOException {
		CSVFormat csvFormat = initCSVFormat();
		
		FileWriter fileWriter = new FileWriter(csvFile);
		
		CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFormat);
		
		for (ParkExportLine cols : rows) {
			csvFilePrinter.print(cols.map());
		}

		fileWriter.flush();
		fileWriter.close();
		csvFilePrinter.close();
	}

	public OutputStream steam(List<ParkExportLine> lines) throws IOException {
		OutputStream os = new ByteArrayOutputStream();
		
		this.steam(os, lines);
		return os;
	}
	
	public OutputStream steam(OutputStream os, List<ParkExportLine> lines) throws IOException {
		CSVFormat csvFormat = initCSVFormat();
		String encoding="UTF-8";
		
		String line = drawLine(csvFormat, csvFormat.getHeader());
		IOUtils.write(line, os, encoding);
		//IOUtils.write(csvFormat.getRecordSeparator(), os, encoding);
		
		for (ParkExportLine parkExportLine : lines) {
			line = drawLine(csvFormat, parkExportLine.map());
			IOUtils.write(line, os, encoding);
		}
		return os;
	}
	
	private String drawLine(CSVFormat csvFormat , String[] cols) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cols.length; i++) {
			if (i!=0) {
				sb.append(csvFormat.getDelimiterString());
			}
			sb.append(cols[i]);
		}
		sb.append(csvFormat.getRecordSeparator());
		return sb.toString();
	}
	
}
