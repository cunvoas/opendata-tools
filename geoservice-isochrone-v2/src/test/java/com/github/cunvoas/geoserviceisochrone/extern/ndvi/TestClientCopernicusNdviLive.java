package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;


/**
 * Appel réel à l'API Statistical CDSE sur le polygone du parc de la Citadelle (Lille).
 * Désactivé par défaut : nécessite un compte CDSE configuré et consomme le quota mensuel.
 */
@SpringBootTest
//@ActiveProfiles({"dev"})
@ActiveProfiles({"pi_nuc"})
//@Disabled("Appel réel CDSE : credentials requis + consommation du quota mensuel (10 000 req/mois)")
class TestClientCopernicusNdviLive  {

    @Autowired
    private ClientCopernicusNdvi clientCopernicusNdvi;
    @Autowired
    private ParkAreaComputedRepository parkAreaComputedRepository;

	@Autowired
	private ServiceReadReferences serviceReadReferences;
	
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
    
    @Test
//    @Disabled("Appel réel CDSE : credentials requis + consommation du quota mensuel (10 000 req/mois)")
    void processCom2Co() {

		Long com2coId=1l;
		Integer[] annees= {2021};

		CommunauteCommune com2co=serviceReadReferences.getCommunauteCommuneById(com2coId);
		try {
			for (Integer anneeIter : annees) {
				
				for (City city : com2co.getCities()) {
					processNdvi( city.getId(), anneeIter);
				}
			}

		} catch (Exception e) {
			fail(e.getMessage());
		}
    }
    

    
    @Test
    @Disabled("Appel réel CDSE : credentials requis + consommation du quota mensuel (10 000 req/mois)")
    void processCity() {

		Long lille = 2878L;
		Integer annee= 2021;

		try {
			processNdvi( lille, annee);
			

		} catch (Exception e) {
			fail(e.getMessage());
		}
    }
    
    @Test
    @Disabled("Appel réel CDSE : credentials requis + consommation du quota mensuel (10 000 req/mois)")
    void processPark() {
    	Long idPark=317L;
		Integer annee= 2021;
		
		ParkArea parkArea = serviceReadReferences.getByIdParcEtJardin(idPark);
		ParkAreaComputed parcCpu = serviceReadReferences.getParkAreaComputedById(parkArea.getId(), annee);
		
		persist(parkArea, parcCpu);
    }
    
    
    
    void processNdvi(Long cityId, Integer annees) {
    	List<ParcEtJardin> pjs = serviceReadReferences.getParcEtJardinByCityId(cityId);
    	for (ParcEtJardin parcEtJardin : pjs) {
    		ParkArea parkArea = serviceReadReferences.getByIdParcEtJardin(parcEtJardin.getId());
    		
    		if (parkArea!=null && parkArea.getPolygon()!=null) {
        		ParkAreaComputed parcCpu = serviceReadReferences.getParkAreaComputedById(parkArea.getId(), annees);
        		persist(parkArea, parcCpu);
    		}
		}
    }
    
    @Transactional
    void persist(ParkArea parkArea,  ParkAreaComputed parcCpu) {

		if (parcCpu!=null && parcCpu.getNdviMean()==null && clientCopernicusNdvi.isEnabled()) {
			parcCpu.setNdviRequestDate(new Date());
			Optional<NdviStatistics> stats = clientCopernicusNdvi.fetchNdviForPark(parkArea.getId(), parkArea.getPolygon());
			if (stats.isPresent()) {
				NdviStatistics stat = stats.get();
				
				 parcCpu.setNdviMean(stat.mean());
				 parcCpu.setNdviMax(stat.max());
				 parcCpu.setNdviMin(stat.min());
				 parcCpu.setNdviStddev(stat.stdDev());
				 parcCpu.setNdviAcquisitionDate(stat.acquisitionDate());
				
			}
			 parkAreaComputedRepository.save(parcCpu);
		}
    }
}
