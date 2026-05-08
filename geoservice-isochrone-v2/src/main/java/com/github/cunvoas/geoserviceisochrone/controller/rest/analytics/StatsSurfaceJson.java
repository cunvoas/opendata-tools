
package com.github.cunvoas.geoserviceisochrone.controller.rest.analytics;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "nom",
    "insee",
    "annee",
    "stats",
    "seuils"
})
public class StatsSurfaceJson {

    @JsonProperty("nom")
    private String nom;
    @JsonProperty("insee")
    private String insee;
    @JsonProperty("annee")
    private String annee;
    @JsonProperty("stats")
    private List<Stat> stats=new java.util.ArrayList<>();
    @JsonProperty("seuils")
    private List<Seuil> seuils=new java.util.ArrayList<>();

    @JsonProperty("nom")
    public String getNom() {
        return nom;
    }

    @JsonProperty("nom")
    public void setNom(String nom) {
        this.nom = nom;
    }

    @JsonProperty("insee")
    public String getInsee() {
        return insee;
    }

    @JsonProperty("insee")
    public void setInsee(String insee) {
        this.insee = insee;
    }

    @JsonProperty("annee")
    public String getAnnee() {
        return annee;
    }

    @JsonProperty("annee")
    public void setAnnee(String annee) {
        this.annee = annee;
    }

    @JsonProperty("stats")
    public List<Stat> getStats() {
        return stats;
    }

    @JsonProperty("stats")
    public void setStats(List<Stat> stats) {
        this.stats = stats;
    }

    @JsonProperty("seuils")
    public List<Seuil> getSeuils() {
        return seuils;
    }

    @JsonProperty("seuils")
    public void setSeuils(List<Seuil> seuils) {
        this.seuils = seuils;
    }


}
