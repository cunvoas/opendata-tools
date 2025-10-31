
package com.github.cunvoas.geoserviceisochrone.controller.rest.analytics;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surface",
    "habitants",
    "ratio",
    "annee",
    "barColor"
})
public class Seuil {

    @JsonProperty("surface")
    private String surface;
    @JsonProperty("habitants")
    private Integer habitants=0;
    @JsonProperty("ratio")
    private String ratio;
    @JsonProperty("barColor")
    private String barColor;

    @JsonProperty("surface")
    public String getSurface() {
        return surface;
    }

    @JsonProperty("surface")
    public void setSurface(String surface) {
        this.surface = surface;
    }

    @JsonProperty("habitants")
    public Integer getHabitants() {
        return habitants;
    }

    @JsonProperty("habitants")
    public void setHabitants(Integer habitants) {
        this.habitants = habitants;
    }

    @JsonProperty("ratio")
    public String getRatio() {
        return ratio;
    }

    @JsonProperty("ratio")
    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    @JsonProperty("barColor")
    public String getBarColor() {
        return barColor;
    }

    @JsonProperty("barColor")
    public void setBarColor(String barColor) {
        this.barColor = barColor;
    }


}
