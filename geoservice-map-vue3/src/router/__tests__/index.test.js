import { describe, it, expect, afterEach, vi } from 'vitest';

// Seule la déclaration des routes compte ici : les écrans sont remplacés par des composants vides.
vi.mock('../../views/AppLanding.vue', () => ({ default: { render: () => null } }));
vi.mock('../../views/AppCarte.vue', () => ({ default: { render: () => null } }));
vi.mock('../../views/AppStatistics.vue', () => ({ default: { render: () => null } }));
vi.mock('../../views/AppEtudeInsee.vue', () => ({ default: { render: () => null } }));
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

    ['appLanding', 'appCarte', 'stats', 'appInformation', 'appEtudeInsee', 'appVideoAdmin'].forEach((name) => {
      expect(router.hasRoute(name), name).toBe(true);
    });
  });
});

describe('router : landing à la racine', () => {
  afterEach(() => {
    vi.unstubAllEnvs();
    vi.resetModules();
  });

  it('sert la landing sur / sans redirection vers la carte', async () => {
    const router = await loadRouter('false');

    await router.push('/');
    expect(router.currentRoute.value.path).toBe('/');
    expect(router.currentRoute.value.name).toBe('appLanding');
  });

  it('garde la redirection des adresses inconnues vers la carte', async () => {
    const router = await loadRouter('false');

    await router.push('/page-inconnue');
    expect(router.currentRoute.value.path).toBe('/carte');
  });

  it('chaque écran a un titre et une description pour le référencement', async () => {
    const router = await loadRouter('false');

    router.getRoutes().filter((r) => r.name).forEach((r) => {
      expect(r.meta.title, r.name).toBeTruthy();
      expect(r.meta.description, r.name).toBeTruthy();
    });
  });

  it("n'indexe pas la page étude INSEE tant qu'elle n'a pas de contenu propre", async () => {
    const router = await loadRouter('false');

    expect(router.resolve('/etude-insee').name).toBe('appEtudeInsee');
    expect(router.resolve('/etude-insee').meta.robots).toContain('noindex');
    expect(router.resolve('/carte').meta.robots).toBeUndefined();
  });

  it('la landing a un titre et une description qui tiennent dans les résultats de recherche et citent Lille', async () => {
    const router = await loadRouter('false');
    const { title, description } = router.resolve('/').meta;

    expect(title.length).toBeLessThanOrEqual(62);
    expect(description.length).toBeLessThanOrEqual(155);
    expect(title).toMatch(/Lille/);
    expect(title).toMatch(/Espaces verts/i);
    expect(description).toContain('Métropole Européenne de Lille');
  });

  it('charge les écrans à la demande, sauf la landing, pour alléger la première page', async () => {
    const router = await loadRouter('false');
    const parNom = Object.fromEntries(router.getRoutes().filter((r) => r.name).map((r) => [r.name, r.components.default]));

    // un composant importé à la demande est une fonction ; la landing est un objet composant
    ['appCarte', 'stats', 'appInformation', 'appEtudeInsee', 'appVideoAdmin'].forEach((nom) => {
      expect(typeof parNom[nom], nom).toBe('function');
    });
    expect(typeof parNom.appLanding).toBe('object');
    await router.push('/carte');
    expect(router.currentRoute.value.name).toBe('appCarte');
  });
});
