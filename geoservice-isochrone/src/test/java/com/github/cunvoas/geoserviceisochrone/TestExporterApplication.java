package com.github.cunvoas.geoserviceisochrone;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.service.analytics.StatsSurfaceService;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.export.ServicePublicationExporter;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceIris;


@SpringBootTest
@ActiveProfiles({"secret", "pi_nuc"}) //, "rep", "pi_nuc"
/**
 * 
cp -Rf /var/isochrone/data/* /work/PERSO/github/gh_pages/geoservice-data
cd /work/PERSO/github/gh_pages/geoservice-data
git status
 */
class TestExporterApplication {
	
	@Autowired
	private ServicePublicationExporter servicePublicationExporter;

	@Autowired
	private ServiceReadReferences serviceReadReferences;
	

	@Autowired
	private StatsSurfaceService statsSurfaceService;
	
	@Autowired
	private ServiceIris serviceIris;

	@Test
	@Order(1)
	void hostname() {
		try {
			System.out.println(
					InetAddress.getLocalHost().getHostName()
					);
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	@Disabled
	@Order(10)
	void writeRegions() {
		try {
			servicePublicationExporter.writeRegions();


		} catch (IOException e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}


	@Test
	@Disabled
	@Order(11)
	void writeGeoJsonCadastres() {
		try {
			servicePublicationExporter.writeGeoJsonCadastres();;


		} catch (IOException e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}


	@Test
	@Disabled
	@Order(21)
	void writeGeoJsonCarreaux() {
		try {
//			servicePublicationExporter.writeGeoJsonCarreaux();
			
			CommunauteCommune com2co=serviceReadReferences.getCommunauteCommuneById(1l);
			servicePublicationExporter.writeGeoJsonCarreaux(com2co, 2019);
			servicePublicationExporter.writeGeoJsonCarreaux(com2co, 2017);
			servicePublicationExporter.writeGeoJsonCarreaux(com2co, 2015);


		} catch (IOException e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}
	
	
	@Test
//	@Disabled
	@Order(100)
	void writeLiveDataByCom2coAndYear() {
		
		Integer[] annees= {2015,2017,2019};
//		Integer annee=2019;
		Long com2coId=1l;
		
		CommunauteCommune com2co=serviceReadReferences.getCommunauteCommuneById(com2coId);
		try {

//			export( com2co, annee);
			
			for (Integer anneeIter : annees) {
				export( com2co, anneeIter);
				
			}
			
			

		} catch (IOException e) {
			fail(e.getMessage());
		}

 	}
	
	private void  export(CommunauteCommune com2co, Integer annee) throws IOException {
		servicePublicationExporter.writeGeoJsonParkOutline(com2co, annee);
		servicePublicationExporter.writeGeoJsonIsochrone(com2co, annee);
		servicePublicationExporter.writeGeoJsonCarreaux(com2co, annee);
		servicePublicationExporter.writeGeoJsonIris(com2co, annee);
		// stats  par ville
		statsSurfaceService.writeStatsSurfaceByCom2CoIdAndAnnee(com2co.getId(), annee);
		// stats globales
		statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllDense(annee, com2co.getId());
		statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllSuburbs(annee, com2co.getId());
		statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllV2(annee, com2co.getId());
	}

	@Test
	@Disabled
	@Order(22)
	void writeGeoJsonParkOutline() {
		try {
			servicePublicationExporter.writeGeoJsonParkOutline();


		} catch (IOException e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}

	@Test
	@Disabled
	@Order(23)
	void writeGeoJsonIsochrone() {
		try {
			servicePublicationExporter.writeGeoJsonIsochrone();

		} catch (IOException e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}


	@Test
	@Disabled
	@Order(24)
	void writeGeoJsonIris() {
		try {
			servicePublicationExporter.writeGeoJsonIris();


		} catch (IOException e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}



	@Test
	@Disabled
	@Order(25)
	void writeStatsSurfaceByCom2CoIdAndAnnee() {
		try {

			statsSurfaceService.writeStatsSurfaceByCom2CoIdAndAnnee(1l, 2015);
			statsSurfaceService.writeStatsSurfaceByCom2CoIdAndAnnee(1l, 2017);
			statsSurfaceService.writeStatsSurfaceByCom2CoIdAndAnnee(1l, 2019);

		} catch (IOException e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}
	

	@Test
	@Disabled
	@Order(26)
	void writeStatsSurfaceByCom2CoIdAndAnneeAll() {

			try {
			statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllDense(2015, 1L);
			statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllDense(2017, 1L);
			statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllDense(2019, 1L);

			statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllSuburbs(2015, 1L);
			statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllSuburbs(2017, 1L);
			statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllSuburbs(2019, 1L);
				
			statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllV2(2015, 1L);
			statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllV2(2017, 1L);
			statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllV2(2019, 1L);
				
				
			} catch (StreamWriteException e) {
				fail(e.getMessage());
			} catch (DatabindException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			}
			
	}
	
	
}