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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * DTO.
 */
@Service
@Slf4j
public class CityService {
	// WGS-84 SRID
	private GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	private OkHttpClient client = new OkHttpClient().newBuilder().build();

	@Autowired
	private GeoJson2GeometryHelper geoJson2GeometryHelper;
	@Autowired
	private CityRepository cityRepository;
	@Autowired
	private CadastreRepository cadastreRepository;
	@Autowired
	private LaposteRepository laposteRepository;

	/**
	 * populate cadastral data.
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
	 * findAll.
	 * @param page
	 * @return list City
	 */
	public Page<City> findAll(Pageable page) {
		return cityRepository.findAll(page);
	}

	/**
	 * findAround
	 * @param lat latitude
	 * @param lon longitude
	 * @param distanceM in meter
	 * @return list City
	 */
	
	public List<City> findAround(double lat, double lon, double distanceM) {
		// log.info("Looking for city around ({},{}) withing {} meters", lat, lon,
		// distanceM);
		Point p = factory.createPoint(new Coordinate(lon, lat));
		return cityRepository.findNearWithinDistance(p, distanceM);
	}
	/**
	 * findAround
	 * @param p Point
	 * @param distanceM in meter
	 * @return list City
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
	protected byte[] getGzipCadastre(String insee) {
		byte[] gzipFile = null;

		String dept = insee.substring(0, 2);
		String gzUrl = String.format(
				"https://cadastre.data.gouv.fr/data/etalab-cadastre/latest/geojson/communes/%s/%s/cadastre-%s-communes.json.gz",
				dept, insee, insee);

		Request request = new Request.Builder().url(gzUrl).get().build();

		try {
			Response response = client.newCall(request).execute();
			log.debug(response.headers().toString());

			gzipFile = response.body().bytes();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return gzipFile;
	}

	/**
	 * get cadastre.
	 * 
	 * @see https://cadastre.data.gouv.fr/data/etalab-cadastre/latest/geojson/communes/59/59001/cadastre-59001-communes.json.gz
	 * @see https://commons.apache.org/proper/commons-compress/examples.html
	 * @param gzipFile bytes of zip file
	 * @return  geoJson
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
