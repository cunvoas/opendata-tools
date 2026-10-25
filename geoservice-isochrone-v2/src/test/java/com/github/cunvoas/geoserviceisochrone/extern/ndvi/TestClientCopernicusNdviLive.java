package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.BaseTest;

/**
 * Appel réel à l'API Statistical CDSE sur le polygone du parc de la Citadelle (Lille).
 * Désactivé par défaut : nécessite un compte CDSE configuré et consomme le quota mensuel.
 */
@SpringBootTest
@ActiveProfiles({"dev"})
@Disabled("Appel réel CDSE : credentials requis + consommation du quota mensuel (10 000 req/mois)")
class TestClientCopernicusNdviLive  {

    @Autowired
    private ClientCopernicusNdvi clientCopernicusNdvi;

    @Test
    @Disabled("Appel réel CDSE : credentials requis + consommation du quota mensuel (10 000 req/mois)")
    void appel_reel_sur_le_parc_de_la_citadelle() {
        // Emprise du parc de la Citadelle à Lille (EPSG:4326)
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Polygon citadelle = factory.createPolygon(new Coordinate[] {
                new Coordinate(3.0405, 50.6355),
                new Coordinate(3.0545, 50.6355),
                new Coordinate(3.0545, 50.6455),
                new Coordinate(3.0405, 50.6455),
                new Coordinate(3.0405, 50.6355) });

        Optional<NdviStatistics> stats = clientCopernicusNdvi.fetchNdviForPark(1L, citadelle);

        System.out.println(stats);
        assertTrue(stats.isPresent(), "NDVI attendu pour le parc de la Citadelle");
        assertNotNull(stats.get().mean());
        assertTrue(stats.get().mean().doubleValue() >= -1.0 && stats.get().mean().doubleValue() <= 1.0,
                "NDVI hors bornes : " + stats.get().mean());
        assertNotNull(stats.get().acquisitionDate());
    }
}
