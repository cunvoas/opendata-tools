package com.github.cunvoas.geoserviceisochrone.service.park;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParkOverpass;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkOverpassRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service d'appariement des parcs Overpass (OSM) au cadastre, commune par commune.
 *
 * <p>Pour chaque commune :
 * <ul>
 *   <li><b>Rattachement INSEE</b> : récupère la forme de la commune au cadastre et retrouve
 *       les parcs OSM qui correspondent (centroïde contenu, ou couverture significative du
 *       cadastre, ou coin sud-ouest de la bbox pour les parcs sans forme). Un parc peut
 *       s'étendre sur plusieurs communes : le champ {@code insee} de
 *       {@link ParkOverpass} porte alors un code composite (codes triés séparés par des virgules).</li>
 *   <li><b>Enrichissement des parcs existants</b> : si la commune n'a pas encore de calcul
 *       isochrone (aucun {@code park_area_computed}), les parcs existants sans forme
 *       (point seul) sont appariés au parc Overpass correspondant et récupèrent sa forme.</li>
 * </ul>
 * </p>
 */
@Service
@Slf4j
public class ParkOverpassCadastreService {

	/** Ratio minimal de couverture du parc par le cadastre pour rattacher une commune.
 * Si l'intersection (centroïde ou surface) couvre au moins 10 % de la surface du parc,
 * on considère que le parc appartient à cette commune. Ce seuil permet d'éviter
 * d'attribuer des parcels frontalières à deux communes voisines sur la base
 * d'une simple touche de géométrie. */
	private static final double MIN_COVERAGE_RATIO = 0.10;

	private final CityRepository cityRepository;
	private final CadastreRepository cadastreRepository;
	private final ParkOverpassRepository parkOverpassRepository;
	private final ParkJardinRepository parkJardinRepository;
	private final ParkAreaComputedRepository parkAreaComputedRepository;
	private final InseeCarre200mOnlyShapeRepository surfaceRepo;

	/**
	 * Constructeur avec injection des dépendances.
	 * @param cityRepository repository des communes
	 * @param cadastreRepository repository du cadastre
	 * @param parkOverpassRepository repository des parcs Overpass
	 * @param parkJardinRepository repository des parcs et jardins
	 * @param parkAreaComputedRepository repository des calculs isochrones
	 * @param surfaceRepo repository de calcul de surface
	 */
	@Autowired
	public ParkOverpassCadastreService(CityRepository cityRepository,
									   CadastreRepository cadastreRepository,
									   ParkOverpassRepository parkOverpassRepository,
									   ParkJardinRepository parkJardinRepository,
									   ParkAreaComputedRepository parkAreaComputedRepository,
									   InseeCarre200mOnlyShapeRepository surfaceRepo) {
		this.cityRepository = cityRepository;
		this.cadastreRepository = cadastreRepository;
		this.parkOverpassRepository = parkOverpassRepository;
		this.parkJardinRepository = parkJardinRepository;
		this.parkAreaComputedRepository = parkAreaComputedRepository;
		this.surfaceRepo = surfaceRepo;
	}

	/**
	 * Traite toutes les communes.
	 *
	 * <p>Le rattachement d'un parc à ses communes est accumulé dans une map
	 * ({@code parkId -> codes INSEE}) car un parc peut être matché depuis plusieurs
	 * communes. Le code INSEE composite est écrit à la fin.</p>
	 */
/**
	 * Traite toutes les communes.
	 *
	 * <p>Le traitement s'effectue en deux temps :
	 * <ol>
 *   <li>Rattachement INSEE : pour chaque commune, on retrouve les parcs OSM dont la
 *       forme (centroïde ou surface) correspond au cadastre. Un parc peut s'étendre
 *       sur plusieurs communes : tous les codes INSEE trouvés sont accumulés dans une
 *       map {@code parkId → SortedSet<String}}. À la fin, le code INSEE composite
 *       (codes triés, virgules) est écrit sur chaque parc via {@link #flushCommunes}.</li>
 *   <li>Enrichissement des parcs existants : si la commune n'a pas encore de calcul
 *       isochrone (aucun {@code park_area_computed} lié à ses parcs), on enrichit les
 *       parcs qui ne possèdent pas encore de forme (seulement un point). On y associe
 *       la forme du parc Overpass correspondant (contour, centroid, surface, etc.).</li>
 * </ol>
 * </p>
 */
@Transactional
public void process() {
		log.info("ParkOverpassCadastreService.process: starting");
		Map<Long, SortedSet<String>> communesByPark = new HashMap<>();

		List<City> cities = new ArrayList<>(cityRepository.findAll());
		cities.sort(Comparator.comparing(City::getInseeCode,
				Comparator.nullsLast(Comparator.naturalOrder())));

		for (City city : cities) {
			processCity(city, communesByPark);
		}

		flushCommunes(communesByPark);
		log.info("ParkOverpassCadastreService.process: done ({} cities)", cities.size());
	}

	/**
	 * Traite une commune.
	 * @param city commune à traiter
	 * @param communesByPark accumulateur parkId -> codes INSEE
	 */
	/**
	 * Traite une commune.
	 * <p>
	 * Le traitement d'une commune comporte deux étapes distinctes :
	 * <ul>
 *   <li>Étape A – Rattachement INSEE : on retrouve les parcs Overpass qui correspondent
 *       à la commune via le cadastre (centroïde ou surface significative) et on
 *       accumulate leurs codes INSEE. Même si le parc possède déjà un code INSEE,
 *       on y ajoute les nouveaux codes trouvés (union).</li>
 *   <li>Étape B – Enrichissement des parcs point‑seul : si la commune n'a pas encore
 *       de calculs isochrones (aucun {@code park_area_computed} lié à ses parcs), on
 *       trouve les parcs existants ne possédant pas de contour (seul un {@code coordonnee}).
 *       Pour chacun d'entre eux, on cherche le parc Overpass de la même commune dont la
 *       forme contient le point du parc, et on copie la forme (contour, centroid,
 *       surface, surface du contour, statut, source).</li>
 * </ul>
 * </p>
	 * @param city commune à traiter
	 * @param communesByPark accumulateur parkId -> codes INSEE
	 */
	private void processCity(City city, Map<Long, SortedSet<String>> communesByPark) {
		String insee = city.getInseeCode();
		Optional<Cadastre> optCadastre = cadastreRepository.findById(insee);
		if (optCadastre.isEmpty() || optCadastre.get().getGeoShape() == null) {
			log.debug("processCity: no cadastre shape for INSEE {}", insee);
			return;
		}
		Cadastre cadastre = optCadastre.get();
		String cadastreText = GeometryQueryHelper.toText(cadastre.getGeoShape());

		matchOverpassByCadastre(cadastre, cadastreText, insee, communesByPark);

		if (!parkAreaComputedRepository.isComputedForCity(city.getId())) {
			enrichPointOnlyParks(city, insee);
		}
	}

	/**
	 * Récupère la forme au cadastre et retrouve les parcs OSM qui correspondent,
	 * puis accumule les communes rattachées.
	 * @param cadastre forme cadastrale de la commune
	 * @param cadastreText forme au format "SRID=4326;..."
	 * @param insee code INSEE de la commune
	 * @param communesByPark accumulateur parkId -> codes INSEE
	 */
	/**
	 * Récupère la forme au cadastre et retrouve les parcs OSM qui correspondent,
	 * puis accumule les communes rattachées.
	 * <p>
	 * La correspondance s'effectue en deux temps pour gérer les parcs qui s'étendent
	 * sur plusieurs communes :
	 * <ul>
 *   <li>Parcs possédant une forme (way/relation) : on utilise la requête native
 *       {@link ParkOverpassRepository#findByAreaGeo(String, double)} qui vérifie soit
 *       le containment du centroïde, soit un ratio de surfacegéodésique
 *       {@code ST_Area(ST_Intersection(shape, cadastre), true) / ST_Area(shape, true) >= 0.10}.</li>
 *   <li>Parcs sans forme (node, shape NULL) : on utilise
 *       {@link ParkOverpassRepository#findByCornerSouthWestInArea(String)} pour récupérer
 *       ceux dont le coin sud-ouest de la bounding box est contenu dans le cadastre.</li>
 * </ul>
 * </p>
	 * @param cadastre forme cadastrale de la commune
	 * @param cadastreText forme au format "SRID=4326;..."
	 * @param insee code INSEE de la commune
	 * @param communesByPark accumulateur parkId -> codes INSEE
	 */
	private void matchOverpassByCadastre(Cadastre cadastre, String cadastreText,
										 String insee, Map<Long, SortedSet<String>> communesByPark) {
		List<ParkOverpass> matched = parkOverpassRepository.findByAreaGeo(cadastreText, MIN_COVERAGE_RATIO);
		for (ParkOverpass overpass : matched) {
			communesByPark.computeIfAbsent(overpass.getId(), k -> new TreeSet<>()).add(insee);
		}

		for (ParkOverpass overpass : parkOverpassRepository.findByCornerSouthWestInArea(cadastreText)) {
			if (overpass.getCornerSouthWest() != null
					&& cadastre.getGeoShape().contains(overpass.getCornerSouthWest())) {
				communesByPark.computeIfAbsent(overpass.getId(), k -> new TreeSet<>()).add(insee);
			}
		}
	}

	/**
	 * Écrit le code INSEE composite final de chaque parc accumulé.
	 *
	 * <p>Fusionne les codes trouvés avec les codes existants (préserve les valeurs
	 * précédemment posées), trie, puis persiste si le résultat diffère.</p>
	 * @param communesByPark accumulateur parkId -> codes INSEE
	 */
	/**
	 * Écrit le code INSEE composite final de chaque parc accumulé.
	 *
	 * <p>Pour chaque parc having des codes INSEE accumulés :
 * <ul>
 *   <li>On fusionne les codes trouvés avec les codes existants (préserve les valeurs
 *       précédemment posées, pour ne pas écraser un code manuellement saisi).</li>
 *   <li>On trie les codes par ordre croissant (ex: {@code 59100,59350}).</li>
 *   <li>On concatène les codes avec une virgule.</li>
 *   <li>Si le résultat diffère de la valeur déjà en base, on met à jour le champ
 *       {@code insee} et on persiste via {@code saveAndFlush}.</li>
 * </ul>
 * </p>
	 * @param communesByPark accumulateur parkId -> codes INSEE
	 */
	private void flushCommunes(Map<Long, SortedSet<String>> communesByPark) {
		for (Map.Entry<Long, SortedSet<String>> entry : communesByPark.entrySet()) {
			Optional<ParkOverpass> opt = parkOverpassRepository.findById(entry.getKey());
			if (opt.isEmpty()) {
				continue;
			}
			ParkOverpass overpass = opt.get();
			SortedSet<String> codes = new TreeSet<>(entry.getValue());
			if (overpass.getInsee() != null && !overpass.getInsee().isBlank()) {
				for (String code : overpass.getInsee().split(",")) {
					if (!code.isBlank()) {
						codes.add(code.trim());
					}
				}
			}
			String composite = String.join(",", codes);
			if (!composite.equals(overpass.getInsee())) {
				log.info("flushCommunes: park {} insee '{}' -> '{}'", overpass.getId(), overpass.getInsee(), composite);
				overpass.setInsee(composite);
				parkOverpassRepository.saveAndFlush(overpass);
			}
		}
	}

	/**
	 * Enrichit les parcs existants sans forme (point seul) avec la forme du parc
	 * Overpass correspondant de la même commune.
	 * <p>
	 * Cette méthode ne s'exécute que si la commune n'a pas déjà de calculs isochrones
	 * (testé via {@link #parkAreaComputedRepository#isComputedForCity(long)}).
	 * Elle ne fait rien si :
 * <ul>
 *   <li>aucun parc point‑seul n'existe pour la commune ({@code findByCityIdNoContour} vide),</li>
 *   <li>aucun parc Overpass de cette commune n'est trouvé via {@code findByInseeContains}.</li>
 * </ul>
	 * @param city commune
	 * @param insee code INSEE de la commune
	 */
	private void enrichPointOnlyParks(City city, String insee) {
		List<ParcEtJardin> pointOnlyParks = parkJardinRepository.findByCityIdNoContour(city.getId());
		if (pointOnlyParks.isEmpty()) {
			return;
		}
		List<ParkOverpass> overpassList = parkOverpassRepository.findByInseeContains(insee);
		if (overpassList.isEmpty()) {
			return;
		}

		for (ParcEtJardin parc : pointOnlyParks) {
			ParkOverpass match = findOverpassContaining(overpassList, parc.getCoordonnee());
			if (match != null) {
				applyShape(parc, match);
			}
		}
	}

	/**
	 * Recherche le parc Overpass dont la forme (ou la bbox) contient le point donné.
	 * <p>
	 * Le test se fait d'abord sur la forme géométrique du parc Overpass (si elle existe) :
 * <code>shape.contains(point)</code>. Si le parc n'a pas de forme (shape NULL),
 * on teste ensuite si le point se trouve dans l'enveloppe (bbox) définie par les
 * coins {@code cornerSouthWest} et {@code cornerNorthEast}.
 * </p>
	 * @param overpassList parcs Overpass de la commune
	 * @param point point du parc existant
	 * @return le parc Overpass correspondant, ou {@code null}
	 */
	private ParkOverpass findOverpassContaining(List<ParkOverpass> overpassList, Point point) {
		if (point == null) {
			return null;
		}
		for (ParkOverpass overpass : overpassList) {
			Geometry shape = overpass.getShape();
			if (shape != null) {
				if (shape.contains(point)) {
					return overpass;
				}
			} else if (overpass.getCornerSouthWest() != null && overpass.getCornerNorthEast() != null) {
				Envelope envelope = new Envelope(overpass.getCornerSouthWest().getCoordinate(),
												 overpass.getCornerNorthEast().getCoordinate());
				if (envelope.contains(point.getCoordinate())) {
					return overpass;
				}
			}
		}
		return null;
	}

	/**
	 * Copie la forme du parc Overpass sur le parc existant.
	 *
	 * <p>Reprend la sémantique d'enrichissement existante (copie sécurisée qui ne
 * écrase pas les valeurs déjà présentes) :
 * <ul>
 *   <li><b>contour</b> : la géométrie du parc Overpass (toujours copiée, la méthode
 *       retourne immédiatement si le Overpass source n'a pas de forme).</li>
 *   <li><b>coordonnee</b> : le centroïde de la forme Overpass.</li>
 *   <li><b>surface</b> : la surface du parc Overpass (telle quelle, peut être null).</li>
 *   <li><b>surfaceContour</b> : surface calculée via {@link InseeCarre200mOnlyShapeRepository#getSurface}
 *       si disponible, sinon la valeur existante est conservée.</li>
 *   <li><b>source</b> : définie à {@link ParcSourceEnum#OSM} seulement si elle était
 *       absente avant enrichissement (les libellés existants sont conservés).</li>
 *   <li><b>status</b> : forcé à {@link ParcStatusEnum#TO_QUALIFY}.</li>
 *   <li><b>etatOuverture</b> : copié depuis {@code openingHours} du Overpress si le champ
 *       {@code etatOuverture} du parc était null.</li>
 * </ul>
 * </p>
	 * @param parc parc existant à enrichir
	 * @param overpass parc Overpass source
	 */
	private void applyShape(ParcEtJardin parc, ParkOverpass overpass) {
		if (overpass.getShape() == null) {
			return;
		}
		parc.setContour(overpass.getShape());
		parc.setCoordonnee(overpass.getShape().getCentroid());
		parc.setSurface(overpass.getSurface());
		Long s = surfaceRepo.getSurface(overpass.getShape());
		if (s != null) {
			parc.setSurfaceContour(s.doubleValue());
		}
		if (parc.getSource() == null) {
			parc.setSource(ParcSourceEnum.OSM);
		}
		if (parc.getStatus() == null) {
			parc.setStatus(ParcStatusEnum.TO_QUALIFY);
		}
		if (overpass.getOpeningHours() != null && parc.getEtatOuverture() == null) {
			parc.setEtatOuverture(overpass.getOpeningHours());
		}
		parc.setStatus(ParcStatusEnum.TO_QUALIFY);
		parkJardinRepository.saveAndFlush(parc);
		log.info("applyShape: park {} shaped from overpass {}", parc.getId(), overpass.getId());
	}
}