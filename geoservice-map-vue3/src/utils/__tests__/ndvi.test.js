import { readFileSync, existsSync } from 'node:fs';
import { resolve } from 'node:path';
import { describe, it, expect } from 'vitest';
import { NDVI_CATEGORIES, NDVI_NEUTRAL_COLOR, NDVI_NEUTRAL_COLOR_DALTONIEN, getNdviCategory, getNdviColor } from '../ndvi.js';

describe('NDVI_CATEGORIES', () => {
  it('reprend les cinq seuils universels par type de surface, du plus faible au plus élevé', () => {
    expect(NDVI_CATEGORIES.map((category) => [category.range, category.min])).toEqual([
      ['moins de 0', -Infinity],
      ['0 à 0,1', 0],
      ['0,1 à 0,2', 0.1],
      ['0,2 à 0,5', 0.2],
      ['0,5 à 1,0', 0.5]
    ]);
  });

  it('porte le texte complet de chaque type de surface', () => {
    expect(NDVI_CATEGORIES.map((category) => category.description)).toEqual([
      'Eau (mers, lacs, rivières), neige ou nuages.',
      'Sols nus, roches, sable ou zones urbaines artificielles.',
      'Végétation très éparse, prairies sèches ou début de sénescence.',
      'Végétation modérée, cultures en croissance, prairies vertes ou arbustes.',
      'Végétation dense et saine, forêts tempérées/tropicales ou cultures en pleine maturité.'
    ]);
  });

  it('donne à chaque niveau un libellé court et une couleur propres', () => {
    expect(NDVI_CATEGORIES.map((category) => category.label)).toEqual([
      'Eau, neige ou nuages',
      'Sols nus et zones artificielles',
      'Végétation très éparse',
      'Végétation modérée',
      'Végétation dense et saine'
    ]);
    const colors = NDVI_CATEGORIES.map((category) => category.color);
    expect(new Set(colors).size).toBe(NDVI_CATEGORIES.length);
    expect(colors).not.toContain(NDVI_NEUTRAL_COLOR);
  });
});

describe('getNdviCategory', () => {
  const labelOf = (value) => getNdviCategory(value)?.label;

  it('classe chaque valeur dans sa tranche, borne basse incluse', () => {
    expect(labelOf(-0.5)).toBe('Eau, neige ou nuages');
    expect(labelOf(-0.001)).toBe('Eau, neige ou nuages');
    expect(labelOf(0)).toBe('Sols nus et zones artificielles');
    expect(labelOf(0.099)).toBe('Sols nus et zones artificielles');
    expect(labelOf(0.1)).toBe('Végétation très éparse');
    expect(labelOf(0.199)).toBe('Végétation très éparse');
    expect(labelOf(0.2)).toBe('Végétation modérée');
    expect(labelOf(0.499)).toBe('Végétation modérée');
    expect(labelOf(0.5)).toBe('Végétation dense et saine');
    expect(labelOf(1)).toBe('Végétation dense et saine');
    expect(labelOf(1.3)).toBe('Végétation dense et saine');
  });

  it('classe sur la valeur arrondie à l’affichage : 0,500 n’est jamais rangé sous le seuil 0,5', () => {
    expect(labelOf(0.4996)).toBe('Végétation dense et saine');
    expect(labelOf(0.4994)).toBe('Végétation modérée');
    expect(labelOf(0.1996)).toBe('Végétation modérée');
    expect(labelOf(0.0996)).toBe('Végétation très éparse');
    expect(labelOf(-0.0004)).toBe('Sols nus et zones artificielles');
  });

  it('accepte un texte, avec virgule décimale', () => {
    expect(labelOf('0,593')).toBe('Végétation dense et saine');
    expect(labelOf('0.15')).toBe('Végétation très éparse');
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
  return new Function(`${block}; return { NDVI_CATEGORIES, createNdviHtml };`)();
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
    expect(getNdviColor(0.6, true)).toBe('#00224e');
    expect(getNdviColor(0.6)).toBe('#006837');
    expect(getNdviColor(null, true)).toBe(NDVI_NEUTRAL_COLOR_DALTONIEN);
    expect(getNdviColor(null)).toBe(NDVI_NEUTRAL_COLOR);
  });
});
