import { describe, it, expect, afterEach, vi } from 'vitest';
import { isFeatureEnabled } from '../features.js';

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

describe('features.ndvi', () => {
  afterEach(() => {
    vi.unstubAllEnvs();
    vi.resetModules();
  });

  async function ndviFlag(flag, dev) {
    vi.stubEnv('VITE_FEATURE_NDVI', flag);
    vi.stubEnv('DEV', dev);
    vi.resetModules();
    return (await import('../features.js')).features.ndvi;
  }

  it('lit VITE_FEATURE_NDVI et le mode du build', async () => {
    expect(await ndviFlag('true', false)).toBe(true);
    expect(await ndviFlag('false', true)).toBe(false);
    expect(await ndviFlag('', true)).toBe(true);
    expect(await ndviFlag('', false)).toBe(false);
  });
});
