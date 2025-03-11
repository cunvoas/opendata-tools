package com.github.cunvoas.geoserviceisochrone.service.export.dto;

import org.locationtech.jts.geom.Point;
import org.springframework.util.CollectionUtils;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;

import lombok.Data;

/**
 * DTO.
 */
@Data
public class CommunauteCommuneDto {
	
	public CommunauteCommuneDto(CommunauteCommune model) {
		super();
		this.id=model.getId();
		this.name=model.getName();
		if (model.getCarreCarte()!=null) {
			Point p = model.getCarreCarte().getCentroid();
			this.lonX=p.getX();
			this.latY=p.getY();
			
		} else if (!CollectionUtils.isEmpty(model.getCities())) {
			
			// min and max with inverse of extremum
			double minX=180, maxX=-180, minY=90, maxY=-90;
			double curX, curY;
			
			for (City city : model.getCities()) {
				if (city.getCoordinate()!=null) {
					Point p = city.getCoordinate();
					curX = p.getX();
					curY = p.getY();
					
					if (curX>maxX) {
						maxX=curX;
					}
					
					if (curX<minX) {
						minX=curX;
					}

					if (curY>maxY) {
						maxY=curY;
					}
					if (curY<minY) {
						minY=curY;
					}
				}
			}
			
			if (minX!=180) {
				this.lonX = (maxX - minX)/2;
				this.latY = (maxY - minY)/2;
			}
		}
	}
	
	private Long id;
	private String name;
	private Double lonX;
	private Double latY;


}
