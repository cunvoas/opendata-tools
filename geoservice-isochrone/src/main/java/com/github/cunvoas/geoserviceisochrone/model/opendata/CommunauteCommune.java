package com.github.cunvoas.geoserviceisochrone.model.opendata;


import java.util.Comparator;
import java.util.List;

import org.locationtech.jts.geom.Polygon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity(name = "adm_com2commune")
public class CommunauteCommune implements Comparator<CommunauteCommune> {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_com2com")
	private Long id;

	@Column(name = "name")
	private String name;
	 
	@ManyToOne
	@JoinColumn(name="id_region", nullable=true)
	private Region region;
	
	@OneToMany( targetEntity=City.class, mappedBy="communauteCommune" , fetch = FetchType.EAGER)
	List<City> cities;
	
	@Column(name = "carre_carte")
	private Polygon carreCarte;

	@Override
	public int compare(CommunauteCommune arg0, CommunauteCommune arg1) {
		if (arg0!=null && arg0.getName()!=null && arg1!=null) {
			return arg0.getName().compareTo(arg1.getName());
		}
		return 0;
	}

	
}
