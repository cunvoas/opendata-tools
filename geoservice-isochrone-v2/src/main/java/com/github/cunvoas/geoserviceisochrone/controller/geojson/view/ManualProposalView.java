package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class ManualProposalView extends GeoJsonProperty {
    @EqualsAndHashCode.Include
    private Long id;
    private String name;
    private String mode;
    private Long surface;
    private String description;
}
