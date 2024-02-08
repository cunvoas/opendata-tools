package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

//@Data
//@Entity(name = "park_type")
public class ParkType {

    @Id
    @Column(name="id")
    private long id;

    @Column(name="type")
    private String type;
    
    @Column(name="i18n")
    private String i18n;
    
    @Column(name="oms")
    private boolean oms;
    
    @Transient
    private String label;

}
