package com.github.cunvoas.geoserviceisochrone.model.tools;

import lombok.Data;

@Data
public class EmailToContributor {
	
	private String email;
	private String name;
	
	private String subject;
	private String message;
	
	private String logoAutmel;	
	
	private String status;	
}
