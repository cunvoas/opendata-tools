package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonFormat(shape = Shape.ARRAY)
@JsonPropertyOrder({"lat", "lon"})
public class DtoCoordinate {
    public Double lat;
    public Double lon;

    public DtoCoordinate() {}

    public DtoCoordinate(Double lat, Double lon) {
        this.lat = lat;
        this.lon = lon;
    }

}
