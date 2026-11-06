import { describe, it, expect, afterEach, vi } from 'vitest';
import { isFeatureEnabled } from '../config.js';

describe('isFeatureEnabled', () => {
  it('sans valeur, suit le mode développement', () => {
    expect(isFeatureEnabled(undefined, true)).toBe(true);
    expect(isFeatureEnabled(undefined, false)).toBe(false);
    expect(isFeatureEnabled('', true)).toBe(true);
    expect(isFeatureEnabled('', false)).toBe(false);
  });

  it('« true » active, quel que soit le mode', () => {
    expect(isFeatureEnabled('true', false)).toBe(true);
    expect(isFeatureEnabled(' TRUE ', false)).toBe(true);
  });

  it('toute autre valeur désactive, même en développement', () => {
    expect(isFeatureEnabled('false', true)).toBe(false);
    expect(isFeatureEnabled('0', true)).toBe(false);
    expect(isFeatureEnabled('oui', true)).toBe(false);
  });
});

describe('configuration des données', () => {
  afterEach(() => {
    vi.unstubAllEnvs();
    vi.resetModules();
  });

  async function load(env) {
    Object.entries(env).forEach(([name, value]) => vi.stubEnv(name, value));
    vi.resetModules();
    return import('../config.js');
  }

  it('features.ndvi lit VITE_FEATURE_NDVI et le mode du build', async () => {
    expect((await load({ VITE_FEATURE_NDVI: 'true', DEV: false })).features.ndvi).toBe(true);
    expect((await load({ VITE_FEATURE_NDVI: 'false', DEV: true })).features.ndvi).toBe(false);
    expect((await load({ VITE_FEATURE_NDVI: '', DEV: true })).features.ndvi).toBe(true);
    expect((await load({ VITE_FEATURE_NDVI: '', DEV: false })).features.ndvi).toBe(false);
  });

  it('DATA_MODE vaut static sans VITE_DATA_MODE, quel que soit le mode', async () => {
    expect((await load({ VITE_DATA_MODE: '', DEV: true })).DATA_MODE).toBe('static');
    expect((await load({ VITE_DATA_MODE: '', DEV: false })).DATA_MODE).toBe('static');
  });

  it('VITE_DATA_MODE prime sur la valeur par défaut', async () => {
    expect((await load({ VITE_DATA_MODE: 'dynamic', DEV: false })).DATA_MODE).toBe('dynamic');
    expect((await load({ VITE_DATA_MODE: 'static', DEV: true })).DATA_MODE).toBe('static');
  });

  it('VITE_STATIC_ON_GIT remplace la racine des JSON publiés, sinon le dépôt par défaut', async () => {
    expect((await load({ VITE_STATIC_ON_GIT: '', DEV: false })).STATIC_ON_GIT)
      .toBe('https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main');
    expect((await load({ VITE_STATIC_ON_GIT: 'https://example.test/data', DEV: false })).STATIC_ON_GIT)
      .toBe('https://example.test/data');
  });

  it('VITE_DYNAMIC_API remplace la racine de l’API, sans barre finale', async () => {
    expect((await load({ VITE_DYNAMIC_API: '' })).DYNAMIC_ON_LOCAL).toMatch(/\/isochrone$/);
    expect((await load({ VITE_DYNAMIC_API: 'https://api.example.test/isochrone/' })).DYNAMIC_ON_LOCAL)
      .toBe('https://api.example.test/isochrone');
  });
});
