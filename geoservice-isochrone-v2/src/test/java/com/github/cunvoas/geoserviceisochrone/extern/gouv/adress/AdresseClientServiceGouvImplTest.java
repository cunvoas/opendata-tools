package com.github.cunvoas.geoserviceisochrone.extern.gouv.adress;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.springframework.http.HttpHeaders;
import com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto.AdressBo;
import java.util.Set;

@SuppressWarnings("rawtypes")
class AdresseClientServiceGouvImplTest {
    @Test
    void testSearch() {
        AdressGeoJsonParser parserMock = Mockito.mock(AdressGeoJsonParser.class);
        RestClient restClientMock = Mockito.mock(RestClient.class);
        RequestHeadersUriSpec uriSpecMock = Mockito.mock(RequestHeadersUriSpec.class);
        RequestHeadersSpec headersSpecMock = Mockito.mock(RequestHeadersSpec.class);
        ResponseSpec responseSpecMock = Mockito.mock(ResponseSpec.class);

        Mockito.when(restClientMock.get()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(Mockito.anyString())).thenReturn(headersSpecMock);
        Mockito.when(headersSpecMock.headers(Mockito.any())).thenReturn(headersSpecMock);
        Mockito.when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.body(String.class)).thenReturn("{\"type\":\"FeatureCollection\",\"features\":[]}");

        AdresseClientServiceGouvImpl tested = new AdresseClientServiceGouvImpl(parserMock, restClientMock);
        String res = tested.search("59350", "rue salengro");
        assertNotNull(res);
        assertTrue(res.contains("FeatureCollection"));
    }

    @Test
    void testSearchRealApi() {
        // Utilisation d'un vrai RestClient
        RestClient restClient = RestClient.create();
        AdressGeoJsonParser parserMock = Mockito.mock(AdressGeoJsonParser.class);
        AdresseClientServiceGouvImpl tested = new AdresseClientServiceGouvImpl(parserMock, restClient);
        String res = tested.search("59350", "rue jean jaures");
        assertNotNull(res);
        assertTrue(res.contains("FeatureCollection"));
        assertTrue(res.length() > 50, "La réponse doit contenir des données JSON");
    }
}