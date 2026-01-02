package com.github.cunvoas.geoserviceisochrone.service.map;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.CadastreView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.Carre200AndShapeView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.IrisView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.IsochroneView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.ParkGardenView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.ParkPrefView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.ParkProposalView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.ParkView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.ProjetSimulView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.ProjetSimulWorkView;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonFeature;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.IrisDataComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisData;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisId;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusPrefEnum;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorWork;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.IrisDataComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ParkProposalRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ProjectSimulatorRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ProjectSimulatorlWorkRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisDataRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParcPrefectureRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkTypeService;
import com.google.common.math.BigDecimalMath;
import com.google.common.primitives.Ints;

import lombok.extern.slf4j.Slf4j;

/**
 * Business Service impl.
 */
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
	private ApplicationBusinessProperties applicationBusinessProperties;
	
    @Autowired
    private InseeCarre200mComputedV2Repository inseeCarre200mComputedV2Repository;
    @Autowired
    private InseeCarre200mOnlyShapeRepository inseeCarre200osRepository;
    @Autowired
    private Filosofil200mRepository filosofil200mRepository;

    @Autowired
    private IrisShapeRepository irisShapeRepository;
    @Autowired
    private IrisDataRepository irisDataRepository;
    @Autowired
    private IrisDataComputedRepository irisDataComputedRepository;


    @Autowired
    private ParkProposalRepository parkProposalRepository;
    

    @Autowired
    private ProjectSimulatorRepository projectSimulatorRepository;
	@Autowired
	private ProjectSimulatorlWorkRepository projectSimulatorlWorkRepository;
    
    @Autowired
    private ParkAreaRepository parkAreaRepository;
    @Autowired
    private ParkEntranceRepository parkEntranceRepository;
    @Autowired
    private ParkAreaComputedRepository parkAreaComputedRepository;
    @Autowired
    private ParkJardinRepository parkJardinRepository;
	@Autowired 
	private ParkTypeService parkTypeService;
    
    @Autowired
    private CadastreRepository cadastreRepository;
    @Autowired
    private CommunauteCommuneRepository communauteCommuneRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private ParcPrefectureRepository parcPrefectureRepository;
    @Autowired
    private GeometryQueryHelper geometryQueryHelper;
	
    
	/**
	 * findCadastreByCity.
	 * @param id City
	 * @return GeoJson of adastre
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
	 * findAllCadastreByComm2Co.
	 * @param id Comm2Co
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
     * findAllCadastreByArea.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
     * @return  Cadastre geojson
     */
	public GeoJsonRoot findAllCadastreByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
		GeoJsonRoot root = new GeoJsonRoot();
    	Polygon polygon = geometryQueryHelper.getPolygonFromBounds(swLat, swLng, neLat, neLng);
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
     * findAllParkByArea.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
     * @return  Isochrone geojson
     */
    
	public GeoJsonRoot findAllParkByArea(Integer annee, Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = geometryQueryHelper.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findAllParkByArea(polygon, annee);
    }

	  
    /**
     * findParkProposalByArea.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
     * @return  ParkProposal geojson
     */
    
	public GeoJsonRoot findParkProposalByArea(Integer annee, Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = geometryQueryHelper.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findParkProposalByArea(polygon, annee);
    }
	
	
	/**
	 * findAllParkOutlineByArea.
	 * @param annee year
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
	 * @return Outline geojson
	 */
	public GeoJsonRoot findAllParkOutlineByArea(Integer annee, Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = geometryQueryHelper.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findAllParkOutlineByArea(polygon, annee);
    }
	/**
     * findAllParkOutlineByArea.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
	 * @return Outline geojson
	 */
	public GeoJsonRoot findAllParkOutlineByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = geometryQueryHelper.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findAllParkOutlineByArea(polygon);
    }
	
	/**
     * getProjectByArea.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
	 * @return Outline geojson
	 */
	public GeoJsonRoot findProjetSimulationByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = geometryQueryHelper.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findProjetSimulationByArea(polygon);
    }

    
	/**
     * findParkPrefectureByArea.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
	 * @return   ParcPrefecture geojson
	 */
	public GeoJsonRoot findParkPrefectureByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = geometryQueryHelper.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findParkPrefectureByArea(polygon);
    }
    
    
	/**
     * findParcEtJardinByArea.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
	 * @return   Isochrone geojson
	 */
	public GeoJsonRoot findParcEtJardinByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = geometryQueryHelper.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findParcEtJardinByArea(polygon);
    }
    
    
    /**
     * shuffle color of polygons.
     * @param index idx
     * @return 
     * @deprecated
     */
    @Deprecated
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
     * @param idPark Park
     * @return Isochrone geojson
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
	 * findIsochronePark.
	 * @param idPark parc
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
	 * findAllParkOutlineByArea.
	 * @param polygon Polygon
	 * @param annee year
	 * @return ParkOutline geojson
	 */
	public GeoJsonRoot findAllParkOutlineByArea(Polygon polygon, Integer annee) {
		GeoJsonRoot root = new GeoJsonRoot();

    	if (polygon!=null) {
    		List<ParcEtJardin> parcEtJardins = parkJardinRepository.findByAreaAndYear(annee, polygon);
    		if (!CollectionUtils.isEmpty(parcEtJardins)) {
    			for (ParcEtJardin park : parcEtJardins) {

					GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					if (park.getContour()!=null) {
						feature.setGeometry(park.getContour());
					} else if (park.getCoordonnee()!=null) {
						feature.setGeometry(park.getCoordonnee());
					}
					
					ParkGardenView pv = new ParkGardenView();
					feature.setProperties(pv);
					pv.setId(String.valueOf(park.getId()));
					pv.setName(park.getName());
					if (park.getCommune()!=null) {
						pv.setCity(park.getCommune().getName());
					} 
					pv.setSurface(park.getSurface());
					
					if (park.getOmsCustom()==null) {
						if (park.getTypeId()!=null){
							ParkType pt = parkTypeService.get(park.getTypeId());
							pv.setOms(pt.getOms());
						}
					} else {
						pv.setOms(park.getOmsCustom());
					}
					pv.setActif(getActif(park));
					
					
    			}
    		}
    	}
		return root;
	}
	

	/**
	 * findAllParkOutlineByArea.
	 * @param polygon Polygon
	 * @return ParkOutline geojson
	 */
	public GeoJsonRoot findAllParkOutlineByArea(Polygon polygon) {
		GeoJsonRoot root = new GeoJsonRoot();
		
    	if (polygon!=null) {
    		List<ParcEtJardin> parcEtJardins = parkJardinRepository.findByArea(polygon);
    		if (!CollectionUtils.isEmpty(parcEtJardins)) {
    			for (ParcEtJardin park : parcEtJardins) {

					GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					if (park.getContour()!=null) {
						feature.setGeometry(park.getContour());
					} else if (park.getCoordonnee()!=null) {
						feature.setGeometry(park.getCoordonnee());
					}
					
					ParkGardenView pv = new ParkGardenView();
					feature.setProperties(pv);
					pv.setId(String.valueOf(park.getId()));
					pv.setName(park.getName());
					

					pv.setSurface(park.getSurface());
					if (park.getOmsCustom()==null) {
						if (park.getTypeId()!=null){
							ParkType pt = parkTypeService.get(park.getTypeId());
							pv.setOms(pt.getOms());
						}
					} else {
						pv.setOms(park.getOmsCustom());
					}
					pv.setActif(getActif(park));
					
					ParkArea pa =parkAreaRepository.findByIdParcEtJardin(park.getId());
					pv.setEntry(pa!=null && pa.getPolygon()!=null);
//					if (pa!=null && pa.getPolygon()!=null) {
//						pv.setEntry(true);
//					}
    			}
    		}
    	}
		return root;
	}
	
	public boolean getActif(ParcEtJardin park) {
		boolean ret = true;
		Date now = new Date();
		if (park.getDateDebut()!=null && now.before(park.getDateDebut())) {
			ret = false;
		}
		if (park.getDateFin()!=null && now.after(park.getDateFin())) {
			ret = false;
		}
		return ret;
	}
	
	/**
	 * findAllParkByArea.
	 * @param polygon Polygon
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
	 * findParkProposalByArea.
	 * @param polygon Polygon
	 * @return   park proposal geojson
	 */
	public GeoJsonRoot findParkProposalByArea(Polygon polygon, Integer annee) {
		GeoJsonRoot root = new GeoJsonRoot();
		
    	if (polygon!=null) {
			List<ParkProposal> proposals =  parkProposalRepository.findParkProposalInMapArea(annee, GeometryQueryHelper.toText(polygon));
			if (!CollectionUtils.isEmpty(proposals)) {
				for (ParkProposal proposal : proposals) {
					GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					feature.setGeometry(proposal.getCentre());
					
					
					ParkProposalView pv = new ParkProposalView();
					
					pv.setId(String.valueOf(proposal.getIdInspire()));
					pv.setDense(proposal.getIsDense());
					pv.setRadius(proposal.getRadius());
					pv.setSurface(Math.round(proposal.getParkSurface().longValue()));
					
					feature.setProperties(pv);
					
				}
			}
    	}
		return root;
	}
	

	/**
	 * findProjetSimulationByArea.
	 * @param polygon Polygon
	 * @return   park proposal geojson
	 */
	public GeoJsonRoot findProjetSimulationByArea(Polygon polygon) {
		GeoJsonRoot root = new GeoJsonRoot();
		
    	if (polygon!=null) {
			List<ProjectSimulator> simuls =  projectSimulatorRepository.findInMapArea( GeometryQueryHelper.toText(polygon));
			if (!CollectionUtils.isEmpty(simuls)) {
				for (ProjectSimulator proposal : simuls) {
					GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					feature.setGeometry(proposal.getShapeArea());
					
					ProjetSimulView pv = new ProjetSimulView();
					
					pv.setId(String.valueOf(proposal.getId()));
					pv.setDense(proposal.getIsDense());
					pv.setName(proposal.getName());
					if (proposal.getSurfaceArea()!=null) {
						pv.setSurfaceArea(Math.round(proposal.getSurfaceArea().longValue()));
					}
					if (proposal.getSurfaceFloor()!=null) {
						pv.setSurfaceFloor(Math.round(proposal.getSurfaceFloor().longValue()));
					}
					if (proposal.getSurfacePark()!=null) {
						pv.setSurfacePark(Math.round(proposal.getSurfacePark().longValue()));
					}
					if (proposal.getPopulation()!=null) {
						pv.setPopulation(Math.round(proposal.getPopulation().longValue()));
					}
					feature.setProperties(pv);
					
				}
			}
    	}
		return root;
	}
	

	/**
	 * findProjetSimulationWorkByArea.
	 * @param polygon Polygon
	 * @return   park proposal geojson
	 */
	public GeoJsonRoot findProjetSimulationWorkById(Long idSimulation) {
		GeoJsonRoot root = new GeoJsonRoot();
		
    	if (idSimulation!=null) {
    		List<ProjectSimulatorWork> simuls =  projectSimulatorlWorkRepository.findByIdProjectSimulator(idSimulation);
			if (!CollectionUtils.isEmpty(simuls)) {
				for (ProjectSimulatorWork simul : simuls) {
					GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					
					feature.setGeometry(simul.getGeoShape());
					ProjetSimulWorkView pv = new ProjetSimulWorkView();
					pv.setIdInspire(String.valueOf(simul.getIdInspire()));
					
					pv.setAccessingPopulation(formatDec(simul.getAccessingPopulation()));
					pv.setLocalPopulation(formatDec(simul.getLocalPopulation()));
					
					pv.setAccessingSurface(formatDec(simul.getAccessingSurface()));
					pv.setMissingSurface(formatDec(simul.getMissingSurface()));
					pv.setSurfacePerCapita(formatDec(simul.getSurfacePerCapita()));
					
					pv.setNewSurface(formatDec(simul.getNewSurface()));
					pv.setNewMissingSurface(formatDec(simul.getNewMissingSurface()));
					pv.setNewSurfacePerCapita(formatDec(simul.getNewSurfacePerCapita()));
					
				}
			}
    	}
    	return root;
    }
	
	/**
	 * findParcEtJardinByArea.
	 * @param polygon Polygon
	 * @return   Parc Point geojson
	 */
	public GeoJsonRoot findParcEtJardinByArea(Polygon polygon) {
		GeoJsonRoot root = new GeoJsonRoot();

    	if (polygon!=null) {
    		log.debug("GEOM= {}", GeometryQueryHelper.toTextWoSrid(polygon));
    		//List<ParcEtJardin> parkPrefs =  parkJardinRepository.findByArea(GeometryQueryHelper.toText(polygon));
    		List<ParcEtJardin> parkPrefs =  parkJardinRepository.findByArea(polygon);
			
    		if (!CollectionUtils.isEmpty(parkPrefs)) {
				for (ParcEtJardin park : parkPrefs) {
					
					GeoJsonFeature feature = new GeoJsonFeature();
					root.getFeatures().add(feature);
					if (park.getContour()!=null) {
						feature.setGeometry(park.getContour());
					} else if (park.getCoordonnee()!=null) {
						feature.setGeometry(park.getCoordonnee());
					}
					
					ParkGardenView pv = new ParkGardenView();
					pv.setId(String.valueOf(park.getId()));
					pv.setName(park.getName());

					pv.setSurface(park.getSurface());
					
					if (park.getOmsCustom()==null) {
						if (park.getTypeId()!=null){
							ParkType pt = parkTypeService.get(park.getTypeId());
							pv.setOms(pt.getOms());
						}
					} else {
						pv.setOms(park.getOmsCustom());
					}
					pv.setActif(getActif(park));
					
					feature.setProperties(pv);
				}
			}
    	}
		return root;
		
	}
	
	
	/**
	 * findParkPrefectureByArea.
	 * @param polygon Polygon
	 * @return  GeoJso ParkPrefecture
	 */
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
					

					if (parkPref.getStatus()!=null) {
						pv.setStatus(parkPref.getStatus().toString());
					} else {
						pv.setStatus(ParcStatusPrefEnum.TO_QUALIFY.toString());
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
	 * getFillColorPark.
	 * @param pv DTO
	 * @param areaCputed ParkAreaComputed
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
	 * extraFeature.
	 * @param pv DTO
	 * @param pac ParkAreaComputed
	 */
	protected void extraFeature(ParkView pv, ParkAreaComputed pac) {
		
		NumberFormat nf = new DecimalFormat("# ##0");
		if (pac!=null) {
			pv.setPeople(nf.format(pac.getPopulation()));
			pv.setArea(nf.format(pac.getSurface()));
			pv.setOms(pac.getOms());
			pv.setDense(pac.getIsDense());
			
			//pv.setFillColor(this.getFillColorPark(pv, pac));
		}
	}
	

	/**
	 * fromDouble.
	 * @param d Double
	 * @return BigDecimal
	 */
	protected BigDecimal fromDouble(Double d) {
		NumberFormat formatter = new DecimalFormat("#0");     
		return new BigDecimal(formatter.format(d));
	}
	
	
	/**
	 * getFillColorCarre.
	 * @param v Carre200AndShapeView
	 * @param pacEd InseeCarre200mComputedV2
	 * @return color
	 */
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
	 * getColorGrey.
	 * @param sph number
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
	 * @param v  number
	 * @return num as string
	 */
	protected String formatInt(BigDecimal v) {
		if (v!=null) {
			return  DF_E.format(v);
		}
		return "-";
		
	}
	
	/**
	 * format a decimal.
	 * @param v number
	 * @return num as string
	 */
	public String formatDec(BigDecimal v) {
		if (v!=null) {
			return  DF_S.format(v);
		}
		return "-";
	}
	
	 /**
	 * findAllCarreByArea.
	 * @param polygon Polygon
	 * @return GeoJson Carre
	 * @FIXME until final front permit to change year.
	 * @deprecated
	 */
//	public GeoJsonRoot findAllCarreByArea(Polygon polygon) {
//		//FIXME until final front permit to change year
//		log.error("FIXME findAllCarreByArea(Polygon polygon)");
//		 return this.findAllCarreByArea(polygon, 2019);
//	 }
	 
    /**
     * GET ALL Carre in the map.
	 * @param com2co CommunauteCommune
	 * @param annee year
	 * @return GeoJson carre 
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

	/**
	 * findAllIrisByArea.
	 * @param annee year
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
	 * @return GeoJson carre 
	 */
	public GeoJsonRoot findAllIrisByArea(Integer annee, Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = geometryQueryHelper.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findAllIrisByArea(polygon, annee);
    }
	
	/**
	 * findAllIrisByArea.
	 * @param polygon Polygon
	 * @param annee year
	 * @return GeoJson carre 
	 */
	public GeoJsonRoot findAllIrisByArea(Polygon polygon, Integer annee) {
		log.error("findAllIrisByArea(Polygon polygon, Integer annee {})", annee);
		GeoJsonRoot root = null;
    	if (polygon!=null) {
    		// recherche des IRIS dans la zone
	    	List<IrisShape> iris = irisShapeRepository.findIrisInMapArea(GeometryQueryHelper.toText(polygon));
	    	// popule les IRIS avec les données INSEE
	    	root = findAllIrisByArea(iris, annee);
    	}
    	return root;
    }
    
	
    /**
     * GET ALL IRIS in the map.
	 * @param com2co CommunauteCommune
	 * @param annee year
	 * @return GeoJson carre 
	 */
	public GeoJsonRoot findAllIrisByCommunauteCommune(CommunauteCommune com2co, Integer annee) {
		log.error("findAllIrisByCommunauteCommune(CommunauteCommune {}, Integer annee {})", com2co.getName(), annee);
    	GeoJsonRoot root = null;
		
    	Set<IrisShape> carres = new HashSet<>();
    	
    	for (City city : com2co.getCities()) {
    		List<IrisShape> cShapes = irisShapeRepository.findByCodeInsee(
    				city.getInseeCode() );
    		carres.addAll(cShapes);
    	}
    	
    	root = findAllIrisByArea(carres, annee);
    	
    	return root;
	}
	
	
	
	
	/**
	 * findAllIrisByArea.
	 * @param carres shapes
	 * @param annee year
	 * @return GeoJson carre 
	 */
	protected GeoJsonRoot findAllIrisByArea(Collection<IrisShape> iris, Integer annee) {
		GeoJsonRoot root = new GeoJsonRoot();
		
		for (IrisShape irisShape : iris) {
			
			
			GeoJsonFeature feature = new GeoJsonFeature();
			root.getFeatures().add(feature);
			feature.setGeometry(irisShape.getContour());

			IrisView v = new IrisView();
			feature.setProperties(v);
			v.setId(irisShape.getIris());
			v.setIdIris(irisShape.getNomIris());
			
			City city = cityRepository.findByInseeCode(irisShape.getCodeInsee());
			if (city!=null) {
				v.setCommune(city.getName());
			}
			
			// recherche des données INSEE
			IrisId iId = new IrisId();
			iId.setAnnee(annee);
			iId.setIris(irisShape.getIris());
			Optional<IrisData> oIris = irisDataRepository.findById(iId);
			if (oIris.isPresent()) {
				IrisData iIris = oIris.get();

				if (iIris.getPop()!=null) {
					v.setPeople(formatInt(iIris.getPop()));
				} else {
					v.setPeople("0");
				}
//				if (irisShape.getSurface()!=null) {
//					v.setSurface((irisShape.getSurface().intValue());
//				} else {
//					v.setSurface(0);
//				}
				
				// recherche des données calculées existante
				IrisDataComputed idc = irisDataComputedRepository.findByAnneeAndIris(annee, irisShape.getIris());
				if (idc!=null) {
					v.setPeople(formatInt(idc.getPopAll()));
					v.setSurfaceTotalPark(formatInt(idc.getSurfaceTotalPark()));
					//v.setOms(idc.getOms());
					//v.setDense(iIris.getIsDense());
					
					//v.setFillColor(this.getFillColorIris(v, idc));
					
					if (idc.getComments()!=null) {
						v.setCommentParks(idc.getComments());
					}
				}
			
				
			}
		}
		
		
		return root;
	}
	
	/**
	 * findAllCarreByArea.
	 * @param carres shapes
	 * @param annee year
	 * @return GeoJson carre 
	 */
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
    				//v.setFillColor(this.getFillColorCarre(v, cputed));
    				
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
	
	/**
	 * findAllCarreByArea.
	 * @param polygon Polygon
	 * @param annee year
	 * @return GeoJson carre 
	 */
	public GeoJsonRoot findAllCarreByArea(Polygon polygon, Integer annee) {
		log.error("findAllCarreByArea(Polygon polygon, Integer annee {})", annee);
		GeoJsonRoot root = null;
    	if (polygon!=null) {
	    	List<InseeCarre200mOnlyShape> carres = inseeCarre200osRepository.findCarreInMapArea(GeometryQueryHelper.toText(polygon), Boolean.TRUE);
	    	root = findAllCarreByArea(carres, annee);
    	}
    	return root;
    }
    
	/**
     * findAllCarreByArea.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
	 * @return GeoJson carre 
	 */
	public GeoJsonRoot findAllCarreByArea(Double swLat, Double swLng, Double neLat, Double neLng) {
		Integer annee = applicationBusinessProperties.getDerniereAnnee();
    	return this.findAllCarreByArea(annee, swLat, swLng, neLat, neLng);
    }
    
	/**
	 * findAllCarreByArea.
	 * @param annee year
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
	 * @return GeoJson carre 
	 */
	public GeoJsonRoot findAllCarreByArea(Integer annee, Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon = geometryQueryHelper.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	return this.findAllCarreByArea(polygon, annee);
    }
    
    
    /**
     * Truncate decimal values ti reduce response size.
     * @param inssePop population
     * @return  population
	protected String formatPopulation(String inssePop) {
    	String ret = "";
    	if (StringUtils.isNotBlank(inssePop)) {
    		int index = inssePop.indexOf('.');
    		if (index>0) {
    			ret = inssePop.substring(0, index);
    		} else {
    			ret = inssePop;
    		}
    	}
    	return ret;
    }
//    
//    /**
//     * produce the search Polygon for the map.
//     * @param bounds
//     * @return
//     * @throws JsonMappingException
//     * @throws JsonProcessingException
//     */
//    Polygon getPolygonFromBounds(String bounds) throws JsonMappingException, JsonProcessingException {
//    	Polygon polygon=null;
//    	if (StringUtils.isNotBlank(bounds)) {
//	    	ObjectMapper objectMapper = new ObjectMapper();
//	    	Bound bound= objectMapper.readValue(bounds, Bound.class);
//	    	
//	    	Double x1=  bound.getSouthWest().getLng();
//	    	Double x2=  bound.getNorthEast().getLng();
//	    	Double y1= bound.getSouthWest().getLat();
//	    	Double y2= bound.getNorthEast().getLat();
//	    	
//	    	polygon = geometryQueryHelper.getPolygonFromBounds(y1,x1, y2,x2);
//    	
//    	}
//    	return polygon;
//    }
    

//    /**
//     * getPolygonFromBounds.
//     * @param swLat south-west latitude
//     * @param swLng south-west longitude
//     * @param neLat north-est latitude
//     * @param neLng north-est longitude
//     * @return
//     */
//    protected Polygon getPolygonFromBounds(Double swLat, Double swLng, Double neLat, Double neLng) {
//    	Polygon polygon=null;
//    	
//    	Double x1= swLng;
//    	Double x2= neLng;
//    	Double y1= swLat;
//    	Double y2= neLat;
//    	
//    	Coordinate southWest = new Coordinate(x1,y1);
//    	Coordinate northEast = new Coordinate(x2,y2);
//    	
//    	if (checkDistance(southWest, northEast)) {
//	    	List<Coordinate> coords = new ArrayList<>();
//	    	coords.add( new Coordinate(x1,y1) );
//	    	coords.add( new Coordinate(x1,y2) );
//	    	coords.add( new Coordinate(x2,y2) );
//	    	coords.add( new Coordinate(x2,y1) );
//	    	coords.add( new Coordinate(x1,y1) );
//	    	
////	    	Coordinate[] array = coords.toArray(Coordinate[]::new);
//	    	Coordinate[] array = coords.toArray(new Coordinate[0]);
//
//	    	polygon= factory.createPolygon(array);
//    	}
//    	
//    	return polygon;
//    }
//    
//    
//    
//    /**
//     * check distance between corners to limit big request.
//     * @param southWest
//     * @param northEast
//     * @return
//     */
//    protected boolean checkDistance(Coordinate southWest, Coordinate northEast) {
//    	Double d =  DistanceHelper.crowFlyDistance(
//	    				southWest.getY(), southWest.getX(),
//	    				northEast.getY(), northEast.getX()
//	    			);
//    	return d<50;
//    	
//    }
//    
    
}
