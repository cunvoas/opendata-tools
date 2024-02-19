package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity(name = "park_type")
public class ParkType {

    @Id
    @Column(name="id")
    private long id;

    @Column(name="park_type")
    private String type;
    
    @Column(name="i18n")
    private String i18n;
    
    @Column(name="oms")
    private Boolean oms;
    
    @Transient
    private String label;

}
