package com.github.cunvoas.geoserviceisochrone.controller.form;


import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Form for Association page.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class FormAssociation {

	private Integer idAsso;
	private String nom;
	
	private String siteUrl;
	private String helloassoUrl;
	private String descriprion;
	private MultipartFile logo;
	
}
