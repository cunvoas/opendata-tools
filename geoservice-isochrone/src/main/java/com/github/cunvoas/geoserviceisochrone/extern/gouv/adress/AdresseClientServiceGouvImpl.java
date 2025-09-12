package com.github.cunvoas.geoserviceisochrone.extern.gouv.adress;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto.AdressBo;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Implémentation du service de recherche d'adresses utilisant l'API adresse.data.gouv.fr.
 * <p>
 * Cette classe interroge l'API publique française pour obtenir des suggestions d'adresses
 * et les transforme en objets métier via le parseur GeoJSON.
 *
 * @see <a href="https://adresse.data.gouv.fr/api-doc/adresse">Documentation API adresse.data.gouv.fr</a>
 */
@Component
@Slf4j
public class AdresseClientServiceGouvImpl implements AdresseClientService {
	
	@Autowired
	private AdressGeoJsonParser adressGeoJsonParser;
	
	private static final String URL = "https://api-adresse.data.gouv.fr/";
	
	/**
     * Recherche les adresses via l'API adresse.data.gouv.fr et parse la réponse.
     *
     * @param insee   le code INSEE de la commune
     * @param requete la requête d'adresse
     * @return un ensemble d'adresses trouvées ou null en cas d'erreur de parsing
     */
	@Override
	public Set<AdressBo> getAdresses(String insee, String requete) {
		log.info("insee={}, q={}", insee, requete);
		
		//FIX because select2 send an array
		if (requete.indexOf(',')>0) {
			requete = requete.split(",")[1];
		}
		String strResp = this.search(insee, requete);
		
		try {
			log.info("strResp={}",strResp);
			return adressGeoJsonParser.parse(strResp);
			
		} catch (JsonProcessingException e) {
			log.error("adress not parsable", e);
			return null;
		}
	}
	
	protected String search(String insee, String requete) {
		
		StringBuilder sb = new StringBuilder(URL);
		sb.append("search/?citycode=").append(insee);
		sb.append("&q=").append(encodeValue(requete));
		log.debug(sb.toString());
		
		Request request = new Request.Builder()
				  .url(sb.toString())
				  .get()
				  .addHeader("accept", "application/json")
				  .addHeader("authority", "data.gouv.fr")
				  .addHeader("user-agent", "curl")
				  .build();
		
		return this.request(request);
	}
	
	protected String reverse(String lon, String lat) {
		StringBuilder sb = new StringBuilder(URL);
		sb.append("reverse/?lon=").append(lon);
		sb.append("&lat=").append(lat);
		log.debug(sb.toString());
		
		Request request = new Request.Builder()
				  .url(sb.toString())
				  .get()
				  .addHeader("accept", "application/json")
				  .addHeader("authority", "data.gouv.fr")
				  .addHeader("user-agent", "curl")
				  .build();
		
		return this.request(request);
	}
	
	
	private String request(Request request) {
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		String strResp = "";
		try {
			Response response = client.newCall(request).execute();
			strResp= response.body().string();	
		} catch (IOException e) {
			log.error(request.body().toString(), e);
		}
		return strResp;
	}
	
	private String encodeValue(String value) {
	    try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			return value.replaceAll(" ", "%20");
		}
	}

}