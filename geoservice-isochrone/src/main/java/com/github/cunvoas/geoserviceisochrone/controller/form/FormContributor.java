package com.github.cunvoas.geoserviceisochrone.controller.form;

import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @see https://www.baeldung.com/spring-thymeleaf-error-messages
 */
@Data
public class FormContributor extends AbstractFormLocate {


	private Long id;
	
    @NotEmpty
    @Size(min = 1, max = 50)
	private String nom;
    
    @NotEmpty
    @Size(min = 1, max = 50)
	private String prenom;
    
    @NotEmpty
    @Size(min = 1, max = 30)
	private String login;

    @NotEmpty
    @Email
    @Size(min = 5, max = 100)
	private String email;
	
	private String password;
	 
	private String avatar;
	
//	@NotEmpty(message = "User's name cannot be empty.")
	private ContributeurRole role;
	
//	@Size(min = 1, message = "User's name cannot be empty.")
	private Long idAsso;


}
