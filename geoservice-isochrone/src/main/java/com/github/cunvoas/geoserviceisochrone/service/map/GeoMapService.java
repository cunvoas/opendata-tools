package com.github.cunvoas.geoserviceisochrone.service.map;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.CadastreView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.Carre200AndShapeView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.IsochroneView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.ParkView;
import com.github.cunvoas.geoserviceisochrone.extern.leaflet.Bound;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonFeature;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mShape;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mShapeRepository;
import com.google.common.math.BigDecimalMath;

import io.micrometer.common.util.StringUtils;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GeoMapService {
	
	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired
    private InseeCarre200mRepository inseeCarre200mRepository;
    @Autowired
    private InseeCarre200mComputedRepository inseeCarre200mComputedRepository;
    @Autowired
    private InseeCarre200mShapeRepository inseeCarre200mShapeRepository;
    @Autowired
    private ParkAreaRepository parkAreaRepository;
    @Autowired
    private ParkEntranceRepository parkEntranceRepository;
    @Autowired
    private ParkAreaComputedRepository parkAreaComputedRepository;
    @Autowired
    private CadastreRepository cadastreRepository;
	
    @Value("${application.business.oms.urban.area_min}")
    private Double minUrbSquareMeterPerCapita;
    
    @Value("${application.business.oms.urban.area}")
    private Double recoUrbSquareMeterPerCapita;

    @Value("${application.business.oms.suburban.area_min}")
    private Double minSubUrbSquareMeterPerCapita;
    
    @Value("${application.business.oms.suburban.area}")
    private Double recoSubUrbSquareMeterPerCapita;

    
    /**
     * @param swLat
     * @param swLng
     * @param neLat
     * @param neLng
     * @return
     */
    public GeoJsonRoot findAllCadastreByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
		GeoJsonRoot root = new GeoJsonRoot();
    	Polygon polygon = this.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	if (polygon!=null) {
    		List<Cadastre> cadastres = cadastreRepository.findCadastreInMapArea(GeometryQueryHelper.toText(polygon));
    		if (cadastres!=null && cadastres.size()>0) {
    			for (Cadastre cadastre : cadastres) {
    			
	    			GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					feature.setGeometry(cadastre.getGeoShape());
					
					CadastreView cv = new CadastreView();
					cv.setIdInsee(cadastre.getIdInsee());
					cv.setNom(cadastre.getNom());
					feature.setProperties(cv);
				}
    		}
    		
    	}
    	return root;
    }
    
    
    /**
     * @param swLat
     * @param swLng
     * @param neLat
     * @param neLng
     * @return
     */
    public GeoJsonRoot findAllParkByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = this.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findAllParkByArea(polygon);
    }
    
    /**
     * shuffle color of polygons.
     * @param index
     * @return
     */
    public String getColor(int index) {
    	int start=240;
    	int rawOffest=64;
    	
    	int red=start;
    	int green=start;
    	int blue=start;
    	
    	for (int i = 0; i <= index; i++) {
			if ((index  ) % 3==0) red -= rawOffest;
			if ((index+1) % 3==0) green -= rawOffest;
			if ((index+2) % 3==0) blue -= rawOffest;
		}
		if (red<0) red+=255;
		if (green<0) green+=255;
		if (blue<0) blue+=255;
		String color = String.format("#%02x%02x%02x", red, green, blue);
		log.info(color);
		return color;
    }
    
    
    
    /**
     * isochrone of each entrance
     * @param idPark
     * @return
     */
    public GeoJsonRoot findIsochroneParkEntrance(Long idPark) {
		GeoJsonRoot root = new GeoJsonRoot();
		List<ParkEntrance> entrances = parkEntranceRepository.findByParkId(idPark);
		if (!CollectionUtils.isEmpty(entrances)) {
			int idx=0;
			for (ParkEntrance parkEntrance : entrances) {
				GeoJsonFeature feature = new GeoJsonFeature();
				root.getFeatures().add(feature);
				feature.setGeometry(parkEntrance.getPolygon());
				
				IsochroneView view = new IsochroneView();
				view.setId(String.valueOf(parkEntrance.getId()));
				view.setName(parkEntrance.getDescription());
				view.setFillColor(getColor(idx));
				feature.setProperties(view);
				
				idx++;
			}
		}
    	return root;
    }
    public GeoJsonRoot findIsochronePark(Long idPark) {
		GeoJsonRoot root = new GeoJsonRoot();
		ParkArea pa = parkAreaRepository.findByIdParcEtJardin(idPark);
		
		if (pa!=null) {
			GeoJsonFeature feature = new GeoJsonFeature();
			root.getFeatures().add(feature);
			feature.setGeometry(pa.getPolygon());
			
			IsochroneView view = new IsochroneView();
			view.setId(String.valueOf(pa.getId()));
			view.setName(pa.getName());
			feature.setProperties(view);
				
		}
    	return root;
    	
    }
    
    
    
	/**
	 * @param polygon
	 * @return
	 */
	public GeoJsonRoot findAllParkByArea(Polygon polygon) {
		GeoJsonRoot root = new GeoJsonRoot();
		

    	if (polygon!=null) {
			List<ParkArea> parkAreas =  parkAreaRepository.findParkInMapArea(GeometryQueryHelper.toText(polygon));
			if (!CollectionUtils.isEmpty(parkAreas)) {
				for (ParkArea parkArea : parkAreas) {
					
					
					GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					feature.setGeometry(parkArea.getPolygon());
					
					ParkView pv = new ParkView();
					pv.setId(String.valueOf(parkArea.getId()));
					pv.setName(parkArea.getName());
					pv.setQuartier(parkArea.getBlock());
					

					Optional<ParkAreaComputed> cpu = parkAreaComputedRepository.findById(parkArea.getId());
					if (cpu.isPresent()) {
						extraFeature(pv, cpu.get());
					}
					
					feature.setProperties(pv);
					
				}
			}
    	}
		return root;
	}
	
	
	/**
	 * @param pv
	 * @param cpu
	 */
	public void extraFeature(ParkView pv, ParkAreaComputed cpu) {
		double hightThreshold = recoUrbSquareMeterPerCapita;
		double minThreshold = minUrbSquareMeterPerCapita;
		double lessMidThreshold = minUrbSquareMeterPerCapita*0.8d;
		
		
		NumberFormat nf = new DecimalFormat("# ##0");
		if (cpu!=null) {
			pv.setPeople(nf.format(cpu.getPopulation()));
			pv.setArea(nf.format(cpu.getSurface()));
			
			pv.setOms(cpu.getOms());
			
			if (!cpu.getOms()) {
				pv.setFillColor("#2B100D");
				
			} else {
				if (cpu.getSurfacePerInhabitant()!=null) {
					pv.setAreaPerPeople(cpu.getSurfacePerInhabitant().toPlainString());
					
					Double sph = BigDecimalMath.roundToDouble(cpu.getSurfacePerInhabitant(), RoundingMode.HALF_EVEN);
					
					if (sph>hightThreshold) {
						// vert "parc"
						pv.setFillColor("#58D83E");
					} else if(hightThreshold>=sph && sph>minThreshold) {
						// < recomendation && > minimum
						pv.setFillColor("#D8C13E");
						
					} else if(minThreshold>=sph && sph>lessMidThreshold) {
						// < minimum && > 80% minimum
						pv.setFillColor("#D8783E");
						
					} else {
						// <80M mini
						pv.setFillColor("#D84E3E");
					}
					
				} else {
					pv.setAreaPerPeople("-");
					
					pv.setFillColor("#D84E3E");
				}
				
			}
		}
	}

	BigDecimal fromDouble(Double d) {
		NumberFormat formatter = new DecimalFormat("#0");     
		return new BigDecimal(formatter.format(d));
	}
	
	 
    /**
     * GET ALL IRIS in the map.
     * @param polygon
     * @return
     */
    public GeoJsonRoot findAllCarreByArea(Polygon polygon) {
    	GeoJsonRoot root = new GeoJsonRoot();
    	
    	if (polygon!=null) {
    	List<InseeCarre200m> carreShape = inseeCarre200mRepository.getAllCarreInMap(GeometryQueryHelper.toText(polygon));
    	
    	if (carreShape!=null && carreShape.size()>0) {
    		for (InseeCarre200m c : carreShape) {
    			InseeCarre200mShape s= inseeCarre200mShapeRepository.findByIdInspire(c.getIdInspire());
				
    			
    			Optional<InseeCarre200mComputed> optCputed = inseeCarre200mComputedRepository.findById(c.getId());
    		
    			GeoJsonFeature feature = new GeoJsonFeature();
				root.getFeatures().add(feature);
				feature.setGeometry(s.getGeoShape());

    			Carre200AndShapeView v = new Carre200AndShapeView();
				feature.setProperties(v);
    			v.setId(c.getId());
    			v.setIdInspire(c.getIdInspire());
    			
    			v.setPeople(formatPopulation(c.getPopulation()));
    			
    			if (optCputed.isPresent()) {
    				InseeCarre200mComputed cputed=optCputed.get();
    				v.setPopParkExcluded(String.valueOf(cputed.getPopExcluded()));
    				v.setPopParkIncluded(String.valueOf(cputed.getPopIncluded()));
    				
    				if(BigDecimal.ZERO.compareTo(cputed.getPopAll())==0) {
    					v.setFillColor("#7F00FF");
    				} else {
	    				// ratio of people able to access a park
	    				BigDecimal ratio = cputed.getPopIncluded().divide(cputed.getPopAll(), RoundingMode.FLOOR);
	    				
	    				BigDecimal p98 = new BigDecimal(0.98);
	    				BigDecimal p80 = new BigDecimal(0.80);
	    				BigDecimal p20 = new BigDecimal(0.20);
	    				
	    				if (ratio.compareTo(p20)<0) { // < 25%
	    					v.setFillColor("#D84E3E");
	
	    				} else if (ratio.compareTo(p80)<0) { // < 50%
	    					v.setFillColor("#D8783E");
	    					
	    				} else if(ratio.compareTo(p98)<0) { // < 75%
	    					v.setFillColor("#D8C13E");
	    					
	    				} else { //
	    					v.setFillColor("#58D83E");
	    				}
    				}
    				
    			} else {
    				v.setPopParkExcluded("n/a");
    				v.setPopParkIncluded("n/a");
    			}
    			
    			v.setCommune(s.getCommune());
			}
    	}
    	}
    	
    	return root;
    }
    
    public GeoJsonRoot findAllCarreByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = this.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findAllCarreByArea(polygon);
    }
    
    
    /**
     * Truncate decimal values ti reduce response size.
     * @param inssePop
     * @return
     */
    public String formatPopulation(String inssePop) {
    	String ret = "";
    	if (StringUtils.isNotBlank(inssePop)) {
    		int index = inssePop.indexOf(".");
    		if (index>0) {
    			ret = inssePop.substring(0, index);
    		} else {
    			ret = inssePop;
    		}
    	}
    	return ret;
    }
    
    public Double getPopulation(String inssePop) {
    	Double ret = -1d;
    	if (StringUtils.isNotBlank(inssePop)) {
    		ret = Double.valueOf(inssePop.trim());
    	}
    	return ret;
    }
    
    /**
     * produce the search Polygon for the map.
     * @param bounds
     * @return
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    Polygon getPolygonFromBounds(String bounds) throws JsonMappingException, JsonProcessingException {
    	Polygon polygon=null;
    	if (StringUtils.isNotBlank(bounds)) {
	    	ObjectMapper objectMapper = new ObjectMapper();
	    	Bound bound= objectMapper.readValue(bounds, Bound.class);
	    	
	    	Double x1=  bound.getSouthWest().getLng();
	    	Double x2=  bound.getNorthEast().getLng();
	    	Double y1= bound.getSouthWest().getLat();
	    	Double y2= bound.getNorthEast().getLat();
	    	
	    	polygon = this.getPolygonFromBounds(y1,x1, y2,x2);
    	
    	}
    	return polygon;
    }
    

    Polygon getPolygonFromBounds(Double swLat, Double swLng, Double neLat, Double neLng) {
	    	
    	Polygon polygon=null;
    	
	    	Double x1=  swLng;
	    	Double x2=  neLng;
	    	Double y1= swLat;
	    	Double y2= neLat;
	    	
	    	Coordinate southWest = new Coordinate(x1,y1);
	    	Coordinate northEast = new Coordinate(x2,y2);
	    	
	    	if (checkDistance(southWest, northEast)) {
		    	List<Coordinate> coords = new ArrayList<>();
		    	coords.add(new Coordinate(x1,y1) );
		    	coords.add(new Coordinate(x1,y2) );
		    	coords.add(new Coordinate(x2,y2) );
		    	coords.add(new Coordinate(x2,y1) );
		    	coords.add(new Coordinate(x1,y1) );
		    	
		    	Coordinate[] array = coords.toArray(Coordinate[]::new);
		    	polygon= (Polygon)factory.createPolygon(array).getEnvelope();
	    	}
    	
    	return polygon;
    }
    
    
    
    /**
     * check distance between corners to limit big request.
     * @param southWest
     * @param northEast
     * @return
     */
    boolean checkDistance(Coordinate southWest, Coordinate northEast) {
    	double dist = southWest.distance(northEast);
    	return dist<0.80d;
    }
    
    
}
