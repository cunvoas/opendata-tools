package com.github.cunvoas.geoserviceisochrone.service.map;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionExtract;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Laposte;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.LaposteRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;

/**
 * Service pour la gestion des entités City et Cadastre.
 * <p>
 * Cette classe fournit des méthodes pour peupler, rechercher et manipuler les entités City et Cadastre,
 * en s'appuyant sur les référentiels associés. Elle permet notamment de récupérer et de décompresser
 * les fichiers GeoJSON compressés du cadastre via un appel HTTP distant.
 * <p>
 *
 * @author cunvoas
 */

@Service
@Slf4j

public class CityService {
	// WGS-84 SRID
	private GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

	@Autowired
	private RestClient restClient;

	/**
	 * Constructeur par défaut (utilisé par Spring)
	 */
	public CityService() {}

	/**
	 * Constructeur pour tests unitaires (injection d'un RestClient custom)
	 * @param restClient client HTTP à utiliser
	 */
	public CityService(RestClient restClient) {
		this.restClient = restClient;
	}

	@Autowired
	private GeoJson2GeometryHelper geoJson2GeometryHelper;
	@Autowired
	private CityRepository cityRepository;
	@Autowired
	private CadastreRepository cadastreRepository;
	@Autowired
	private LaposteRepository laposteRepository;

	/**
	 * Récupère et met à jour les entités City et Cadastre à partir des données Laposte et du cadastre.
	 * <p>
	 * Pour chaque entrée Laposte, crée ou met à jour la City correspondante, puis tente de récupérer
	 * le GeoJSON du cadastre (compressé) pour enrichir l'entité Cadastre associée.
	 */
	public void populateCities() {
		List<Laposte> postes = laposteRepository.findAll();
		for (Laposte laposte : postes) {
			String insee = laposte.getIdInsee();
			City city = cityRepository.findByInseeCode(insee);
			if (city == null) {
				city = new City();
				city.setInseeCode(insee);
				city.setPostalCode(laposte.getPostalCode());
				city.setName(laposte.getName());

				String gps = laposte.getCoordonneesGps();
				if (gps != null) {
					gps = gps.replaceAll(" ", "");
					city.setCoordinate(GeoShapeHelper.parsePointLatLng(gps));
				}
			}
			Optional<Cadastre> opCadastre = cadastreRepository.findById(insee);
			Cadastre cadastre=null;
			if (opCadastre.isPresent()) {
				cadastre = opCadastre.get();
			} else {
				cadastre = new Cadastre();
				cadastre.setIdInsee(insee);
				cadastre.setNom(city.getName());
				cadastre.setCreated(new Date());
			}
			
			if (cadastre.getGeoShape()==null) {
				try {
					byte[] gzb = this.getGzipCadastre(insee);
					if (gzb!=null) {
						String geoJson = this.getGeoJsonCadastre(gzb);
						Geometry geoShape = geoJson2GeometryHelper.parse(geoJson);
						cadastre.setGeoShape(geoShape);
						cadastre.setUpdated(new Date());
					}
					cadastreRepository.save(cadastre);
				} catch (Exception ignore) {
					
				}
			}
			cityRepository.save(city);
		}

	}

	/**
	 * Recherche paginée de toutes les entités City.
	 *
	 * @param page pagination
	 * @return page de City
	 */
	public Page<City> findAll(Pageable page) {
		return cityRepository.findAll(page);
	}

	/**
	 * Recherche les villes à proximité d'un point géographique.
	 *
	 * @param lat latitude du point de référence
	 * @param lon longitude du point de référence
	 * @param distanceM distance en mètres
	 * @return liste des villes à proximité
	 */
	
	public List<City> findAround(double lat, double lon, double distanceM) {
		// log.info("Looking for city around ({},{}) withing {} meters", lat, lon,
		// distanceM);
		Point p = factory.createPoint(new Coordinate(lon, lat));
		return cityRepository.findNearWithinDistance(p, distanceM);
	}
	/**
	 * Recherche les villes à proximité d'un point géographique.
	 *
	 * @param p point de référence (JTS)
	 * @param distanceM distance en mètres
	 * @return liste des villes à proximité
	 */
	public List<City> findAround(Point p, double distanceM) {
		// log.info("Looking for city around ({},{}) withing {} meters", lat, lon,
		// distanceM);
		return cityRepository.findNearWithinDistance(p, distanceM);
	}

	/**
	 * getGzipCadastre.
	 * @param insee code
	 * @return bytes
	 */
	/**
	 * Récupère le fichier GeoJSON compressé (gzip) du cadastre pour une commune donnée via HTTP.
	 * <p>
	 * Utilise {@link org.springframework.web.client.RestClient} pour effectuer la requête distante.
	 *
	 * @param insee code INSEE de la commune
	 * @return tableau d'octets du fichier gzip, ou null en cas d'échec
	 */
	protected byte[] getGzipCadastre(String insee) {
		String dept = insee.substring(0, 2);
		String gzUrl = String.format(
				"https://cadastre.data.gouv.fr/data/etalab-cadastre/latest/geojson/communes/%s/%s/cadastre-%s-communes.json.gz",
				dept, insee, insee);
		try {
			return doHttpGet(gzUrl);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Effectue un appel HTTP GET et retourne le corps sous forme de byte[].
	 * Surchargeable pour les tests unitaires.
	 */
	protected byte[] doHttpGet(String url) {
		return restClient.get()
				.uri(url)
				.retrieve()
				.body(byte[].class);
	}

	/**
	 * Décompresse un fichier gzip contenant un GeoJSON et retourne le contenu texte.
	 *
	 * @see <a href="https://cadastre.data.gouv.fr/data/etalab-cadastre/latest/geojson/communes/59/59001/cadastre-59001-communes.json.gz">Exemple de fichier</a>
	 * @see <a href="https://commons.apache.org/proper/commons-compress/examples.html">Exemple d'utilisation de commons-compress</a>
	 * @param gzipFile tableau d'octets du fichier gzip
	 * @return contenu GeoJSON sous forme de chaîne
	 * @throws ExceptionExtract en cas d'erreur de décompression
	 */
	protected String getGeoJsonCadastre(byte[] gzipFile) {

		String geoJson = null;

		OutputStream out = new OutputStream() {
			private StringBuilder sb = new StringBuilder();

			public void write(int b) throws IOException {
				this.sb.append((char) b);
			}

			// overrides this toString()
			public String toString() {
				return this.sb.toString();
			}
		};

		try {
			InputStream fin = new ByteArrayInputStream(gzipFile);
			BufferedInputStream in = new BufferedInputStream(fin);
			GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
			final byte[] buffer = new byte[1024];
			int n = 0;
			while (-1 != (n = gzIn.read(buffer))) {
				out.write(buffer, 0, n);
			}
			out.close();
			gzIn.close();
			geoJson = out.toString();
		} catch (IOException e) {
			throw new ExceptionExtract("GZip");
		}

		return geoJson;
	}
}
