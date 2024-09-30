package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

import lombok.Data;

@Data
public abstract class AbstractFormLocate {

	protected Long idRegion;
	protected Long idCommunauteDeCommunes;
	protected Long idCommune;

	protected List<Region> regions;
	protected List<CommunauteCommune> communautesDeCommunes;
	protected List<City> communes;
	
	private String nameRegion;
	private String nameCommunauteDeCommunes;
	private String nameCommune;
	
	
	public void autoLocate() {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication.getPrincipal() instanceof Contributeur) {
			Contributeur contributeur = (Contributeur)authentication;
			this.idRegion = contributeur.getIdRegion();
			this.idCommunauteDeCommunes = contributeur.getIdCommunauteCommune();
			this.idCommune = contributeur.getIdCommune();
			
		} else {
			
			// FIXME process with User Preferences 
			// TODO remove after test 
			this.setIdRegion(9L);
			this.setIdCommunauteDeCommunes(1L);
		}
		
	}
}
