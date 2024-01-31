package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoCoordinate;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoIsoChrone;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;

@Component
public class MapperIsoChrone {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	public ParkEntrance map(ParkEntrance model, DtoIsoChrone dto)  {
		if (dto!=null) {
			if (model==null) {
				model = new ParkEntrance();
			}
			
			model.setUpdateDate(new Date());
			try {
				model.setIgnDate(df.parse(dto.getResourceVersion()));
			} catch (ParseException ignore) {
				model.setIgnDate(new Date());
			}
			
			
			String[] coords = dto.getPoint().split(",");
			Double lat = Double.valueOf(coords[0]);
			Double lon = Double.valueOf(coords[1]);
			
			Point point = factory.createPoint(new Coordinate(lat, lon));
			model.setEntrancePoint(point);
			
			List<Coordinate> shape = map(dto.getGeometry().getCoordinates());
			Polygon polygon = factory.createPolygon(shape.toArray(Coordinate[]::new));
			model.setPolygon(polygon);
		}
		
		
		return model;
		
	}
	
	private List<Coordinate> map(List<DtoCoordinate> coordinate ) {
		List<Coordinate> shape = new ArrayList<>();
		
		for (DtoCoordinate coord : coordinate) {
			shape.add(new Coordinate(coord.lat, coord.lon));
		}
		
		return shape;
	}
}
