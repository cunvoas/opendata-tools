import donnees from '../data/territoires.json';

/** Territoires disposant de statistiques publiées (src/data/territoires.json). */
export const COMMUNES = donnees.communes;
export const EPCIS = donnees.epcis;

const parInsee = new Map(COMMUNES.map((commune) => [commune.insee, commune]));
const parIdTechnique = new Map(COMMUNES.map((commune) => [commune.id, commune]));
const parSiren = new Map(EPCIS.map((epci) => [epci.siren, epci]));
const parDossier = new Map(EPCIS.map((epci) => [epci.dossier, epci]));

/** Nom lisible : « VILLENEUVE D ASCQ » devient « Villeneuve D Ascq ». */
export function formaterNom(nom) {
  return String(nom || '')
    .toLocaleLowerCase('fr')
    .replace(/(^|[\s\-'])(\p{L})/gu, (m, sep, lettre) => sep + lettre.toLocaleUpperCase('fr'));
}

export const urlCommune = (insee) => `/stats/${insee}`;
export const urlEpci = (siren) => `/stats/epci/${siren}`;

export const communeParInsee = (insee) => parInsee.get(String(insee)) ?? null;
export const communeParId = (id) => parIdTechnique.get(String(id)) ?? null;
export const epciParSiren = (siren) => parSiren.get(String(siren)) ?? null;
export const epciParDossier = (dossier) => parDossier.get(String(dossier)) ?? null;

/** Communes les plus peuplées, pour le maillage interne de l'index. */
export const communesPourLiens = (limite = 50) => COMMUNES.slice(0, limite);

/** Description d'un territoire : nom, population, identifiant public. */
export function titreCommune(commune) {
  return `Espaces verts à ${commune.nom} : m² par habitant | Parcs & Jardins`;
}

export function descriptionCommune(commune) {
  const habitants = new Intl.NumberFormat('fr-FR').format(commune.population);
  return `Espaces verts à ${commune.nom} (${habitants} habitants) : surface de parcs et jardins accessible à pied par habitant, répartition de la population et comparaison aux seuils de l'OMS.`;
}

export function titreEpci(epci) {
  return `Espaces verts : ${epci.nom} | Parcs & Jardins`;
}

export function descriptionEpci(epci) {
  return `Statistiques des espaces verts de ${epci.nom} : répartition des habitants par surface de parcs et jardins accessible à pied, selon les seuils de l'OMS.`;
}

/**
 * Métadonnées d'une route territoriale : title, description, canonical et robots.
 * Une route dont le territoire n'est pas publié reste hors index.
 * @param {{name?: string, path: string, params?: object}} route - route résolue.
 * @returns {{path: string, meta: object}} route à passer à applyRouteMeta.
 */
export function metaRoute(route) {
  if (route.name === 'statsCommune') {
    const commune = communeParInsee(route.params?.insee);
    return {
      path: route.path,
      meta: commune
        ? { title: titreCommune(commune), description: descriptionCommune(commune) }
        : { title: 'Territoires couverts | Parcs & Jardins', robots: 'noindex, follow' }
    };
  }
  if (route.name === 'statsEpci') {
    const epci = epciParSiren(route.params?.siren);
    return {
      path: route.path,
      meta: epci
        ? { title: titreEpci(epci), description: descriptionEpci(epci) }
        : { title: 'Territoires couverts | Parcs & Jardins', robots: 'noindex, follow' }
    };
  }
  return route;
}

/**
 * Traduit les paramètres historiques (`?city=…`, `?c2c=…`) en route canonique.
 * `city` porte l'id technique de la commune, d'où la correspondance par id.
 * @param {object} query - query de la route /stats.
 * @returns {string|null} route canonique, ou null si le territoire n'est pas publié.
 */
export function routeDepuisParametres(query = {}) {
  const valeur = (v) => (Array.isArray(v) ? v[0] : v);
  const city = valeur(query.city);
  if (city) {
    const id = String(city).split(',')[2];
    const commune = communeParId(id);
    if (commune) return urlCommune(commune.insee);
  }
  const c2c = valeur(query.c2c) || valeur(query.com2co);
  if (c2c) {
    const epci = epciParDossier(c2c);
    if (epci) return urlEpci(epci.siren);
  }
  return null;
}
