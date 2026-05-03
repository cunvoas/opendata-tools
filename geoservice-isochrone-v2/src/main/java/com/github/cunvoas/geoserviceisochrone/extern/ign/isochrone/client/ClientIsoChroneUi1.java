package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

import lombok.extern.slf4j.Slf4j;

/**
 * impl reverse v0.
 */
@Component
@Slf4j
@Deprecated
@ConditionalOnProperty(
	name="application.feature-flipping.isochrone-impl", 
	havingValue="ign-reverse-ui-v1")
public class ClientIsoChroneUi1 implements IsoChroneClientService {
	
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
		StringBuilder sb = new StringBuilder(ignServiceBase);
		sb.append("/isochrone?gp-access-lib=3.2.0&resource=bdtopo-iso&point=");
		sb.append(coordinate.getLongitude());
		sb.append(",");
		sb.append(coordinate.getLatitude());
		sb.append("&direction=departure&costType=time&costValue=");
		sb.append(duration);
		sb.append("&profile=pedestrian&timeUnit=second&distanceUnit=meter&crs=EPSG:4326");
		sb.append("&constraints=");
		// contraintes non renseignées ici

		log.debug(sb.toString());

		org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
		headers.add("accept", "*/*");
		headers.add("authority", "wxs.ign.fr");
		headers.add("accept-language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7");
//		headers.add("origin", "https://www.geoportail.gouv.fr");
		headers.add("sec-ch-ua-mobile", "?0");
		headers.add("sec-ch-ua-platform", "Linux");
		headers.add("sec-fetch-dest", "empty");
		headers.add("sec-fetch-mode", "cors");
		headers.add("sec-fetch-site", "cross-site");
		headers.add("Referer", "https://www.geoportail.gouv.fr");
		headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
//		headers.add("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36");

		try {
			org.springframework.web.client.RestClient restClient = org.springframework.web.client.RestClient.create();
			log.info("Appel API isochrone UI1 URL: {}", sb.toString());
			strResp = restClient.get()
				.uri(sb.toString())
				.headers(httpHeaders -> httpHeaders.addAll(headers))
				.retrieve()
				.onStatus(status -> !status.is2xxSuccessful(), (req, resp) -> {
					log.error("Réponse HTTP non 2xx: {}", resp.getStatusCode());
					throw new RuntimeException("Erreur HTTP: " + resp.getStatusCode());
				})
				.body(String.class);
			if (strResp == null) {
				log.error("Réponse de l'API isochrone UI1 null pour l'URL: {}", sb.toString());
			}
		} catch (Exception e) {
			log.error("Exception lors de l'appel à l'API isochrone UI1 (URL: {}): ", sb.toString(), e);
		}
		return strResp;
	}
}