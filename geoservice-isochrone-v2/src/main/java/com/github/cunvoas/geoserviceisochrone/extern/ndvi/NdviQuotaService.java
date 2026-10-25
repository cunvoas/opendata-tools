package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import java.time.YearMonth;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Compteur persistant des appels mensuels à l'API Statistical CDSE.
 *
 * <p>La réservation est atomique en base : l'incrément n'est effectué que si le
 * quota du mois courant n'est pas atteint. Un refus ne consomme donc aucun crédit
 * et le plafond est garanti même après un redémarrage ou en cas d'appels concurrents.</p>
 */
@Service
public class NdviQuotaService {

    /** Incrémente le compteur du mois si le quota n'est pas atteint. */
    private static final String RESERVE_SQL = """
            INSERT INTO ndvi_api_quota (quota_month, request_count) VALUES (?, 1)
            ON CONFLICT (quota_month) DO UPDATE
            SET request_count = ndvi_api_quota.request_count + 1
            WHERE ndvi_api_quota.request_count < ?
            RETURNING request_count
            """;

    private final JdbcTemplate jdbcTemplate;
    private final int monthlyQuota;

    public NdviQuotaService(JdbcTemplate jdbcTemplate,
            @Value("${application.ndvi.monthly-quota:10000}") int monthlyQuota) {
        this.jdbcTemplate = jdbcTemplate;
        this.monthlyQuota = monthlyQuota;
    }

    /**
     * Réserve une requête pour le mois courant.
     * @return vrai si la requête peut être envoyée, faux si le quota mensuel est épuisé
     */
    public boolean tryAcquire() {
        List<Integer> reserved = jdbcTemplate.query(
                RESERVE_SQL,
                (rs, rowNum) -> rs.getInt(1),
                YearMonth.now().toString(),
                monthlyQuota);
        return !reserved.isEmpty();
    }

    /** @return plafond mensuel configuré */
    public int getMonthlyQuota() {
        return monthlyQuota;
    }
}
