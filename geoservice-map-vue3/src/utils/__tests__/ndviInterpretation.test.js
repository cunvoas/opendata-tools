import { describe, it, expect } from 'vitest';
import { interpreterStatistiquesNdvi } from '../ndviInterpretation.js';

const stats = (overrides = {}) => ({ moyenne: 0.6, minimum: 0.2, maximum: 0.7, ecartType: 0.1, ...overrides });
const vigueurOf = (overrides) => interpreterStatistiquesNdvi(stats(overrides)).split('\n')[0];
const structureOf = (overrides) => interpreterStatistiquesNdvi(stats(overrides)).split('\n')[1];

describe('interpreterStatistiquesNdvi', () => {
  it('donne deux lignes : vigueur puis structure spatiale', () => {
    const lines = interpreterStatistiquesNdvi(stats()).split('\n');

    expect(lines).toHaveLength(2);
    expect(lines[0]).toMatch(/^Diagnostic : /);
    expect(lines[1]).toMatch(/^Structure spatiale : /);
  });

  it('donne la vigueur selon la moyenne, bornes basses incluses', () => {
    expect(vigueurOf({ moyenne: 0.05 })).toBe('Diagnostic : Absence de végétation (surfaces minérales, eau, sols nus).');
    expect(vigueurOf({ moyenne: 0.1 })).toBe('Diagnostic : Sol nu ou végétation morte/sèche.');
    expect(vigueurOf({ moyenne: 0.2 })).toBe('Diagnostic : Végétation très clairsemée ou en stress (pelouse tondue, début de levée).');
    expect(vigueurOf({ moyenne: 0.4 })).toBe('Diagnostic : Végétation en croissance active, couverture du sol modérée.');
    expect(vigueurOf({ moyenne: 0.7 })).toBe('Diagnostic : Zone de saturation : canopée dense, forêt ou végétation à son maximum ; une plante malade peut encore afficher 0,85.');
  });

  it('donne la structure selon l’écart-type, bornes basses incluses', () => {
    expect(structureOf({ ecartType: 0.05 })).toMatch(/^Structure spatiale : Zone parfaitement homogène/);
    expect(structureOf({ ecartType: 0.08 })).toMatch(/^Structure spatiale : Zone relativement uniforme/);
    expect(structureOf({ ecartType: 0.15 })).toMatch(/^Structure spatiale : Forte mosaïque paysagère/);
  });

  it('signale les extrêmes avec une tournure correcte pour une ou deux remarques', () => {
    expect(structureOf({ minimum: 0.05, maximum: 0.8 })).toBe(
      'Structure spatiale : Zone relativement uniforme avec de légères variations contenant à la fois des surfaces non-végétalisées (sols nus, bâtis, routes) et des zones de végétation au maximum de leur potentiel chlorophyllien.'
    );
    expect(structureOf({ minimum: 0.05, maximum: 0.7 })).toBe(
      'Structure spatiale : Zone relativement uniforme avec de légères variations contenant des surfaces non-végétalisées (sols nus, bâtis, routes).'
    );
    expect(structureOf({ minimum: 0.2, maximum: 0.8 })).toBe(
      'Structure spatiale : Zone relativement uniforme avec de légères variations contenant des zones de végétation au maximum de leur potentiel chlorophyllien.'
    );
    expect(structureOf({ minimum: 0.2, maximum: 0.7 })).toBe(
      'Structure spatiale : Zone relativement uniforme avec de légères variations.'
    );
  });

  it('classe sur la valeur affichée à 3 décimales', () => {
    expect(vigueurOf({ moyenne: 0.4996 })).toBe('Diagnostic : Végétation en croissance active, couverture du sol modérée.');
    expect(structureOf({ ecartType: 0.0799 })).toMatch(/^Structure spatiale : Zone relativement uniforme/);
  });

  it('ne produit aucun diagnostic si une statistique manque, sans prendre null pour 0', () => {
    expect(interpreterStatistiquesNdvi(stats({ moyenne: null }))).toBe('');
    expect(interpreterStatistiquesNdvi(stats({ minimum: undefined }))).toBe('');
    expect(interpreterStatistiquesNdvi(stats({ ecartType: 'N/A' }))).toBe('');
  });

  it('ne parle jamais de culture ou d’agriculture : les parcs sont urbains ou périurbains', () => {
    const agricole = /cultur|agricol|prairie|champ|récolte|monoculture/i;
    for (const moyenne of [0.05, 0.2, 0.4, 0.6, 0.8]) {
      for (const ecartType of [0.05, 0.1, 0.2]) {
        for (const minimum of [0.05, 0.2]) {
          for (const maximum of [0.7, 0.8]) {
            expect(interpreterStatistiquesNdvi({ moyenne, minimum, maximum, ecartType })).not.toMatch(agricole);
          }
        }
      }
    }
  });
});
