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
 *
 * <p>La réservation est débitée avant l'envoi puis <b>remboursée</b> si la réponse
 * n'est pas 2XX ({@link #release()}) : CDSE ne facture que les requêtes réussies.
 * Le token OAuth n'est pas un appel Sentinel Hub et n'est pas décompté.</p>
 *
 * <p>Le SQL est spécifique à PostgreSQL ({@code ON CONFLICT ... RETURNING}) ; les
 * tests unitaires passent par un {@link JdbcTemplate} mocké.</p>
 */
@Service
public class NdviQuotaService {

    /**
     * Réserve une unité si le quota du mois n'est pas atteint.
     *
     * <p>Si la ligne du mois existe et que {@code request_count < quota}, l'upsert
     * l'incrémente et renvoie la nouvelle valeur. Si le quota est atteint, la clause
     * {@code WHERE} empêche l'incrément et {@code RETURNING} ne produit aucune ligne.</p>
     */
    private static final String RESERVE_SQL = """
            INSERT INTO ndvi_api_quota (quota_month, request_count) VALUES (?, 1)
            ON CONFLICT (quota_month) DO UPDATE
            SET request_count = ndvi_api_quota.request_count + 1
            WHERE ndvi_api_quota.request_count < ?
            RETURNING request_count
            """;

    /** Annule une réservation non facturée (réponse non-2XX). */
    private static final String RELEASE_SQL = """
            UPDATE ndvi_api_quota
            SET request_count = request_count - 1
            WHERE quota_month = ? AND request_count > 0
            """;

    private final JdbcTemplate jdbcTemplate;
    private final int monthlyQuota;

    /**
     * @param jdbcTemplate accès à la table {@code ndvi_api_quota}
     * @param monthlyQuota plafond mensuel ({@code application.ndvi.monthly-quota})
     */
    public NdviQuotaService(JdbcTemplate jdbcTemplate,
            @Value("${application.ndvi.monthly-quota:10000}") int monthlyQuota) {
        this.jdbcTemplate = jdbcTemplate;
        this.monthlyQuota = monthlyQuota;
    }

    /**
     * Réserve une requête pour le mois courant.
     *
     * <p>La clé de mois est au format {@code YYYY-MM} ({@link YearMonth#toString()}),
     * ce qui remet automatiquement le compteur à zéro au changement de mois sans
     * purge : une nouvelle ligne est créée.</p>
     *
     * @return vrai si la requête peut être envoyée, faux si le quota mensuel est épuisé
     */
    public boolean tryAcquire() {
        // Aucune ligne retournée = quota atteint : la requête ne doit pas partir
        List<Integer> reserved = jdbcTemplate.query(
                RESERVE_SQL,
                (rs, rowNum) -> rs.getInt(1),
                YearMonth.now().toString(),
                monthlyQuota);
        return !reserved.isEmpty();
    }

    /**
     * Annule la dernière réservation du mois courant.
     *
     * <p>Appelée quand la requête n'a pas abouti en 2XX : CDSE ne la facture pas.
     * La clause {@code request_count > 0} évite un compteur négatif.</p>
     *
     * <p>shortcut: à la frontière exacte d'un changement de mois, le remboursement
     * peut viser la nouvelle ligne ; impact limité à une unité, jamais de dépassement.</p>
     */
    public void release() {
        jdbcTemplate.update(RELEASE_SQL, YearMonth.now().toString());
    }

    /**
     * @return plafond mensuel configuré
     */
    public int getMonthlyQuota() {
        return monthlyQuota;
    }
}
