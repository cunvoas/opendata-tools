package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class TestNdviQuotaService {

    @Test
    void reserve_une_requete_si_le_quota_n_est_pas_atteint() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyString(), anyInt()))
                .thenReturn(List.of(42));

        NdviQuotaService service = new NdviQuotaService(jdbcTemplate, 10000);

        assertTrue(service.tryAcquire());
        assertEquals(10000, service.getMonthlyQuota());
    }

    @Test
    void refuse_la_requete_si_le_quota_est_atteint() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyString(), anyInt()))
                .thenReturn(List.of());

        assertFalse(new NdviQuotaService(jdbcTemplate, 10000).tryAcquire());
    }

    @Test
    void rembourse_une_reservation_non_facturee() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        new NdviQuotaService(jdbcTemplate, 10000).release();

        verify(jdbcTemplate).update(anyString(), anyString());
    }
}
