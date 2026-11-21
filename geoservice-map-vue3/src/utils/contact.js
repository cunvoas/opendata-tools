const enTexte = (codes) => codes.map((code) => String.fromCodePoint(code)).join('');

/**
 * Ouvre un message vers l'adresse du projet. L'adresse n'est écrite en clair ni dans le code ni dans le HTML,
 * pour échapper à la collecte par les robots.
 * @param {string} sujet - objet du message.
 */
export function ecrireAuProjet(sujet) {
  const adresse = enTexte([111, 120, 121, 103, 101, 110, 101, 46, 108, 109]) + enTexte([64, 103, 109, 97, 105, 108, 46, 99, 111, 109]);
  window.location.href = `${enTexte([77, 65, 73, 76, 84, 79])}:${adresse}?subject=${encodeURIComponent(sujet || '')}`;
}
