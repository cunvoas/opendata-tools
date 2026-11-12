package com.github.cunvoas.geoserviceisochrone.service.analytics;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.Seuil;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.Stat;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.StatsSurfaceJson;
import com.github.cunvoas.geoserviceisochrone.model.analytics.StatsSeuilOmsEnum;
import com.github.cunvoas.geoserviceisochrone.model.analytics.StatsSurface;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.analytics.StatsSurfaceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.exc.StreamWriteException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class StatsSurfaceService {

	private final ObjectMapper objectMapper;

	@Autowired
	public StatsSurfaceService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		configureObjectMapper();
	}

	private void configureObjectMapper() {
		// Configuration personnalisée si nécessaire
		objectMapper.rebuild()
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
			.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
			.disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
			.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
			.disable(DeserializationFeature.USE_NULL_FOR_MISSING_REFERENCE_VALUES)
		.build();
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
	private static String[] colors= {"#0000e8","#6060e8","#b0b0e8","#578817","#57ee17"};

	private static String[] txtSeuilOMS= {
			"Non respect des minima OMS",
			"Minimim conseillé par l'OMS",
			"Préconisé par l'OMS" };
	
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
					
				} catch (Exception e) {
					log.error("Error processing stats surface for city {} / {} : {}", city.getInseeCode(), city.getName(), e.getMessage());
				}
			}
		}
	}
	
	public StatsSurfaceJson getStatsSurfaceByCom2CoAndAnneeAllDense(Integer annee, Long com2CoId) throws StreamWriteException, DatabindException, IOException {
		StatsSurfaceJson ret = computeStatsSurfaceByCom2CoAndAnneeAllDense(annee, com2CoId);
		if (ret != null) {
			String sPath = applicationBusinessProperties.getJsonFileFolder()+"/data/stats/com2co/"+String.valueOf(com2CoId);
			File file = new File(sPath);
			file.mkdirs();

			file = new File(sPath+"/stats_c2c_"+String.valueOf(com2CoId)+"_urbans_"+String.valueOf(annee)+".json");
			objectMapper.writeValue(file, ret);
		}
		return ret;
	}

	/**
	 * Statistiques urbaines d'un EPCI, sans écriture de fichier.
	 * @return statistiques, null si l'EPCI est inconnu
	 * @throws NoStatsException si l'EPCI n'a aucune ligne de stats pour l'année
	 */
	public StatsSurfaceJson computeStatsSurfaceByCom2CoAndAnneeAllDense(Integer annee, Long com2CoId) {
		StatsSurfaceJson ret = new StatsSurfaceJson();
		ret.setAnnee(String.valueOf(annee));
		ret.setInsee("C2C_dense_"+String.valueOf(annee)+"_"+String.valueOf(com2CoId));

		Optional<CommunauteCommune> opt=communauteCommuneRepository.findById(com2CoId);
		if (opt.isEmpty()) {
			log.warn("Communauté de commune with id {} not found", com2CoId);
			return null;
		}
		ret.setNom(opt.get().getName() +" - Urbain");

		List<StatsSurface> stats = statsSurfaceRepository.getStatsForCom2CoDense(annee, com2CoId);
		if (stats.isEmpty()) {
			throw new NoStatsException("EPCI " + com2CoId + " (urbain) " + annee);
		}
		this.populateSurface(ret, stats, seuilDense);
		return ret;
	}

	public StatsSurfaceJson getStatsSurfaceByCom2CoAndAnneeAllSuburbs(Integer annee, Long com2CoId) throws StreamWriteException, DatabindException, IOException {
		StatsSurfaceJson ret = computeStatsSurfaceByCom2CoAndAnneeAllSuburbs(annee, com2CoId);
		if (ret != null) {
			String sPath = applicationBusinessProperties.getJsonFileFolder()+"/data/stats/com2co/"+String.valueOf(com2CoId);
			File file = new File(sPath);
			file.mkdirs();

			file = new File(sPath+"/stats_c2c_"+String.valueOf(com2CoId)+"_suburbs_"+String.valueOf(annee)+".json");
			objectMapper.writeValue(file, ret);
		}
		return ret;
	}

	/**
	 * Statistiques périurbaines d'un EPCI, sans écriture de fichier.
	 * @return statistiques, null si l'EPCI est inconnu
	 * @throws NoStatsException si l'EPCI n'a aucune ligne de stats pour l'année
	 */
	public StatsSurfaceJson computeStatsSurfaceByCom2CoAndAnneeAllSuburbs(Integer annee, Long com2CoId) {
		StatsSurfaceJson ret = new StatsSurfaceJson();
		ret.setAnnee(String.valueOf(annee));
		ret.setInsee("C2C_suburbs_"+String.valueOf(annee)+"_"+String.valueOf(com2CoId));
		Optional<CommunauteCommune> opt=communauteCommuneRepository.findById(com2CoId);
		if (opt.isEmpty()) {
			log.warn("Communauté de commune with id {} not found", com2CoId);
			return null;
		}
		ret.setNom(opt.get().getName() + " - Périurbain");

		List<StatsSurface> stats = statsSurfaceRepository.getStatsForCom2CoSubUrbs(annee, com2CoId);
		if (stats.isEmpty()) {
			throw new NoStatsException("EPCI " + com2CoId + " (périurbain) " + annee);
		}
		this.populateSurface(ret, stats, seuilSuburbs);
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
		StatsSurfaceJson ret = computeStatsSurfaceByCom2CoAndAnneeAllV2(annee, com2CoId);
		if (ret != null) {
			String sPath = applicationBusinessProperties.getJsonFileFolder()+"/data/stats/com2co/"+String.valueOf(com2CoId);
			File file = new File(sPath);
			file.mkdirs();

			file = new File(sPath+"/stats_c2c_"+String.valueOf(com2CoId)+"_all_"+String.valueOf(annee)+".json");
			objectMapper.writeValue(file, ret);
		}
		return ret;
	}

	/**
	 * Statistiques urbaines et périurbaines d'un EPCI, sans écriture de fichier.
	 * @return statistiques, null si l'EPCI est inconnu
	 * @throws NoStatsException si l'EPCI n'a aucune ligne de stats pour l'année
	 */
	public StatsSurfaceJson computeStatsSurfaceByCom2CoAndAnneeAllV2(Integer annee, Long com2CoId) {
		StatsSurfaceJson ret = new StatsSurfaceJson();
		ret.setAnnee(String.valueOf(annee));
		ret.setInsee("C2C_all_"+String.valueOf(annee)+"_"+String.valueOf(com2CoId));
		Optional<CommunauteCommune> opt=communauteCommuneRepository.findById(com2CoId);
		if (opt.isEmpty()) {
			log.warn("Communauté de commune with id {} not found", com2CoId);
			return null;
		}
		ret.setNom(opt.get().getName() + " - Dense et Périurbain");

		List<StatsSurface> statsDense = statsSurfaceRepository.getStatsForCom2CoDense(annee, com2CoId);
		List<StatsSurface> statsSubrubs = statsSurfaceRepository.getStatsForCom2CoSubUrbs(annee, com2CoId);
		if (statsDense.isEmpty() && statsSubrubs.isEmpty()) {
			throw new NoStatsException("EPCI " + com2CoId + " (tous) " + annee);
		}

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
			 stats = statsSurfaceRepository.getStatsForCity(annee, insee);
			 seuils=  seuilDense;
		} else {
			 stats = statsSurfaceRepository.getStatsForSuburbs(annee, insee);
			 seuils=  seuilSuburbs;
		}
		if (stats.isEmpty()) {
			throw new NoStatsException(insee);
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
		int populationTotalExclue = 0;
		int populationTotale = 0;
		
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
			seuil.setSeuilId(index);
			seuil.setBarColor(colors[index+2]);
			seuil.setHabitants(stat.getHabitants());
			seuil.setRatio(pourcent(stat.getHabitants(), populationTotale));
			index++;
		}
		
	}
	
	/**
	 * Remplit le JSON à partir des lignes de statistiques, une par tranche non vide.
	 * <p>
	 * Les tranches sont [seuil, seuil suivant[ : minimum inclus, maximum exclu. Les lignes sont retrouvées par
	 * leur borne minimum, pas par leur position. Les habitants sans accès à un parc OMS ({@code pop_exc}, toutes
	 * tranches confondues) sont comptés dans la première tranche, avec ceux qui disposent de très peu de m².
	 * </p>
	 * @param json JSON à remplir
	 * @param stats lignes de statistiques
	 * @param seuils bornes minimum des cinq tranches
	 */
	void populateSurface(StatsSurfaceJson json, List<StatsSurface> stats, int[] seuils) {
		Map<Integer, StatsSurface> parBorne = new HashMap<>();
		for (StatsSurface row : stats) {
			parBorne.put(row.getSurfaceMin(), row);
		}
		String[] txtSeuil = new String[colors.length];
		int populationTotalExclue = 0;
		int populationTotale = 0;

		for (int i = 0; i < colors.length; i++) {
			int deb = seuils[i];
			boolean derniere = i == colors.length - 1;
			int fin = derniere ? deb : seuils[i + 1];

			Stat stat = new Stat();
			json.getStats().add(stat);
			stat.setSurface(derniere ? deb + " et +" : deb + " à <" + fin);
			stat.setBarColor(colors[i]);
			txtSeuil[i] = derniere
					? String.format(" >= %s m²/hab.", deb)
					: String.format("%s <=  surface/habitant < %s m²/hab.", deb, fin);

			StatsSurface row = parBorne.remove(deb);
			if (row == null) {
				stat.setHabitants(0);
			} else {
				stat.setHabitants(row.getPopulationInclue().intValue());
				populationTotalExclue += row.getPopulationExclue().intValue();
				populationTotale += row.getPopulationInclue().intValue() + row.getPopulationExclue().intValue();
			}
		}
		if (!parBorne.isEmpty()) {
			log.warn("Tranches ignorées, bornes absentes des seuils attendus {} : {}", Arrays.toString(seuils), parBorne.keySet());
		}

		// ajout dans la première tranche : habitants sans accès à un parc
		if (populationTotalExclue > 0) {
			Stat stat0 = json.getStats().get(0);
			stat0.setHabitants(stat0.getHabitants() + populationTotalExclue);
		}

		int index = 0;
		for (Stat stat : json.getStats()) {
			Seuil seuil = new Seuil();
			json.getSeuils().add(seuil);
			seuil.setSurface(txtSeuil[index]);
			seuil.setBarColor(colors[index]);
			seuil.setHabitants(stat.getHabitants());
			seuil.setRatio(pourcent(stat.getHabitants(), populationTotale));
			index++;
		}
	}

	/**
	 * Part en pourcentage, à une décimale.
	 * @param habitants effectif de la tranche
	 * @param total effectif total
	 * @return par exemple "44.4", "0.0" si le total est nul
	 */
	static String pourcent(int habitants, int total) {
		return total == 0 ? "0.0" : String.format(Locale.ROOT, "%.1f", 100.0 * habitants / total);
	}

	public String getStringStatsSurfaceByInseeAndAnnee(String insee, Integer annee) {
		StatsSurfaceJson obj = this.getStatsSurfaceByInseeAndAnnee(insee, annee);
		return objectMapper.writeValueAsString(obj);
	}
}