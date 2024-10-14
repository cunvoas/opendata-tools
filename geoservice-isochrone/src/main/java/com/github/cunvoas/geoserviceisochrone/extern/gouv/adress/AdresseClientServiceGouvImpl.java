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
 * Client qui implémente: adresse.data.gouv
 * @see https://adresse.data.gouv.fr/api-doc/adresse
 */
@Component
@Slf4j
public class AdresseClientServiceGouvImpl implements AdresseClientService {
	
	@Autowired
	private AdressGeoJsonParser adressGeoJsonParser;
	
	private static final String URL = "https://api-adresse.data.gouv.fr/";
	
	@Override
	public Set<AdressBo> getAdresses(String insee, String requete) {
		String strResp = this.search(insee, requete);
		
		try {
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
