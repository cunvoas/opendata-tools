package com.github.cunvoas.geoserviceisochrone;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.service.export.ServicePublicationExporter;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceIris;


@SpringBootTest
@ActiveProfiles({"secret","rep"})
class TestExporterApplication {
	
	@Autowired
	private ServicePublicationExporter servicePublicationExporter;
	
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
			servicePublicationExporter.writeGeoJsonCarreaux();


		} catch (IOException e) {
			System.err.println(e);
			fail(e.getMessage());
		}
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


	
}