package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.time.Year;

import com.github.cunvoas.geoserviceisochrone.service.solver.compute.ProposalComputationTypeAlgo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Form object for ProjectSimulator view.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FormProposition extends AbstractFormLocate {
    
    private Long id;
    private Integer annee = Year.now().getValue();
    private String codeInsee;
    private ProposalComputationTypeAlgo type;
    private String name;
    
    // Coordonnées carte (valeurs par défaut, redéfinies depuis AbstractFormLocate)
}
