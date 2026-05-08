package com.github.cunvoas.geoserviceisochrone.extern.mel;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.AdresseClientService;
import com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto.AdressBo;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusEnum;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;


/**
 * Analyseur de fichiers CSV pour les parcs et jardins de Lyon.
 * Permet d'extraire les informations des espaces verts à partir d'un fichier CSV.
 * Utilise les entêtes définies dans l'énumération CsvHeaders.
 * 
 * @author cunvoas
 * @see https://commons.apache.org/proper/commons-csv/user-guide.html
 * 
 * @see https://opendata.roubaix.fr/explore/dataset/liste-des-jardins-familiaux-et-partages-de-roubaix/api/
 */
@Component
public class CsvLyonParkJardinParser {
	
	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private AdresseClientService adresseClientService;
	

	/**
	 * Énumération des entêtes de colonnes du CSV pour faciliter la maintenance.
	 * @author cunvoas
	 */
	//uid	nom	num	numvoie	voie	codepost	commune	code_insee	surf_tot_m2	clos

	public enum CsvHeaders {
		identifiant("uid"),
		nom("num"),
		num("nom"),
		numero("numvoie"),
		adresse("voie"),
		codePostal("codepost"),
		commune("commune"),
		codeInsee("Codeinsee"),
		surface("surf_tot_m2"),
		annee("clos");
		
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
	 * Analyse le fichier CSV fourni et retourne la liste des parcs et jardins extraits.
	 * @param csvFile Fichier CSV à analyser
	 * @return Liste des objets ParcEtJardin extraits
	 * @throws IOException en cas d'erreur de lecture du fichier
	 */
	public List<ParcEtJardin> parseCsv(File csvFile) throws IOException {
		List<ParcEtJardin> parks = new ArrayList<>();

		if (csvFile.isFile()) {
			CSVFormat format = CSVFormat.DEFAULT.builder()
					.setDelimiter(",")
					.setQuote('"')
					.setHeader(CsvHeaders.class)
					.setSkipHeaderRecord(true)
					.build();

			try (Reader reader = new FileReader(csvFile, Charset.forName("ISO-8859-1"))) {
				Iterable<CSVRecord> rows = format.parse(reader);

				for (CSVRecord row : rows) {
					ParcEtJardin park = new ParcEtJardin();
					park.setName(row.get(CsvHeaders.nom));
					
					String adr = row.get(CsvHeaders.numero);
					if (adr!=null && adr.length()>0) {
						adr=adr+" "+row.get(CsvHeaders.adresse);
					} else {
						adr=row.get(CsvHeaders.adresse);
					}
					park.setAdresse(adr);
					
					String insee = row.get(CsvHeaders.codeInsee);
					City c = cityRepository.findByInseeCode(insee);
					park.setCommune(c);
					park.setStatus(ParcStatusEnum.TO_QUALIFY);
					park.setSource(ParcSourceEnum.OPENDATA);
					park.setTypeId(1L);
					
					String s = row.get(CsvHeaders.surface);
					if (StringUtils.isNotBlank(s)) {
						park.setSurface(Double.parseDouble(s));
					}
					
					s = row.get(CsvHeaders.annee);
					if (s!=null && StringUtils.isNotBlank(s.trim()) && s.trim().matches("[0-9]{4}")) {
						park.setDateDebut(new Date(Integer.parseInt(s.trim()), 0, 1));
					}
					
					// get with IGN API
					
					Set<AdressBo> adresses = adresseClientService.getAdresses(insee, adr);
					for (AdressBo adresse : adresses) {
						if (adresse.getScore()>80f) {
							Double lng=adresse.getPoint().getX();
							Double lat=adresse.getPoint().getY();
							Point p = GeoShapeHelper.getPoint(lng, lat);
							park.setCoordonnee(p);
						}
						try {
							Thread.sleep(300L);
						} catch (InterruptedException e) {
						}
						break;
					}
					

					parks.add(park);
				}
			}
		}

		return parks;
	}
}