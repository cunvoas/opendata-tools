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
 * @see https://geoservices.ign.fr/documentation/services/services-geoplateforme/itineraire#72786
 * @see demo https://geoservices.ign.fr/documentation/services/utilisation-web/exemples/bibiotheque-dacces-calcul-disochrones-et 
 */
@Component
@Slf4j
@ConditionalOnProperty(
		name="application.feature-flipping.isochrone-impl", 
		havingValue="ign-api-v2")
public class ClientIsoChroneApiV2 implements IsoChroneClientService {
	
	private static final String URL = "https://data.geopf.fr/navigation/isochrone";
	
	@Override
	public String getIsoChrone(Coordinate coordinate, String duration) {
		String strResp = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		StringBuilder sb = new StringBuilder(URL);
		sb.append("?gp-access-lib=3.4.1&apiKey=calcul&resource=bdtopo-valhalla&point=");
		sb.append(coordinate.getLongitude());
		sb.append("%2C");
		sb.append(coordinate.getLatitude()); //50.62485026020619
		sb.append("&costValue=");
		sb.append(duration);
		
		sb.append("&direction=departure&costType=time&profile=pedestrian&timeUnit=second&distanceUnit=meter&crs=EPSG%3A4326&constraints=");
		
		log.debug(sb.toString());
		
		Request request = new Request.Builder()
				  .url(sb.toString())
				  .get()
				  .addHeader("accept", "application/json")
				  .addHeader("authority", "wxs.ign.fr")
				  .addHeader("accept-language", "fr-FR,fr")
				  .addHeader("sec-fetch-dest", "empty")
				  .addHeader("sec-fetch-mode", "cors")
				  .addHeader("sec-fetch-site", "cross-site")
				  .addHeader("Referer", "https://storage.gra.cloud.ovh.net/")
				  .addHeader("Origin", "https://storage.gra.cloud.ovh.net/")
//				  .addHeader("Referrer-Policy", "strict-origin-when-cross-origin")
				  .build();
		
		try {
			
			Response response = client.newCall(request).execute();
			log.debug(response.headers().toString());
			
			strResp= response.body().string();	
		} catch (IOException e) {
			log.error(sb.toString(), e);
		}
		
		return strResp;
	}

}
