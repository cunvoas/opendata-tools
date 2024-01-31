package com.github.cunvoas.geoserviceisochrone.model.admin;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "adm_asso")
public class Association {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_asso")
	private Long id;
	
	private String nom;
	private String email;
	private String logo;
	private String description;

	@Column(name = "site_url")
	private String siteUrl;
	@Column(name = "hello_asso_url")
	private String helloAssoUrl;
	
	
}
