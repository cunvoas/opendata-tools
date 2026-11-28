package com.github.cunvoas.geoserviceisochrone.service.export.dashboard;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GitHelper;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Lecture de l'état des exports et commit final unique.
 * Le dashboard ne modifie aucun fichier de données.
 */
@Service
@Slf4j
public class ExportDashboardService {

	@Autowired
	private GitHelper gitHelper;

	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;

	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;

	public ExportDashboard scan() {
		return scan(null);
	}

	/**
	 * @param epciFilter null pour un administrateur, EPCI du gestionnaire sinon
	 */
	public ExportDashboard scan(Long epciFilter) {
		return scan(epciFilter, null, null, null);
	}

	/**
	 * @param epciFilter null pour un administrateur, EPCI du gestionnaire sinon
	 * @param page page demandée (null = toutes les lignes, pour le commit)
	 * @param size taille de page (null = toutes)
	 */
	public ExportDashboard scan(Long epciFilter, Integer page, Integer size) {
		return scan(epciFilter, page, size, null);
	}

	/**
	 * @param epciFilter null pour un administrateur, EPCI du gestionnaire sinon
	 * @param page page demandée (null = toutes les lignes, pour le commit)
	 * @param size taille de page (null = toutes)
	 * @param yearFilter année affichée (null = toutes)
	 */
	public ExportDashboard scan(Long epciFilter, Integer page, Integer size, Integer yearFilter) {
		ExportDashboard dashboard = new ExportDashboard();
		String root = repoRoot();
		dashboard.setRepoPath(root);
		dashboard.setScopeEpciId(epciFilter);
		dashboard.setYearFilter(yearFilter);
		try {
			GitHelper.RepoState state = gitHelper.state(root);
			dashboard.setRepositoryFound(state.repositoryFound());
			dashboard.setHead(state.head());
			dashboard.setBranch(state.branch());
			List<String> changed = gitHelper.changedPaths(root).stream()
					.filter(this::isExportPath)
					.filter(path -> isPathForEpci(path, epciFilter))
					.toList();
			dashboard.setChangedFiles(new ArrayList<>(changed));
			dashboard.setSnapshotHash(snapshotHash(root, state.head(), changed));
			List<CommunauteCommune> epcis = loadEpcis(dashboard, epciFilter, page, size);
			buildEpciStates(dashboard, root, epcis, yearFilter);
			buildBlockers(dashboard, root, changed, epciFilter);
		} catch (Exception e) {
			log.error("Dashboard export : lecture impossible", e);
			dashboard.getBlockers().add("Lecture du dépôt impossible : " + e.getMessage());
		}
		return dashboard;
	}

	private List<CommunauteCommune> loadEpcis(ExportDashboard dashboard, Long epciFilter, Integer page, Integer size) {
		if (epciFilter != null) {
			CommunauteCommune epci = communauteCommuneRepository.findById(epciFilter).orElse(null);
			dashboard.setTotalEpcis(epci == null ? 0 : 1);
			dashboard.setPage(0);
			dashboard.setPageSize(1);
			if (epci != null) {
				dashboard.setScopeLabel(epci.getName() + " (#" + epci.getId() + ")");
			}
			return epci == null ? List.of() : List.of(epci);
		}
		if (page != null && size != null) {
			org.springframework.data.domain.Page<CommunauteCommune> result =
					communauteCommuneRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size));
			if (result.getTotalPages() > 0 && page >= result.getTotalPages()) {
				result = communauteCommuneRepository.findAll(
						org.springframework.data.domain.PageRequest.of(result.getTotalPages() - 1, size));
			}
			dashboard.setPage(result.getNumber());
			dashboard.setPageSize(result.getSize());
			dashboard.setTotalEpcis(result.getTotalElements());
			return result.getContent();
		}
		List<CommunauteCommune> all = communauteCommuneRepository.findAll();
		dashboard.setPage(0);
		dashboard.setPageSize(all.size());
		dashboard.setTotalEpcis(all.size());
		return all;
	}

	/**
	 * Commit final d'une liste exacte de fichiers.
	 * @param snapshotHash hash retourné par {@link #scan()}
	 * @param message message optionnel
	 * @return résultat du commit
	 */
	public GitHelper.CommitResult commit(String snapshotHash, String message) {
		return commit(snapshotHash, message, null);
	}

	public GitHelper.CommitResult commit(String snapshotHash, String message, Long epciFilter) {
		ExportDashboard current = scan(epciFilter, 0, 1);
		if (snapshotHash == null || !snapshotHash.equals(current.getSnapshotHash())) {
			throw new IllegalStateException("Le dépôt a changé depuis le chargement du dashboard. Rafraîchissez avant de commiter.");
		}
		if (!current.getBlockers().isEmpty()) {
			throw new IllegalStateException("Commit refusé : " + String.join(" ; ", current.getBlockers()));
		}
		if (current.getChangedFiles().isEmpty()) {
			throw new IllegalStateException("Aucun fichier d'export à commiter.");
		}
		String commitMessage = (message == null || message.isBlank())
				? "chore(data): publication " + current.getChangedFiles().size() + " fichier(s) "
						+ LocalDateTime.now()
				: message;
		try {
			return gitHelper.commitFiles(current.getRepoPath(), current.getChangedFiles(), commitMessage);
		} catch (Exception e) {
			log.error("Dashboard export : commit impossible", e);
			throw new IllegalStateException("Commit impossible : " + e.getMessage(), e);
		}
	}

	private void buildEpciStates(ExportDashboard dashboard, String root, List<CommunauteCommune> epcis, Integer yearFilter) {
		Integer[] years = applicationBusinessProperties.getInseeAnnees();
		if (years == null) {
			return;
		}
		for (CommunauteCommune com2co : epcis) {
			List<City> cities = com2co.getCities() == null ? List.of() : com2co.getCities();
			for (Integer year : years) {
				if (yearFilter != null && !yearFilter.equals(year)) {
					continue;
				}
				ExportDashboard.EpciState state = new ExportDashboard.EpciState();
				state.setId(com2co.getId());
				state.setName(com2co.getName());
				state.setSiren(com2co.getSiren());
				state.setYear(year);
				state.setReferential(referentialExists(root, com2co));
				state.setTerritoires(exists(root, "data/territoires.json"));
				state.setStatsUrbans(exists(root, statsPath(com2co.getId(), year, "urbans")));
				state.setStatsSuburbs(exists(root, statsPath(com2co.getId(), year, "suburbs")));
				state.setStatsAll(exists(root, statsPath(com2co.getId(), year, "all")));
				state.setParkOutline(exists(root, geoPath("parkOutline", com2co.getId(), year, "parkOutline")));
				state.setIsochrone(exists(root, geoPath("isochrones", com2co.getId(), year, "isochrone")));
				state.setCarres(exists(root, geoPath("carres", com2co.getId(), year, "carre")));
				state.setIris(exists(root, geoPath("iris", com2co.getId(), year, "iris")));
				state.setNdvi(exists(root, ndviPath(com2co.getId(), year, null)));
				state.setCitiesTotal(cities.size());
				state.setCitiesWithStats(countCityStats(root, com2co, cities, year));
				state.setCitiesWithNdvi(countCityNdvi(root, com2co, cities, year));
				state.computeStatus();
				if ("PARTIEL".equals(state.getStatus())) {
					dashboard.getWarnings().add("Export partiel : EPCI " + com2co.getName() + " / " + year);
				}
				dashboard.getEpcis().add(state);
			}
		}
	}

	private void buildBlockers(ExportDashboard dashboard, String root, List<String> changed, Long epciFilter) {
		if (!dashboard.isRepositoryFound()) {
			dashboard.getBlockers().add("Dépôt Git introuvable : " + dashboard.getRepoPath());
		}
		if (changed.isEmpty()) {
			dashboard.getWarnings().add("Aucun fichier d'export à commiter.");
		}
		boolean statsChanged = changed.stream().anyMatch(path -> path.startsWith("data/stats/")
				|| path.startsWith("data/com2cos/") || path.startsWith("data/cities/"));
		if (epciFilter == null && statsChanged && !exists(root, "data/territoires.json")) {
			dashboard.getBlockers().add("data/territoires.json absent : il doit être publié avec le référentiel.");
		}
		for (String path : changed) {
			if (path.matches("data/stats/.*/stats_[^/]+\\.json$") && !path.endsWith(".v2.json")) {
				String v2 = path.substring(0, path.length() - ".json".length()) + ".v2.json";
				if (!exists(root, v2)) {
					dashboard.getWarnings().add("Fichier v2 absent : " + v2);
				}
			}
		}
	}

	private String repoRoot() {
		String local = gitHelper.getLocalPath();
		if (local != null && !local.isBlank()) {
			return Paths.get(local).toAbsolutePath().normalize().toString();
		}
		return Paths.get(applicationBusinessProperties.getJsonFileFolder()).toAbsolutePath().normalize().toString();
	}

	private boolean isExportPath(String path) {
		return path.startsWith("data/") || path.startsWith("geojson/");
	}

	/**
	 * Filtre les fichiers d'un EPCI : GeoJSON, stats EPCI, NDVI, stats de ses villes.
	 * Les référentiels globaux (regions, com2cos, cities, territoires) restent réservés à l'admin.
	 */
	private boolean isPathForEpci(String path, Long epciFilter) {
		if (epciFilter == null) {
			return true;
		}
		String id = String.valueOf(epciFilter);
		if (path.startsWith("data/stats/com2co/" + id + "/")
				|| path.startsWith("data/ndvi/" + id + "/")
				|| path.startsWith("geojson/") && path.contains("/" + id + "/")) {
			return true;
		}
		for (String insee : cityInseeFor(epciFilter)) {
			if (insee != null && insee.length() >= 2
					&& path.startsWith("data/stats/" + insee.substring(0, 2) + "/" + insee + "/")) {
				return true;
			}
		}
		return false;
	}

	private java.util.Set<String> cityInseeFor(Long epciId) {
		return communauteCommuneRepository.findById(epciId)
				.map(c2c -> c2c.getCities() == null ? java.util.Set.<String>of()
						: c2c.getCities().stream()
								.map(City::getInseeCode)
								.filter(java.util.Objects::nonNull)
								.collect(java.util.stream.Collectors.toSet()))
				.orElse(java.util.Set.of());
	}

	private String snapshotHash(String root, String head, List<String> changed) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.update((head == null ? "" : head).getBytes(StandardCharsets.UTF_8));
		for (String path : changed) {
			digest.update((byte) 0);
			digest.update(path.getBytes(StandardCharsets.UTF_8));
			Path file = Path.of(root, path);
			digest.update((byte) 0);
			if (Files.isRegularFile(file)) {
				digest.update(Long.toString(Files.size(file)).getBytes(StandardCharsets.UTF_8));
				digest.update((byte) 0);
				digest.update(Long.toString(Files.getLastModifiedTime(file).toMillis()).getBytes(StandardCharsets.UTF_8));
			}
		}
		return HexFormat.of().formatHex(digest.digest());
	}

	private boolean referentialExists(String root, CommunauteCommune com2co) {
		boolean regions = exists(root, "data/regions.json");
		boolean densite = exists(root, "data/cities/densite.json");
		boolean cities = exists(root, "data/cities/com2co/cities_" + com2co.getId() + ".json");
		boolean epci = com2co.getRegion() == null
				|| exists(root, "data/com2cos/" + com2co.getRegion().getId()
						+ "/com2cos_" + com2co.getRegion().getId() + ".json");
		return regions && densite && cities && epci;
	}

	private int countCityStats(String root, CommunauteCommune com2co, List<City> cities, Integer year) {
		int count = 0;
		for (City city : cities) {
			String insee = city.getInseeCode();
			if (insee == null || insee.length() < 2) {
				continue;
			}
			if (exists(root, "data/stats/" + insee.substring(0, 2) + "/" + insee
					+ "/stats_" + insee + "_" + year + ".json")) {
				count++;
			}
		}
		return count;
	}

	private int countCityNdvi(String root, CommunauteCommune com2co, List<City> cities, Integer year) {
		int count = 0;
		for (City city : cities) {
			if (city.getInseeCode() != null && exists(root, ndviPath(com2co.getId(), year, city.getInseeCode()))) {
				count++;
			}
		}
		return count;
	}

	private boolean exists(String root, String relative) {
		return relative != null && Files.isRegularFile(Path.of(root, relative));
	}

	private String statsPath(Long c2cId, Integer year, String type) {
		return "data/stats/com2co/" + c2cId + "/stats_c2c_" + c2cId + "_" + type + "_" + year + ".json";
	}

	private String geoPath(String folder, Long c2cId, Integer year, String prefix) {
		return "geojson/" + folder + "/" + c2cId + "/" + prefix + "_" + year + "_" + c2cId + ".json";
	}

	private String ndviPath(Long c2cId, Integer year, String insee) {
		if (insee == null) {
			return "data/ndvi/" + c2cId + "/ndvi_" + year + "_" + c2cId + ".json";
		}
		return "data/ndvi/" + c2cId + "/" + insee + "/ndvi_" + year + "_" + insee + ".json";
	}
}
