
package com.github.cunvoas.geoserviceisochrone.controller.rest.analytics;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surface",
    "habitants",
    "annee",
    "barColor"
})
public class Stat {

    @JsonProperty("surface")
    private String surface;
    @JsonProperty("habitants")
    private Integer habitants;
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

    @JsonProperty("barColor")
    public String getBarColor() {
        return barColor;
    }

    @JsonProperty("barColor")
    public void setBarColor(String barColor) {
        this.barColor = barColor;
    }


}
