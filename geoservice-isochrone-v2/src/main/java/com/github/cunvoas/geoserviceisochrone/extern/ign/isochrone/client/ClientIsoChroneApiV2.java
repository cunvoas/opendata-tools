package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

import lombok.extern.slf4j.Slf4j;

/**
 * <b>ClientIsoChroneApiV2</b> : Client pour l'appel à l'API isochrone v2 de l'IGN (GéoPlateforme).<br>
 * <br>
 * <b>Fonctionnalités principales :</b>
 * <ul>
 *   <li>Construit et exécute une requête HTTP vers l'API isochrone IGN (v2) pour obtenir une isochrone à partir d'une coordonnée et d'une durée.</li>
 *   <li>Gère la construction de l'URL avec tous les paramètres nécessaires (clé API, ressource, point, durée, profil, etc.).</li>
 *   <li>Utilise RestClient (Spring) pour l'appel HTTP et gère les en-têtes attendus par l'API IGN.</li>
 *   <li>Retourne la réponse brute JSON de l'API.</li>
 * </ul>
 *
 * <b>Dépendances :</b>
 * <ul>
 *   <li>{@link Coordinate} : Coordonnées du point de départ.</li>
 *   <li>RestClient (Spring) : Client HTTP pour Java.</li>
 *   <li>Spring (@Component, @ConditionalOnProperty) pour l'injection et l'activation conditionnelle.</li>
 * </ul>
 *
 * <b>Ressources externes :</b>
 * <ul>
 *   <li>Documentation API isochrone IGN v2 :
 *     <a href="https://geoservices.ign.fr/documentation/services/services-geoplateforme/itineraire#72786">https://geoservices.ign.fr/documentation/services/services-geoplateforme/itineraire#72786</a>
 *     <a href="https://geoservices.ign.fr/documentation/services/services-geoplateforme/isochrone">https://geoservices.ign.fr/documentation/services/services-geoplateforme/isochrone</a>
 *   </li>
 *   <li>Exemple d'utilisation :
 *     <a href="https://geoservices.ign.fr/documentation/services/utilisation-web/exemples/bibiotheque-dacces-calcul-disochrones-et">https://geoservices.ign.fr/documentation/services/utilisation-web/exemples/bibiotheque-dacces-calcul-disochrones-et</a>
 *   </li>
 *   <!-- OkHttp n'est plus utilisé -->
 * </ul>
 *
 * <b>Utilisation :</b>
 * <pre>
 *   ClientIsoChroneApiV2 client = new ClientIsoChroneApiV2();
 *   String json = client.getIsoChrone(new Coordinate(2.337306, 48.849319), "300");
 * </pre>
 *
 * <b>Remarques :</b>
 * <ul>
 *   <li>Le client est activé uniquement si la propriété <code>application.feature-flipping.isochrone-impl</code> vaut <code>ign-api-v2</code>.</li>
 *   <li>La réponse est retournée telle quelle (JSON brut).</li>
 *   <li>En cas d'erreur réseau, la pile d'exception est affichée dans les logs.</li>
 * </ul>
 */
@Component
@Slf4j
@ConditionalOnProperty(
		name="application.feature-flipping.isochrone-impl", 
		havingValue="ign-api-v2")
public class ClientIsoChroneApiV2 implements IsoChroneClientService {

	private static final String URL = "https://data.geopf.fr/navigation/isochrone";

	private final RestClient restClient;

	@Autowired
	public ClientIsoChroneApiV2(RestClient restClient) {
		this.restClient = restClient;
	}

	@Override
	public String getIsoChrone(Coordinate coordinate, String duration) {
		StringBuilder sb = new StringBuilder(URL);
		sb.append("?gp-access-lib=3.4.1&apiKey=calcul&resource=bdtopo-valhalla&point=");
		sb.append(coordinate.getLongitude());
		sb.append(",");
		sb.append(coordinate.getLatitude());
		sb.append("&costValue=");
		sb.append(duration);
		sb.append("&direction=departure&costType=time&profile=pedestrian&timeUnit=second&distanceUnit=meter&crs=EPSG:4326&constraints=");

		String url = sb.toString();
		log.debug(url);

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
			String response = restClient.get()
					.uri(url)
					.headers(httpHeaders -> httpHeaders.addAll(headers))
					.retrieve()
					.body(String.class);
			return response;
		} catch (org.springframework.web.client.HttpStatusCodeException e) {
			log.error("HTTP error {} for URL {}: {}", e.getStatusCode(), url, e.getResponseBodyAsString());
			return null;
		} catch (Exception e) {
			log.error("Exception for URL {}: {}", url, e.getMessage(), e);
			return null;
		}
	}

}