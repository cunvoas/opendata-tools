package com.github.cunvoas.geoserviceisochrone.extern.overpass.common;

/**
 * Représente un point géographique (lat, lon) dans le champ "geometry" d'un élément Overpass.
 */
public class LatLon {
    public double lat;
    public double lon;
    
    public LatLon() {
    	super();
    }
    public LatLon(double lat, double lon) {
    	super();
        this.lat = lat;
        this.lon = lon;
    }
}