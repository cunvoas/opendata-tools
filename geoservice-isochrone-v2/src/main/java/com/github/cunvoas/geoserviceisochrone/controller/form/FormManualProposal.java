package com.github.cunvoas.geoserviceisochrone.controller.form;

import com.github.cunvoas.geoserviceisochrone.service.solver.helper.ProposalComputationTypeAlgo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FormManualProposal extends AbstractFormLocate {

    private Long id;
    private Long idMeta;
    private String name;
    private String mode = "CIRCLE";
    private String surface;
    private String diameter;
    private String description;
    private String sGeometry;
    private String codeInsee;
    private Integer annee;
    private ProposalComputationTypeAlgo type;
}
