package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

@SpringBootTest
@ActiveProfiles({"dev"})
//@Disabled("manual integration - needs real DB with Overpass data for INSEE 44109")
class ParkOverpassServiceIntegrationTest {

    @Autowired
    private ParkOverpassService parkOverpassService;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private ParkJardinRepository parkJardinRepository;

    @Test
    void integrateByInsee_should_create_or_update_parcs_for_nantes() {
//        String inseeCode = "44109"; // Nantes
        String inseeCode = "76540";	// Rouen
        City nantes = cityRepository.findByInseeCode(inseeCode);
        assertNotNull(nantes, "Nantes city should exist in DB");

        List<ParcEtJardin> before = parkJardinRepository.findByCityId(nantes.getId());
        assertNotNull(before);

        parkOverpassService.integrateByInsee(inseeCode);

        List<ParcEtJardin> after = parkJardinRepository.findByCityId(nantes.getId());
        assertNotNull(after);

        assertTrue(after.size() >= before.size(),
            "should have at least as many parks after integration");

        for (ParcEtJardin pj : after) {
            assertNotNull(pj.getSource(), "source should be set");
            if (ParcSourceEnum.OSM.equals(pj.getSource())) {
                assertTrue(pj.getContour() != null || pj.getCoordonnee() != null,
                    "OSM_OVERPASS park should have geometry");
            }
        }
    }

    @Test
    @Disabled
    void integrateByInsee_should_not_fail_for_unknown_insee() {
        parkOverpassService.integrateByInsee("00000");
    }

    @Test
    @Disabled
    void integrateByInsee_should_not_fail_when_no_overpass_data() {
        parkOverpassService.integrateByInsee("99999");
    }
}
