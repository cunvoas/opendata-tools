import { readFileSync, existsSync } from 'node:fs';
import { resolve } from 'node:path';
import { describe, it, expect } from 'vitest';
import { NDVI_CATEGORIES, NDVI_NEUTRAL_COLOR, NDVI_NEUTRAL_COLOR_DALTONIEN, getNdviCategory, getNdviColor } from '../ndvi.js';

describe('NDVI_CATEGORIES', () => {
  it('reprend les cinq seuils universels par type de surface, du plus faible au plus élevé', () => {
    expect(NDVI_CATEGORIES.map((category) => [category.range, category.min])).toEqual([
      ['-1,0 à 0,1', -Infinity],
      ['0,1 à 0,2', 0.1],
      ['0,2 à 0,4', 0.2],
      ['0,4 à 0,7', 0.4],
      ['0,7 à 1,0', 0.7]
    ]);
  });

  it('porte le texte complet de chaque type de surface', () => {
    expect(NDVI_CATEGORIES.map((category) => category.description)).toEqual([
      'Eau, neige, roche, routes et bâtiments.',
      'Sol nu, sable ou végétation morte/sèche.',
      'Végétation très clairsemée, début de levée, ou pelouse tondue/brûlée.',
      'Végétation en croissance active, couverture du sol modérée.',
      'Zone de saturation. Canopée dense, forêt, culture en pleine maturité. Attention : une plante malade peut encore afficher 0,85.'
    ]);
  });

  it('donne à chaque niveau un libellé court et une couleur propres', () => {
    expect(NDVI_CATEGORIES.map((category) => category.label)).toEqual([
      'Eau, neige, roche, bâti',
      'Sol nu ou végétation morte',
      'Végétation très clairsemée',
      'Végétation en croissance',
      'Zone de saturation'
    ]);
    const colors = NDVI_CATEGORIES.map((category) => category.color);
    expect(new Set(colors).size).toBe(NDVI_CATEGORIES.length);
    expect(colors).not.toContain(NDVI_NEUTRAL_COLOR);
  });
});

describe('getNdviCategory', () => {
  const labelOf = (value) => getNdviCategory(value)?.label;

  it('classe chaque valeur dans sa tranche, borne basse incluse', () => {
    expect(labelOf(-0.5)).toBe('Eau, neige, roche, bâti');
    expect(labelOf(-0.001)).toBe('Eau, neige, roche, bâti');
    expect(labelOf(0)).toBe('Eau, neige, roche, bâti');
    expect(labelOf(0.099)).toBe('Eau, neige, roche, bâti');
    expect(labelOf(0.1)).toBe('Sol nu ou végétation morte');
    expect(labelOf(0.199)).toBe('Sol nu ou végétation morte');
    expect(labelOf(0.2)).toBe('Végétation très clairsemée');
    expect(labelOf(0.399)).toBe('Végétation très clairsemée');
    expect(labelOf(0.4)).toBe('Végétation en croissance');
    expect(labelOf(0.699)).toBe('Végétation en croissance');
    expect(labelOf(0.7)).toBe('Zone de saturation');
    expect(labelOf(1)).toBe('Zone de saturation');
    expect(labelOf(1.3)).toBe('Zone de saturation');
  });

  it('classe sur la valeur arrondie à l’affichage : 0,700 n’est jamais rangé sous le seuil 0,7', () => {
    expect(labelOf(0.6996)).toBe('Zone de saturation');
    expect(labelOf(0.6994)).toBe('Végétation en croissance');
    expect(labelOf(0.3996)).toBe('Végétation en croissance');
    expect(labelOf(0.1996)).toBe('Végétation très clairsemée');
    expect(labelOf(0.0996)).toBe('Sol nu ou végétation morte');
    expect(labelOf(-0.0004)).toBe('Eau, neige, roche, bâti');
  });

  it('accepte un texte, avec virgule décimale', () => {
    expect(labelOf('0,593')).toBe('Végétation en croissance');
    expect(labelOf('0.15')).toBe('Sol nu ou végétation morte');
  });

  it('retourne null pour une valeur absente ou non numérique', () => {
    [null, undefined, '', 'N/A', NaN, Infinity, -Infinity].forEach((value) => {
      expect(getNdviCategory(value), String(value)).toBeNull();
    });
  });
});

describe('getNdviColor', () => {
  it('retourne la couleur de la catégorie', () => {
    NDVI_CATEGORIES.forEach((category) => {
      const sample = Number.isFinite(category.min) ? category.min : -0.2;
      expect(getNdviColor(sample), category.range).toBe(category.color);
    });
  });

  it('retourne la couleur neutre pour une valeur absente', () => {
    expect(getNdviColor(null)).toBe(NDVI_NEUTRAL_COLOR);
    expect(getNdviColor(undefined)).toBe(NDVI_NEUTRAL_COLOR);
    expect(getNdviColor('N/A')).toBe(NDVI_NEUTRAL_COLOR);
  });
});

// La popup de la carte de consultation du backend (Thymeleaf) ne peut pas importer ce module : sa copie de la grille
// est vérifiée ici. Le test est ignoré si le backend n'est pas présent à côté du front (tests lancés depuis la racine du front).
const consultMap = resolve(process.cwd(), '../geoservice-isochrone-v2/src/main/resources/templates/consultMap.html');

function loadBackendGrid() {
  const source = readFileSync(consultMap, 'utf8');
  const block = source.slice(source.indexOf('const NDVI_CATEGORIES'), source.indexOf('function createPopupContent'));
  return new Function(`${block}; return { NDVI_CATEGORIES, createNdviHtml, createIndicesVegetationHtml, diagnostiquerCroisement };`)();
}

describe.skipIf(!existsSync(consultMap))('alignement avec la popup du backend (consultMap.html)', () => {
  const grid = (categories) => categories.map(({ min, range, color, description }) => ({ min, range, color, description }));

  it('reprend les mêmes seuils, couleurs et textes que la grille du front', () => {
    expect(grid([...loadBackendGrid().NDVI_CATEGORIES].reverse())).toEqual(grid(NDVI_CATEGORIES));
  });

  it('classe toutes les valeurs comme le front, y compris aux seuils arrondis', () => {
    const { createNdviHtml } = loadBackendGrid();
    for (let value = -2000; value <= 12000; value += 1) {
      const ndviMean = value / 10000;
      const html = createNdviHtml({ ndviEnabled: true, ndviMean });
      const expected = getNdviCategory(ndviMean);
      if (!html.includes(`<i>${expected.range} :</i>`) || !html.includes(expected.color)) {
        throw new Error(`NDVI ${ndviMean} : le backend ne classe pas « ${expected.range} »`);
      }
    }
  });

  it('n’affiche rien sans NDVI activé et signale un NDVI absent ou non numérique', () => {
    const { createNdviHtml } = loadBackendGrid();
    expect(createNdviHtml({ ndviEnabled: false, ndviMean: 0.5 })).toBe('');
    expect(createNdviHtml({ ndviEnabled: true, ndviMean: null })).toContain('pas encore calculé');
    expect(createNdviHtml({ ndviEnabled: true, ndviMean: 'abc' })).toContain('pas encore calculé');
  });

  it('ajoute les seuils NDRE et GNDVI à l’infobulle', () => {
    const { createIndicesVegetationHtml } = loadBackendGrid();
    const html = createIndicesVegetationHtml({
      ndviEnabled: true, ndviMean: 0.6, ndviMin: 0.2, ndviMax: 0.8,
      ndreMean: 0.5, ndreMin: 0.3, ndreMax: 0.6,
      gndviMean: 0.55, gndviMin: 0.4, gndviMax: 0.7
    });
    expect(html).toContain('NDRE moyen');
    expect(html).toContain('0,45 à 0,7 :');
    expect(html).toContain('GNDVI moyen');
    expect(html).toContain('0,5 à 0,75 :');
  });

  it('croise les trois indices pour détecter les anomalies', () => {
    const { diagnostiquerCroisement } = loadBackendGrid();
    expect(diagnostiquerCroisement(0.8, 0.4, 0.7)).toContain('Alerte maladie');
    expect(diagnostiquerCroisement(0.8, 0.6, 0.4)).toContain('Alerte maladie');
    expect(diagnostiquerCroisement(0.2, 0.2, 0.45)).toContain('Alerte bruit du sol');
    expect(diagnostiquerCroisement(0.85, 0.6, 0.7)).toContain('Vigueur maximale confirmée');
    expect(diagnostiquerCroisement(0.5, 0.4, 0.5)).toContain("Pas d'anomalie croisée détectée");
    expect(diagnostiquerCroisement(0.5, null, 0.5)).toBe('');
  });
});

// Mode daltonien : lisibilité mesurée sur les couleurs réellement affichées (remplissage à 50 % sur le fond de carte),
// en vue normale et en deutéranopie (simulation de Machado 2009, sévérité 1). ΔE : distance CIE76.
const FOND_CARTE = [242, 239, 233];
// Couleurs existantes du mode daltonien, affichées dans la même légende (Isochrone.vue, AppCarte.vue).
const LEGENDE_DALTONIEN = ['#d73027', '#fc8d59', '#fee090', '#91bfdb', '#4575b4', '#2CA02C', '#FF7F0E', '#9467BD'];
const lineariser = (c) => (c <= 0.04045 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4);
const gamma = (c) => { const v = Math.min(1, Math.max(0, c)); return v <= 0.0031308 ? 12.92 * v : 1.055 * v ** (1 / 2.4) - 0.055; };
const MACHADO_DEUTERANOPIE = [[0.367322, 0.860646, -0.227968], [0.280085, 0.672501, 0.047413], [-0.011820, 0.042940, 0.968881]];
const rgbDe = (hex) => [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16));
const affiche = (hex) => rgbDe(hex).map((v, i) => 0.5 * v + 0.5 * FOND_CARTE[i]);
const deuteranope = (rgb) => {
  const [r, g, b] = rgb.map((v) => lineariser(v / 255));
  return MACHADO_DEUTERANOPIE.map((row) => gamma(row[0] * r + row[1] * g + row[2] * b) * 255);
};
const lab = (rgb) => {
  const [r, g, b] = rgb.map((v) => lineariser(v / 255));
  const X = (0.4124 * r + 0.3576 * g + 0.1805 * b) / 0.95047;
  const Y = 0.2126 * r + 0.7152 * g + 0.0722 * b;
  const Z = (0.0193 * r + 0.1192 * g + 0.9505 * b) / 1.08883;
  const f = (t) => (t > 216 / 24389 ? Math.cbrt(t) : (24389 / 27 * t + 16) / 116);
  return [116 * f(Y) - 16, 500 * (f(X) - f(Y)), 200 * (f(Y) - f(Z))];
};
const deltaE = (a, b) => {
  const [A, B] = [lab(a), lab(b)];
  return Math.hypot(A[0] - B[0], A[1] - B[1], A[2] - B[2]);
};

describe('palette du mode daltonien', () => {
  const niveaux = NDVI_CATEGORIES.map((category) => affiche(category.colorDaltonien));

  it('reste lisible en deutéranopie : chaque niveau se distingue des autres (ΔE ≥ 10)', () => {
    niveaux.forEach((niveau, i) => {
      niveaux.slice(i + 1).forEach((autre) => {
        expect(deltaE(deuteranope(niveau), deuteranope(autre))).toBeGreaterThanOrEqual(10);
      });
    });
  });

  it('se distingue des couleurs déjà affichées dans la légende, en vue normale et en deutéranopie', () => {
    niveaux.forEach((niveau, i) => {
      LEGENDE_DALTONIEN.forEach((hex) => {
        const existante = affiche(hex);
        expect(deltaE(niveau, existante), `niveau ${i} vs ${hex}`).toBeGreaterThanOrEqual(10);
        expect(deltaE(deuteranope(niveau), deuteranope(existante)), `niveau ${i} vs ${hex} (deutéranope)`).toBeGreaterThanOrEqual(10);
      });
    });
  });

  it('distingue le gris « non mesuré » des niveaux et de la légende', () => {
    const neutre = affiche(NDVI_NEUTRAL_COLOR_DALTONIEN);
    [...niveaux, ...LEGENDE_DALTONIEN.map(affiche)].forEach((couleur) => {
      expect(deltaE(neutre, couleur)).toBeGreaterThanOrEqual(10);
      expect(deltaE(deuteranope(neutre), deuteranope(couleur))).toBeGreaterThanOrEqual(10);
    });
  });

  it('getNdviColor applique la palette du mode demandé', () => {
    expect(getNdviColor(0.6, true)).toBe('#434e6c');
    expect(getNdviColor(0.6)).toBe('#31a354');
    expect(getNdviColor(null, true)).toBe(NDVI_NEUTRAL_COLOR_DALTONIEN);
    expect(getNdviColor(null)).toBe(NDVI_NEUTRAL_COLOR);
  });
});
