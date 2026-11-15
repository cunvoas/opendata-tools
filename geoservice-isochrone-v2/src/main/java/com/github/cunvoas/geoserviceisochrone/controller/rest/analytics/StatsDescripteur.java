package com.github.cunvoas.geoserviceisochrone.controller.rest.analytics;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Résumé d'un territoire (commune ou EPCI) pour une année, publié avec les tranches.
 * <p>
 * Il évite aux lecteurs de recharger les carreaux : un EPCI comme la MEL en compte 17 000.
 * Les deux m²/habitant sont définis dans docs/stats-surface.md.
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "habitants",
    "m2ParHabitantMoyen",
    "surfaceParcsOmsM2",
    "m2ParHabitantGlobal"
})
public class StatsDescripteur {

    /** Population prise en compte (somme des habitants des carreaux, au prorata du cadastre). */
    @JsonProperty("habitants")
    private Long habitants;

    /** Somme(population carreau x m²/habitant du carreau) / somme(population carreau). */
    @JsonProperty("m2ParHabitantMoyen")
    private Double m2ParHabitantMoyen;

    /** Somme des surfaces des parcs OMS actifs du territoire, chaque parc compté une fois. */
    @JsonProperty("surfaceParcsOmsM2")
    private Long surfaceParcsOmsM2;

    /** Somme(surface des parcs OMS) / population prise en compte. */
    @JsonProperty("m2ParHabitantGlobal")
    private Double m2ParHabitantGlobal;

    @JsonProperty("habitants")
    public Long getHabitants() {
        return habitants;
    }

    @JsonProperty("habitants")
    public void setHabitants(Long habitants) {
        this.habitants = habitants;
    }

    @JsonProperty("m2ParHabitantMoyen")
    public Double getM2ParHabitantMoyen() {
        return m2ParHabitantMoyen;
    }

    @JsonProperty("m2ParHabitantMoyen")
    public void setM2ParHabitantMoyen(Double m2ParHabitantMoyen) {
        this.m2ParHabitantMoyen = m2ParHabitantMoyen;
    }

    @JsonProperty("surfaceParcsOmsM2")
    public Long getSurfaceParcsOmsM2() {
        return surfaceParcsOmsM2;
    }

    @JsonProperty("surfaceParcsOmsM2")
    public void setSurfaceParcsOmsM2(Long surfaceParcsOmsM2) {
        this.surfaceParcsOmsM2 = surfaceParcsOmsM2;
    }

    @JsonProperty("m2ParHabitantGlobal")
    public Double getM2ParHabitantGlobal() {
        return m2ParHabitantGlobal;
    }

    @JsonProperty("m2ParHabitantGlobal")
    public void setM2ParHabitantGlobal(Double m2ParHabitantGlobal) {
        this.m2ParHabitantGlobal = m2ParHabitantGlobal;
    }
}
