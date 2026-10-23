package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import com.google.common.util.concurrent.RateLimiter;

@ExtendWith(MockitoExtension.class)
class TestRateLimitInterceptor {



    @Mock
    private RateLimiter rateLimiter; 

    @InjectMocks
    private RateLimitingInterceptor interceptor;

    @Mock
    private HttpRequest request;

    @Mock
    private ClientHttpRequestExecution execution;

    @Mock
    private ClientHttpResponse response429;

    @Mock
    private ClientHttpResponse response200;

    private final byte[] body = new byte[0];

    @BeforeEach
    void setUp() {
        // Optionnel : Initialisation si nécessaire
    }

    @Test
    void should_RetryRequest_When_StatusIs429AndRetryAfterHeaderIsPresent() throws IOException {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.add("retry-after", "3");

        // Configuration de la réponse 429 initiale
        when(response429.getStatusCode()).thenReturn(HttpStatus.TOO_MANY_REQUESTS);
        when(response429.getHeaders()).thenReturn(headers);

        // Configuration de la réponse 200 lors du rejeu
        when(response200.getStatusCode()).thenReturn(HttpStatus.OK);

        // Chaînage des comportements : 1ère exécution -> 429, 2ème exécution -> 200
        when(execution.execute(request, body))
                .thenReturn(response429)
                .thenReturn(response200);

        // When
        ClientHttpResponse finalResponse = interceptor.intercept(request, body, execution);

        // Then
        // 1. Vérifie que la réponse finale est bien le succès (200 OK)
        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());

        // 2. Vérifie que la réponse 429 a bien été fermée pour libérer la connexion
        verify(response429, times(1)).close();

        // 3. Vérifie que le rate limiter a bien été sollicité avec la valeur "3" issue du header
        verify(rateLimiter, times(1)).acquire(3);

        // 4. Vérifie que l'exécution globale de la requête a eu lieu exactement 2 fois
        verify(execution, times(2)).execute(request, body);
    }

}
