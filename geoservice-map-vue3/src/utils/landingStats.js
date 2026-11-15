import { DATA_MODE, STATIC_ON_GIT, DYNAMIC_ON_LOCAL } from './config.js';

/** Dernier millésime Filosofi publié dans geoservice-data. */
export const ANNEE_DONNEES = 2021;
/** Code INSEE de Lille. */
export const COMMUNE_LILLE = '59350';
/** Identifiant de la Métropole Européenne de Lille dans geoservice-data (dossier com2co). */
export const EPCI_MEL = 1;

/**
 * URL des statistiques d'une commune, au même format que StatsGraph.
 * @param {string} insee - code INSEE de la commune.
 * @param {number} annee - millésime.
 * @returns {string} URL du JSON.
 */
export function urlStatsCommune(insee, annee = ANNEE_DONNEES) {
  return DATA_MODE === 'dynamic'
    ? `${DYNAMIC_ON_LOCAL}/map/stats/city/${insee}?annee=${annee}`
    : `${STATIC_ON_GIT}/data/stats/${insee.substring(0, 2)}/${insee}/stats_${insee}_${annee}.json`;
}

/**
 * URL des statistiques « densités confondues » d'un EPCI.
 * @param {number} id - identifiant de l'EPCI.
 * @param {number} annee - millésime.
 * @returns {string} URL du JSON.
 */
export function urlStatsEpci(id, annee = ANNEE_DONNEES) {
  return DATA_MODE === 'dynamic'
    ? `${DYNAMIC_ON_LOCAL}/map/stats/com2co/${id}/all?annee=${annee}`
    : `${STATIC_ON_GIT}/data/stats/com2co/${id}/stats_c2c_${id}_all_${annee}.json`;
}

const somme = (stats) => stats.reduce((total, tranche) => total + (tranche.habitants || 0), 0);
const nombreOuNull = (valeur) => (typeof valeur === 'number' && Number.isFinite(valeur) ? valeur : null);
const pourcent = (part, total) => Math.round((1000 * part) / total) / 10;

/**
 * Résume un JSON de statistiques : part des habitants sous le minimum OMS.
 * Les communes ont cinq tranches (les trois premières sont sous le minimum), les EPCI « all » trois niveaux
 * (le premier est « non respect des minima »). Les habitants sans parc accessible sont dans la première tranche.
 * @param {object} json - JSON publié par geoservice-data.
 * Le bloc `descripteur`, calculé par le serveur, apporte les deux m²/habitant (moyenne pondérée par la population,
 * ratio global) ; absent des anciens JSON, il donne alors null.
 * @returns {{annee: string, habitants: number, sousMinimum: number, minimumM2: (number|null), premiereBorne: (number|null), premiereTranche: (number|null), m2Moyen: (number|null), m2Global: (number|null)}|null}
 *   null si le JSON est inexploitable.
 */
export function resumeStats(json) {
  const stats = json?.stats;
  if (!Array.isArray(stats) || stats.length < 3) return null;
  const habitants = somme(stats);
  if (habitants <= 0) return null;

  const parCommune = stats.length >= 5;
  const sousMinimum = parCommune ? somme(stats.slice(0, 3)) : stats[0].habitants;
  const borne = (texte, motif) => {
    const trouve = motif.exec(texte ?? '');
    return trouve ? Number(trouve[1]) : null;
  };
  return {
    annee: String(json.annee ?? ''),
    habitants,
    sousMinimum: pourcent(sousMinimum, habitants),
    minimumM2: parCommune ? borne(json.seuils?.[3]?.surface, /^\s*(\d+)\s*<=/) : null,
    premiereBorne: parCommune ? borne(json.seuils?.[0]?.surface, /<\s*(\d+)\s*m²/) : null,
    premiereTranche: parCommune ? pourcent(stats[0].habitants, habitants) : null,
    m2Moyen: nombreOuNull(json.descripteur?.m2ParHabitantMoyen),
    m2Global: nombreOuNull(json.descripteur?.m2ParHabitantGlobal)
  };
}

/**
 * Télécharge et résume un JSON de statistiques.
 * @param {string} url - URL du JSON.
 * @param {Function} fetchFn - fonction fetch (injectable pour les tests).
 * @returns {Promise<object|null>} résumé, null si le téléchargement ou le JSON échoue.
 */
export async function chargerResume(url, fetchFn = fetch) {
  try {
    const reponse = await fetchFn(url);
    return reponse.ok ? resumeStats(await reponse.json()) : null;
  } catch {
    return null;
  }
}

const ID_DONNEES_INTEGREES = 'landing-stats';

/**
 * Lit les chiffres intégrés au HTML au pré-rendu : la page s'affiche avec ses chiffres dès le premier rendu,
 * sans repasser par « Chargement… ». Renvoie un objet vide si rien n'est intégré ou si le contenu est invalide.
 * @param {Document} doc - document à lire.
 * @returns {{lille?: object, mel?: object}} chiffres intégrés.
 */
export function lireDonneesIntegrees(doc = document) {
  try {
    const brut = doc.getElementById(ID_DONNEES_INTEGREES)?.textContent;
    const donnees = brut ? JSON.parse(brut) : {};
    return donnees && typeof donnees === 'object' ? donnees : {};
  } catch {
    return {};
  }
}

/**
 * Intègre les chiffres au document, pour que le pré-rendu les emporte dans le HTML.
 * @param {{lille: object, mel: object}} donnees - résumés à intégrer.
 * @param {Document} doc - document à écrire.
 */
export function integrerDonnees(donnees, doc = document) {
  let script = doc.getElementById(ID_DONNEES_INTEGREES);
  if (!script) {
    script = doc.createElement('script');
    script.type = 'application/json';
    script.id = ID_DONNEES_INTEGREES;
    doc.head.appendChild(script);
  }
  // « < » échappé : le contenu ne peut jamais refermer la balise script
  script.textContent = JSON.stringify(donnees).replace(/</g, '\\u003c');
}

