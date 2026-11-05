package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.time.Year;

import com.github.cunvoas.geoserviceisochrone.service.solver.helper.ProposalComputationTypeAlgo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Form object for ProjectSimulator view.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FormProposition extends AbstractFormLocate {

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof FormProposition;
    }
    
    private Long id;
    private Integer annee = Year.now().getValue();
    private String codeInsee;
    private ProposalComputationTypeAlgo type;
    private String name;
    
    // Coordonnées carte (valeurs par défaut, redéfinies depuis AbstractFormLocate)
}
