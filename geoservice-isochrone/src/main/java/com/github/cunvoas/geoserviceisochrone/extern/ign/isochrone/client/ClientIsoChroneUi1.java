package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
@Slf4j
@Deprecated
@ConditionalOnProperty(
	name="application.feature-flipping.isochrone-impl", 
	havingValue="ign-reverse-ui1")
public class ClientIsoChroneUi1 implements IsoChroneClientService {
	
	//@Autowired
	private String ignServiceBase="https://wxs.ign.fr/an7nvfzojv5wa96dsga5nk8w/geoportail/isochrone/rest/1.0.0";

	/**
	 * //point=3.1069023679917662,50.62485026020619
	 * @param longitude x approx  3 for Lille
	 * @param latitude  y  approx 50 for Lille
	 * @param distance  
	 * @return
	 */
	@Override
	public String getIsoChrone(Coordinate coordinate, String duration) {
		
		
		
		String strResp = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		StringBuilder sb = new StringBuilder(ignServiceBase);
		sb.append("/isochrone?gp-access-lib=3.2.0&resource=bdtopo-iso&point=");
		sb.append(coordinate.getLongitude());
		sb.append(",");
		sb.append(coordinate.getLatitude()); //50.62485026020619
		sb.append("&direction=departure&costType=time&costValue=");
		sb.append(duration);
		sb.append("&profile=pedestrian&timeUnit=second&distanceUnit=meter&crs=EPSG:4326");
		sb.append("&constraints=");
		//{%22constraintType%22:%22banned%22,%22key%22:%22wayType%22,%22operator%22:%22=%22,%22value%22:%22autoroute%22}|{%22constraintType%22:%22banned%22,%22key%22:%22wayType%22,%22operator%22:%22=%22,%22value%22:%22pont%22}|{%22constraintType%22:%22banned%22,%22key%22:%22wayType%22,%22operator%22:%22=%22,%22value%22:%22tunnel%22}");
		
		log.debug(sb.toString());;
		
		Request request = new Request.Builder()
				  .url(sb.toString())
				  .get()
				  .addHeader("accept", "*/*")
				  .addHeader("authority", "wxs.ign.fr")
				  .addHeader("accept-language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7")
//				  .addHeader("origin", "https://www.geoportail.gouv.fr")
				  .addHeader("sec-ch-ua-mobile", "?0")
				  .addHeader("sec-ch-ua-platform", "Linux")
				  .addHeader("sec-fetch-dest", "empty")
				  .addHeader("sec-fetch-mode", "cors")
				  .addHeader("sec-fetch-site", "cross-site")
				  .addHeader("Referer", "https://www.geoportail.gouv.fr")
				  .addHeader("Referrer-Policy", "strict-origin-when-cross-origin")
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
		
//		curl 'https://wxs.ign.fr/an7nvfzojv5wa96dsga5nk8w/geoportail/isochrone/rest/1.0.0/isochrone?gp-access-lib=3.2.0&resource=bdtopo-iso&point=3.1069023679917662,50.62485026020619&direction=departure&costType=time&costValue=300&profile=pedestrian&timeUnit=second&distanceUnit=meter&crs=EPSG:4326&constraints=\{%22constraintType%22:%22banned%22,%22key%22:%22wayType%22,%22operator%22:%22=%22,%22value%22:%22autoroute%22\}|\{%22constraintType%22:%22banned%22,%22key%22:%22wayType%22,%22operator%22:%22=%22,%22value%22:%22pont%22\}|\{%22constraintType%22:%22banned%22,%22key%22:%22wayType%22,%22operator%22:%22=%22,%22value%22:%22tunnel%22\}' \
//		  -H 'authority: wxs.ign.fr' \
//		  -H 'accept: */*' \
//		  -H 'accept-language: fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7' \
//		  -H 'origin: https://www.geoportail.gouv.fr' \
//		  -H 'referer: https://www.geoportail.gouv.fr/' \
//		  -H 'sec-ch-ua: "Chromium";v="110", "Not A(Brand";v="24", "Google Chrome";v="110"' \
//		  -H 'sec-ch-ua-mobile: ?0' \
//		  -H 'sec-ch-ua-platform: "Linux"' \
//		  -H 'sec-fetch-dest: empty' \
//		  -H 'sec-fetch-mode: cors' \
//		  -H 'sec-fetch-site: cross-site' \
//		  -H 'user-agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36' \
//		  --compressed
	}
}
