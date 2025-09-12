package com.github.cunvoas.geoserviceisochrone.service.export;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonFeature;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

import lombok.extern.slf4j.Slf4j;

/**
 * Service métier pour l'export statique des données (cadastre, isochrone).
 * Permet de générer des fichiers statiques pour l'archivage ou la publication.
 */
@Service
@Slf4j
public class StaticExport {

    @Autowired
    private CommunauteCommuneRepository communauteCommuneRepository;
    @Autowired
    private CadastreRepository cadastreRepository;
    
	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
    @Autowired
    private GeoMapServiceV2 geoMapService;

	private ObjectMapper mapper = new ObjectMapper();   
	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	
	
    /**
     * Exporte les données de cadastre pour chaque communauté de communes.
     *
     * @deprecated Utiliser ServicePublicationExporter à la place.
     * @see ServicePublicationExporter
     */
    @Deprecated
    public void exportCadastre() {
    	List<CommunauteCommune> com2cos = communauteCommuneRepository.findAll();
    			
    	for (CommunauteCommune com2co : com2cos) {
    		
    		StringBuilder sb = new StringBuilder();
			sb.append(applicationBusinessProperties.getExportPath());
			sb.append("/cadastre/cadastre_c2c_").append(String.valueOf(com2co.getId())).append(".json");
			
    		GeoJsonRoot geojson = geoMapService.findAllCadastreByComm2Co(com2co.getId());
    		try (FileOutputStream fos = new FileOutputStream(new File(sb.toString()))){
				mapper.writeValue(fos, geojson);
			} catch (Exception e) {
				log.error("exportCadastre", e);
			}
		}
    }


    /**
     * Exporte les isochrones pour chaque communauté de communes.
     *
     * @deprecated Utiliser ServicePublicationExporter à la place.
     * @see ServicePublicationExporter
     */
    @Deprecated
    public void exportIsochrone() {
    	List<CommunauteCommune> com2cos = communauteCommuneRepository.findAll();
    	for (CommunauteCommune com2co : com2cos) {
    		if (com2co.getId()!=1) {
    			continue;
    		}
    		
			List<String> ids = new ArrayList<>();
			for (City city : com2co.getCities()) {
				ids.add(city.getInseeCode());
			}
			
			List<Cadastre> cadastres = cadastreRepository.findAllById(ids);
			//FIXME
			Integer annee=2015;

			Set<GeoJsonFeature> featureSet = new HashSet<>();	
			GeoJsonRoot geojsonMerged = new GeoJsonRoot();
			for (Cadastre cadastre : cadastres) {
				
				
				MultiPolygon pl = (MultiPolygon)cadastre.getGeoShape();
				Coordinate[] coords = pl.getCoordinates();
				Polygon polygon = (Polygon)factory.createPolygon(coords).getEnvelope();
				
				GeoJsonRoot geojson = geoMapService.findAllParkByArea(polygon, annee);
				featureSet.addAll(geojson.getFeatures());
				
				StringBuilder sb = new StringBuilder();
				sb.append(applicationBusinessProperties.getExportPath());
				sb.append("/park/park_c2c_").append(String.valueOf(com2co.getId()));
				sb.append("_com_").append(cadastre.getIdInsee());
				sb.append("_").append(annee).append(".json");
				
				try (FileOutputStream fos = new FileOutputStream(new File(sb.toString()))) {
					mapper.writeValue(fos, geojson);
				} catch (Exception e) {
					log.error("exportCarre", e);
				}
			}
			geojsonMerged.getFeatures().addAll(featureSet);
			
			StringBuilder sb = new StringBuilder();
			sb.append(applicationBusinessProperties.getExportPath());
			sb.append("/park/park_c2c_").append(String.valueOf(com2co.getId()));
			sb.append("_").append(annee).append(".json");
			
			try (FileOutputStream fos = new FileOutputStream(new File(sb.toString()))) {
				mapper.writeValue(fos, geojsonMerged);
			} catch (Exception e) {
				log.error("exportCarre", e);
			}
    	}
    }
    
    /**
     * exportCarre.
     */
    public void exportCarre() {
    	List<CommunauteCommune> com2cos = communauteCommuneRepository.findAll();
    	for (CommunauteCommune com2co : com2cos) {
    		if (com2co.getId()!=1) {
    			continue;
    		}
    		
			List<String> ids = new ArrayList<>();
			for (City city : com2co.getCities()) {
				ids.add(city.getInseeCode());
			}
			
			List<Cadastre> cadastres = cadastreRepository.findAllById(ids);
			
			Integer[] annees = applicationBusinessProperties.getInseeAnnees();
			for (Integer annee : annees) {

				Set<GeoJsonFeature> featureSet = new HashSet<>();	
				GeoJsonRoot geojsonMerged = new GeoJsonRoot();
				
				for (Cadastre cadastre : cadastres) {
					
					MultiPolygon pl = (MultiPolygon)cadastre.getGeoShape();
					Coordinate[] coords = pl.getCoordinates();
					Polygon polygon = (Polygon)factory.createPolygon(coords).getEnvelope();
					
					GeoJsonRoot geojson = geoMapService.findAllCarreByArea(polygon, annee);
					featureSet.addAll(geojson.getFeatures());
					
					
					StringBuilder sb = new StringBuilder();
					sb.append(applicationBusinessProperties.getExportPath());
					sb.append("/carre200m/carre200m_c2c_").append(String.valueOf(com2co.getId()));
					sb.append("_com_").append(cadastre.getIdInsee());
					sb.append("_").append(annee).append(".json");
					
					try (FileOutputStream fos = new FileOutputStream(new File(sb.toString()))) {
						mapper.writeValue(fos, geojson);
					} catch (Exception e) {
						log.error("exportCarre", e);
					}
				}
				geojsonMerged.getFeatures().addAll(featureSet);
				
				StringBuilder sb = new StringBuilder();
				sb.append(applicationBusinessProperties.getExportPath());
				sb.append("/carre200m/carre200m_c2c_").append(String.valueOf(com2co.getId()));
				sb.append("_").append(annee).append(".json");
				
				try (FileOutputStream fos = new FileOutputStream(new File(sb.toString()))) {
					mapper.writeValue(fos, geojsonMerged);
				} catch (Exception e) {
					log.error("exportCarre", e);
				}
			}
    	}
    	
    }
    
    
}