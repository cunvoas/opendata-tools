package com.github.cunvoas.geoserviceisochrone.model.opendata;


import java.util.Comparator;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity(name = "adm_region")
public class Region implements Comparator<Region> {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_region")
	private Long id;

	@Column(name = "name")
	private String name;
	
	@OneToMany( targetEntity=CommunauteCommune.class, mappedBy="region" )
	private List<CommunauteCommune> communauteCommunes;
	
	@OneToMany( targetEntity=City.class, mappedBy="region" )
	private List<City> cities;

	@Override
	public int compare(Region arg0, Region arg1) {
		if (arg0!=null && arg0.getName()!=null && arg1!=null) {
			return arg0.getName().compareTo(arg1.getName());
		}
		return 0;
	}
	

}
