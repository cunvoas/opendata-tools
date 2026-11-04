package com.github.cunvoas.geoserviceisochrone.service.export;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;

/**
 * Classe de test « détournée » pour produire les JSON statiques NDVI consommés par le front public :
 * data/ndvi/&lt;com2coId&gt;/ndvi_&lt;annee&gt;_&lt;com2coId&gt;.json (EPCI) et
 * data/ndvi/&lt;com2coId&gt;/&lt;cityId&gt;/ndvi_&lt;annee&gt;_&lt;cityId&gt;.json (communes ayant des mesures).
 *
 * <p>Export manuel : retirer {@link Disabled} puis lancer
 * {@code mvn -Dtest=TestNdviJsonExport#exportNdviJson test}.
 * La sortie liste chaque répertoire et chaque ville écrits.</p>
 */
@SpringBootTest
@ActiveProfiles({"dev"})
class TestNdviJsonExport {

	@Autowired
	private ServicePublicationExporter servicePublicationExporter;

	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;

	@Autowired
	private ParkAreaComputedRepository parkAreaComputedRepository;

	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;

	@Test
	@Disabled("Export manuel : retirer @Disabled pour générer les JSON statiques NDVI")
	void exportNdviJson() {
		try {
			for (CommunauteCommune com2co : communauteCommuneRepository
					.findAllById(parkAreaComputedRepository.findCom2coIdsWithComputedParks())) {
				Map<String, String> cityLabels = new HashMap<>();
				for (City city : com2co.getCities()) {
					cityLabels.put(String.valueOf(city.getId()), city.getName() + " (" + city.getInseeCode() + ")");
				}
				for (Integer annee : applicationBusinessProperties.getInseeAnnees()) {
					for (File file : servicePublicationExporter.writeNdviJson(com2co, annee)) {
						// un fichier de commune est dans <com2coId>/<cityId>/, celui de l'EPCI dans <com2coId>/
						boolean isCity = file.getParentFile().getParentFile().getName().equals(String.valueOf(com2co.getId()));
						String where = isCity
								? "ville " + cityLabels.get(file.getParentFile().getName())
								: "EPCI " + com2co.getName();
						System.out.println("NDVI " + annee + " " + where + " -> " + file);
					}
				}
			}
		} catch (Exception e) {
			fail("Export des JSON NDVI impossible", e);
		}
	}
}
