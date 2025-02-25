package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Data;

/**
 * Type od park.
 * translations are in messages.propoerties, key park.type.*
 */
@Data
@Entity(name = "park_type")
public class ParkType {

    @Id
    @Column(name="id")
    private Long id;

    @Column(name="park_type")
    private String type;
    
    @Column(name="i18n")
    private String i18n;
    
    @Column(name="oms")
    private Boolean oms;
    @Column(name="strict")
    private Boolean strict;
    
    @Transient
    private String label;

}
