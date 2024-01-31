package com.github.cunvoas.geoserviceisochrone.controller.form;


import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class FormLogin {


	private Integer idUser;
	private String nom;
	private String prenom;
	private String email;
	private String password;

	private MultipartFile avatar;
	
}
