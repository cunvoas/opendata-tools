package com.github.cunvoas.geoserviceisochrone.extern.ndvi;


import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenEOResponse(
    @JsonProperty("result")
    Map<String, Object> result,

    @JsonProperty("ndviMean") // Selon la structure exacte configurée dans votre contrat d'API
    Double ndviMean
) {}
