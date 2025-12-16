package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.math.BigDecimal;
import java.time.Year;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Form object for ProjectSimulator view.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FormProjectSimulator extends AbstractFormLocate {
    
    private Long id;
    private Integer annee = Year.now().getValue();
    private Boolean isDense;
    
    private BigDecimal population;
    private BigDecimal floorSurface;
    
    private BigDecimal densityPerAccommodation=new BigDecimal("2.16");
    private BigDecimal avgAreaAccommodation;
    
    private BigDecimal surfaceArea;
    private BigDecimal surfacePark;
    
    // Coordonnées carte (valeurs par défaut, redéfinies depuis AbstractFormLocate)
    public FormProjectSimulator() {
        super();
        this.mapLat = "50.626419";
        this.mapLng = "3.0719121";
    }
}
