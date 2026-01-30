package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.time.Year;

import com.github.cunvoas.geoserviceisochrone.service.solver.compute.ProposalComputationStrategyFactory;

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
    private ProposalComputationStrategyFactory.Type type;
    
    
    // Coordonnées carte (valeurs par défaut, redéfinies depuis AbstractFormLocate)
    public FormProposition() {
        super();
    }
}
