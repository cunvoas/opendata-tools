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
 * <b>ClientIsoChroneApiV2</b> : Client pour l'appel à l'API isochrone v2 de l'IGN (GéoPlateforme).<br>
 * <br>
 * <b>Fonctionnalités principales :</b>
 * <ul>
 *   <li>Construit et exécute une requête HTTP vers l'API isochrone IGN (v2) pour obtenir une isochrone à partir d'une coordonnée et d'une durée.</li>
 *   <li>Gère la construction de l'URL avec tous les paramètres nécessaires (clé API, ressource, point, durée, profil, etc.).</li>
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
 *   <li>Documentation API isochrone IGN v2 :
 *     <a href="https://geoservices.ign.fr/documentation/services/services-geoplateforme/itineraire#72786">https://geoservices.ign.fr/documentation/services/services-geoplateforme/itineraire#72786</a>
 *   </li>
 *   <li>Exemple d'utilisation :
 *     <a href="https://geoservices.ign.fr/documentation/services/utilisation-web/exemples/bibiotheque-dacces-calcul-disochrones-et">https://geoservices.ign.fr/documentation/services/utilisation-web/exemples/bibiotheque-dacces-calcul-disochrones-et</a>
 *   </li>
 *   <li>Documentation OkHttp :
 *     <a href="https://square.github.io/okhttp/">https://square.github.io/okhttp/</a>
 *   </li>
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