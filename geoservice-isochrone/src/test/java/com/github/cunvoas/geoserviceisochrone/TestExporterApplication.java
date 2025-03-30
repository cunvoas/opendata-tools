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


@SpringBootTest
@ActiveProfiles({"prod","dev"})
class TestExporterApplication {
	
	@Autowired
	private ServicePublicationExporter servicePublicationExporter;

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
	void loadParks() {
		try {
			servicePublicationExporter.writeRegions();


		} catch (IOException e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}
	
}