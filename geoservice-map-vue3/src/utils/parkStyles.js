/**
 * @typedef {Object} ParkStyle
 * @property {string} color - Couleur de la bordure
 * @property {string} fillColor - Couleur de remplissage
 * @property {number} weight - Épaisseur de la bordure
 * @property {number} fillOpacity - Opacité du remplissage
 */

/**
 * @typedef {Object} ParkFeature
 * @property {Object} properties - Propriétés du parc
 * @property {boolean} [properties.actif] - Indique si le parc est actif
 * @property {boolean} [properties.oms] - Indique si le parc est inclus OMS
 */

/**
 * Obtient le style de couleur pour un parc en fonction de ses propriétés
 * @param {ParkFeature} feature - La feature GeoJSON du parc
 * @returns {ParkStyle} Le style à appliquer au parc
 * 
 * Codes couleur:
 * - #3aa637: Parc inclus OMS (par défaut)
 * - #e96020: Parc exclus OMS
 * - #DC20E9: Futur parc ou détruit (parc inactif)
 */
export function getParkStyle(feature) {
  let fillColor = '#3aa637'; // Couleur par défaut (parc inclus OMS)

  if (feature && feature.properties) {
    // Vérifier d'abord si le parc est actif
    if (feature.properties.actif === false) {
      fillColor = '#DC20E9'; // Futur parc ou détruit
    } else if (feature.properties.oms === false) {
      fillColor = '#e96020'; // Parc exclus OMS
    } else if (feature.properties.oms === true) {
      fillColor = '#3aa637'; // Parc inclus OMS
    }
  }

  return { 
    color: '#1e4d1c', 
    fillColor: fillColor, 
    weight: 1, 
    fillOpacity: 0.35 
  };
}
