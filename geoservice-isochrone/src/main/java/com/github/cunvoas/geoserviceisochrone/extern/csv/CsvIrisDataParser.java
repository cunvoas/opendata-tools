package com.github.cunvoas.geoserviceisochrone.extern.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisData;

/**
 * Parseur CSV pour l'import des données IRIS (découpage statistique INSEE).
 * Permet de lire et d'associer les données démographiques par IRIS.
 * @author cunvoas
 * @see https://commons.apache.org/proper/commons-csv/user-guide.html
 */
@Component
public class CsvIrisDataParser {
	
	public static final String HEADER="IRIS;COM;TYP_IRIS;LAB_IRIS;P20_POP;P20_POP0002;P20_POP0305;P20_POP0610;P20_POP1117;P20_POP1824;P20_POP2539;P20_POP4054;P20_POP5564;P20_POP6579;P20_POP80P;P20_POP0014;P20_POP1529;P20_POP3044;P20_POP4559;P20_POP6074;P20_POP75P;P20_POP0019;P20_POP2064;P20_POP65P;P20_POPH;P20_H0014;P20_H1529;P20_H3044;P20_H4559;P20_H6074;P20_H75P;P20_H0019;P20_H2064;P20_H65P;P20_POPF;P20_F0014;P20_F1529;P20_F3044;P20_F4559;P20_F6074;P20_F75P;P20_F0019;P20_F2064;P20_F65P;C20_POP15P;C20_POP15P_CS1;C20_POP15P_CS2;C20_POP15P_CS3;C20_POP15P_CS4;C20_POP15P_CS5;C20_POP15P_CS6;C20_POP15P_CS7;C20_POP15P_CS8;C20_H15P;C20_H15P_CS1;C20_H15P_CS2;C20_H15P_CS3;C20_H15P_CS4;C20_H15P_CS5;C20_H15P_CS6;C20_H15P_CS7;C20_H15P_CS8;C20_F15P;C20_F15P_CS1;C20_F15P_CS2;C20_F15P_CS3;C20_F15P_CS4;C20_F15P_CS5;C20_F15P_CS6;C20_F15P_CS7;C20_F15P_CS8;P20_POP_FR;P20_POP_ETR;P20_POP_IMM;P20_PMEN;P20_PHORMEN";

	/**
	 * Enumération des entêtes du CSV IRIS pour faciliter la maintenance et l'évolution.
	 * Permet de référencer chaque colonne du fichier source.
	 * @author cunvoas
	 */
	public enum IrisDataCsvHeaders {
		iris("IRIS"),
		com("COM"),
		typ_iris("TYP_IRIS"),
		lab_iris("LAB_IRIS"),
		pop("P20_POP"),
		pop0002("P20_POP0002"),
		pop0305("P20_POP0305"),
		pop0610("P20_POP0610"),
		pop1117("P20_POP1117"),
		pop1824("P20_POP1824"),
		pop2539("P20_POP2539"),
		pop4054("P20_POP4054"),
		pop5564("P20_POP5564"),
		pop6579("P20_POP6579"),
		pop80p("P20_POP80P"),
		pop0014("P20_POP0014"),
		pop1529("P20_POP1529"),
		pop3044("P20_POP3044"),
		pop4559("P20_POP4559"),
		pop6074("P20_POP6074"),
		pop75p("P20_POP75P"),
		pop0019("P20_POP0019"),
		pop2064("P20_POP2064"),
		pop65p("P20_POP65P"),
		poph("P20_POPH"),
		h0014("P20_H0014"),
		h1529("P20_H1529"),
		h3044("P20_H3044"),
		h6074("P20_H6074"),
		h75p("P20_H75P"),
		h0019("P20_H0019"),
		h2064("P20_H2064"),
		h65p("P20_H65P"),
		popf("P20_POPF"),
		f0014("P20_F0014"),
		f1529("P20_F1529"),
		f3044("P20_F3044"),
		f6074("P20_F6074"),
		f75p("P20_F75P"),
		f0019("P20_F0019"),
		f2064("P20_F2064"),
		f65p("P20_F65P"),
		pop15p_cs1("C20_POP15P_CS1"),
		pop15p_cs2("C20_POP15P_CS2"),
		pop15p_cs3("C20_POP15P_CS3"),
		pop15p_cs4("C20_POP15P_CS4"),
		pop15p_cs5("C20_POP15P_CS5"),
		pop15p_cs6("C20_POP15P_CS6"),
		pop15p_cs7("C20_POP15P_CS7"),
		pop15p_cs8("C20_POP15P_CS8"),
		h15p("C20_H15P"),
		h15p_cs1("C20_H15P_CS1"),
		h15p_cs2("C20_H15P_CS2"),
		h15p_cs3("C20_H15P_CS3"),
		h15p_cs4("C20_H15P_CS4"),
		h15p_cs5("C20_H15P_CS5"),
		h15p_cs6("C20_H15P_CS6"),
		h15p_cs7("C20_H15P_CS7"),
		h15p_cs8("C20_H15P_CS8"),
		f15p("C20_F15P"),
		f15p_cs1("C20_F15P_CS1"),
		f15p_cs2("C20_F15P_CS2"),
		f15p_cs3("C20_F15P_CS3"),
		f15p_cs4("C20_F15P_CS4"),
		f15p_cs5("C20_F15P_CS5"),
		f15p_cs6("C20_F15P_CS6"),
		f15p_cs7("C20_F15P_CS7"),
		f15p_cs8("C20_F15P_CS8"),
		pop_fr("P20_POP_FR"),
		pop_etr("P20_POP_ETR"),
		pop_imm("P20_POP_IMM"),
		pmen("P20_PMEN"),
		phormen("P20_PHORMEN")
		;
		
		private String column;

		IrisDataCsvHeaders(String column) {
			this.column = column;
		}

		public String getColumn() {
			return column;
		}

		// Lookup table
		private static final Map<String, IrisDataCsvHeaders> lookup = new HashMap<>();

		// Populate the lookup table on loading time
		static {
			for (IrisDataCsvHeaders env : IrisDataCsvHeaders.values()) {
				lookup.put(env.getColumn(), env);
			}
		}

		// This method can be used for reverse lookup purpose
		public static IrisDataCsvHeaders get(String column) {
			return lookup.get(column);
		}

	}
	

	/**
	 * Parse CSV.
	 * @param csvFile
	 * @return
	 * @throws IOException
	 */
	public List<IrisData> parseIrisData(Integer annee, File csvFile) throws IOException {
		List<IrisData> irisDatas = new ArrayList<>();

		if (csvFile.isFile()) {
			CSVFormat format = CSVFormat.DEFAULT.builder()
					.setDelimiter(";")
					.setQuote('"')
					.setHeader(IrisDataCsvHeaders.class)
					.setSkipHeaderRecord(true)
					.build();

			try (Reader reader = new FileReader(csvFile)) {
				Iterable<CSVRecord> rows = format.parse(reader);

				for (CSVRecord row : rows) {
					IrisData iris = new IrisData();
					iris.setAnnee(annee);
					iris.setIris(row.get(IrisDataCsvHeaders.iris));
					iris.setCodeInsee(row.get(IrisDataCsvHeaders.com));
					iris.setTypIris(row.get(IrisDataCsvHeaders.typ_iris));
					iris.setLabIris(row.get(IrisDataCsvHeaders.lab_iris));
					iris.setPop(new BigDecimal(row.get(IrisDataCsvHeaders.pop)));
					iris.setPop0002(new BigDecimal(row.get(IrisDataCsvHeaders.pop0002)));
					iris.setPop0305(new BigDecimal(row.get(IrisDataCsvHeaders.pop0305)));
					iris.setPop0610(new BigDecimal(row.get(IrisDataCsvHeaders.pop0610)));
					iris.setPop1117(new BigDecimal(row.get(IrisDataCsvHeaders.pop1117)));
					iris.setPop1824(new BigDecimal(row.get(IrisDataCsvHeaders.pop1824)));
					iris.setPop2539(new BigDecimal(row.get(IrisDataCsvHeaders.pop2539)));
					iris.setPop4054(new BigDecimal(row.get(IrisDataCsvHeaders.pop4054)));
					iris.setPop5564(new BigDecimal(row.get(IrisDataCsvHeaders.pop5564)));
					iris.setPop6579(new BigDecimal(row.get(IrisDataCsvHeaders.pop6579)));
					iris.setPop80p(new BigDecimal(row.get(IrisDataCsvHeaders.pop80p)));
					iris.setPop0014(new BigDecimal(row.get(IrisDataCsvHeaders.pop0014)));
					iris.setPop1529(new BigDecimal(row.get(IrisDataCsvHeaders.pop1529)));
					iris.setPop3044(new BigDecimal(row.get(IrisDataCsvHeaders.pop3044)));
					iris.setPop4559(new BigDecimal(row.get(IrisDataCsvHeaders.pop4559)));
					iris.setPop6074(new BigDecimal(row.get(IrisDataCsvHeaders.pop6074)));
					iris.setPop75p(new BigDecimal(row.get(IrisDataCsvHeaders.pop75p)));
					iris.setPop0019(new BigDecimal(row.get(IrisDataCsvHeaders.pop0019)));
					iris.setPop2064(new BigDecimal(row.get(IrisDataCsvHeaders.pop2064)));
					iris.setPop65p(new BigDecimal(row.get(IrisDataCsvHeaders.pop65p)));
					iris.setPoph(new BigDecimal(row.get(IrisDataCsvHeaders.poph)));
					iris.setH0014(new BigDecimal(row.get(IrisDataCsvHeaders.h0014)));
					iris.setH1529(new BigDecimal(row.get(IrisDataCsvHeaders.h1529)));
					iris.setH3044(new BigDecimal(row.get(IrisDataCsvHeaders.h3044)));
					iris.setH6074(new BigDecimal(row.get(IrisDataCsvHeaders.h6074)));
					iris.setH75p(new BigDecimal(row.get(IrisDataCsvHeaders.h75p)));
					iris.setH0019(new BigDecimal(row.get(IrisDataCsvHeaders.h0019)));
					iris.setH2064(new BigDecimal(row.get(IrisDataCsvHeaders.h2064)));
					iris.setH65p(new BigDecimal(row.get(IrisDataCsvHeaders.h65p)));
					iris.setPopf(new BigDecimal(row.get(IrisDataCsvHeaders.popf)));
					iris.setF0014(new BigDecimal(row.get(IrisDataCsvHeaders.f0014)));
					iris.setF1529(new BigDecimal(row.get(IrisDataCsvHeaders.f1529)));
					iris.setF3044(new BigDecimal(row.get(IrisDataCsvHeaders.f3044)));
					iris.setF6074(new BigDecimal(row.get(IrisDataCsvHeaders.f6074)));
					iris.setF75p(new BigDecimal(row.get(IrisDataCsvHeaders.f75p)));
					iris.setF0019(new BigDecimal(row.get(IrisDataCsvHeaders.f0019)));
					iris.setF2064(new BigDecimal(row.get(IrisDataCsvHeaders.f2064)));
					iris.setF65p(new BigDecimal(row.get(IrisDataCsvHeaders.f65p)));
					iris.setPop15p_cs1(new BigDecimal(row.get(IrisDataCsvHeaders.pop15p_cs1)));
					iris.setPop15p_cs2(new BigDecimal(row.get(IrisDataCsvHeaders.pop15p_cs2)));
					iris.setPop15p_cs3(new BigDecimal(row.get(IrisDataCsvHeaders.pop15p_cs3)));
					iris.setPop15p_cs4(new BigDecimal(row.get(IrisDataCsvHeaders.pop15p_cs4)));
					iris.setPop15p_cs5(new BigDecimal(row.get(IrisDataCsvHeaders.pop15p_cs5)));
					iris.setPop15p_cs6(new BigDecimal(row.get(IrisDataCsvHeaders.pop15p_cs6)));
					iris.setPop15p_cs7(new BigDecimal(row.get(IrisDataCsvHeaders.pop15p_cs7)));
					iris.setPop15p_cs8(new BigDecimal(row.get(IrisDataCsvHeaders.pop15p_cs8)));
					iris.setH15p(new BigDecimal(row.get(IrisDataCsvHeaders.h15p)));
					iris.setH15p_cs1(new BigDecimal(row.get(IrisDataCsvHeaders.h15p_cs1)));
					iris.setH15p_cs2(new BigDecimal(row.get(IrisDataCsvHeaders.h15p_cs2)));
					iris.setH15p_cs3(new BigDecimal(row.get(IrisDataCsvHeaders.h15p_cs3)));
					iris.setH15p_cs4(new BigDecimal(row.get(IrisDataCsvHeaders.h15p_cs4)));
					iris.setH15p_cs5(new BigDecimal(row.get(IrisDataCsvHeaders.h15p_cs5)));
					iris.setH15p_cs7(new BigDecimal(row.get(IrisDataCsvHeaders.h15p_cs7)));
					iris.setH15p_cs8(new BigDecimal(row.get(IrisDataCsvHeaders.h15p_cs8)));
					iris.setF15p(new BigDecimal(row.get(IrisDataCsvHeaders.f15p)));
					iris.setF15p_cs1(new BigDecimal(row.get(IrisDataCsvHeaders.f15p_cs1)));
					iris.setF15p_cs2(new BigDecimal(row.get(IrisDataCsvHeaders.f15p_cs2)));
					iris.setF15p_cs3(new BigDecimal(row.get(IrisDataCsvHeaders.f15p_cs3)));
					iris.setF15p_cs4(new BigDecimal(row.get(IrisDataCsvHeaders.f15p_cs4)));
					iris.setF15p_cs5(new BigDecimal(row.get(IrisDataCsvHeaders.f15p_cs5)));
					iris.setF15p_cs6(new BigDecimal(row.get(IrisDataCsvHeaders.f15p_cs6)));
					iris.setF15p_cs7(new BigDecimal(row.get(IrisDataCsvHeaders.f15p_cs7)));
					iris.setF15p_cs8(new BigDecimal(row.get(IrisDataCsvHeaders.f15p_cs8)));
					iris.setPop_fr(new BigDecimal(row.get(IrisDataCsvHeaders.pop_fr)));
					iris.setPop_etr(new BigDecimal(row.get(IrisDataCsvHeaders.pop_etr)));
					iris.setPop_imm(new BigDecimal(row.get(IrisDataCsvHeaders.pop_imm)));
					iris.setPmen(new BigDecimal(row.get(IrisDataCsvHeaders.pmen)));
					iris.setPhormen(new BigDecimal(row.get(IrisDataCsvHeaders.phormen)));

					irisDatas.add(iris);
				}
			}
		}

		return irisDatas;
	}
}