package com.github.cunvoas.geoserviceisochrone.service.map;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.CadastreView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.Carre200AndShapeView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.IsochroneView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.ParkGardenView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.ParkPrefView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.ParkView;
import com.github.cunvoas.geoserviceisochrone.extern.helper.DistanceHelper;
import com.github.cunvoas.geoserviceisochrone.extern.leaflet.Bound;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonFeature;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusPrefEnum;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParcPrefectureRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.google.common.math.BigDecimalMath;
import com.google.common.primitives.Ints;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GeoMapServiceV2 {
	
	// https://htmlcolorcodes.com/
	public String COLOR_TO_QUALIFY="#ff7070";
	public String COLOR_CANCEL="#997e94";
	public String COLOR_PROCESSED="#5afffa";
	public String COLOR_VALID="#6efffa";

	/**
	 * park surface per capita > OMS reco
	 */
	public String THRESHOLD_PERFECT="#1a9900";
	
	/**
	 *  park surface per capita > OMS mini
	 */
	public String THRESHOLD_CORRECT="#9ee88f";
	/**
	 *  park surface per capita > OMS mini
	 */
	public String THRESHOLD_CORRECT_OR_CORRECT_INCOMPLETE="#d8973e";
	/**
	 *  park surface per capita < OMS mini
	 */
	public String THRESHOLD_BAD="#EOEOEO";
	/**
	 * this park in greenwashed
	 */
	public String THRESHOLD_GREENWASHED="#f1e2e2";
	
	public String THRESHOLD_NOT_COMPUTED="#4944f5";
	
	
	
	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired
    private InseeCarre200mComputedV2Repository inseeCarre200mComputedV2Repository;
    @Autowired
    private InseeCarre200mOnlyShapeRepository inseeCarre200osRepository;
    @Autowired
    private Filosofil200mRepository filosofil200mRepository;
    @Autowired
    private ParkAreaRepository parkAreaRepository;
    @Autowired
    private ParkEntranceRepository parkEntranceRepository;
    @Autowired
    private ParkAreaComputedRepository parkAreaComputedRepository;
    @Autowired
    private CadastreRepository cadastreRepository;
    @Autowired
    private CommunauteCommuneRepository communauteCommuneRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private ParcPrefectureRepository parcPrefectureRepository;
    @Autowired
    private ParkJardinRepository parkJardinRepository;
	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	
	/**
	 * @param id
	 * @return
	 */
	public GeoJsonRoot findCadastreByCity(Long id) {
		GeoJsonRoot root = new GeoJsonRoot();
		
		Optional<City> city = cityRepository.findById(id);
		if (city.isPresent()) {
			Optional<Cadastre> oCadastre = cadastreRepository.findById(city.get().getInseeCode());
			
			if (oCadastre.isPresent()) {
				Cadastre cadastre = oCadastre.get();
				
				GeoJsonFeature feature = new GeoJsonFeature();
				root.getFeatures().add(feature);
				feature.setGeometry(cadastre.getGeoShape());
				
				CadastreView cv = new CadastreView();
				cv.setIdInsee(cadastre.getIdInsee());
				cv.setNom(cadastre.getNom());
				//cv.setCommunauteCommune(com2co.get().getName());
				feature.setProperties(cv);
			}
		}
		return root;
	}
	
	/**
	 * @param id
	 * @return Cadastre geojson
	 */
	public GeoJsonRoot findAllCadastreByComm2Co(Long id) {
		GeoJsonRoot root = new GeoJsonRoot();
		
		Optional<CommunauteCommune> com2co = communauteCommuneRepository.findById(id);
		if (com2co.isPresent()) {
			
			List<String> ids = new ArrayList<>();
			try {
				for (City city : com2co.get().getCities()) {
					ids.add(city.getInseeCode());
				}
			} catch (Exception e) {
				List<City> cities = cityRepository.findByCommunauteCommune_Id(id);
				for (City city : cities) {
					ids.add(city.getInseeCode());
				}
			}
			
			
			List<Cadastre> cadastres = cadastreRepository.findAllById(ids);
			if (cadastres!=null && cadastres.size()>0) {
    			for (Cadastre cadastre : cadastres) {
    			
	    			GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					feature.setGeometry(cadastre.getGeoShape());
					
					CadastreView cv = new CadastreView();
					cv.setIdInsee(cadastre.getIdInsee());
					cv.setNom(cadastre.getNom());
					cv.setCommunauteCommune(com2co.get().getName());
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
     * @return  Cadastre geojson
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
     * @return  Isochrone geojson
     */
    
	public GeoJsonRoot findAllParkByArea(Integer annee, Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = this.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findAllParkByArea(polygon, annee);
    }
	
	/**
	 * @param annee
	 * @param swLat
	 * @param swLng
	 * @param neLat
	 * @param neLng
	 * @return kOutline geojson
	 */
	public GeoJsonRoot findAllParkOutlineByArea(Integer annee, Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = this.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findAllParkOutlineByArea(polygon, annee);
    }
	public GeoJsonRoot findAllParkOutlineByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = this.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findAllParkOutlineByArea(polygon);
    }
    
    
	/**
	 * @param swLat
	 * @param swLng
	 * @param neLat
	 * @param neLng
	 * @return   Park Prefecture geojson
	 */
	public GeoJsonRoot findParkPrefectureByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = this.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findParkPrefectureByArea(polygon);
    }
    
    
	/**
	 * @param swLat
	 * @param swLng
	 * @param neLat
	 * @param neLng
	 * @return   Isochrone geojson
	 */
	public GeoJsonRoot findParcEtJardinByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = this.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findParcEtJardinByArea(polygon);
    }
    
    
    /**
     * shuffle color of polygons.
     * @param index
     * @return
     */
    
	protected String getColor(int index) {
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
     * @return   Isochrone geojson
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
    
	/**
	 * @param idPark
	 * @return   Isochrone geojson
	 */
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
	 * @param annee
	 * @return
	 */
	public GeoJsonRoot findAllParkOutlineByArea(Polygon polygon, Integer annee) {
		GeoJsonRoot root = new GeoJsonRoot();
		

    	if (polygon!=null) {
    		List<ParcEtJardin> parcEtJardins = parkJardinRepository.findByAreaAndYear(annee, polygon);
    		if (!CollectionUtils.isEmpty(parcEtJardins)) {
    			for (ParcEtJardin park : parcEtJardins) {

					GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					feature.setGeometry(park.getContour());
					
					ParkGardenView pv = new ParkGardenView();
					feature.setProperties(pv);
					pv.setId(String.valueOf(park.getId()));
					pv.setName(park.getName());
    			}
    		}
    	}
		return root;
	}
	

	/**
	 * @param polygon
	 * @param annee
	 * @return
	 */
	public GeoJsonRoot findAllParkOutlineByArea(Polygon polygon) {
		GeoJsonRoot root = new GeoJsonRoot();
		
    	if (polygon!=null) {
    		List<ParcEtJardin> parcEtJardins = parkJardinRepository.findByArea(polygon);
    		if (!CollectionUtils.isEmpty(parcEtJardins)) {
    			for (ParcEtJardin park : parcEtJardins) {

					GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					feature.setGeometry(park.getContour());
					
					ParkGardenView pv = new ParkGardenView();
					feature.setProperties(pv);
					pv.setId(String.valueOf(park.getId()));
					pv.setName(park.getName());
    			}
    		}
    	}
		return root;
	}
	
	/**
	 * @param polygon
	 * @return   Isochrone geojson
	 */
	public GeoJsonRoot findAllParkByArea(Polygon polygon, Integer annee) {
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
					
					

					Optional<ParkAreaComputed> cpu = parkAreaComputedRepository.findByIdAndAnnee(parkArea.getId(), annee);
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
	 * @param polygon
	 * @return   Parc Point geojson
	 */
	public GeoJsonRoot findParcEtJardinByArea(Polygon polygon) {
		GeoJsonRoot root = new GeoJsonRoot();

    	if (polygon!=null) {
    		log.debug("GEOM= {}", GeometryQueryHelper.toTextWoSrid(polygon));
    		//List<ParcEtJardin> parkPrefs =  parkJardinRepository.findByArea(GeometryQueryHelper.toText(polygon));
    		List<ParcEtJardin> parkPrefs =  parkJardinRepository.findByArea(polygon);
			
    		if (!CollectionUtils.isEmpty(parkPrefs)) {
				for (ParcEtJardin parcJardin : parkPrefs) {
					
					GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					feature.setGeometry(parcJardin.getCoordonnee());
					
					ParkGardenView pv = new ParkGardenView();
					pv.setId(String.valueOf(parcJardin.getId()));
					pv.setName(parcJardin.getName());
					pv.setSurface(parcJardin.getSurface());
					
					if (parcJardin.getSource()!=null) {
						pv.setSource(parcJardin.getSource().toString());
					} else {
						pv.setSource(ParcSourceEnum.OPENDATA.toString());
					}
					
					feature.setProperties(pv);
				}
			}
    	}
		return root;
		
	}
	
	
	public GeoJsonRoot findParkPrefectureByArea(Polygon polygon) {
		GeoJsonRoot root = new GeoJsonRoot();

    	if (polygon!=null) {
			List<ParcPrefecture> parkPrefs =  parcPrefectureRepository.findByArea(GeometryQueryHelper.toText(polygon));
			if (!CollectionUtils.isEmpty(parkPrefs)) {
				for (ParcPrefecture parkPref : parkPrefs) {
					
					GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					feature.setGeometry(parkPref.getArea());
					
					ParkPrefView pv = new ParkPrefView();
					pv.setId(String.valueOf(parkPref.getId()));
					pv.setName(parkPref.getName());
					pv.setNamePrefecture(parkPref.getNamePrefecture());
					pv.setProcessed(parkPref.getProcessed());
					pv.setSurface(parkPref.getSurface());
					if (parkPref.getStatus()!=null) {
						pv.setStatus(parkPref.getStatus().toString());
					} else {
						pv.setStatus(ParcStatusPrefEnum.TO_QUALIFY.toString());
					}
					
					if (parkPref.getParcEtJardin()!=null) {
						pv.setIdParcJardin(parkPref.getParcEtJardin().getId());
						pv.setNameParcJardin(parkPref.getParcEtJardin().getName());
						pv.setQuartier(parkPref.getParcEtJardin().getQuartier());
						pv.setType(parkPref.getParcEtJardin().getType());
						pv.setSousType(parkPref.getParcEtJardin().getSousType());
						if (parkPref.getParcEtJardin().getSource()!=null) {
							pv.setSource(parkPref.getParcEtJardin().getSource().toString());
						} else {
							pv.setSource(ParcSourceEnum.OPENDATA.toString());
						}
					}
					
					
					if (ParcStatusPrefEnum.VALID.equals(parkPref.getStatus())) {
						pv.setFillColor(COLOR_VALID);
						
					} else if (ParcStatusPrefEnum.PROCESSED.equals(parkPref.getStatus())) {
						pv.setFillColor(COLOR_PROCESSED);
						
					} else if (ParcStatusPrefEnum.CANCEL.equals(parkPref.getStatus())) {
						pv.setFillColor(COLOR_CANCEL);
						
					} else if (ParcStatusPrefEnum.TO_QUALIFY.equals(parkPref.getStatus())) {
						pv.setFillColor(COLOR_TO_QUALIFY);
						
					} else {
						pv.setFillColor(COLOR_TO_QUALIFY);
					}
					
					if (Boolean.TRUE.equals(parkPref.getProcessed())) {
						pv.setFillColor(COLOR_VALID);
					} else {
						pv.setFillColor(COLOR_TO_QUALIFY);
					}
					
					feature.setProperties(pv);
				}
			}
    	}
		return root;
	}

	/**
	 * @param pv
	 * @param areaCputed
	 * @return color of an isochrone park.
	 */
	protected String getFillColorPark(ParkView pv, ParkAreaComputed areaCputed) {
		String color= THRESHOLD_GREENWASHED;

		double thresholdReco = 12;
		double thresholdMin = 10;
		if (areaCputed.getIsDense()!=null?areaCputed.getIsDense():Boolean.TRUE) {
			 thresholdReco = applicationBusinessProperties.getRecoUrbSquareMeterPerCapita();
			 thresholdMin = applicationBusinessProperties.getMinUrbSquareMeterPerCapita();
		} else {
			 thresholdReco = applicationBusinessProperties.getRecoSubUrbSquareMeterPerCapita();
			 thresholdMin = applicationBusinessProperties.getMinSubUrbSquareMeterPerCapita();
		}
		
		
		if (areaCputed.getOms()) {
			if (areaCputed.getSurfacePerInhabitant()==null) {
				pv.setAreaPerPeople("-");
				return THRESHOLD_NOT_COMPUTED;
			}

			pv.setAreaPerPeople(areaCputed.getSurfacePerInhabitant().toPlainString());
			Double sph = BigDecimalMath.roundToDouble(areaCputed.getSurfacePerInhabitant(), RoundingMode.HALF_EVEN);
			if (sph>thresholdReco) {
				color = THRESHOLD_PERFECT;
			} else if (sph>thresholdMin) {
				color = THRESHOLD_CORRECT;
			} else {
				color =  getColorGrey(sph);
			}
		}
		
		return color;
	}
	
	/**
	 * @param pv
	 * @param pac
	 */
	
	protected void extraFeature(ParkView pv, ParkAreaComputed pac) {
		
		NumberFormat nf = new DecimalFormat("# ##0");
		if (pac!=null) {
			pv.setPeople(nf.format(pac.getPopulation()));
			pv.setArea(nf.format(pac.getSurface()));
			pv.setOms(pac.getOms());
			pv.setDense(pac.getIsDense());
			
			pv.setFillColor(this.getFillColorPark(pv, pac));
		}
	}
	

	protected BigDecimal fromDouble(Double d) {
		NumberFormat formatter = new DecimalFormat("#0");     
		return new BigDecimal(formatter.format(d));
	}
	
	
	protected String getFillColorCarre(Carre200AndShapeView v, InseeCarre200mComputedV2 pacEd) {
		String color = THRESHOLD_GREENWASHED;

		if (pacEd.getUpdated()!=null) {
			if (pacEd.getPopAll()==null) {
				v.setAreaPerPeople("-");
				return THRESHOLD_NOT_COMPUTED;
			}
			
			double thresholdReco = 12;
			double thresholdMin = 10;
			if (Boolean.FALSE.equals(pacEd.getIsDense())) {
				 thresholdReco = applicationBusinessProperties.getRecoSubUrbSquareMeterPerCapita();
				 thresholdMin = applicationBusinessProperties.getMinSubUrbSquareMeterPerCapita();
			} else {
				 thresholdReco = applicationBusinessProperties.getRecoUrbSquareMeterPerCapita();
				 thresholdMin = applicationBusinessProperties.getMinUrbSquareMeterPerCapita();
			}
			BigDecimal spc = pacEd.getSurfaceParkPerCapitaOms();
			if (spc==null) {
				spc=BigDecimal.ZERO;
			}
			
			Double sph = BigDecimalMath.roundToDouble(spc, RoundingMode.HALF_EVEN);
			
			Boolean allInhabitant = pacEd.getPopAll()!=null?pacEd.getPopAll().equals(pacEd.getPopIncludedOms()):Boolean.FALSE;
			
			if ("LAEA200M_N15407E19138".equals(pacEd.getIdCarre200())) {
				int debug=0;
			}
			if (sph>thresholdReco && allInhabitant) {
				color = THRESHOLD_PERFECT;
			} else if (sph>thresholdReco && !allInhabitant) {
				color = THRESHOLD_CORRECT_OR_CORRECT_INCOMPLETE;
				
			} else if (sph>thresholdMin && allInhabitant) {
				color = THRESHOLD_CORRECT;
				
			} else if (sph>thresholdMin && !allInhabitant) {
				color = THRESHOLD_CORRECT_OR_CORRECT_INCOMPLETE;
				
			} else {
				color = getColorGrey(sph);
			}
		}
		
		return color;
	
		
	}
	
	
	/**
	 * @param sph
	 * @return gradient color
	 */
	protected String getColorGrey(Double sph) {
		String color=THRESHOLD_BAD;
		// color from 0 to 255
		Long v = Math.round(123+sph*10);
		String s = Integer.toString(Ints.checkedCast(v), 16);
		color = String.format("#%s%s%s", s, s, s);
		return color;
	}
	
	
	/**
	 * format an integer pattern.
	 */
	private static final NumberFormat DF_E = new DecimalFormat("#0");
	/**
	 * format a decimal pattern.
	 */
	private static final NumberFormat DF_S = new DecimalFormat("#0.00");
	
	/**
	 * format an integer .
	 * @param v
	 * @return
	 */
	protected String formatInt(BigDecimal v) {
		if (v!=null) {
			return  DF_E.format(v);
		}
		return "-";
		
	}
	
	/**
	 * format a decimal.
	 * @param v
	 * @return
	 */
	public String formatDec(BigDecimal v) {
		if (v!=null) {
			return  DF_S.format(v);
		}
		return "-";
		
	}
	
	
	 /**
	 * @param polygon
	 * @return
	 * @FIXME until final front permit to change year.
	 */
	public GeoJsonRoot findAllCarreByArea(Polygon polygon) {
		//FIXME until final front permit to change year
		log.error("FIXME findAllCarreByArea(Polygon polygon)");
		 return this.findAllCarreByArea(polygon, 2019);
	 }
	 
    /**
     * GET ALL IRIS in the map.
     * @param polygon
     * @return
     */
	public GeoJsonRoot findAllCarreByCommunauteCommune(CommunauteCommune com2co, Integer annee) {
		log.error("findAllCarreByArea(CommunauteCommune {}, Integer annee {})", com2co.getName(), annee);
    	GeoJsonRoot root = null;
		
    	Set<InseeCarre200mOnlyShape> carres = new HashSet<>();
    	
    	for (City city : com2co.getCities()) {
    		List<InseeCarre200mOnlyShape> cShapes = inseeCarre200osRepository.findCarreByInseeCode(
    				city.getInseeCode(), 
    				Boolean.TRUE);
    		carres.addAll(cShapes);
    	}
    	
    	root = findAllCarreByArea(carres, annee);
    	
    	return root;
	}
	
	protected GeoJsonRoot findAllCarreByArea(Collection<InseeCarre200mOnlyShape> carres, Integer annee) {
		GeoJsonRoot root = new GeoJsonRoot();
		
    	if (carres!=null && carres.size()>0) {
    		for (InseeCarre200mOnlyShape c : carres) {
    			
    			Optional<InseeCarre200mComputedV2> ocputed = inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(annee, c.getIdInspire());
    		
    			GeoJsonFeature feature = new GeoJsonFeature();
				root.getFeatures().add(feature);
				feature.setGeometry(c.getGeoShape());

    			Carre200AndShapeView v = new Carre200AndShapeView();
				feature.setProperties(v);
    			v.setId(c.getIdInspire());
    			
    			StringBuilder sb = new StringBuilder();
    			sb.append( StringUtils.left(c.getIdInspire(), 14) );
    			sb.append("<br />");
    			sb.append( StringUtils.substring(c.getIdInspire(), 14) );
    			
    			v.setIdInspire(sb.toString());
    			
    			
    			if (ocputed.isPresent()) {
    				InseeCarre200mComputedV2 cputed = ocputed.get();
    				
    				
    				
    				v.setPeople(formatInt(cputed.getPopAll()));
    				
    				// declared by public organisation (^possible greenwashing)
    				v.setSurfaceTotalPark(formatInt(cputed.getSurfaceTotalPark()));
    				v.setPopParkExcluded(formatInt(cputed.getPopExcluded()));
    				v.setPopParkIncluded(formatInt(cputed.getPopIncluded()));
    				v.setPopSquareShare(formatInt(cputed.getPopulationInIsochrone()));
    				v.setSquareMtePerCapita(formatDec(cputed.getSurfaceParkPerCapita()));

    				// check by Aut'MEL from OMS prerequisit
    				v.setSurfaceTotalParkOms(formatInt(cputed.getSurfaceTotalParkOms()));
    				v.setPopParkExcludedOms(formatInt(cputed.getPopExcludedOms()));
    				v.setPopParkIncludedOms(formatInt(cputed.getPopIncludedOms()));
    				v.setPopSquareShareOms(formatInt(cputed.getPopulationInIsochroneOms()));
    				v.setSquareMtePerCapitaOms(formatDec(cputed.getSurfaceParkPerCapitaOms()));
    				v.setIsDense(cputed.getIsDense());
    				v.setFillColor(this.getFillColorCarre(v, cputed));
    				
    				if (cputed.getComments()!=null) {
    					v.setCommentParks(cputed.getComments());
    				}
    				
    			} else {
    				Filosofil200m carreData = filosofil200mRepository.findByAnneeAndIdInspire(annee, c.getIdInspire());
    				if (carreData!=null) {
    					v.setPeople(formatInt(carreData.getNbIndividus()));
    				} else {
    					v.setPeople("0");
    				}
    				
    				v.setPopParkExcluded("n/a");
    				v.setPopParkIncluded("n/a");
    			}
    			City city = cityRepository.findByInseeCode(c.getCodeInsee());
    			if (city!=null) {
    				v.setCommune(city.getName());
    			}
			}
    	}
		
		return root;
	}
	
	public GeoJsonRoot findAllCarreByArea(Polygon polygon, Integer annee) {
		log.error("findAllCarreByArea(Polygon polygon, Integer annee {})", annee);
		GeoJsonRoot root = null;
    	if (polygon!=null) {
	    	//List<InseeCarre200m> carres = inseeCarre200mRepository.getAllCarreInMap(GeometryQueryHelper.toText(polygon));
	    	List<InseeCarre200mOnlyShape> carres = inseeCarre200osRepository.findCarreInMapArea(GeometryQueryHelper.toText(polygon), Boolean.TRUE);
	    	
	    	root = findAllCarreByArea(carres, annee);
    	}
    	return root;
    }
    
	public GeoJsonRoot findAllCarreByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
		Integer annee = applicationBusinessProperties.getDerniereAnnee();
    	return this.findAllCarreByArea(annee, swLat, swLng, neLat, neLng);
    }
    
	public GeoJsonRoot findAllCarreByArea(Integer annee, Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = this.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findAllCarreByArea(polygon, annee);
    }
    
    
    /**
     * Truncate decimal values ti reduce response size.
     * @param inssePop
     * @return
     */
    
	protected String formatPopulation(String inssePop) {
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
    

    /**
     * @param swLat
     * @param swLng
     * @param neLat
     * @param neLng
     * @return
     */
    protected Polygon getPolygonFromBounds(Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon=null;
    	
    	Double x1= swLng;
    	Double x2= neLng;
    	Double y1= swLat;
    	Double y2= neLat;
    	
    	Coordinate southWest = new Coordinate(x1,y1);
    	Coordinate northEast = new Coordinate(x2,y2);
    	
    	if (checkDistance(southWest, northEast)) {
	    	List<Coordinate> coords = new ArrayList<>();
	    	coords.add( new Coordinate(x1,y1) );
	    	coords.add( new Coordinate(x1,y2) );
	    	coords.add( new Coordinate(x2,y2) );
	    	coords.add( new Coordinate(x2,y1) );
	    	coords.add( new Coordinate(x1,y1) );
	    	
	    	Coordinate[] array = coords.toArray(Coordinate[]::new);
//	    	polygon= (Polygon)factory.createPolygon(array).getEnvelope();
	    	polygon= factory.createPolygon(array);
    	}
    	
    	return polygon;
    }
    
    
    
    /**
     * check distance between corners to limit big request.
     * @param southWest
     * @param northEast
     * @return
     */
    protected boolean checkDistance(Coordinate southWest, Coordinate northEast) {
    	Double d =  DistanceHelper.crowFlyDistance(
	    				southWest.getY(), southWest.getX(),
	    				northEast.getY(), northEast.getX()
	    			);
    	return d<50;
    	
    }
    
    
}
