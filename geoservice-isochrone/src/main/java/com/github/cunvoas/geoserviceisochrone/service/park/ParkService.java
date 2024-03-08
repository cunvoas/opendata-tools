package com.github.cunvoas.geoserviceisochrone.service.park;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.exception.ExceptionExtract;
import com.github.cunvoas.geoserviceisochrone.extern.csv.CsvParkEntranceParser;
import com.github.cunvoas.geoserviceisochrone.extern.csv.CsvParkLine;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.extern.helper.UrlPointParser;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.MapperIsoChrone;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.DtoIsoChroneParser;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.IsoChroneClientService;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoIsoChrone;
import com.github.cunvoas.geoserviceisochrone.extern.mel.CsvMelParkJardinParser;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Log
public class ParkService {
	
	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

	@Autowired
	private ServiceOpenData serviceOpenData;
	
    

	@Autowired
	private CsvParkEntranceParser csvParkEntranceParser;
	@Autowired
	private ParkEntranceRepository parkEntranceRepository;
	@Autowired
	private UrlPointParser gmapPointParser;
	
	@Autowired
	private ParkAreaRepository parkAreaRepository;
	@Autowired
	private CityRepository cityRepository;
	
	@Autowired
	private CsvMelParkJardinParser csvMelParkJardinParser;
	@Autowired 
	private ParkJardinRepository parkJardinRepository;
	
	@Autowired
	private IsoChroneClientService clientIsoChrone;
	@Autowired
	private DtoIsoChroneParser dtoIsoChroneParser;	
	@Autowired
	private MapperIsoChrone mapperIsoChrone;
	
	
	
	public ParkArea getParkAreaById(Long id) {
		Optional<ParkArea> opt=parkAreaRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}
	public ParkArea getParkAreaByIdParcEtJardin(Long id) {
		return parkAreaRepository.findByIdParcEtJardin(id);
	}
	
	public ParcEtJardin getParcEtJardinById(Long id) {
		Optional<ParcEtJardin> opt=parkJardinRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}
	
	/**
	 * Save entrance from UI.
	 * @param parkEntrance
	 * @param withIgn
	 * @return
	 * @deprecated
	 */
	public ParkEntrance saveEdited(ParkEntrance parkEntrance, String distance, boolean withIgn) {
		if (withIgn) {
			try {
				Coordinate coord = new Coordinate(
						parkEntrance.getEntrancePoint().getX(),
						parkEntrance.getEntrancePoint().getY());
				
				// 300 en zone dense, 1200 sinon
				String ignResp = clientIsoChrone.getIsoChrone(coord, distance);
				DtoIsoChrone dtoIsoChone = dtoIsoChroneParser.parseBasicIsoChrone(ignResp);
				parkEntrance = mapperIsoChrone.map(parkEntrance, dtoIsoChone);
				
			} catch (Exception e) {
				throw new ExceptionExtract("IGN_UPDATE");
			}
		}
		
		//TODO presave parkArea  //ERREUR ICI
		ParkArea pa = parkAreaRepository.save(parkEntrance.getParkArea());
		parkEntrance.setParkArea(pa);
		
		parkEntrance = parkEntranceRepository.save(parkEntrance);
		return parkEntrance;
	}
	
	/**
	 * @param parkEntrance
	 * @param withIgn true if isochrone request required
	 * @param cityId city to get density of the city
	 * @return
	 */
	public ParkEntrance saveEdited(ParkEntrance parkEntrance,boolean withIgn, Long gardenId, Long cityId){
		
		//check area
		if (parkEntrance.getParkArea()==null || parkEntrance.getParkArea().getId()==null) {
			
			
			Optional<ParcEtJardin> opt = parkJardinRepository.findById(gardenId);
			if (opt.isPresent()) {
				ParcEtJardin parcEtJardin = opt.get();
			
				ParkArea parkArea = new ParkArea();
				parkArea.setName(parcEtJardin.getName());
				parkArea.setIdParcEtJardin(parcEtJardin.getId());
				parkArea.setBlock(parcEtJardin.getQuartier());
				parkArea = parkAreaRepository.save(parkArea);
				parkEntrance.setParkArea(parkArea);
			
			} else {
				throw new ExceptionAdmin("ERR_AREA_NOT_EXITS");
			}
		} 
		

		if (withIgn) {
			String distance = "300";
			Optional<City> opt=cityRepository.findById(cityId);
			if (opt.isPresent()) {
				City city = opt.get();
				distance = serviceOpenData.getDistanceDense(city);
			}
			
			try {
				Coordinate coord = new Coordinate(
						parkEntrance.getEntrancePoint().getX(),
						parkEntrance.getEntrancePoint().getY());
				
				// 300 en zone dense, 1200 sinon
				String ignResp = clientIsoChrone.getIsoChrone(coord, distance);
				parkEntrance.setIgnReponse(ignResp);
				
				DtoIsoChrone dtoIsoChone = dtoIsoChroneParser.parseBasicIsoChrone(ignResp);
				parkEntrance = mapperIsoChrone.map(parkEntrance, dtoIsoChone);
				
			} catch (Exception e) {
				throw new ExceptionExtract("IGN_UPDATE");
			}
		}
		parkEntrance = parkEntranceRepository.save(parkEntrance);
		return parkEntrance;
	}
	
	/**
	 * Import CSV file and generate iso-chrone.
	 * @param file
	 * @throws IOException
	 * @see https://postgis.net/docs/reference.html#operators-distance
	 */
	public void importIsoChroneEntrance(File file) throws IOException {
		List<CsvParkLine> csvLines = csvParkEntranceParser.parseParkEntrance(file);
		
		for (CsvParkLine csvParkLine : csvLines) {
			
			// get opendata
			ParcEtJardin parcEtJardin = parkJardinRepository.findByName(csvParkLine.getPark());
			
			// already processed?
			ParkArea parkArea = parkAreaRepository.findByName(csvParkLine.getPark());
			if (parkArea==null) {
				parkArea = new ParkArea();
				parkArea.setName(csvParkLine.getPark());
			}
			parkArea.setIdParcEtJardin(parcEtJardin.getId());
			parkArea.setBlock(parcEtJardin.getQuartier());
			parkArea = parkAreaRepository.save(parkArea);
			
			log.info("PROCESS : {}", csvParkLine.toString());
			Coordinate coordinate = gmapPointParser.parse(	csvParkLine.getUrl() );

			// map geometry fields
			ParkEntrance entrance=parkEntranceRepository.findByParkAreaAndDescription(parkArea, csvParkLine.getEntrance());
			try {
				boolean toProcess=false;
						
				if (entrance!=null && !StringUtils.endsWithIgnoreCase(csvParkLine.getUrl(), entrance.getEntranceLink())) {
					toProcess=true;
				} else if (entrance==null || entrance.getIgnReponse()==null) {
					toProcess=true;
				}

				if (toProcess) {
					String isoChroneResponse = clientIsoChrone.getIsoChrone(coordinate, "300");
					DtoIsoChrone dtoIsoChone = dtoIsoChroneParser.parseBasicIsoChrone(isoChroneResponse);
				
					entrance = mapperIsoChrone.map(entrance, dtoIsoChone);
					entrance.setIgnReponse(isoChroneResponse);
					entrance.setEntranceLink(csvParkLine.getUrl());
					

					entrance.setParkArea(parkArea);
					entrance.setDescription(csvParkLine.getEntrance());
					
					parkEntranceRepository.save(entrance);
					
					parkArea.setPolygon(null);
					parkArea = parkAreaRepository.save(parkArea);
				}
				
			} catch (Exception e) {
				log.error("ERROR ON PROCESS : {}\n{}", csvParkLine.toString(), e.getMessage());
			}
			
			
		}
	}
	
	
	public void importOpenDataMelParcJardin(File csv) throws IOException {
		List<ParcEtJardin> parks = csvMelParkJardinParser.parseCsv(csv);
		for (ParcEtJardin parcEtJardin : parks) {
			parkJardinRepository.save(parcEtJardin);
			
		}
	}	
		
	
	private void mergeEntranceAreas(ParkArea parkArea) {
		
//		Point p = parkArea.getPoint();
		
		List<ParkEntrance> entances = parkEntranceRepository.findByParkArea(parkArea);
		log.info("merge {}", parkArea.getName());
		
		Polygon merged=null;
		for (ParkEntrance entance : entances) {
			log.info("\tprocess {}", entance.getParkArea().getName());
			Polygon p =entance.getPolygon();
			
			if (merged==null) {
				// clone the fist one
				merged = factory.createPolygon(p.getCoordinates());
				
			} else {
				//process merge
				merged = GeoShapeHelper.mergePolygonsWithoutHoles(merged, p);
				
//				Geometry geomMerged = merged.union(p);
//				
//				if (geomMerged instanceof Polygon) {
//					merged = (Polygon) geomMerged;
//				} else {
//					log.error("Entrances not mergeable {}{}", parkArea.getName(), entance.getDescription());
//				}
			}
		}
		parkArea.setPolygon(merged);
		parkArea.setUpdated(new Date());
		log.info("\tMerged is {}", merged);
		
	}
	
	
	public ParkArea mergeParkAreaEntrance(ParkArea parkArea) {
		this.mergeEntranceAreas(parkArea);
		return parkAreaRepository.save(parkArea);
	}
	
	public void mergeUpdatedEntranceAreas() {
		List<ParkArea> areas = parkAreaRepository.polygonToUpdate();
		for (ParkArea parkArea : areas) {
			this.mergeEntranceAreas(parkArea);
		}
		parkAreaRepository.saveAll(areas);
	}
	
	/**
	 * merge all entrances for one park to compute park isochrone.
	 */
	public void mergeNullEntranceAreas() {
		List<ParkArea> areas = parkAreaRepository.polygonNull();
		for (ParkArea parkArea : areas) {
			this.mergeEntranceAreas(parkArea);
		}
		parkAreaRepository.saveAll(areas);
	}
	

}
