package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author cunvoas
 * @see https://geoservices.ign.fr/documentation/services/api-et-services-ogc/isochrone/api
 */
@Component
@Slf4j
@ConditionalOnProperty(
		name="application.feature-flipping.isochrone-impl", 
		havingValue="ign-api-v1")
public class ClientIsoChroneApiV1 implements IsoChroneClientService {
	
	private static final String URL = "https://wxs.ign.fr/calcul/geoportail/isochrone/rest/1.0.0/";
	
	/*
	 *  
	 * curl 'https://wxs.ign.fr/calcul/geoportail/isochrone/rest/1.0.0/isochrone?
	 * 		point=2.337306%2C48.849319&costValue=300
	 * 		&resource=bdtopo-iso&distanceUnit=meter&costType=time&profile=pedestrian&direction=departure&geometryFormat=geojson&timeUnit=second&crs=EPSG%3A4326
	 * 		&constraints=%7B%22constraintType%22%3A%22banned%22%2C%22key%22%3A%22wayType%22%2C%22operator%22%3A%22%3D%22%2C%22value%22%3A%22autoroute%22%7D
	 * 		' 
	 * -H 'User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:120.0) Gecko/20100101 Firefox/120.0' 
	 * -H 'Accept: application/json' 
	 * -H 'Accept-Language: fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3' 
	 * -H 'Accept-Encoding: gzip, deflate, br' 
	 * -H 'Referer: https://storage.gra.cloud.ovh.net/' 
	 * -H 'Origin: https://storage.gra.cloud.ovh.net' 
	 * -H 'Connection: keep-alive' 
	 * -H 'Sec-Fetch-Dest: empty' 
	 * -H 'Sec-Fetch-Mode: cors' 
	 * -H 'Sec-Fetch-Site: cross-site'
	 * 
	 */
	@Override
	public String getIsoChrone(Coordinate coordinate, String duration) {
		String strResp = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		StringBuilder sb = new StringBuilder(URL);
		sb.append("/isochrone?point=");
		sb.append(coordinate.getLongitude());
		sb.append("%2C");
		sb.append(coordinate.getLatitude()); //50.62485026020619
		sb.append("&costValue=");
		sb.append(duration);
		//resource=bdtopo-pgr
		//resource=bdtopo-iso
		sb.append("&resource=bdtopo-pgr&distanceUnit=meter&costType=time&profile=pedestrian&direction=departure&geometryFormat=geojson&timeUnit=second&crs=EPSG%3A4326");
//		sb.append("&constraints=%7B%22constraintType%22%3A%22banned%22%2C%22key%22%3A%22wayType%22%2C%22operator%22%3A%22%3D%22%2C%22value%22%3A%22autoroute%22%7D");
		
		log.debug(sb.toString());;
		
		Request request = new Request.Builder()
				  .url(sb.toString())
				  .get()
				  .addHeader("accept", "application/json")
				  .addHeader("authority", "wxs.ign.fr")
				  .addHeader("accept-language", "fr-FR,fr")
//				  .addHeader("sec-ch-ua-mobile", "?0")
//				  .addHeader("sec-ch-ua-platform", "Linux")
				  .addHeader("sec-fetch-dest", "empty")
				  .addHeader("sec-fetch-mode", "cors")
				  .addHeader("sec-fetch-site", "cross-site")
				  .addHeader("Referer", "https://storage.gra.cloud.ovh.net/")
				  .addHeader("Origin", "https://storage.gra.cloud.ovh.net/")
				  
//				  .addHeader("Referrer-Policy", "strict-origin-when-cross-origin")
//				  .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
				  .build();
		
		try {
			
			Response response = client.newCall(request).execute();
			log.debug(response.headers().toString());
			
			strResp= response.body().string();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return strResp;
	}

}
