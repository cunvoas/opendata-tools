import { describe, it, expect } from 'vitest';
import { hexToRgba, getNdviColor } from '../color.js';

describe('hexToRgba', () => {
  it('convertit une couleur hexadécimale', () => {
    expect(hexToRgba('#0000e8', 0.4)).toBe('rgba(0, 0, 232, 0.4)');
    expect(hexToRgba('#3aa637', 0.5)).toBe('rgba(58, 166, 55, 0.5)');
  });

  it('accepte la notation courte', () => {
    expect(hexToRgba('#fff', 1)).toBe('rgba(255, 255, 255, 1)');
  });

  it('retourne du css valide pour toute la palette', () => {
    const palette = ['#0000e8', '#6060e8', '#b0b0e8', '#578817', '#57ee17', '#959595', '#e96020', '#DC20E9'];
    for (const color of palette) {
      expect(hexToRgba(color, 0.4)).toMatch(/^rgba\(\d+, \d+, \d+, 0\.4\)$/);
    }
  });
});

describe('getNdviColor', () => {
  it('retourne une couleur neutre pour une valeur absente', () => {
    expect(getNdviColor(null)).toBe('#959595');
    expect(getNdviColor(undefined)).toBe('#959595');
    expect(getNdviColor('N/A')).toBe('#959595');
  });

  it('classe les valeurs NDVI par palier croissant', () => {
    expect(getNdviColor(0.1)).toBe('#d9f0a3');
    expect(getNdviColor(0.25)).toBe('#addd8e');
    expect(getNdviColor(0.35)).toBe('#78c679');
    expect(getNdviColor(0.45)).toBe('#31a354');
    expect(getNdviColor(0.7)).toBe('#006837');
  });

  it('accepte une valeur décimale à virgule', () => {
    expect(getNdviColor('0,55')).toBe('#006837');
  });
});
