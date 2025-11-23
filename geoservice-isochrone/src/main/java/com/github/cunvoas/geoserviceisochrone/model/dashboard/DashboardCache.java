package com.github.cunvoas.geoserviceisochrone.model.dashboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Modèle représentant une entrée de cache pour le tableau de bord.
 * Permet de stocker une valeur d'indicateur associée à un code spécifique.
 */
@Data
@EqualsAndHashCode(of = {"code"})
@Entity(name = "dashboard_cache")
public class DashboardCache {
    /** Constantes de codes pour différents types d'indicateurs. */
    public static final String ASSOS="ASSOS";
    public static final String CONTRIB="CONTRIB";
    public static final String CARREAUX="CARREAUX";
    public static final String FILOSOFIL="FILOSOFIL";
    public static final String ANNES="ANNES";
    public static final String COM2CO="COM2CO";
    public static final String COMMUNE="COMMUNE";
    public static final String PARC_ENTREE="PARC_ENTREE";
    public static final String PARC_REF="PARC_REF";
    public static final String PARC_CHCK="PARC_CHCK";
    public static final String PARC_CALC="PARC_CALC";
    public static final String COMPUTE_JOB_PENDING="COMPUTE_JOB_PENDING";

    /** Code identifiant l'indicateur. */
    @Id
    @Column(name = "id", length = 50)
    private String code;
    /** Valeur de l'indicateur. */
    @Column(name = "indicator_value")
    private Long indicator;

    /**
     * Constructeur par défaut.
     */
    public DashboardCache() {
        super();
    }
    /**
     * Constructeur avec code et valeur (Long).
     * @param code code de l'indicateur
     * @param nb valeur de l'indicateur
     */
    public DashboardCache(String code, Long nb) {
        super();
        this.code=code;
        this.indicator=nb;
    }
    /**
     * Constructeur avec code et valeur (Integer).
     * @param code code de l'indicateur
     * @param nb valeur de l'indicateur
     */
    public DashboardCache(String code, Integer nb) {
        super();
        this.code=code;
        this.indicator=Long.valueOf(nb);
    }
}