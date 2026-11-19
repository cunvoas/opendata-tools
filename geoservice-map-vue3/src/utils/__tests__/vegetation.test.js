import { describe, expect, it } from 'vitest';
import {
  VEGETATION_INDICES,
  anomalieLabel,
  availableIndices,
  indexStats,
  vegetationEvaluation
} from '../vegetation.js';

const park = {
  ndviMean: 0.593, ndviMin: 0.1, ndviMax: 0.9, ndviStddev: 0.3,
  ndreMean: 0.36, ndreMin: 0.2, ndreMax: 0.5, ndreStddev: 0.06,
  gndviMean: 0.55, gndviMin: 0.3, gndviMax: 0.7, gndviStddev: 0.05,
  saviMean: 0.5, saviMin: 0.2, saviMax: 0.7, saviStddev: 0.1,
  evaluation: {
    recouvrement: 'Couvert dense / Strate arborée ou parc mature',
    scoreSante: 46.5,
    confiance: 'Élevée',
    diagnosticPhysiologique: 'Normal',
    homogeneite: 'Modérément hétérogène',
    anomalieLocaleDetectee: true
  }
};

describe('vegetation utils', () => {
  it('expose les quatre indices dans l’ordre NDVI, NDRE, GNDVI, SAVI', () => {
    expect(VEGETATION_INDICES.map((i) => i.label)).toEqual(['NDVI', 'NDRE', 'GNDVI', 'SAVI']);
  });

  it('lit les statistiques d’un indice', () => {
    expect(indexStats(park, VEGETATION_INDICES[3])).toEqual({
      label: 'SAVI', mean: 0.5, min: 0.2, max: 0.7, std: 0.1
    });
  });

  it('ignore un indice sans moyenne', () => {
    expect(indexStats({}, VEGETATION_INDICES[0])).toBeNull();
    expect(availableIndices({ saviMean: 0.4 })).toHaveLength(1);
  });

  it('accepte les nombres au format texte et à virgule', () => {
    expect(indexStats({ saviMean: '0,42' }, VEGETATION_INDICES[3]).mean).toBeCloseTo(0.42);
  });

  it('lit l’évaluation imbriquée du résumé NDVI', () => {
    const evaluation = vegetationEvaluation(park);
    expect(evaluation.score).toBeCloseTo(46.5);
    expect(evaluation.confiance).toBe('Élevée');
    expect(evaluation.anomalie).toBe(true);
  });

  it('lit l’évaluation plate des propriétés de carte', () => {
    const evaluation = vegetationEvaluation({
      vegetationScore: 36.6,
      vegetationRecouvrement: 'Couvert faible',
      vegetationConfiance: 'Moyenne',
      vegetationDiagnostic: 'Normal',
      vegetationHomogeneite: 'Homogène',
      vegetationAnomalie: null
    });
    expect(evaluation.score).toBeCloseTo(36.6);
    expect(evaluation.recouvrement).toBe('Couvert faible');
    expect(evaluation.anomalie).toBeNull();
  });

  it('renvoie null sans évaluation', () => {
    expect(vegetationEvaluation({ ndviMean: 0.5 })).toBeNull();
  });

  it('libelle l’anomalie, y compris non calculable', () => {
    expect(anomalieLabel(true)).toBe('oui');
    expect(anomalieLabel(false)).toBe('non');
    expect(anomalieLabel(null)).toBe('non calculable');
  });
});
