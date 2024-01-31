package com.github.cunvoas.geoserviceisochrone.controller.form;


import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class FormAssociation {

	private Integer idAsso;
	private String nom;
	
	private String siteUrl;
	private String helloassoUrl;
	private String descriprion;
	private MultipartFile logo;
	
}
