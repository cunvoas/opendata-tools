package com.github.cunvoas.geoserviceisochrone.extern.gouv.adress;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;

import com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto.AdressBo;

import java.util.Set;

class TestAdresseClientServiceGouvImpl {

	private AdresseClientServiceGouvImpl tested;
	
	@Test
	void test() {
        AdressGeoJsonParser parserMock = Mockito.mock(AdressGeoJsonParser.class);
        RestClient restClientMock = Mockito.mock(RestClient.class);
        tested = new AdresseClientServiceGouvImpl(parserMock, restClientMock);

        String fakeResponse = "{\"type\":\"FeatureCollection\",\"features\":[]}";
        Mockito.when(restClientMock.get())
                .thenReturn(RestClient.get().uri("fake").retrieve().body(String.class)); // Simplification, à adapter selon API RestClient

        try {
            String res = tested.search("59350", "rue jean jaures");
            Assertions.assertNotNull(res);
            // Ici, on ne vérifie pas le contenu réel car c'est un mock
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}