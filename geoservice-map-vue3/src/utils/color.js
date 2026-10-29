/**
 * Convertit une couleur hexadécimale (#rrggbb) en rgba() avec une alpha donnée.
 * Sert aux pastilles de légende : elles reprennent la transparence
 * (fillOpacity) des polygones de la carte.
 * @param {string} hex - couleur #rrggbb ou #rgb.
 * @param {number} alpha - opacité de 0 à 1.
 * @returns {string} chaîne rgb(r, g, b) / rgba(r, g, b, a).
 */
export function hexToRgba(hex, alpha) {
  let value = String(hex).replace('#', '').trim();
  if (value.length === 3) {
    value = value.split('').map((c) => c + c).join('');
  }
  const int = parseInt(value, 16);
  const r = (int >> 16) & 255;
  const g = (int >> 8) & 255;
  const b = int & 255;
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

/**
 * Couleur NDVI d'un parc, du plus clair (végétation faible) au plus foncé.
 * Les valeurs absentes ou non numériques sont neutres (pas encore calculé).
 * @param {number|string|null} ndviMean - NDVI moyen du parc.
 * @returns {string} couleur #rrggbb.
 */
export function getNdviColor(ndviMean) {
  const value = Number.parseFloat(String(ndviMean ?? '').replace(',', '.'));
  if (Number.isNaN(value)) return '#959595';
  if (value < 0.2) return '#d9f0a3';
  if (value < 0.3) return '#addd8e';
  if (value < 0.4) return '#78c679';
  if (value < 0.5) return '#31a354';
  return '#006837';
}
