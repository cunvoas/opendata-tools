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

import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mShape;


/**
 * @author cus
 * @see https://commons.apache.org/proper/commons-csv/user-guide.html
 */
@Component
@Deprecated
public class CsvCarre200ShapeParser {
	
	/**
	 * CSV Header definition for easier mods.
	 * @author cus
	 */
	public enum ParkEntranceCsvHeaders {
		idInspire("Identifiant INSPIRE du carreau habité"),
		idCarreHab("Identifiant du carreau habité"),
		idRectHab("Identifiant du rectangle d’appartenance du carreau habité"),
		nbHabCarre("Nombre d’individus résidant dans le carreau"),
		nbCarRect("Nombre de carreaux habités du rectangle d’appartenance"),
		geoPoint2d("geo_point_2d"),
		geoShape("geo_shape"),
		code("code"),
		epci("EPCI"),
		commune("Commune"),
		region("region"),
		departement("departement");
		
		
		
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
	public List<InseeCarre200mShape> parseCarree200Shape(File csvFile) throws IOException {
		List<InseeCarre200mShape> shapes = new ArrayList<>();

		if (csvFile.isFile()) {
			CSVFormat format = CSVFormat.DEFAULT.builder()
					.setDelimiter(";")
					.setQuote('"')
					.setHeader(ParkEntranceCsvHeaders.class)
					.setSkipHeaderRecord(true)
					.build();

			try (Reader reader = new FileReader(csvFile)) {
				Iterable<CSVRecord> rows = format.parse(reader);
				
				for (CSVRecord row : rows) {
					InseeCarre200mShape shape = new InseeCarre200mShape();
					shape.setIdInspire(row.get(ParkEntranceCsvHeaders.idInspire));
					shape.setIdCarreHab(row.get(ParkEntranceCsvHeaders.idCarreHab));
					shape.setIdRectHab(row.get(ParkEntranceCsvHeaders.idRectHab));
					shape.setNbHabCarre(parseDouble(row.get(ParkEntranceCsvHeaders.nbHabCarre)));
					shape.setNbCarreHabRect(parseDouble(row.get(ParkEntranceCsvHeaders.nbCarRect)));
					shape.setGeoPoint2d(GeoShapeHelper.parsePointLatLon(row.get(ParkEntranceCsvHeaders.geoPoint2d)));
					shape.setGeoShape(GeoShapeHelper.parsePolygon(row.get(ParkEntranceCsvHeaders.geoShape)));
					shape.setCode(row.get(ParkEntranceCsvHeaders.code));
					shape.setEpci(row.get(ParkEntranceCsvHeaders.epci));
					shape.setCommune(row.get(ParkEntranceCsvHeaders.commune));
					shape.setRegion(row.get(ParkEntranceCsvHeaders.region));
					shape.setDepartement(row.get(ParkEntranceCsvHeaders.departement));
					shapes.add(shape);
				}
			}
		}

		return shapes;
	}
	
	private Double parseDouble(String val) {
		Double ret = Double.valueOf(0);
		
		if (val!=null && val.trim().length()>0) {
			ret = Double.valueOf(val);
		}
		return ret;
	}
	
}
