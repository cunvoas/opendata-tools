package com.github.cunvoas.geoserviceisochrone.service.analytics;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.Seuil;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.Stat;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.StatsSurfaceJson;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoCoordinate;
import com.github.cunvoas.geoserviceisochrone.model.analytics.StatsSeuilOmsEnum;
import com.github.cunvoas.geoserviceisochrone.model.analytics.StatsSurface;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.analytics.StatsSurfaceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StatsSurfaceService {
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public StatsSurfaceService() {
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
	private StatsSurfaceRepository statsSurfaceRepository;
	@Autowired
	private ServiceOpenData serviceOpenData;
	@Autowired
	private CityRepository cityRepository;
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	
	
	private static int[] seuilDense= {0,3,7,10,12};
	private static int[] seuilSuburbs= {0,8,17,25,45};
	private static String[] colors= {"#0000e8","#6060e8","#b0b0e8","#57ee17","#578817"};

	private static String[] txtSeuilOMS= {
			"Non respect des minimas OMS",
			"Minimim conseillé par l'OMS",
			"Préconisé par l'OMS" };
	
	private static String[] txtSeuil= {"","","","",""};
	
	public void writeStatsSurfaceByCom2CoIdAndAnnee(Long com2coId, Integer annee) throws IOException {
		CommunauteCommune com2co = communauteCommuneRepository.getReferenceById(com2coId);
		if (com2co!=null) {
			List<City> cities = cityRepository.findByCommunauteCommuneId(com2coId);
			for (City city : cities) {
				try {
					log.info("Stats surface for city {} / {}", city.getInseeCode(), city.getName());
					StatsSurfaceJson stat =  getStatsSurfaceByInseeAndAnnee(city.getInseeCode(), annee);
					
					
					String dept = city.getInseeCode().substring(0, 2);
					String sPath = applicationBusinessProperties.getJsonFileFolder()+"/data/stats/"+dept+"/"+city.getInseeCode();
					
					File file = new File(sPath);
					file.mkdirs();
					
					file = new File(sPath+"/stats_"+city.getInseeCode()+"_"+String.valueOf(annee)+".json");
					objectMapper.writeValue(file, stat);
					
				} catch (JsonProcessingException e) {
					log.error("Error processing stats surface for city {} / {} : {}", city.getInseeCode(), city.getName(), e.getMessage());
				}
			}
		}
	}
	
	public StatsSurfaceJson getStatsSurfaceByCom2CoAndAnneeAllDense(Integer annee, Long com2CoId) throws StreamWriteException, DatabindException, IOException {
		StatsSurfaceJson ret = new StatsSurfaceJson();
		
		ret.setAnnee(String.valueOf(annee));
		ret.setInsee("C2C_dense_"+String.valueOf(annee)+"_"+String.valueOf(com2CoId));
		
		Optional<CommunauteCommune> opt=communauteCommuneRepository.findById(com2CoId);
		if (opt.isEmpty()) {
			log.warn("Communauté de commune with id {} not found", com2CoId);
			return null;
		} else {	
			ret.setNom(opt.get().getName() +" - Urbain");
			
			List<StatsSurface> stats = statsSurfaceRepository.getStatsForCom2CoDense(annee, com2CoId);
			this.populateSurface(ret, stats, seuilDense);
			
			
			String sPath = applicationBusinessProperties.getJsonFileFolder()+"/data/stats/com2co/"+String.valueOf(com2CoId);
			File file = new File(sPath);
			file.mkdirs();
			
			file = new File(sPath+"/stats_c2c_"+String.valueOf(com2CoId)+"_urbans_"+String.valueOf(annee)+".json");
			objectMapper.writeValue(file, ret);
		}
		return ret;
	}
	
	public StatsSurfaceJson getStatsSurfaceByCom2CoAndAnneeAllSuburbs(Integer annee, Long com2CoId) throws StreamWriteException, DatabindException, IOException {
		StatsSurfaceJson ret = new StatsSurfaceJson();
		ret.setAnnee(String.valueOf(annee));
		ret.setInsee("C2C_suburbs_"+String.valueOf(annee)+"_"+String.valueOf(com2CoId));
		Optional<CommunauteCommune> opt=communauteCommuneRepository.findById(com2CoId);
		if (opt.isEmpty()) {
			log.warn("Communauté de commune with id {} not found", com2CoId);
			return null;
		} else {
			ret.setNom(opt.get().getName() + " - Périurbain");
			
			List<StatsSurface> stats = statsSurfaceRepository.getStatsForCom2CoSubUrbs(annee, com2CoId);
			this.populateSurface(ret, stats, seuilSuburbs);
			

			String sPath = applicationBusinessProperties.getJsonFileFolder()+"/data/stats/com2co/"+String.valueOf(com2CoId);
			File file = new File(sPath);
			file.mkdirs();
			
			file = new File(sPath+"/stats_c2c_"+String.valueOf(com2CoId)+"_suburbs_"+String.valueOf(annee)+".json");
			objectMapper.writeValue(file, ret);
		}
		return ret;
	}

	static List<StatsSurface> setupSurfaceAll() {
		List<StatsSurface> stats=new ArrayList<>();

		StatsSurface stat0= new StatsSurface();
		stat0.setPopulationInclue(BigDecimal.ZERO);
		stat0.setPopulationExclue(BigDecimal.ZERO);
		stat0.setSeuil(StatsSeuilOmsEnum.INSUFFISANT);
		stats.add(stat0);

		StatsSurface stat1= new StatsSurface();
		stat1.setPopulationInclue(BigDecimal.ZERO);
		stat1.setPopulationExclue(BigDecimal.ZERO);
		stat1.setSeuil(StatsSeuilOmsEnum.MINIMUM);
		stats.add(stat1);

		StatsSurface stat2= new StatsSurface();
		stat2.setPopulationInclue(BigDecimal.ZERO);
		stat2.setPopulationExclue(BigDecimal.ZERO);
		stat2.setSeuil(StatsSeuilOmsEnum.PRECONISE);
		stats.add(stat2);
		
		return stats;
	}
	
	public StatsSurfaceJson getStatsSurfaceByCom2CoAndAnneeAllV2(Integer annee, Long com2CoId) throws StreamWriteException, DatabindException, IOException {
		StatsSurfaceJson ret = new StatsSurfaceJson();
		ret.setAnnee(String.valueOf(annee));
		ret.setInsee("C2C_all_"+String.valueOf(annee)+"_"+String.valueOf(com2CoId));
		Optional<CommunauteCommune> opt=communauteCommuneRepository.findById(com2CoId);
		if (opt.isEmpty()) {
			log.warn("Communauté de commune with id {} not found", com2CoId);
			return null;
		} else {
			ret.setNom(opt.get().getName() + " - Dense et Périurbain");
			
			List<StatsSurface> statsDense = statsSurfaceRepository.getStatsForCom2CoDense(annee, com2CoId);
			List<StatsSurface> statsSubrubs = statsSurfaceRepository.getStatsForCom2CoSubUrbs(annee, com2CoId);
			
			
			// fusion des listes
			List<StatsSurface> stats = setupSurfaceAll();
			for (StatsSurface statsSurface : stats) {
				for (StatsSurface stat : statsDense) {
					if (statsSurface.getSeuil().equals(stat.getSeuil())) {
						statsSurface.setAnnee(annee);
						statsSurface.setPopulationInclue(statsSurface.getPopulationInclue().add(stat.getPopulationInclue()));
						statsSurface.setPopulationExclue(statsSurface.getPopulationExclue().add(stat.getPopulationExclue()));
					}
				}
				for (StatsSurface stat : statsSubrubs) {
					if (statsSurface.getSeuil().equals(stat.getSeuil())) {
						statsSurface.setAnnee(annee);
						statsSurface.setPopulationInclue(statsSurface.getPopulationInclue().add(stat.getPopulationInclue()));
						statsSurface.setPopulationExclue(statsSurface.getPopulationExclue().add(stat.getPopulationExclue()));
					}
				}
				
			}
			
			this.populateAll(ret, stats, txtSeuilOMS);
			
			
			String sPath = applicationBusinessProperties.getJsonFileFolder()+"/data/stats/com2co/"+String.valueOf(com2CoId);
			File file = new File(sPath);
			file.mkdirs();
			
			file = new File(sPath+"/stats_c2c_"+String.valueOf(com2CoId)+"_all_"+String.valueOf(annee)+".json");
			objectMapper.writeValue(file, ret);
		}
		return ret;
	}
		
	@Deprecated
	public StatsSurfaceJson getStatsSurfaceByCom2CoAndAnneeAll(Integer annee, Long com2CoId) throws StreamWriteException, DatabindException, IOException {
		StatsSurfaceJson ret = new StatsSurfaceJson();
		ret.setAnnee(String.valueOf(annee));
		ret.setInsee("C2C_all_"+String.valueOf(annee)+"_"+String.valueOf(com2CoId));
		Optional<CommunauteCommune> opt=communauteCommuneRepository.findById(com2CoId);
		if (opt.isEmpty()) {
			log.warn("Communauté de commune with id {} not found", com2CoId);
			return null;
		} else {
			ret.setNom(opt.get().getName() + " - Dense et Périurbain");
			
			List<StatsSurface> stats = statsSurfaceRepository.getStatsForCom2Co(annee, com2CoId);
			this.populateAll(ret, stats, txtSeuilOMS);
			
			
			String sPath = applicationBusinessProperties.getJsonFileFolder()+"/data/stats/com2co/"+String.valueOf(com2CoId);
			File file = new File(sPath);
			file.mkdirs();
			
			file = new File(sPath+"/stats_c2c_"+String.valueOf(com2CoId)+"_all_"+String.valueOf(annee)+".json");
			objectMapper.writeValue(file, ret);
		}
		return ret;
	}
	
	public StatsSurfaceJson getStatsSurfaceByInseeAndAnnee(String insee, Integer annee) {
		StatsSurfaceJson ret = new StatsSurfaceJson();
		ret.setAnnee(String.valueOf(annee));
		ret.setInsee(insee);
		
		City city = cityRepository.findByInseeCode(insee);
		if (city!=null) {
			ret.setNom(city.getName());
		}
		
		Boolean dense= serviceOpenData.isDistanceDense(insee);
		List<StatsSurface> stats =null;
		int[] seuils= {};
		if (dense) {
			 stats = statsSurfaceRepository.getStatsForCity(annee, "%"+insee+"%");
			 seuils=  seuilDense;
		} else {
			 stats = statsSurfaceRepository.getStatsForSuburbs(annee, "%"+insee+"%");
			 seuils=  seuilSuburbs;
		}
		this.populateSurface(ret, stats, seuils);
		return ret;
	}
	
	/**
	 * @param json
	 * @param stats
	 * @param seuils
	 * @Deprecated
	 */
	void populateAll(StatsSurfaceJson json, List<StatsSurface> stats, String[] seuils) {
		Integer populationTotalExclue = 0;
		Integer populationTotale = 0;
		
		for (StatsSurface statsSurface : stats) {
			Stat stat = new Stat();
			json.getStats().add(stat);
			
			stat.setHabitants(statsSurface.getPopulationInclue().intValue());
			populationTotalExclue += statsSurface.getPopulationExclue().intValue();
			
			populationTotale+=statsSurface.getPopulationInclue().intValue()+statsSurface.getPopulationExclue().intValue();

		}
		
		// ajout dans le premier lot >0 <seuil 1
		if (populationTotalExclue>0) {
			Stat stat0 = json.getStats().get(0);
			Integer cur = stat0.getHabitants();
			stat0.setHabitants(cur+populationTotalExclue);
		}
		
		int index=0;
		for (Stat stat : json.getStats()) {
			Seuil seuil = new Seuil();
			json.getSeuils().add(seuil);
			seuil.setSurface(txtSeuilOMS[index]);
			seuil.setBarColor(colors[index+2]);
			seuil.setHabitants(stat.getHabitants());
			seuil.setRatio(String.valueOf(100*stat.getHabitants().intValue()/populationTotale)+"");
			index++;
		}
		
	}
	
	/**
	 * @param json
	 * @param stats
	 * @param seuils
	 */
	void populateSurface(StatsSurfaceJson json, List<StatsSurface> stats, int[] seuils) {
		Iterator<StatsSurface> iter = stats.iterator();
		StatsSurface statsSurface = iter.next(); // premier élément de liste (0 à 5 éléments)
		Integer populationTotalExclue = 0;
		Integer populationTotale = 0;
		int fin=0;
		
		for (int i = 0; i < colors.length; i++) {
			
			
			
			int deb = seuils[i];
			boolean match=false;
			if (statsSurface!=null && statsSurface.getSurfaceMin()==deb) {
				match=true;
				log.warn("Seuils incohérents entre code et BDD pour la surface min : attendu {} / trouvé {}", deb, statsSurface.getSurfaceMin());
			}
			
			
			StringBuilder sb=new StringBuilder();
			sb.append("> ").append(deb);
			if (i==colors.length-1) {
				sb.append(" +");
				txtSeuil[i]= String.format(" >= %s m²/hab.", fin);
			} else {
				fin = seuils[i+1];
				sb.append("<=").append(fin);
				
				txtSeuil[i]= String.format("%s <=  surface/habitant < %s m²/hab.", deb, fin);
			}
			
			Stat stat = new Stat();
			json.getStats().add(stat);
			
			stat.setSurface(sb.toString());
			stat.setBarColor(colors[i]);
			
			if(match) {
				stat.setHabitants(statsSurface.getPopulationInclue().intValue());
				populationTotalExclue += statsSurface.getPopulationExclue().intValue();
				
				populationTotale+=statsSurface.getPopulationInclue().intValue()+statsSurface.getPopulationExclue().intValue();
			} else {
				stat.setHabitants(0);
			}
			
			if (match && iter.hasNext()) {
				statsSurface = iter.next();
			}
		}
		
		// ajout dans le premier lot >0 <seuil 1
		if (populationTotalExclue>0) {
			Stat stat0 = json.getStats().get(0);
			Integer cur = stat0.getHabitants();
			stat0.setHabitants(cur+populationTotalExclue);
		}
		
		int index=0;
		for (Stat stat : json.getStats()) {
			Seuil seuil = new Seuil();
			json.getSeuils().add(seuil);
			seuil.setSurface(txtSeuil[index]);
			seuil.setBarColor(colors[index]);
			seuil.setHabitants(stat.getHabitants());
			seuil.setRatio(String.valueOf(100*stat.getHabitants().intValue()/populationTotale)+"");
			index++;
		}
	}
	
	public String getStringStatsSurfaceByInseeAndAnnee(String insee, Integer annee) throws JsonProcessingException {
		StatsSurfaceJson obj = this.getStatsSurfaceByInseeAndAnnee(insee, annee);
		return objectMapper.writeValueAsString(obj);
	}
}