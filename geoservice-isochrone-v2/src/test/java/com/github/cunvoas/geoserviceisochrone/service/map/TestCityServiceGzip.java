package com.github.cunvoas.geoserviceisochrone.service.map;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import static org.junit.jupiter.api.Assertions.*;

class CityServiceGzipTest {
    @Test
    void testGetGeoJsonCadastre_decompressesGzip() throws IOException {
        CityService service = new CityService();
        String json = "{\"type\":\"FeatureCollection\"}";
        byte[] gzipped;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(json.getBytes());
            gzip.close();
            gzipped = bos.toByteArray();
        }
        String result = service.getGeoJsonCadastre(gzipped);
        assertTrue(result.contains("FeatureCollection"));
    }
}
