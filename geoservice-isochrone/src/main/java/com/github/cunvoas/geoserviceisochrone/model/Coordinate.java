package com.github.cunvoas.geoserviceisochrone.model;

import lombok.Data;

/**
 * Représente une coordonnée géographique avec une longitude et une latitude.
 * <p>
 * Cette classe est utilisée pour manipuler des points géographiques dans le système.
 * </p>
 * @author cunvoas
 */
@Data
public class Coordinate {
    /**
     * La longitude du point (axe X).
     */
    private Double longitude; //x
    /**
     * La latitude du point (axe Y).
     */
    private Double latitude;  //y
    
    /**
     * Construit une nouvelle coordonnée géographique.
     * @param longitude la longitude du point
     * @param latitude la latitude du point
     */
    public Coordinate(Double longitude, Double latitude) {
        super();
        this.longitude=longitude;
        this.latitude=latitude;
    }
    /**
     * Retourne la longitude (X).
     * @return la longitude
     */
    public Double getX() {
        return longitude;
    }
    /**
     * Définit la longitude (X).
     * @param longitude la nouvelle longitude
     */
    public void setX(Double longitude) {
        this.longitude = longitude;
    }
    /**
     * Retourne la latitude (Y).
     * @return la latitude
     */
    public Double getY() {
        return latitude;
    }
    /**
     * Définit la latitude (Y).
     * @param latitude la nouvelle latitude
     */
    public void setY(Double latitude) {
        this.latitude = latitude;
    }
    /**
     * Retourne une représentation textuelle de la coordonnée sous la forme "longitude,latitude".
     * @return la chaîne représentant la coordonnée
     */
    @Override
    public String toString() {
        return longitude +","+ latitude;
    }
    
    

}