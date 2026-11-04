import { describe, it, expect, afterEach, vi } from 'vitest';

// Seule la déclaration des routes compte ici : les écrans sont remplacés par des composants vides.
vi.mock('../../views/AppCarte.vue', () => ({ default: { render: () => null } }));
vi.mock('../../views/AppStatistics.vue', () => ({ default: { render: () => null } }));
vi.mock('../../views/AppInformation.vue', () => ({ default: { render: () => null } }));
vi.mock('../../views/AppVideoAdmin.vue', () => ({ default: { render: () => null } }));

async function loadRouter(flag) {
  vi.stubEnv('VITE_FEATURE_NDVI', flag);
  vi.resetModules();
  return (await import('../index.js')).default;
}

describe('router : écran NDVI derrière le feature flag VITE_FEATURE_NDVI', () => {
  afterEach(() => {
    vi.unstubAllEnvs();
    vi.resetModules();
  });

  it('déclare la route /ndvi quand le flag est actif', async () => {
    const router = await loadRouter('true');

    expect(router.hasRoute('ndvi')).toBe(true);
    expect(router.resolve('/ndvi').name).toBe('ndvi');
  });

  it('redirige /ndvi vers /carte quand le flag est inactif', async () => {
    const router = await loadRouter('false');

    expect(router.hasRoute('ndvi')).toBe(false);
    await router.push('/ndvi');
    expect(router.currentRoute.value.path).toBe('/carte');
  });

  it('garde les autres écrans quand le flag est inactif', async () => {
    const router = await loadRouter('false');

    ['appCarte', 'stats', 'appInformation', 'appVideoAdmin'].forEach((name) => {
      expect(router.hasRoute(name), name).toBe(true);
    });
  });
});
