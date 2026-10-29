package com.github.cunvoas.geoserviceisochrone.extern.ndvi;


import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Composant d'intégration avec l'API Statistical de Copernicus Data Space Ecosystem (CDSE).
 * Récupère les statistiques NDVI (moyenne, min, max, écart-type) d'un polygone de parc.
 *
 * <p>Toute erreur (configuration absente, indisponibilité, quota, timeout) est absorbée :
 * un log WARN est émis et {@link Optional#empty()} est retourné afin de ne jamais bloquer
 * le calcul des indicateurs OMS.</p>
 *
 * @see <a href="https://documentation.dataspace.copernicus.eu/APIs/SentinelHub/Statistical.html">CDSE - Statistical API</a>
 * @see <a href="https://documentation.dataspace.copernicus.eu/APIs/Token.html">CDSE - Access Token</a>
 */
@Component
@Slf4j
public class ClientCopernicusNdvi {

    private static final Logger log = LoggerFactory.getLogger(ClientCopernicusNdvi.class);

    private static final String CRS_WGS84 = "http://www.opengis.net/def/crs/EPSG/0/4326";
    /** Sentinel-2 Level-2A (surface reflectance) : bandes B04/B08 et masque SCL disponibles. */
    private static final String COLLECTION_L2A = "sentinel-2-l2a";
    // Marge de sécurité avant l'expiration effective du token (30 s)
    private static final Duration TOKEN_EXPIRY_MARGIN = Duration.ofSeconds(30);

    /**
     * NDVI calculé sur les bandes rouge/proche infrarouge.
     * Pixels nuageux, saturés, ombres et eau exclus via le masque SCL.
     */
    private static final String NDVI_EVALSCRIPT = """
            //VERSION=3
            function setup() {
              return {
                input: [{ bands: ["B04", "B08", "SCL", "dataMask"] }],
                output: [
                  { id: "ndvi", bands: 1, sampleType: "FLOAT32" },
                  { id: "dataMask", bands: 1 }
                ]
              };
            }
            function evaluatePixel(s) {
              var ndvi = (s.B08 - s.B04) / (s.B08 + s.B04);
              var valid = s.dataMask === 1 && (s.B04 + s.B08) !== 0
                  && [1, 3, 6, 8, 9, 10].indexOf(s.SCL) === -1;
              return { ndvi: [ndvi], dataMask: [valid ? 1 : 0] };
            }
            """;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RestClient restClient;
    private final RestClient authClient;

    // Token en cache, recyclé jusqu'à expiration puis renouvelé
    private String accessToken;
    private Instant tokenExpiry = Instant.EPOCH;

    @Value("${application.ndvi.enabled:false}")
    private boolean enabled;

    @Value("${application.ndvi.copernicus.client-id:}")
    private String clientId;

    @Value("${application.ndvi.copernicus.client-secret:}")
    private String clientSecret;

    @Value("${application.ndvi.copernicus.token-url}")
    private String tokenUrl;

    @Value("${application.ndvi.copernicus.statistics-url}")
    private String statisticsUrl;

    /** Premier jour de la fenêtre NDVI, au format MM-dd (inclus). */
    @Value("${application.ndvi.window.from:06-01}")
    private String windowFrom;

    /** Dernier jour de la fenêtre NDVI, au format MM-dd (inclus). */
    @Value("${application.ndvi.window.to:06-06}")
    private String windowTo;

    /** Premier jour de la fenêtre de secours, au format MM-dd (inclus). Vide = pas de secours. */
    @Value("${application.ndvi.window.fallback.from:07-01}")
    private String fallbackWindowFrom;

    /** Dernier jour de la fenêtre de secours, au format MM-dd (inclus). Vide = pas de secours. */
    @Value("${application.ndvi.window.fallback.to:07-06}")
    private String fallbackWindowTo;

    /**
     * @param restClientBuilder builder fourni par Spring Boot
     * @param timeoutMs délai max (connexion + lecture) d'un appel CDSE
     */
    public ClientCopernicusNdvi(
    		RestClient.Builder restClientBuilder,
            NdviQuotaService quotaService,
            @Value("${application.ndvi.timeout-ms:10000}") int timeoutMs) {
    	
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                	HttpClient
                		.newBuilder()
                		.connectTimeout(Duration.ofMillis(timeoutMs))
                		.build());
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
        
        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                // quota à l'extérieur : une seule réservation par appel logique, remboursée si non-2XX
                .requestInterceptor(new MonthlyQuotaInterceptor(quotaService))
                .requestInterceptor(new RateLimitingInterceptor())
                .build();
        this.authClient = restClientBuilder
                .requestFactory(requestFactory)
                // le token OAuth n'est pas un appel Sentinel Hub : hors quota
                .build();
    }

    /**
     * Indique si le NDVI peut être récupéré (feature activée et credentials présents).
     * @return vrai si un appel CDSE est possible
     */
    public boolean isEnabled() {
        return enabled && StringUtils.hasText(clientId) && StringUtils.hasText(clientSecret);
    }

    /**
     * Retourne un token d'accès OAuth2 valide, en le recyclant jusqu'à expiration.
     * @return le JWT à passer en Bearer aux appels sur les API CDSE
     */
    public synchronized String getAccessToken() {
        if (Instant.now().isAfter(this.tokenExpiry.minus(TOKEN_EXPIRY_MARGIN))) {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);

            CdsTokenResponse token = this.authClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(CdsTokenResponse.class);

            if (token == null || token.accessToken() == null || token.expiresIn() == null) {
                log.warn("Copernicus OIDC: réponse de token invalide ou vide");
                throw new IllegalStateException("Récupération du token Copernicus impossible");
            }

            this.accessToken = token.accessToken();
            this.tokenExpiry = Instant.now().plusSeconds(token.expiresIn());
            log.info("Copernicus OIDC: token renouvelé, expiration dans {} s", token.expiresIn());
        }
        return this.accessToken;
    }

    /**
     * Calcule les statistiques NDVI du polygone d'un parc.
     *
     * @param parkId identifiant du parc (traçabilité des logs)
     * @param parkGeometry polygone JTS du parc (EPSG:4326)
     * @return les statistiques NDVI, ou vide en cas d'échec ou d'absence de données
     */
    public Optional<NdviStatistics> fetchNdviForPark(Long parkId, Polygon parkGeometry) {
        if (!isEnabled()) {
            log.warn("NDVI non récupéré pour le parc {} : configuration CDSE absente ou désactivée", parkId);
            return Optional.empty();
        }
        try {
            Optional<NdviStatistics> stats = fetchWindow(parkId, parkGeometry, windowFrom, windowTo);
            if (stats.isEmpty() && StringUtils.hasText(fallbackWindowFrom) && StringUtils.hasText(fallbackWindowTo)) {
                log.info("NDVI: aucune donnée sur la période principale pour le parc {}, essai de la période de secours", parkId);
                stats = fetchWindow(parkId, parkGeometry, fallbackWindowFrom, fallbackWindowTo);
            }
            if (stats.isEmpty()) {
                log.warn("NDVI: aucune donnée exploitable pour le parc {}", parkId);
            }
            return stats;
        } catch (Exception e) {
            log.warn("NDVI non récupéré pour le parc {} : {}", parkId, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<NdviStatistics> fetchWindow(Long parkId, Polygon parkGeometry, String from, String to) throws Exception {
        JsonNode response = restClient.post()
                .uri(statisticsUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildRequest(parkGeometry, from, to))
                .retrieve()
                .body(JsonNode.class);

        Optional<NdviStatistics> stats = parseResponse(response);
        if (stats.isEmpty()) {
            log.debug("NDVI: pas de donnée pour le parc {} sur la fenêtre {} → {}", parkId, from, to);
        }
        return stats;
    }

    private Map<String, Object> buildRequest(Polygon parkGeometry, String from, String to) throws Exception {
        JsonNode geometry = OBJECT_MAPPER.readTree(new GeoJsonWriter().write(parkGeometry));

        // Fenêtre configurée (année précédente pour disposer de l'archive L2A complète)
        int year = LocalDate.now().getYear() - 1;
        LocalDate start = MonthDay.parse("--" + from).atYear(year);
        // borne de fin exclusive : lendemain du dernier jour inclus
        LocalDate endExclusive = MonthDay.parse("--" + to).atYear(year).plusDays(1);
        String isoFrom = start + "T00:00:00Z";
        String isoTo = endExclusive + "T00:00:00Z";
        // Un seul intervalle d'agrégation sur toute la fenêtre (API Statistical : champ obligatoire)
        String aggregationInterval = "P" + ChronoUnit.DAYS.between(start, endExclusive) + "D";
        Map<String, Object> input = Map.of(
                "bounds", Map.of("geometry", geometry, "properties", Map.of("crs", CRS_WGS84)),
                "data", List.of(Map.of(
                        "type", COLLECTION_L2A,
                        "dataFilter", Map.of("mosaickingOrder", "leastCC"))));
        int[] size = requestSize(parkGeometry);
        Map<String, Object> aggregation = Map.of(
                "timeRange", Map.of(
                        "from", isoFrom,
                        "to", isoTo),
                "aggregationInterval", Map.of("of", aggregationInterval),
                "evalscript", NDVI_EVALSCRIPT,
                "width", size[0],
                "height", size[1]);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("input", input);
        request.put("aggregation", aggregation);
        return request;
    }

    /**
     * Calcule la taille de sortie à la résolution native Sentinel-2 (10 m) pour éviter
     * de suréchantillonner un petit parc et de gonfler les Processing Units CDSE.
     * Plafonnée à 512×512 (coût area ≤ 1 PU).
     *
     * @param parkGeometry emprise du parc en WGS84
     * @return largeur et hauteur en pixels, chacune entre 1 et 512
     */
    static int[] requestSize(Polygon parkGeometry) {
        Envelope env = parkGeometry.getEnvelopeInternal();
        double midLat = Math.toRadians((env.getMinY() + env.getMaxY()) / 2.0);
        int width = (int) Math.ceil(env.getWidth() * 111_320.0 * Math.cos(midLat) / 10.0);
        int height = (int) Math.ceil(env.getHeight() * 110_540.0 / 10.0);
        return new int[] {
                Math.max(1, Math.min(512, width)),
                Math.max(1, Math.min(512, height))
        };
    }

    private Optional<NdviStatistics> parseResponse(JsonNode response) {
        JsonNode data = response == null ? null : response.path("data");
        if (data == null || !data.isArray()) {
            return Optional.empty();
        }
        for (int i = data.size() - 1; i >= 0; i--) {
            JsonNode item = data.get(i);
            JsonNode stats = item.path("outputs").path("ndvi").path("bands").path("B0").path("stats");
            if (stats.path("mean").isNumber() && stats.path("sampleCount").asInt(0) > 0) {
                return Optional.of(new NdviStatistics(
                        toDecimal(stats, "mean"),
                        toDecimal(stats, "min"),
                        toDecimal(stats, "max"),
                        toDecimal(stats, "stDev"),
                        toDate(item.path("interval").path("to").asText(null))));
            }
        }
        return Optional.empty();
    }

    private static BigDecimal toDecimal(JsonNode stats, String field) {
        JsonNode value = stats.path(field);
        return value.isNumber() ? BigDecimal.valueOf(value.asDouble()) : null;
    }

    private static Date toDate(String isoInstant) {
        if (!StringUtils.hasText(isoInstant)) {
            return null;
        }
        try {
            return Date.from(Instant.parse(isoInstant));
        } catch (Exception e) {
            return null;
        }
    }
}
