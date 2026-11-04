package com.github.cunvoas.geoserviceisochrone.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ContributeurRepository;

/**
 * E2E CUJ critique de sanity : "un contributeur consulte la carte d'une commune
 * et les couches GeoJSON répondent".
 * <p>
 * Boot réel (Tomcat + PostGIS via Testcontainers) → healthcheck → formLogin →
 * /mvc/consult/map → couches /map/** et /mvc/geojson/**.
 * Détails : {@code docs/cuj-sanity-consult-map.md}
 * </p>
 * <p>
 * Exécution (nécessite Docker) :
 * {@code mvn test -De2e=true -Dtest=CujSanityConsultMapE2EIT}
 * </p>
 * @author cunvoas
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
@EnabledIfSystemProperty(named = "e2e", matches = "true",
	disabledReason = "CUJ sanity E2E (Docker requis). Exécuter: mvn test -De2e=true -Dtest=CujSanityConsultMapE2EIT")
class CujSanityConsultMapE2EIT {

	private static final String LOGIN = "e2e-cuj";
	private static final String PASSWORD = "e2e-password-1234";

	/** PostGIS réel : le plan de données exécute du SQL natif ST_*. */
	static final PostgreSQLContainer<?> POSTGIS = new PostgreSQLContainer<>(
			DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"))
		.withDatabaseName("autmel").withUsername("autmel").withPassword("autmel");

	/**
	 * Datasource PostGIS de Testcontainers ; schema généré par Hibernate car les
	 * migrations Flyway supposent des données de prod absentes en base de test.
	 */
	@DynamicPropertySource
	static void datasource(DynamicPropertyRegistry registry) {
		POSTGIS.start();
		registry.add("spring.datasource.url", POSTGIS::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGIS::getUsername);
		registry.add("spring.datasource.password", POSTGIS::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
		registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
		registry.add("spring.flyway.enabled", () -> "false");
	}

	@LocalServerPort
	private int port;

	@Autowired
	private ContributeurRepository contributeurRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	/** Cookie jar minimal (JSESSIONID + XSRF-TOKEN). */
	private final List<String> cookies = new ArrayList<>();

	@Test
	void sanity_contributeur_consulte_carte_commune() throws IOException {
		// 1. Boot : l'application répond
		Response home = exchange("GET", "/home", null);
		assertEquals(200, home.status(), home.body());
		assertTrue(home.body().contains("AUT'MEL"), "/home doit contenir AUT'MEL");

		// 2. Healthcheck public autorisé en local
		assertEquals(200, exchange("HEAD", "/awake", null).status(), "healthcheck /awake");

		// 3. Utilisateur de test (schema Hibernate déjà créé)
		Contributeur contributeur = new Contributeur();
		contributeur.setLogin(LOGIN);
		contributeur.setEmail("e2e-cuj@autmel.local");
		contributeur.setPassword(passwordEncoder.encode(PASSWORD));
		contributeur.setRole(ContributeurRole.ASSOCIATION_CONSTRIBUTOR);
		contributeur.setCreationDate(new Date());
		contributeurRepository.save(contributeur);

		// 4. Page de login → pose le cookie CSRF (CookieCsrfTokenRepository)
		assertEquals(200, exchange("GET", "/login", null).status(), "page /login");
		String csrf = cookies.stream()
				.filter(v -> v.startsWith("XSRF-TOKEN="))
				.map(v -> URLDecoder.decode(v.substring(11), StandardCharsets.UTF_8))
				.findFirst().orElse(null);
		assertNotNull(csrf, "cookie XSRF-TOKEN doit être posé par /login");

		// 5. FormLogin avec CSRF → redirection de succès
		String form = "username=" + LOGIN + "&password=" + PASSWORD + "&_csrf=" + csrf;
		Response login = exchange("POST", "/login", form);
		assertEquals(302, login.status(), "formLogin doit rediriger");
		assertNotNull(login.location(), "Location après formLogin");
		assertFalse(login.location().contains("login?error"), "formLogin d'un compte valide doit réussir");
		String appUrl = login.location();

		// Suivre la redirection de succès ("" → /mvc/dashboard)
		if (appUrl.startsWith("/")) {
			Response redirect = exchange("GET", appUrl, null);
			if (redirect.location() != null) {
				appUrl = redirect.location();
			}
			Response dest = exchange("GET", appUrl, null);
			assertEquals(200, dest.status(), "destination après login: " + appUrl);
		}

		// 6. CUJ : page de consultation carte authentifiée
		Response map = exchange("GET", "/mvc/consult/map", null);
		assertEquals(200, map.status(), map.body());
		assertTrue(map.body().contains("id=\"idRegion\""), "sélecteur Région absent de consultMap");

		// 7. Couches GeoJSON authentifiées du plan de données (bbox Métropole Lille)
		Response parks = exchange("GET",
				"/mvc/geojson/parkPrefectureByCorner?swLat=50.60&swLng=2.95&neLat=51.00&neLng=3.30", null);
		assertEquals(200, parks.status(), parks.body());
		assertTrue(parks.body().contains("FeatureCollection"), "parkPrefectureByCorner doit renvoyer une FeatureCollection");

		// 8. Couche publique
		Response years = exchange("GET", "/map/years", null);
		assertEquals(200, years.status(), years.body());
		assertTrue(years.body().trim().startsWith("["), "/map/years doit répondre un JSON array");
	}

	/* ---------- petit client HTTP minimal, sans dépendance ---------- */

	private Response exchange(String method, String path, String body) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) URI.create("http://localhost:" + port + path)
				.toURL().openConnection();
		conn.setRequestMethod(method);
		conn.setInstanceFollowRedirects(false);
		if (!cookies.isEmpty()) {
			conn.setRequestProperty("Cookie", String.join("; ", cookies));
		}
		if (body != null) {
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
		}
		int status = conn.getResponseCode();
		collectCookies(conn);
		String location = conn.getHeaderField("Location");
		String text;
		try (var in = status >= 400 ? conn.getErrorStream() : conn.getInputStream()) {
			text = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		return new Response(status, location, text);
	}

	private void collectCookies(HttpURLConnection conn) {
		for (String setCookie : conn.getHeaderFields().getOrDefault("Set-Cookie", List.of())) {
			String pair = setCookie.split(";", 2)[0];
			if (!cookies.contains(pair)) {
				cookies.add(pair);
			}
		}
	}

	private record Response(int status, String location, String body) {
	}
}
