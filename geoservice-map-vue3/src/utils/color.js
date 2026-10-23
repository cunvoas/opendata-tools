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
