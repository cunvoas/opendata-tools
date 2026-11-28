package com.github.cunvoas.geoserviceisochrone.support;

import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base pour les tests qui doivent charger le contexte Spring complet.
 *
 * <p>Le schéma de l'application utilise des colonnes PostgreSQL/PostGIS : une base
 * H2 ne suffit pas. On démarre donc un conteneur PostGIS (Testcontainers) et on
 * laisse Hibernate créer le schéma (Flyway est désactivé, les migrations supposant
 * le jeu de données de production).</p>
 *
 * <p>Les tests sont ignorés si Docker n'est pas disponible.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIf("dockerAvailable")
@TestPropertySource(properties = {
		// propriétés obligatoires absentes du profil test, valeurs factices
		"application.admin.export-path=/tmp/",
		"application.admin.export-data-path=/tmp/",
		"application.admin.photo-path=/tmp/",
		"application.admin.photo-path-origin=/tmp/",
		"application.business.oms.min_size=650",
		"application.business.oms.urban.duration=300",
		"application.business.oms.urban.area_min=3",
		"application.business.oms.urban.area_reco=12",
		"application.business.oms.suburban.duration=1200",
		"application.business.oms.suburban.area_min=10",
		"application.business.oms.suburban.area_reco=45",
		"application.business.insee.densite=11,12",
		"application.business.insee.annees=2020,2021,2022",
		"application.business.iris.annees=2020,2021",
		"application.mailjet.apiToken=dummy",
		"application.mailjet.apiSecret=dummy",
		"application.mailjet.attachementPath=/tmp/",
		"application.mailjet.senderEmail=noreply@localhost",
		"application.mailjet.senderName=Test",
		"application.feature-flipping.sendEmail=false",
		"application.feature-flipping.carre200m-impl=v4",
		"application.feature-flipping.iris-impl=v3",
		"application.ndvi.copernicus.statistics-url=http://localhost/statistics",
		"application.ndvi.copernicus.token-url=http://localhost/token",
		"application.security.export-credential-key=dummy",
		"application.security.export-ip-address=127.0.0.1",
		"application.security.token.expiration-ms=60000",
		"application.security.token.secret=dummy-secret-for-tests-only-0123456789",
		"web.cors.allowed-origins=http://localhost",
		"ISOCHRONE_SITE_GESTION=http://localhost/gestion",
		"ISOCHRONE_SITE_PUBLIQUE=http://localhost/public"
})
public abstract class PostgisContextTestBase {

	static final PostgreSQLContainer<?> POSTGIS = new PostgreSQLContainer<>(
			DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"))
			.withDatabaseName("autmel").withUsername("autmel").withPassword("autmel");

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

	static boolean dockerAvailable() {
		try {
			return DockerClientFactory.instance().isDockerAvailable();
		} catch (RuntimeException e) {
			return false;
		}
	}
}
