package com.github.cunvoas.geoserviceisochronev2.integration;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

@SpringBootTest
@Testcontainers
@ActiveProfiles("integration-tests")
public class ParcsJardinsLilleSteps {
    private static final String DOWNLOAD_URL = "https://www.data.gouv.fr/api/1/datasets/r/6d2c405d-f8ae-45dd-a85f-9a52ff4d9082";
    private static final String FILE_PATH = "target/parcs_jardins_lille.csv";

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgis/postgis:18-3.4");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    public static void beforeAll() throws Exception {
        // Téléchargement du fichier
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(DOWNLOAD_URL).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("Echec téléchargement: " + response);
            try (InputStream in = response.body().byteStream();
                 FileOutputStream out = new FileOutputStream(FILE_PATH)) {
                in.transferTo(out);
            }
        }
        // TODO: Créer la table et importer le CSV dans PostGIS
    }

    @Given("le fichier opendata des parcs et jardins de Lille est téléchargé et importé")
    public void fichier_telecharge_et_importe() throws Exception {
        // TODO: Créer la table et importer le CSV dans PostGIS
    }

    @When("je recherche un parc nommé {string}")
    public void recherche_parc_nom(String nom) {
        // TODO: Stocker le nom pour la vérification
    }

    @Then("je trouve un parc avec ce nom dans la base")
    public void trouve_parc() throws SQLException {
        // TODO: Vérifier la présence du parc dans la base
        // Exemple:
        // int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM parcs WHERE nom = ?", Integer.class, nom);
        // Assertions.assertTrue(count > 0);
    }
}
