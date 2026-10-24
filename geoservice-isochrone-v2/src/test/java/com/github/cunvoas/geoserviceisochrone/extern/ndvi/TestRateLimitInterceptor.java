package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import com.google.common.util.concurrent.RateLimiter;

class TestRateLimitInterceptor {

    private final HttpRequest request = mock(HttpRequest.class);
    private final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
    private final byte[] body = new byte[0];

    @Test
    void doit_limiter_a_4_requetes_par_seconde() {
        // RateLimiter.create attend des permis PAR seconde : 4.0 = 4 req/s
        RateLimitingInterceptor interceptor = new RateLimitingInterceptor();

        assertEquals(4.0, interceptor.getRate(), 0.0001);
    }

    @Test
    void doit_consommer_un_permis_avant_chaque_appel() throws IOException {
        RateLimiter rateLimiter = mock(RateLimiter.class);
        ClientHttpResponse response200 = mock(ClientHttpResponse.class);
        when(response200.getStatusCode()).thenReturn(HttpStatus.OK);
        when(execution.execute(request, body)).thenReturn(response200);

        ClientHttpResponse result = new RateLimitingInterceptor(rateLimiter).intercept(request, body, execution);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(rateLimiter, times(1)).acquire();
        verify(execution, times(1)).execute(request, body);
    }

    @Test
    void doit_rejouer_la_requete_apres_un_429_et_respecter_retry_after() throws IOException {
        RateLimiter rateLimiter = mock(RateLimiter.class);
        ClientHttpResponse response429 = mock(ClientHttpResponse.class);
        ClientHttpResponse response200 = mock(ClientHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("retry-after", "1");
        when(response429.getStatusCode()).thenReturn(HttpStatus.TOO_MANY_REQUESTS);
        when(response429.getHeaders()).thenReturn(headers);
        when(response200.getStatusCode()).thenReturn(HttpStatus.OK);
        when(execution.execute(request, body)).thenReturn(response429).thenReturn(response200);

        long start = System.currentTimeMillis();
        ClientHttpResponse finalResponse = new RateLimitingInterceptor(rateLimiter).intercept(request, body, execution);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
        verify(response429, times(1)).close();
        verify(execution, times(2)).execute(request, body);
        verify(rateLimiter, times(1)).acquire();
        assertEquals(true, elapsed >= 1000L, "retry-after non respecté : " + elapsed + " ms");
    }
}
