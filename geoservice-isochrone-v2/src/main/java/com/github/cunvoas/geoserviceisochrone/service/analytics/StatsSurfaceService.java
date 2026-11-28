package com.github.cunvoas.geoserviceisochrone.service.analytics;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.Seuil;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.StatsDescripteur;
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
	
	
	// @Ref seuils-12-45 : les bornes 12 et 45 m²/hab. et les libellés « conseillé / préconisé par l'OMS » n'ont pas de source identifiée (docs/stats-surface.md).
	private static int[] seuilDense= {0,3,7,10,12};
	private static int[] seuilSuburbs= {0,8,17,25,45};
	private static String[] colors= {"#0000e8","#6060e8","#b0b0e8","#578817","#57ee17"};

	private static String[] txtSeuilOMS= {
			"Non respect des minima OMS",
			"Minimim conseillé par l'OMS",
			"Préconisé par l'OMS" };

	/** Version du contrat JSON v2 publié à côté de l'ancien format. */
	public static final String VERSION_STATS = "2.0";
	
	/** Callback de progression pour la boucle des villes. */
	@FunctionalInterface
	public interface CityProgress {
		void onCity(int index, int total, City city);
	}

	public void writeStatsSurfaceByCom2CoIdAndAnnee(Long com2coId, Integer annee) throws IOException {
		writeStatsSurfaceByCom2CoIdAndAnnee(com2coId, annee, null);
	}

	public void writeStatsSurfaceByCom2CoIdAndAnnee(Long com2coId, Integer annee, CityProgress progress) throws IOException {
		CommunauteCommune com2co = communauteCommuneRepository.getReferenceById(com2coId);
		if (com2co!=null) {
			List<City> cities = cityRepository.findByCommunauteCommuneId(com2coId);
			for (int i = 0; i < cities.size(); i++) {
				City city = cities.get(i);
				if (progress != null) {
					progress.onCity(i + 1, cities.size(), city);
				}
				try {
					log.info("Stats surface for city {} / {}", city.getInseeCode(), city.getName());
					StatsSurfaceJson stat =  getStatsSurfaceByInseeAndAnnee(city.getInseeCode(), annee);
					
					
					String dept = city.getInseeCode().substring(0, 2);
					String sPath = applicationBusinessProperties.getJsonFileFolder()+"/data/stats/"+dept+"/"+city.getInseeCode();
					
					File file = new File(sPath);
					file.mkdirs();
					
					file = new File(sPath+"/stats_"+city.getInseeCode()+"_"+String.valueOf(annee)+".json");
					stat.setDeprecated(Boolean.TRUE);
					objectMapper.writeValue(file, stat);
					writeStatsV2(stat, "commune", city.getInseeCode(), null,
							new File(sPath), "stats_"+city.getInseeCode()+"_"+String.valueOf(annee));
					
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
			ret.setDeprecated(Boolean.TRUE);
			objectMapper.writeValue(file, ret);
			writeStatsV2(ret, "epci", null, sirenEpci(com2CoId), new File(sPath),
					"stats_c2c_"+String.valueOf(com2CoId)+"_urbans_"+String.valueOf(annee));
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
		ret.setDescripteur(descripteur(statsSurfaceRepository.getDescripteurForCom2CoDense(annee, com2CoId)));
		return ret;
	}

	public StatsSurfaceJson getStatsSurfaceByCom2CoAndAnneeAllSuburbs(Integer annee, Long com2CoId) throws StreamWriteException, DatabindException, IOException {
		StatsSurfaceJson ret = computeStatsSurfaceByCom2CoAndAnneeAllSuburbs(annee, com2CoId);
		if (ret != null) {
			String sPath = applicationBusinessProperties.getJsonFileFolder()+"/data/stats/com2co/"+String.valueOf(com2CoId);
			File file = new File(sPath);
			file.mkdirs();

			file = new File(sPath+"/stats_c2c_"+String.valueOf(com2CoId)+"_suburbs_"+String.valueOf(annee)+".json");
			ret.setDeprecated(Boolean.TRUE);
			objectMapper.writeValue(file, ret);
			writeStatsV2(ret, "epci", null, sirenEpci(com2CoId), new File(sPath),
					"stats_c2c_"+String.valueOf(com2CoId)+"_suburbs_"+String.valueOf(annee));
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
		ret.setDescripteur(descripteur(statsSurfaceRepository.getDescripteurForCom2CoSubUrbs(annee, com2CoId)));
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
			ret.setDeprecated(Boolean.TRUE);
			objectMapper.writeValue(file, ret);
			writeStatsV2(ret, "epci", null, sirenEpci(com2CoId), new File(sPath),
					"stats_c2c_"+String.valueOf(com2CoId)+"_all_"+String.valueOf(annee));
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
		ret.setDescripteur(descripteur(statsSurfaceRepository.getDescripteurForCom2CoAll(annee, com2CoId)));
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
		ret.setDescripteur(descripteur(statsSurfaceRepository.getDescripteurForCity(annee, insee)));
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

	/** Numéro Siren d'un EPCI, ou null s'il n'est pas renseigné. */
	private String sirenEpci(Long com2CoId) {
		return communauteCommuneRepository.findById(com2CoId)
				.map(CommunauteCommune::getSiren)
				.orElse(null);
	}

	/**
	 * Écrit le contrat JSON v2 à côté du fichier historique, sans le remplacer.
	 * Le fichier v2 ne porte ni couleur ni libellé d'affichage : seulement les identifiants
	 * officiels, des bornes numériques et la version du contrat.
	 * @param json données historiques
	 * @param typeTerritoire `commune` ou `epci`
	 * @param codeInsee code officiel géographique pour une commune
	 * @param siren numéro Siren pour un EPCI
	 * @param folder dossier de publication
	 * @param baseName nom du fichier historique, sans extension
	 * @throws IOException en cas d'erreur d'écriture
	 */
	void writeStatsV2(StatsSurfaceJson json, String typeTerritoire, String codeInsee, String siren,
			File folder, String baseName) throws IOException {
		Map<String, Object> v2 = new LinkedHashMap<>();
		v2.put("version", VERSION_STATS);
		v2.put("typeTerritoire", typeTerritoire);
		if ("commune".equals(typeTerritoire)) {
			v2.put("codeInsee", codeInsee);
		} else {
			v2.put("siren", siren);
		}
		v2.put("nom", json.getNom());
		v2.put("annee", json.getAnnee());
		v2.put("population", population(json));
		v2.put("miseAJour", LocalDate.now(ZoneOffset.UTC).toString());
		v2.put("stats", tranchesV2(json.getStats(), json.getSeuils()));
		v2.put("seuils", seuilsV2(json.getSeuils()));
		if (json.getDescripteur() != null) {
			v2.put("descripteur", json.getDescripteur());
		}
		objectMapper.writerWithDefaultPrettyPrinter()
				.writeValue(new File(folder, baseName + ".v2.json"), v2);
	}

	private static int population(StatsSurfaceJson json) {
		return json.getStats().stream()
				.mapToInt(stat -> stat.getHabitants() == null ? 0 : stat.getHabitants())
				.sum();
	}

	private static List<Map<String, Object>> tranchesV2(List<Stat> stats, List<Seuil> seuils) {
		List<Map<String, Object>> tranches = new ArrayList<>();
		for (int i = 0; i < stats.size(); i++) {
			Stat stat = stats.get(i);
			Map<String, Object> tranche = new LinkedHashMap<>();
			Seuil seuil = i < seuils.size() ? seuils.get(i) : null;
			ajouterIdentiteTranche(tranche, seuil);
			tranche.put("habitants", stat.getHabitants() == null ? 0 : stat.getHabitants());
			tranches.add(tranche);
		}
		return tranches;
	}

	private static List<Map<String, Object>> seuilsV2(List<Seuil> seuils) {
		int total = seuils.stream().mapToInt(seuil -> seuil.getHabitants() == null ? 0 : seuil.getHabitants()).sum();
		List<Map<String, Object>> tranches = new ArrayList<>();
		for (Seuil seuil : seuils) {
			Map<String, Object> tranche = new LinkedHashMap<>();
			ajouterIdentiteTranche(tranche, seuil);
			int habitants = seuil.getHabitants() == null ? 0 : seuil.getHabitants();
			tranche.put("habitants", habitants);
			tranche.put("pourcentage", total == 0 ? 0.0 : Math.round(1000.0 * habitants / total) / 10.0);
			tranches.add(tranche);
		}
		return tranches;
	}

	private static void ajouterIdentiteTranche(Map<String, Object> cible, Seuil seuil) {
		if (seuil == null) {
			return;
		}
		Integer[] bornes = bornes(seuil.getSurface());
		if (bornes != null) {
			if (bornes[0] != null) {
				cible.put("borneMin", bornes[0]);
			}
			if (bornes[1] != null) {
				cible.put("borneMax", bornes[1]);
			}
			cible.put("unite", "m2/hab");
		}
		String niveau = niveauOms(seuil.getSurface());
		if (niveau != null) {
			cible.put("niveauOms", niveau);
		}
	}

	/**
	 * Bornes numériques d'un libellé de tranche historique.
	 * @param surface libellé (« 0 <= ... < 3 m²/hab. », « > 0<=3 », « >= 12 m²/hab. »)
	 * @return [borneMin, borneMax], l'une des deux pouvant être nulle ; null si aucun nombre.
	 */
	static Integer[] bornes(String surface) {
		if (surface == null) {
			return null;
		}
		List<Double> nombres = new ArrayList<>();
		Matcher matcher = Pattern.compile("\\d+(?:[.,]\\d+)?").matcher(surface);
		while (matcher.find()) {
			nombres.add(Double.parseDouble(matcher.group().replace(',', '.')));
		}
		if (nombres.isEmpty()) {
			return null;
		}
		if (nombres.size() >= 2) {
			return new Integer[] { nombres.get(0).intValue(), nombres.get(1).intValue() };
		}
		if (surface.trim().startsWith("<")) {
			return new Integer[] { null, nombres.get(0).intValue() };
		}
		return new Integer[] { nombres.get(0).intValue(), null };
	}

	private static String niveauOms(String surface) {
		if (surface == null) {
			return null;
		}
		if (surface.matches("(?i).*non\\s*respect.*")) {
			return "INSUFFISANT";
		}
		if (surface.matches("(?i).*minim\\w*\\s*conseill.*")) {
			return "MINIMUM";
		}
		if (surface.matches("(?i).*pr[ée]conis.*")) {
			return "PRECONISE";
		}
		return null;
	}

	/**
	 * Résume un territoire à partir des sommes calculées en base, sans passer par les tranches.
	 * <ul>
	 *   <li>{@code m2ParHabitantMoyen} = somme(population carreau x m²/habitant du carreau) / somme(population carreau) ;</li>
	 *   <li>{@code m2ParHabitantGlobal} = somme(surface des parcs OMS) / population totale.</li>
	 * </ul>
	 * Les divisions portent sur les sommes exactes ; l'arrondi à deux décimales n'est fait qu'à la fin.
	 * @param lignes résultat de la requête : population, somme(population x m²/hab.), surface des parcs OMS
	 * @return le résumé, null si le territoire n'a pas de population
	 */
	static StatsDescripteur descripteur(List<Object[]> lignes) {
		if (lignes == null || lignes.isEmpty() || lignes.get(0) == null) {
			return null;
		}
		Object[] ligne = lignes.get(0);
		BigDecimal population = enDecimal(ligne[0]);
		if (population.signum() <= 0) {
			return null;
		}
		BigDecimal ponderes = enDecimal(ligne[1]);
		BigDecimal surface = enDecimal(ligne[2]);

		StatsDescripteur descripteur = new StatsDescripteur();
		descripteur.setHabitants(population.setScale(0, RoundingMode.HALF_UP).longValue());
		descripteur.setM2ParHabitantMoyen(ponderes.divide(population, 2, RoundingMode.HALF_UP).doubleValue());
		descripteur.setSurfaceParcsOmsM2(surface.setScale(0, RoundingMode.HALF_UP).longValue());
		descripteur.setM2ParHabitantGlobal(surface.divide(population, 2, RoundingMode.HALF_UP).doubleValue());
		return descripteur;
	}

	private static BigDecimal enDecimal(Object valeur) {
		if (valeur instanceof BigDecimal decimal) {
			return decimal;
		}
		return valeur instanceof Number nombre ? BigDecimal.valueOf(nombre.doubleValue()) : BigDecimal.ZERO;
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