package com.github.cunvoas.geoserviceisochrone.service.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.GeoJsonCadastreController;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoCoordinate;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.export.dto.CityDto;
import com.github.cunvoas.geoserviceisochrone.service.export.dto.CommunauteCommuneDto;
import com.github.cunvoas.geoserviceisochrone.service.export.dto.RegionDto;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

@Service
public class ServicePublicationExporter {
	@Value("${application.admin.export-data-path}")
	private String jsonFileFolder;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	public ServicePublicationExporter() {
		super();
		objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, DtoCoordinate.class);
		objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}
	
	@Autowired
	private ServiceReadReferences serviceReadReferences;
	
	@Autowired
	private GeoJsonCadastreController geoJsonCadastreController;
	
	@Autowired
	private GeoMapServiceV2 geoMapServiceV2;
	

	/**
	 * /data/carres
	 * @throws StreamWriteException
	 * @throws DatabindException
	 * @throws IOException
	 */
	public void writeCarreaux() throws StreamWriteException, DatabindException, IOException {
		File file = new File(jsonFileFolder+"/data/carres/c2c/");
		file.mkdirs();
		file = new File(jsonFileFolder+"/data/carres/city/");
		file.mkdirs();

		List<CommunauteCommune> lc2c = serviceReadReferences.getCommunauteCommune();
		for (CommunauteCommune c2c : lc2c) {
			GeoJsonRoot geoJson = geoMapServiceV2.findAllCadastreByComm2Co(c2c.getId());
			
			file = new File(jsonFileFolder+"/data/carres/c2c/carres_c2c_"+String.valueOf(c2c.getId())+".json");
			objectMapper.writeValue(file, geoJson);
		}
		
		// TODO get list city with all carre complete
//		List<City> lcity = null;
//		for (City city : lcity) {
//			GeoJsonRoot geoJson = geoMapServiceV2.findAllCadastreByComm2Co(c2c.getId());
//		}
		
	}
	
	public void writeCadastres() throws StreamWriteException, DatabindException, IOException {
	
		File file = new File(jsonFileFolder+"/data/cadastres");
		file.mkdirs();
		
		List<CommunauteCommune> lc2c = serviceReadReferences.getCommunauteCommune();
		for (CommunauteCommune c2c : lc2c) {
			GeoJsonRoot geoJson = geoJsonCadastreController.getCadastreByCom2Com(c2c.getId());
			
			String regId = String.valueOf(c2c.getRegion().getId());
			file = new File(jsonFileFolder+"/data/cadastres/"+regId+"/cadastre_c2c_"+String.valueOf(c2c.getId())+".json");
			objectMapper.writeValue(file, geoJson);
		}
		
	}
	
	public void writeRegions() throws StreamWriteException, DatabindException, IOException {
		
		File file = new File(jsonFileFolder+"/data/com2cos");
		file.mkdirs();
		file = new File(jsonFileFolder+"/data/cities");
		file.mkdirs();
		
		List<Region> regions = serviceReadReferences.getRegion();
		for (Region region : regions) {
			String regId = String.valueOf(region.getId());
			file = new File(jsonFileFolder+"/data/com2cos/"+regId);
			file.mkdirs();
			file = new File(jsonFileFolder+"/data/cadastres/"+regId);
			file.mkdirs();
			file = new File(jsonFileFolder+"/data/carres/"+regId);
			file.mkdirs();
		}
		
		
		file = new File(jsonFileFolder+"/data/regions.json");
		
		
		
		List<RegionDto> regionsSer=new ArrayList<>();
		for (Region region : regions) {
			RegionDto regionSer=new RegionDto(region);
			regionsSer.add(regionSer);
		}
		objectMapper.writeValue(file, regionsSer);
		
		for (Region region : regions) {
			List<CommunauteCommune> lc2c = serviceReadReferences.getCommunauteByRegionId(region.getId());
			if (!lc2c.isEmpty()) {
				String regId = String.valueOf(region.getId());
				file = new File(jsonFileFolder+"/data/com2cos/"+regId+"/com2cos_"+region.getId()+".json");
				List<CommunauteCommuneDto> lc2cSer=new ArrayList<>();
				for (CommunauteCommune c2c : lc2c) {
					CommunauteCommuneDto c2cSer=new CommunauteCommuneDto(c2c);
					lc2cSer.add(c2cSer);
				}
				objectMapper.writeValue(file, lc2cSer);
				
				for (CommunauteCommune c2c : lc2c) {
					List<City> lc = serviceReadReferences.getCityByCommunauteCommuneId(c2c.getId());
					file = new File(jsonFileFolder+"/data/cities/cities_"+c2c.getId()+".json");
					
					List<CityDto> lcSer=new ArrayList<>();
					for (City c : lc) {
						CityDto cSer = new CityDto(c);
						lcSer.add(cSer);
					}
					objectMapper.writeValue(file, lcSer);
				}
			}
		}
	}

}
