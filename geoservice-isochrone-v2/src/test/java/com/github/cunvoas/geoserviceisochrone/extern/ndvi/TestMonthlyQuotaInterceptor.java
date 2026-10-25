package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

class TestMonthlyQuotaInterceptor {

    private final HttpRequest request = mock(HttpRequest.class);
    private final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
    private final byte[] body = new byte[0];

    @Test
    void bloque_l_appel_si_le_quota_est_epuise() throws IOException {
        NdviQuotaService quotaService = mock(NdviQuotaService.class);
        when(quotaService.tryAcquire()).thenReturn(false);
        when(quotaService.getMonthlyQuota()).thenReturn(10000);

        assertThrows(NdviQuotaExceededException.class,
                () -> new MonthlyQuotaInterceptor(quotaService).intercept(request, body, execution));
        verify(execution, never()).execute(request, body);
    }

    @Test
    void laisse_passer_l_appel_si_le_quota_le_permet() throws IOException {
        NdviQuotaService quotaService = mock(NdviQuotaService.class);
        ClientHttpResponse response200 = mock(ClientHttpResponse.class);
        when(quotaService.tryAcquire()).thenReturn(true);
        when(response200.getStatusCode()).thenReturn(HttpStatus.OK);
        when(execution.execute(request, body)).thenReturn(response200);

        ClientHttpResponse result = new MonthlyQuotaInterceptor(quotaService).intercept(request, body, execution);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(quotaService).tryAcquire();
    }
}
