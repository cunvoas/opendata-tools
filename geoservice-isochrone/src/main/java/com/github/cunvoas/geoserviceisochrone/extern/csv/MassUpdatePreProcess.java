package com.github.cunvoas.geoserviceisochrone.extern.csv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.extern.helper.UrlPointParser;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;

@Component
public class MassUpdatePreProcess {

	@Autowired
	private UrlPointParser gmapPointParser;
	
	/**
	sample file
	cityId;ParkId;nom;surface;nomE;coord
	2878;13;Square Dompsin;3367,99462890625;e1;50.64156,2.99015
	;;;;e2;50.64170,2.99042
	;;;;e3;50.64192,2.99048
	1140,34912109375;e1;50.64725,2.97930
	;;;;e2;50.64710,2.97995
	;;;;e3;50.64698,2.97916

	 * @param lines
	 * @return
	 */
	public List<CsvMassUpdatePivot> preProcess(List<CsvParkUpdate> lines) {
		List<CsvMassUpdatePivot> preprocessed = new ArrayList<>();
		
		String parkRupt="^begin$";
		CsvMassUpdatePivot pivot=null;
		
		for (CsvParkUpdate line : lines) {
			String sRupt = line.getParkId()+line.getNom();
			if (!StringUtils.isBlank(sRupt)) {
				if (!parkRupt.equals(sRupt)) {
					parkRupt = sRupt;
					pivot = new CsvMassUpdatePivot();
					preprocessed.add(pivot);
					
					if (!StringUtils.isBlank(line.getCityId())) {
						City c = new City();
						c.setId(Long.parseLong(line.getCityId()));
						pivot.setCommune(c);
					}
					
					ParcEtJardin pj = new ParcEtJardin();
					if (line.getParkId().matches("[0-9]+")) {
						pj.setId(Long.parseLong(line.getParkId()));
					}
					pj.setName(line.getNom());
					if (!StringUtils.isBlank(line.getSurface())) {
						pj.setSurface(Double.parseDouble(line.getSurface().replaceAll(",", ".")));
					}
					pivot.setParcEtJardin(pj);
				}
			}

			ParkEntrance pa = new ParkEntrance();
			pa.setDescription(line.getNomE());
			pa.setEntranceLink(line.getCoord());
			pa.setEntrancePoint(GeoShapeHelper.parsePointLatLng(line.getCoord()));
			
			pivot.getEntrances().add(pa);
		}
		
		return preprocessed;
	}
}
