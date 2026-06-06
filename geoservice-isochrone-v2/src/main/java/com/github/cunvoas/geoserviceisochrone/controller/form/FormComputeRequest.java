package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

import lombok.Data;

@Data
public class FormComputeRequest {

    private String token;

    private Long idRegion;
    private Long idCommunauteDeCommunes;
    private Long idCommune;
    private Integer requestedYear;

    private List<Region> regions;
    private List<CommunauteCommune> communautesDeCommunes;
    private List<City> communes;
}
