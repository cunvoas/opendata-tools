package com.github.cunvoas.geoserviceisochrone.model.opendata;


import java.util.Comparator;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity(name = "city")
public class City implements Comparator<City> {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_city")
	private long id;

	@Column(name = "name")
	private String name;

	@Column(name = "postal_code")
	private String postalCode;

	@Column(name = "insee_code")
	private String inseeCode;

	@Column(columnDefinition = "geometry(Point,4326)")
	private Point coordinate;

	@ManyToOne  
	@JoinColumn( name="id_comm2co" )
	private CommunauteCommune communauteCommune;
	 
	@ManyToOne
	@JoinColumn(name="id_region", nullable=true)
	private Region region;

	
	/**
	 * city label for search.
	 * @return
	 */
	public String getDisplay() {
		return postalCode+" - "+name;
	}


	@Override
	public int compare(City arg0, City arg1) {
		if (arg0!=null && arg0.getName()!=null && arg1!=null) {
			return arg0.getName().compareTo(arg1.getName());
		}
		return 0;
	}
}
