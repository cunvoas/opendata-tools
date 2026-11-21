package com.github.cunvoas.geoserviceisochrone.service.compute;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.config.property.FeatureFlippingProperties;
import com.sun.net.httpserver.HttpServer;

class TestCpuTemperatureService {

	private HttpServer server;
	private String metricsBody;
	private int port;

	@BeforeEach
	void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		port = server.getAddress().getPort();
		server.createContext("/metrics", exchange -> {
			byte[] body = metricsBody.getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(body);
			}
		});
		server.start();
	}

	@AfterEach
	void stopServer() {
		if (server != null) {
			server.stop(0);
		}
	}

	private CpuTemperatureService service(double threshold) {
		FeatureFlippingProperties props = new FeatureFlippingProperties();
		props.setCpuMetricsUrl("http://127.0.0.1:" + port + "/metrics");
		props.setCpuMetricsTimeoutMs(2000);
		props.setCpuTempThreshold(threshold);
		return new CpuTemperatureService(props);
	}

	@Test
	void overheated_when_metric_exceeds_threshold() {
		metricsBody = "# HELP cpu_temperature\ncpu_temperature{cpu=\"0\"} 82.5\n";

		assertTrue(service(75d).isCpuOverheated());
	}

	@Test
	void nominal_when_metric_is_below_threshold() {
		metricsBody = "cpu_temperature{cpu=\"0\"} 60\n";

		assertFalse(service(75d).isCpuOverheated());
	}

	@Test
	void ignores_comment_lines() {
		metricsBody = "# cpu_temperature 99\ncpu_temperature 50\n";

		assertFalse(service(75d).isCpuOverheated());
	}

	@Test
	void returns_false_when_metric_is_absent() {
		metricsBody = "other_metric 12\n";

		assertFalse(service(75d).isCpuOverheated());
	}

	@Test
	void returns_false_when_endpoint_is_unreachable() {
		FeatureFlippingProperties props = new FeatureFlippingProperties();
		props.setCpuMetricsUrl("http://127.0.0.1:1/metrics");
		props.setCpuMetricsTimeoutMs(200);
		props.setCpuTempThreshold(75d);

		assertFalse(new CpuTemperatureService(props).isCpuOverheated());
	}
}
