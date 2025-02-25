package com.github.cunvoas.geoserviceisochrone.service.export;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.export.dto.ParkExportLine;

@Service
public class ServiceVerificationExporter {
	
	@Autowired
	private ParkJardinRepository parkJardinRepository;

	@Autowired
	private ParkAreaRepository parkAreaRepository;
	
	public List<ParkExportLine> export4verif(CommunauteCommune com2co) {
		List<ParkExportLine> ret = new ArrayList<>();
		
		for (City city : com2co.getCities()) {
			ret.addAll(export4verif(city));
		}
		return ret;
	}
	
	public List<ParkExportLine> export4verif(City city) {
		List<ParkExportLine> ret = new ArrayList<>();
		
		List<ParcEtJardin> pjs = parkJardinRepository.findByCityId(city.getId());
		for (ParcEtJardin parcEtJardin : pjs) {
			ret.add(map(city, parcEtJardin));
		}
		return ret;
	}
	
	private ParkExportLine map(City city, ParcEtJardin parcEtJardin) {
		ParkExportLine prl = new ParkExportLine();
		prl.setIdRegion(city.getRegion().getId());
		prl.setIdCom2Co(city.getCommunauteCommune().getId());
		
		prl.setIdCommune(city.getId());
		prl.setCommune(city.getName());
		
		prl.setIdPark(parcEtJardin.getId());
		prl.setParkName(parcEtJardin.getName());
		
		prl.setIdTypePark(parcEtJardin.getId());
		prl.setOmsCustom(parcEtJardin.getOmsCustom());
		
		prl.setSurfaceOpendata(parcEtJardin.getSurface());
		prl.setSurfaceContour(parcEtJardin.getSurfaceContour());
		
		ParkArea pa = parkAreaRepository.findByIdParcEtJardin(parcEtJardin.getId());
		if (pa!=null) {
			prl.setIdParkArea(pa.getId());
			if (pa.getEntrances()!=null) {
				prl.setNbParkEntrance(pa.getEntrances().size());
			}
			if (Boolean.FALSE.equals(pa.getToCompute())) {
				prl.setParkAreaComputedDate(pa.getUpdated());
			}
		}
		
		return prl;
	}

}
