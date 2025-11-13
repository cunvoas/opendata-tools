package com.github.cunvoas.geoserviceisochrone.service.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.GeoJsonCadastreController;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoCoordinate;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.export.dto.CityDto;
import com.github.cunvoas.geoserviceisochrone.service.export.dto.CommunauteCommuneDto;
import com.github.cunvoas.geoserviceisochrone.service.export.dto.RegionDto;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

import lombok.extern.slf4j.Slf4j;

/**
 * Service métier pour l'exportation des données de publication (GeoJSON, IRIS, etc.).
 * Permet de générer et d'écrire les fichiers nécessaires à la publication des données géographiques.
 */
@Service
@Slf4j
public class ServicePublicationExporter {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	private ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * Constructeur du service d'exportation de publication.
	 * Initialise la configuration de l'ObjectMapper pour la sérialisation/désérialisation JSON.
	 */
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
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	@Autowired
	private ServiceReadReferences serviceReadReferences;
	
    @Autowired
    private CadastreRepository cadastreRepository;
    
	@Autowired
	private GeoJsonCadastreController geoJsonCadastreController;
	
	@Autowired
	private GeoMapServiceV2 geoMapServiceV2;
	


	/**
	 * Écrit les fichiers GeoJSON pour les IRIS INSEE de chaque communauté de communes et chaque année configurée.
	 * Crée les dossiers nécessaires et génère les fichiers à l'emplacement défini dans la configuration.
	 *
	 * @throws StreamWriteException en cas d'erreur d'écriture du flux
	 * @throws DatabindException en cas d'erreur de mapping des données
	 * @throws IOException en cas d'erreur d'entrée/sortie
	 */
	public void writeGeoJsonIris() throws StreamWriteException, DatabindException, IOException {

		Integer[] tAnnees = applicationBusinessProperties.getInseeAnnees();
		
		List<CommunauteCommune> lc2c = serviceReadReferences.getCommunauteCommune();
		for (CommunauteCommune com2co : lc2c) {

			for (int i = 0; i < tAnnees.length; i++) {
				Integer annee=tAnnees[i];
				this.writeGeoJsonIris(com2co, annee);
			}
		}
	}
	
	public void writeGeoJsonIris(CommunauteCommune com2co, Integer annee) throws StreamWriteException, DatabindException, IOException {
		
		String path = applicationBusinessProperties.getJsonFileFolder()+"/geojson/iris/"+String.valueOf(com2co.getId());
		File file = new File(path);
		file.mkdirs();
		
		// check if park data are changed !
		// TODO
		
		GeoJsonRoot geojson = geoMapServiceV2.findAllIrisByCommunauteCommune(com2co, annee);
		if (geojson!=null && !geojson.getFeatures().isEmpty()) {
			file = new File(path+"/iris_"+String.valueOf(annee)+"_"+String.valueOf(com2co.getId())+".json");
			objectMapper.writeValue(file, geojson);
		}
	}
	
	/**
	 * Write INSEE files.
	 * @param com2co Communaute de Commune
	 * @param annee annee
	 * @throws StreamWriteException
	 * @throws DatabindException
	 * @throws IOException
	 */
	public void writeGeoJsonCarreaux(CommunauteCommune com2co, Integer annee) throws StreamWriteException, DatabindException, IOException {
		
		String path = applicationBusinessProperties.getJsonFileFolder()+"/geojson/carres/"+String.valueOf(com2co.getId());
		File file = new File(path);
		file.mkdirs();
		
		// check if park data are changed !
		// TODO
		
		GeoJsonRoot geojson = geoMapServiceV2.findAllCarreByCommunauteCommune(com2co, annee);
		if (geojson!=null && !geojson.getFeatures().isEmpty()) {
			file = new File(path+"/carre_"+String.valueOf(annee)+"_"+String.valueOf(com2co.getId())+".json");
			objectMapper.writeValue(file, geojson);
		}
	}
	
	
	/**
	 * Write INSEE files.
	 * @throws StreamWriteException ex
	 * @throws DatabindException ex
	 * @throws IOException ex
	 */
	public void writeGeoJsonCarreauxAll() throws StreamWriteException, DatabindException, IOException {
		File file = new File(applicationBusinessProperties.getJsonFileFolder()+"/geojson/carres");
		file.mkdirs();

		Integer[] tAnnees = applicationBusinessProperties.getInseeAnnees();
		
		List<CommunauteCommune> lc2c = serviceReadReferences.getCommunauteCommune();
		for (CommunauteCommune com2co : lc2c) {

			for (int i = 0; i < tAnnees.length; i++) {
				Integer annee=tAnnees[i];
				this.writeGeoJsonCarreaux(com2co, annee);
			}	
		}
	}
	
	/**
	 * Generate a square that contains the global shape of CommunauteCommune.
	 * fast select but slower after because too many item after.
	 * efficient for isochrones
	 * @param com2co CommunauteCommune
	 * @return Polygon
	 */
	Polygon getCom2CoSquareShape(CommunauteCommune com2co) {

		// get all insee code of CommunauteCommune
		List<String> ids = new ArrayList<>();
		for (City city : com2co.getCities()) {
			ids.add(city.getInseeCode());
		}

		// union all polys of Cadastre
		List<Cadastre> cadastres = cadastreRepository.findAllById(ids);
		
		// inverse extremum
		double minX=180;
		double maxX=-180;
		double minY=90;
		double maxY=-90;
		

		// extract envelope
		for (Cadastre cadastre : cadastres) {
			Geometry envel = cadastre.getGeoShape().getEnvelope();
			Coordinate[] coords = envel.getCoordinates();
			for (int i = 0; i < coords.length; i++) {
				Coordinate c = coords[i];
				
				if (c.getX()>maxX) {
					maxX = c.getX();
				}
				if (c.getX()<minX) {
					minX = c.getX();
				}
				if (c.getY()>maxY) {
					maxY = c.getY();
				}
				if (c.getY()<minY) {
					minY = c.getY();
				}
			}
			
		}
		
		List<Coordinate> lCoords = new ArrayList<>();
		lCoords.add( new Coordinate(minX,minY) );
		lCoords.add( new Coordinate(minX,maxY) );
		lCoords.add( new Coordinate(maxX,maxY) );
		lCoords.add( new Coordinate(maxX,minY) );
		lCoords.add( new Coordinate(minX,minY) );
    	
//    	Coordinate[] coords = lCoords.toArray(Coordinate[]::new);
    	Coordinate[] coords = lCoords.toArray(new Coordinate[0]);
    	
		
		return (Polygon)factory.createPolygon(coords).getEnvelope();
	}
	

    /**
	 * Write  ParkOutline files.
     * @throws StreamWriteException ex
     * @throws DatabindException ex
     * @throws IOException ex
     */
    public void writeGeoJsonParkOutline() throws StreamWriteException, DatabindException, IOException {

		Integer[] tAnnees = applicationBusinessProperties.getInseeAnnees();
		
    	List<CommunauteCommune> com2cos = serviceReadReferences.getCommunauteCommune();
    	for (CommunauteCommune com2co : com2cos) {
			
			for (int i = 0; i < tAnnees.length; i++) {
				Integer annee=tAnnees[i];
				this.writeGeoJsonParkOutline(com2co, annee);
			}
    	}
    }
    
    public void writeGeoJsonParkOutline(CommunauteCommune com2co, Integer annee) throws StreamWriteException, DatabindException, IOException {
    	String path = applicationBusinessProperties.getJsonFileFolder()+"/geojson/parkOutline/"+String.valueOf(com2co.getId());
		File file = new File(path);
		file.mkdirs();
		
		Polygon polygon = getCom2CoSquareShape(com2co);
    	
		GeoJsonRoot geojson = geoMapServiceV2.findAllParkOutlineByArea(polygon, annee);
		
		if (!geojson.getFeatures().isEmpty()) {
			file = new File(path+"/parkOutline_"+String.valueOf(annee)+"_"+String.valueOf(com2co.getId())+".json");
			objectMapper.writeValue(file, geojson);
		}
    }
    
    /**
	 * Write Isochrone files.
     * @throws StreamWriteException ex
     * @throws DatabindException ex
     * @throws IOException ex
     */
    public void writeGeoJsonIsochrone() throws StreamWriteException, DatabindException, IOException {

		Integer[] tAnnees = applicationBusinessProperties.getInseeAnnees();
		
    	List<CommunauteCommune> com2cos = serviceReadReferences.getCommunauteCommune();
    	for (CommunauteCommune com2co : com2cos) {

			for (int i = 0; i < tAnnees.length; i++) {
				Integer annee=tAnnees[i];
				this.writeGeoJsonIsochrone(com2co, annee);

			}
    	}
    }
    
    public void writeGeoJsonIsochrone(CommunauteCommune com2co, Integer annee) throws StreamWriteException, DatabindException, IOException {
		String path = applicationBusinessProperties.getJsonFileFolder()+"/geojson/isochrones/"+String.valueOf(com2co.getId());
		File file = new File(path);
		file.mkdirs();
		
		Polygon polygon = getCom2CoSquareShape(com2co);
		
		GeoJsonRoot geojson = geoMapServiceV2.findAllParkByArea(polygon, annee);
		
		if (!geojson.getFeatures().isEmpty()) {
			file = new File(path+"/isochrone_"+String.valueOf(annee)+"_"+String.valueOf(com2co.getId())+".json");
			objectMapper.writeValue(file, geojson);
		}
    }
	
	/**
	 * Write Cadastre files.
	 * @throws StreamWriteException ex
	 * @throws DatabindException ex
	 * @throws IOException ex
	 */
	public void writeGeoJsonCadastres() throws StreamWriteException, DatabindException, IOException {
	
		File file = new File(applicationBusinessProperties.getJsonFileFolder()+"/data/cadastres");
		file.mkdirs();
		
		List<CommunauteCommune> lc2c = serviceReadReferences.getCommunauteCommune();
		for (CommunauteCommune c2c : lc2c) {
			GeoJsonRoot geoJson = geoJsonCadastreController.getCadastreByCom2Com(c2c.getId());
			
			String regId = String.valueOf(c2c.getRegion().getId());
			file = new File(applicationBusinessProperties.getJsonFileFolder()+"/data/cadastres/"+regId);
			file.mkdirs();
			
			file = new File(applicationBusinessProperties.getJsonFileFolder()+"/data/cadastres/"+regId+"/cadastre_c2c_"+String.valueOf(c2c.getId())+".json");
			objectMapper.writeValue(file, geoJson);
		}
	}
	
	/**
	 * Write region files.
	 * @throws StreamWriteException ex
	 * @throws DatabindException ex
	 * @throws IOException ex
	 */
	public void writeRegions() throws StreamWriteException, DatabindException, IOException {
		
		File file = new File(applicationBusinessProperties.getJsonFileFolder()+"/data/com2cos");
		file.mkdirs();
		file = new File(applicationBusinessProperties.getJsonFileFolder()+"/data/cities/com2co/");
		file.mkdirs();
		file = new File(applicationBusinessProperties.getJsonFileFolder()+"/data/cities/dept/");
		file.mkdirs();
		
		List<Region> regions = serviceReadReferences.getRegion();
		for (Region region : regions) {
			String regId = String.valueOf(region.getId());
			file = new File(applicationBusinessProperties.getJsonFileFolder()+"/data/com2cos/"+regId);
			file.mkdirs();
			file = new File(applicationBusinessProperties.getJsonFileFolder()+"/data/cadastres/"+regId);
			file.mkdirs();
		}
		file = new File(applicationBusinessProperties.getJsonFileFolder()+"/data/regions.json");
		
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
				file = new File(applicationBusinessProperties.getJsonFileFolder()+"/data/com2cos/"+regId+"/com2cos_"+region.getId()+".json");
				List<CommunauteCommuneDto> lc2cSer=new ArrayList<>();
				for (CommunauteCommune c2c : lc2c) {
					CommunauteCommuneDto c2cSer=new CommunauteCommuneDto(c2c);
					lc2cSer.add(c2cSer);
				}
				objectMapper.writeValue(file, lc2cSer);
				
				for (CommunauteCommune c2c : lc2c) {
					List<City> lc = serviceReadReferences.getCityByCommunauteCommuneId(c2c.getId());
					file = new File(applicationBusinessProperties.getJsonFileFolder()+"/data/cities/com2co/cities_"+c2c.getId()+".json");
					
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