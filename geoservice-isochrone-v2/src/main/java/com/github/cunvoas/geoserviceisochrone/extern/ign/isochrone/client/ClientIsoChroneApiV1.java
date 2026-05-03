package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * <b>ClientIsoChroneApiV1</b> : Client pour l'appel à l'API isochrone v1 de l'IGN.<br>
 * <br>
 * <b>Fonctionnalités principales :</b>
 * <ul>
 *   <li>Construit et exécute une requête HTTP vers l'API isochrone IGN (v1) pour obtenir une isochrone à partir d'une coordonnée et d'une durée.</li>
 *   <li>Gère la construction de l'URL avec tous les paramètres nécessaires (point, durée, profil, etc.).</li>
 *   <li>Utilise OkHttp pour l'appel HTTP et gère les en-têtes attendus par l'API IGN.</li>
 *   <li>Retourne la réponse brute JSON de l'API.</li>
 * </ul>
 *
 * <b>Dépendances :</b>
 * <ul>
 *   <li>{@link Coordinate} : Coordonnées du point de départ.</li>
 *   <li>OkHttp : Client HTTP pour Java (<a href="https://square.github.io/okhttp/">https://square.github.io/okhttp/</a>).</li>
 *   <li>Spring (@Component, @ConditionalOnProperty) pour l'injection et l'activation conditionnelle.</li>
 * </ul>
 *
 * <b>Ressources externes :</b>
 * <ul>
 *   <li>Documentation API isochrone IGN :
 *     <a href="https://geoservices.ign.fr/documentation/services/api-et-services-ogc/isochrone/api">https://geoservices.ign.fr/documentation/services/api-et-services-ogc/isochrone/api</a>
 *   </li>
 *   <li>Documentation OkHttp :
 *     <a href="https://square.github.io/okhttp/">https://square.github.io/okhttp/</a>
 *   </li>
 * </ul>
 *
 * <b>Utilisation :</b>
 * <pre>
 *   ClientIsoChroneApiV1 client = new ClientIsoChroneApiV1();
 *   String json = client.getIsoChrone(new Coordinate(2.337306, 48.849319), "300");
 * </pre>
 *
 * <b>Remarques :</b>
 * <ul>
 *   <li>Le client est activé uniquement si la propriété <code>application.feature-flipping.isochrone-impl</code> vaut <code>ign-api-v1</code>.</li>
 *   <li>La réponse est retournée telle quelle (JSON brut).</li>
 *   <li>En cas d'erreur réseau, la pile d'exception est affichée dans les logs.</li>
 * </ul>
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

		StringBuilder sb = new StringBuilder(URL);
		sb.append("/isochrone?point=");
		sb.append(coordinate.getLongitude());
		sb.append("%2C");
		sb.append(coordinate.getLatitude());
		sb.append("&costValue=");
		sb.append(duration);
		sb.append("&resource=bdtopo-pgr&distanceUnit=meter&costType=time&profile=pedestrian&direction=departure&geometryFormat=geojson&timeUnit=second&crs=EPSG%3A4326");
//		sb.append("&constraints=%7B%22constraintType%22%3A%22banned%22%2C%22key%22%3A%22wayType%22%2C%22operator%22%3A%22%3D%22%2C%22value%22%3A%22autoroute%22%7D");

		log.debug(sb.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.add("authority", "wxs.ign.fr");
		headers.add("accept-language", "fr-FR,fr");
		headers.add("sec-fetch-dest", "empty");
		headers.add("sec-fetch-mode", "cors");
		headers.add("sec-fetch-site", "cross-site");
		headers.add("Referer", "https://storage.gra.cloud.ovh.net/");
		headers.add("Origin", "https://storage.gra.cloud.ovh.net/");

		try {
			RestClient restClient = RestClient.create();
			log.info("Appel API isochrone URL: {}", sb.toString());
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
				log.error("Réponse de l'API isochrone null pour l'URL: {}", sb.toString());
			}
		} catch (Exception e) {
			log.error("Exception lors de l'appel à l'API isochrone (URL: {}): ", sb.toString(), e);
		}
		return strResp;
	}

}