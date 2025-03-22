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
 * CSV parser to import.
 * @author cunvoas
 * @see https://commons.apache.org/proper/commons-csv/user-guide.html
 */
@Component
public class CsvIrisDataParser {
	
	public static final String HEADER="IRIS;COM;TYP_IRIS;LAB_IRIS;P20_POP;P20_POP0002;P20_POP0305;P20_POP0610;P20_POP1117;P20_POP1824;P20_POP2539;P20_POP4054;P20_POP5564;P20_POP6579;P20_POP80P;P20_POP0014;P20_POP1529;P20_POP3044;P20_POP4559;P20_POP6074;P20_POP75P;P20_POP0019;P20_POP2064;P20_POP65P;P20_POPH;P20_H0014;P20_H1529;P20_H3044;P20_H4559;P20_H6074;P20_H75P;P20_H0019;P20_H2064;P20_H65P;P20_POPF;P20_F0014;P20_F1529;P20_F3044;P20_F4559;P20_F6074;P20_F75P;P20_F0019;P20_F2064;P20_F65P;C20_POP15P;C20_POP15P_CS1;C20_POP15P_CS2;C20_POP15P_CS3;C20_POP15P_CS4;C20_POP15P_CS5;C20_POP15P_CS6;C20_POP15P_CS7;C20_POP15P_CS8;C20_H15P;C20_H15P_CS1;C20_H15P_CS2;C20_H15P_CS3;C20_H15P_CS4;C20_H15P_CS5;C20_H15P_CS6;C20_H15P_CS7;C20_H15P_CS8;C20_F15P;C20_F15P_CS1;C20_F15P_CS2;C20_F15P_CS3;C20_F15P_CS4;C20_F15P_CS5;C20_F15P_CS6;C20_F15P_CS7;C20_F15P_CS8;P20_POP_FR;P20_POP_ETR;P20_POP_IMM;P20_PMEN;P20_PHORMEN";

	/**
	 * CSV Header definition for easier mods.
	 * @author cunvoas
	 */
	public enum IrisDataCsvHeaders {
		iris("IRIS"),
		com("COM"),
		typ_iris("TYP_IRIS"),
		lab_iris("LAB_IRIS"),
		p20_pop("P20_POP"),
		p20_pop0002("P20_POP0002"),
		p20_pop0305("P20_POP0305"),
		p20_pop0610("P20_POP0610"),
		p20_pop1117("P20_POP1117"),
		p20_pop1824("P20_POP1824"),
		p20_pop2539("P20_POP2539"),
		p20_pop4054("P20_POP4054"),
		p20_pop5564("P20_POP5564"),
		p20_pop6579("P20_POP6579"),
		p20_pop80p("P20_POP80P"),
		p20_pop0014("P20_POP0014"),
		p20_pop1529("P20_POP1529"),
		p20_pop3044("P20_POP3044"),
		p20_pop4559("P20_POP4559"),
		p20_pop6074("P20_POP6074"),
		p20_pop75p("P20_POP75P"),
		p20_pop0019("P20_POP0019"),
		p20_pop2064("P20_POP2064"),
		p20_pop65p("P20_POP65P"),
		p20_poph("P20_POPH"),
		p20_h0014("P20_H0014"),
		p20_h1529("P20_H1529"),
		p20_h3044("P20_H3044"),
		p20_h6074("P20_H6074"),
		p20_h75p("P20_H75P"),
		p20_h0019("P20_H0019"),
		p20_h2064("P20_H2064"),
		p20_h65p("P20_H65P"),
		p20_popf("P20_POPF"),
		p20_f0014("P20_F0014"),
		p20_f1529("P20_F1529"),
		p20_f3044("P20_F3044"),
		p20_f6074("P20_F6074"),
		p20_f75p("P20_F75P"),
		p20_f0019("P20_F0019"),
		p20_f2064("P20_F2064"),
		p20_f65p("P20_F65P"),
		c20_pop15p_cs1("C20_POP15P_CS1"),
		c20_pop15p_cs2("C20_POP15P_CS2"),
		c20_pop15p_cs3("C20_POP15P_CS3"),
		c20_pop15p_cs4("C20_POP15P_CS4"),
		c20_pop15p_cs5("C20_POP15P_CS5"),
		c20_pop15p_cs6("C20_POP15P_CS6"),
		c20_pop15p_cs7("C20_POP15P_CS7"),
		c20_pop15p_cs8("C20_POP15P_CS8"),
		c20_h15p("C20_H15P"),
		c20_h15p_cs1("C20_H15P_CS1"),
		c20_h15p_cs2("C20_H15P_CS2"),
		c20_h15p_cs3("C20_H15P_CS3"),
		c20_h15p_cs4("C20_H15P_CS4"),
		c20_h15p_cs5("C20_H15P_CS5"),
		c20_h15p_cs6("C20_H15P_CS6"),
		c20_h15p_cs7("C20_H15P_CS7"),
		c20_h15p_cs8("C20_H15P_CS8"),
		c20_f15p("C20_F15P"),
		c20_f15p_cs1("C20_F15P_CS1"),
		c20_f15p_cs2("C20_F15P_CS2"),
		c20_f15p_cs3("C20_F15P_CS3"),
		c20_f15p_cs4("C20_F15P_CS4"),
		c20_f15p_cs5("C20_F15P_CS5"),
		c20_f15p_cs6("C20_F15P_CS6"),
		c20_f15p_cs7("C20_F15P_CS7"),
		c20_f15p_cs8("C20_F15P_CS8"),
		p20_pop_fr("P20_POP_FR"),
		p20_pop_etr("P20_POP_ETR"),
		p20_pop_imm("P20_POP_IMM"),
		p20_pmen("P20_PMEN"),
		p20_phormen("P20_PHORMEN")
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
					iris.setP20pop(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop)));
					iris.setP20pop0002(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop0002)));
					iris.setP20pop0305(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop0305)));
					iris.setP20pop0610(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop0610)));
					iris.setP20pop1117(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop1117)));
					iris.setP20pop1824(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop1824)));
					iris.setP20pop2539(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop2539)));
					iris.setP20pop4054(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop4054)));
					iris.setP20pop5564(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop5564)));
					iris.setP20pop6579(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop6579)));
					iris.setP20pop80p(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop80p)));
					iris.setP20pop0014(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop0014)));
					iris.setP20pop1529(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop1529)));
					iris.setP20pop3044(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop3044)));
					iris.setP20pop4559(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop4559)));
					iris.setP20pop6074(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop6074)));
					iris.setP20pop75p(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop75p)));
					iris.setP20pop0019(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop0019)));
					iris.setP20pop2064(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop2064)));
					iris.setP20pop65p(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop65p)));
					iris.setP20poph(new BigDecimal(row.get(IrisDataCsvHeaders.p20_poph)));
					iris.setP20h0014(new BigDecimal(row.get(IrisDataCsvHeaders.p20_h0014)));
					iris.setP20h1529(new BigDecimal(row.get(IrisDataCsvHeaders.p20_h1529)));
					iris.setP20h3044(new BigDecimal(row.get(IrisDataCsvHeaders.p20_h3044)));
					iris.setP20h6074(new BigDecimal(row.get(IrisDataCsvHeaders.p20_h6074)));
					iris.setP20h75p(new BigDecimal(row.get(IrisDataCsvHeaders.p20_h75p)));
					iris.setP20h0019(new BigDecimal(row.get(IrisDataCsvHeaders.p20_h0019)));
					iris.setP20h2064(new BigDecimal(row.get(IrisDataCsvHeaders.p20_h2064)));
					iris.setP20h65p(new BigDecimal(row.get(IrisDataCsvHeaders.p20_h65p)));
					iris.setP20popf(new BigDecimal(row.get(IrisDataCsvHeaders.p20_popf)));
					iris.setP20f0014(new BigDecimal(row.get(IrisDataCsvHeaders.p20_f0014)));
					iris.setP20f1529(new BigDecimal(row.get(IrisDataCsvHeaders.p20_f1529)));
					iris.setP20f3044(new BigDecimal(row.get(IrisDataCsvHeaders.p20_f3044)));
					iris.setP20f6074(new BigDecimal(row.get(IrisDataCsvHeaders.p20_f6074)));
					iris.setP20f75p(new BigDecimal(row.get(IrisDataCsvHeaders.p20_f75p)));
					iris.setP20f0019(new BigDecimal(row.get(IrisDataCsvHeaders.p20_f0019)));
					iris.setP20f2064(new BigDecimal(row.get(IrisDataCsvHeaders.p20_f2064)));
					iris.setP20f65p(new BigDecimal(row.get(IrisDataCsvHeaders.p20_f65p)));
					iris.setC20pop15p_cs1(new BigDecimal(row.get(IrisDataCsvHeaders.c20_pop15p_cs1)));
					iris.setC20pop15p_cs2(new BigDecimal(row.get(IrisDataCsvHeaders.c20_pop15p_cs2)));
					iris.setC20pop15p_cs3(new BigDecimal(row.get(IrisDataCsvHeaders.c20_pop15p_cs3)));
					iris.setC20pop15p_cs4(new BigDecimal(row.get(IrisDataCsvHeaders.c20_pop15p_cs4)));
					iris.setC20pop15p_cs5(new BigDecimal(row.get(IrisDataCsvHeaders.c20_pop15p_cs5)));
					iris.setC20pop15p_cs6(new BigDecimal(row.get(IrisDataCsvHeaders.c20_pop15p_cs6)));
					iris.setC20pop15p_cs7(new BigDecimal(row.get(IrisDataCsvHeaders.c20_pop15p_cs7)));
					iris.setC20pop15p_cs8(new BigDecimal(row.get(IrisDataCsvHeaders.c20_pop15p_cs8)));
					iris.setC20h15p(new BigDecimal(row.get(IrisDataCsvHeaders.c20_h15p)));
					iris.setC20h15p_cs1(new BigDecimal(row.get(IrisDataCsvHeaders.c20_h15p_cs1)));
					iris.setC20h15p_cs2(new BigDecimal(row.get(IrisDataCsvHeaders.c20_h15p_cs2)));
					iris.setC20h15p_cs3(new BigDecimal(row.get(IrisDataCsvHeaders.c20_h15p_cs3)));
					iris.setC20h15p_cs4(new BigDecimal(row.get(IrisDataCsvHeaders.c20_h15p_cs4)));
					iris.setC20h15p_cs5(new BigDecimal(row.get(IrisDataCsvHeaders.c20_h15p_cs5)));
					iris.setC20h15p_cs6(new BigDecimal(row.get(IrisDataCsvHeaders.c20_h15p_cs6)));
					iris.setC20h15p_cs7(new BigDecimal(row.get(IrisDataCsvHeaders.c20_h15p_cs7)));
					iris.setC20h15p_cs8(new BigDecimal(row.get(IrisDataCsvHeaders.c20_h15p_cs8)));
					iris.setC20f15p(new BigDecimal(row.get(IrisDataCsvHeaders.c20_f15p)));
					iris.setC20f15p_cs1(new BigDecimal(row.get(IrisDataCsvHeaders.c20_f15p_cs1)));
					iris.setC20f15p_cs2(new BigDecimal(row.get(IrisDataCsvHeaders.c20_f15p_cs2)));
					iris.setC20f15p_cs3(new BigDecimal(row.get(IrisDataCsvHeaders.c20_f15p_cs3)));
					iris.setC20f15p_cs4(new BigDecimal(row.get(IrisDataCsvHeaders.c20_f15p_cs4)));
					iris.setC20f15p_cs5(new BigDecimal(row.get(IrisDataCsvHeaders.c20_f15p_cs5)));
					iris.setC20f15p_cs6(new BigDecimal(row.get(IrisDataCsvHeaders.c20_f15p_cs6)));
					iris.setC20f15p_cs7(new BigDecimal(row.get(IrisDataCsvHeaders.c20_f15p_cs7)));
					iris.setC20f15p_cs8(new BigDecimal(row.get(IrisDataCsvHeaders.c20_f15p_cs8)));
					iris.setP20pop_fr(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop_fr)));
					iris.setP20pop_etr(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop_etr)));
					iris.setP20pop_imm(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pop_imm)));
					iris.setP20pmen(new BigDecimal(row.get(IrisDataCsvHeaders.p20_pmen)));
					iris.setP20phormen(new BigDecimal(row.get(IrisDataCsvHeaders.p20_phormen)));

					irisDatas.add(iris);
				}
			}
		}

		return irisDatas;
	}
}
