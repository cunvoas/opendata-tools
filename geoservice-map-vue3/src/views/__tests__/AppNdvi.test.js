import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount, flushPromises, config } from '@vue/test-utils';
import AppNdvi from '../AppNdvi.vue';
import SearchLocation from '../../components/SearchLocation.vue';
import { NDVI_CATEGORIES } from '../../utils/ndvi.js';
import { hexToRgba } from '../../utils/color.js';

// Le sélecteur charge ses référentiels sur GitHub : il est stubé et ses événements sont émis à la main.
config.global.stubs = { SearchLocation: true };

const payload = {
  annee: 2021,
  total: 10,
  withNdvi: 4,
  latestAcquisitionDate: '2025-06-10',
  parks: [
    { parkId: '1', name: 'Citadelle', surface: 822000, ndviMean: 0.593, ndviMin: 0.1, ndviMax: 0.9, ndviStddev: 0.3, acquisitionDate: '2025-06-10' },
    { parkId: '2', name: 'Parc A', surface: 1200, ndviMean: 0.35, ndviMin: 0.2, ndviMax: 0.5, ndviStddev: 0.1, acquisitionDate: '2025-06-09' },
    { parkId: '3', name: 'Parc B', surface: 900, ndviMean: 0.25, ndviMin: 0.1, ndviMax: 0.4, ndviStddev: 0.1, acquisitionDate: '2025-06-09' },
    { parkId: '4', name: 'Parc C', surface: 800, ndviMean: 0.15, ndviMin: 0.05, ndviMax: 0.3, ndviStddev: 0.1, acquisitionDate: '2025-06-08' }
  ]
};

function mockFetch(response) {
  vi.stubGlobal('fetch', vi.fn(() => Promise.resolve(response)));
}

const detailTable = (wrapper) => wrapper.find('.overflow-x-auto table');
const parkNames = (wrapper) => detailTable(wrapper).findAll('tbody tr th').map((th) => th.text());
const header = (wrapper, label) =>
  detailTable(wrapper).findAll('thead th').find((th) => th.text().startsWith(label));
const sortBy = (wrapper, label) => header(wrapper, label).find('button').trigger('click');
const rowOf = (wrapper, name) =>
  detailTable(wrapper).findAll('tbody tr').find((tr) => tr.find('th').text() === name);
const categoryOf = (wrapper, name) => rowOf(wrapper, name).findAll('td')[2].text();
const chartRows = (wrapper) => wrapper.findAll('div[role="group"] > div').map((row) => {
  const count = row.find('span.w-10');
  return {
    label: row.find('span').text(),
    bar: row.find('span.h-4'),
    countText: count.text(),
    count: Number.parseInt(count.text(), 10)
  };
});
const cssColor = (hex) => {
  const probe = document.createElement('span');
  probe.style.backgroundColor = hex;
  return probe.style.backgroundColor;
};
const filterBy = (wrapper, text) => wrapper.find('#ndviFiltreParc').setValue(text);

async function mountWith(parks = payload.parks) {
  mockFetch({ ok: true, json: () => Promise.resolve({ ...payload, parks }) });
  const wrapper = mount(AppNdvi);
  await flushPromises();
  return wrapper;
}

describe('AppNdvi', () => {
  beforeEach(() => {
    window.history.replaceState({}, '', '/');
    localStorage.clear();
    vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('affiche la couverture, les parcs sans NDVI et la date de fraîcheur', async () => {
    mockFetch({ ok: true, json: () => Promise.resolve(payload) });
    const wrapper = mount(AppNdvi);
    await flushPromises();

    expect(wrapper.text()).toContain('Parcs avec NDVI');
    expect(wrapper.text()).toContain('Parcs sans NDVI');
    expect(wrapper.text()).toContain('6 (60 %)');
    expect(wrapper.text()).toContain('10/06/2025');
    expect(wrapper.text()).toContain('Citadelle');
  });

  it('affiche la répartition sous forme de graphique lisible par les lecteurs d’écran', async () => {
    mockFetch({ ok: true, json: () => Promise.resolve(payload) });
    const wrapper = mount(AppNdvi);
    await flushPromises();
    const chart = wrapper.find('div[role="group"]');

    expect(chart.attributes('aria-label')).toBe('Répartition des parcs par tranche de NDVI moyen');
    expect(chart.attributes('aria-hidden')).toBeUndefined();
    expect(chartRows(wrapper)).toHaveLength(5);
  });

  it('affiche un message explicite pour une année sans NDVI', async () => {
    mockFetch({
      ok: true,
      json: () => Promise.resolve({ annee: 2015, total: 800, withNdvi: 0, latestAcquisitionDate: null, parks: [] })
    });
    const wrapper = mount(AppNdvi);
    await flushPromises();

    expect(wrapper.text()).toContain('NDVI non disponible pour ce territoire et cette année');
    expect(wrapper.text()).not.toContain('NDVI moyen');
  });

  it('affiche une erreur accessible quand l’API échoue', async () => {
    mockFetch({ ok: false, status: 500 });
    const wrapper = mount(AppNdvi);
    await flushPromises();

    expect(wrapper.find('[role="alert"]').exists()).toBe(true);
    expect(wrapper.find('table').exists()).toBe(false);
  });

  it('refuse une réponse vide au lieu d’afficher des données trompeuses', async () => {
    mockFetch({ ok: true, json: () => Promise.resolve({}) });
    const wrapper = mount(AppNdvi);
    await flushPromises();

    expect(wrapper.find('[role="alert"]').exists()).toBe(true);
  });

  it('charge la restitution de l’EPCI courant', async () => {
    window.history.replaceState({}, '', '/?com2co=42');
    mockFetch({ ok: true, json: () => Promise.resolve(payload) });
    mount(AppNdvi);
    await flushPromises();

    expect(fetch).toHaveBeenCalledWith(expect.stringContaining('/map/ndvi/summaries?annee=2021&com2co=42'));
  });

  it('reprend l’EPCI mémorisé par le sélecteur de localisation', async () => {
    localStorage.setItem('location-selected', JSON.stringify({ regionId: 11, com2coId: 21 }));
    mockFetch({ ok: true, json: () => Promise.resolve(payload) });
    mount(AppNdvi);
    await flushPromises();

    expect(fetch).toHaveBeenCalledWith(expect.stringContaining('com2co=21'));
  });

  it('reprend la commune mémorisée quand elle appartient à l’EPCI affiché', async () => {
    localStorage.setItem('location-selected', JSON.stringify({ com2coId: 1, cityInsee: '59350', cityName: 'LILLE (59350)' }));
    mockFetch({ ok: true, json: () => Promise.resolve(payload) });
    const wrapper = mount(AppNdvi);
    await flushPromises();

    expect(fetch).toHaveBeenCalledWith(expect.stringContaining('/map/ndvi/summaries?annee=2021&com2co=1&city=59350'));
    expect(wrapper.text()).toContain('Commune : LILLE (59350)');
  });

  it('ignore la commune mémorisée quand l’URL demande un autre EPCI', async () => {
    localStorage.setItem('location-selected', JSON.stringify({ com2coId: 1, cityInsee: '59350', cityName: 'LILLE (59350)' }));
    window.history.replaceState({}, '', '/?com2co=2');
    mockFetch({ ok: true, json: () => Promise.resolve(payload) });
    const wrapper = mount(AppNdvi);
    await flushPromises();

    expect(fetch).toHaveBeenCalledWith(expect.stringContaining('com2co=2'));
    expect(fetch).not.toHaveBeenCalledWith(expect.stringContaining('city='));
    expect(wrapper.text()).not.toContain('Commune :');
  });

  it('affiche « NDVI non disponible », et non une erreur, quand aucun fichier n’est publié pour l’EPCI', async () => {
    mockFetch({ ok: false, status: 404 });
    const wrapper = mount(AppNdvi);
    await flushPromises();

    expect(wrapper.find('[role="alert"]').exists()).toBe(false);
    expect(wrapper.text()).toContain('NDVI non disponible pour ce territoire et cette année');
  });

  describe('sélecteur de localisation', () => {
    const emitFrom = (wrapper, event, location) => wrapper.findComponent(SearchLocation).vm.$emit(event, location);

    it('affiche le sélecteur Région / EPCI / commune, sans recherche d’adresse', async () => {
      const wrapper = await mountWith();
      const selector = wrapper.findComponent(SearchLocation);

      expect(selector.exists()).toBe(true);
      expect(selector.props('displaySearchAddress')).toBe(false);
    });

    it('recharge les données dès qu’un autre EPCI est choisi, sans attendre la commune', async () => {
      const wrapper = await mountWith();
      expect(fetch).toHaveBeenCalledTimes(1);

      emitFrom(wrapper, 'com2co-selected', { regionId: 9, com2coId: 809, com2coName: 'CA Béthune' });
      await flushPromises();

      expect(fetch).toHaveBeenCalledTimes(2);
      expect(fetch).toHaveBeenLastCalledWith(expect.stringContaining('/map/ndvi/summaries?annee=2021&com2co=809'));
    });

    it('rappelle le service avec la commune choisie et l’indique au-dessus de la liste', async () => {
      const wrapper = await mountWith();

      emitFrom(wrapper, 'city-selected', { com2coId: 1, cityInsee: '59350', cityName: 'LILLE (59350)' });
      await flushPromises();

      expect(fetch).toHaveBeenCalledTimes(2);
      expect(fetch).toHaveBeenLastCalledWith(expect.stringContaining('/map/ndvi/summaries?annee=2021&com2co=1&city=59350'));
      expect(wrapper.text()).toContain('Commune : LILLE (59350)');
    });

    it('suit une commune d’un autre EPCI', async () => {
      const wrapper = await mountWith();

      emitFrom(wrapper, 'city-selected', { com2coId: 809, cityInsee: '62119', cityName: 'BETHUNE (62119)' });
      await flushPromises();

      expect(fetch).toHaveBeenLastCalledWith(expect.stringContaining('com2co=809&city=62119'));
    });

    it('ne rappelle pas le service quand la même commune est signalée deux fois (nombre ou texte)', async () => {
      const wrapper = await mountWith();

      emitFrom(wrapper, 'city-selected', { com2coId: 1, cityInsee: '59350', cityName: 'LILLE (59350)' });
      emitFrom(wrapper, 'city-selected', { com2coId: '1', cityInsee: '59350', cityName: 'LILLE (59350)' });
      await flushPromises();

      expect(fetch).toHaveBeenCalledTimes(2);
    });

    it('change de commune sans repasser par l’EPCI', async () => {
      const wrapper = await mountWith();

      emitFrom(wrapper, 'city-selected', { com2coId: 1, cityInsee: '59350', cityName: 'LILLE (59350)' });
      emitFrom(wrapper, 'city-selected', { com2coId: 1, cityInsee: '59512', cityName: 'ROUBAIX (59512)' });
      await flushPromises();

      expect(fetch).toHaveBeenLastCalledWith(expect.stringContaining('com2co=1&city=59512'));
      expect(wrapper.text()).toContain('Commune : ROUBAIX (59512)');
    });

    it('conserve la commune quand on change d’année', async () => {
      const wrapper = await mountWith();
      emitFrom(wrapper, 'city-selected', { com2coId: 1, cityInsee: '59350', cityName: 'LILLE (59350)' });
      await flushPromises();

      await wrapper.find('#ndviAnnee').setValue('2019');
      await flushPromises();

      expect(fetch).toHaveBeenLastCalledWith(expect.stringContaining('annee=2019&com2co=1&city=59350'));
    });

    it('un nouvel EPCI efface la commune', async () => {
      const wrapper = await mountWith();
      emitFrom(wrapper, 'city-selected', { com2coId: 1, cityInsee: '59350', cityName: 'LILLE (59350)' });
      await flushPromises();

      emitFrom(wrapper, 'com2co-selected', { com2coId: 809 });
      await flushPromises();

      expect(fetch).toHaveBeenLastCalledWith(expect.not.stringContaining('city='));
      expect(wrapper.text()).not.toContain('Commune :');
    });

    it('revient à toute l’intercommunalité depuis la commune', async () => {
      const wrapper = await mountWith();
      emitFrom(wrapper, 'city-selected', { com2coId: 1, cityInsee: '59350', cityName: 'LILLE (59350)' });
      await flushPromises();

      await wrapper.findAll('button').find((button) => button.text() === 'Voir toute l\'intercommunalité').trigger('click');
      await flushPromises();

      expect(fetch).toHaveBeenLastCalledWith(expect.not.stringContaining('city='));
      expect(wrapper.text()).not.toContain('Commune :');
    });

    it('ignore les localisations sans EPCI ou sans commune', async () => {
      const wrapper = await mountWith();

      emitFrom(wrapper, 'com2co-selected', { regionId: 9 });
      emitFrom(wrapper, 'com2co-selected', null);
      emitFrom(wrapper, 'city-selected', { com2coId: 1 });
      emitFrom(wrapper, 'city-selected', { cityInsee: '59350' });
      emitFrom(wrapper, 'city-selected', null);
      await flushPromises();

      expect(fetch).toHaveBeenCalledTimes(1);
    });

    it('ignore update-location : restauration du stockage ou commune périmée d’un autre EPCI', async () => {
      const wrapper = await mountWith();

      emitFrom(wrapper, 'update-location', { regionId: 9, com2coId: 809, cityId: 5 });
      await flushPromises();

      expect(fetch).toHaveBeenCalledTimes(1);
    });

    it('vide le filtre de nom quand le périmètre change, sans message de statut périmé', async () => {
      const wrapper = await mountWith();
      await filterBy(wrapper, 'citadelle');
      expect(parkNames(wrapper)).toEqual(['Citadelle']);

      emitFrom(wrapper, 'city-selected', { com2coId: 1, cityInsee: '59350', cityName: 'LILLE (59350)' });
      await flushPromises();

      expect(wrapper.find('#ndviFiltreParc').element.value).toBe('');
      expect(parkNames(wrapper)).toEqual(['Citadelle', 'Parc A', 'Parc B', 'Parc C']);
      expect(wrapper.find('p[role="status"]').text()).toBe('');
    });
  });

  describe('en production : fichiers JSON publiés', () => {
    const RAW = 'https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/ndvi';

    afterEach(() => {
      vi.unstubAllEnvs();
      vi.resetModules();
    });

    it('lit le fichier de l’EPCI, puis celui de la commune choisie', async () => {
      vi.stubEnv('VITE_DATA_MODE', 'static');
      vi.resetModules();
      const { default: StaticAppNdvi } = await import('../AppNdvi.vue');
      mockFetch({ ok: true, json: () => Promise.resolve(payload) });
      const wrapper = mount(StaticAppNdvi);
      await flushPromises();

      expect(fetch).toHaveBeenLastCalledWith(`${RAW}/1/ndvi_2021_1.json`);

      wrapper.findComponent({ name: 'SearchLocation' }).vm.$emit('city-selected', { com2coId: 1, cityInsee: '59350', cityName: 'LILLE (59350)' });
      await flushPromises();

      expect(fetch).toHaveBeenLastCalledWith(`${RAW}/1/59350/ndvi_2021_59350.json`);
    });
  });

  describe('tri du détail par parc', () => {
    it('trie par NDVI moyen décroissant par défaut et le signale par aria-sort', async () => {
      const wrapper = await mountWith();

      expect(parkNames(wrapper)).toEqual(['Citadelle', 'Parc A', 'Parc B', 'Parc C']);
      expect(header(wrapper, 'NDVI moyen').attributes('aria-sort')).toBe('descending');
      expect(detailTable(wrapper).findAll('[aria-sort]')).toHaveLength(1);
    });

    it('trie par colonne puis inverse le sens au second clic', async () => {
      const wrapper = await mountWith();

      await sortBy(wrapper, 'Surface');
      expect(parkNames(wrapper)).toEqual(['Parc C', 'Parc B', 'Parc A', 'Citadelle']);
      expect(header(wrapper, 'Surface').attributes('aria-sort')).toBe('ascending');
      expect(header(wrapper, 'NDVI moyen').attributes('aria-sort')).toBeUndefined();

      await sortBy(wrapper, 'Surface');
      expect(parkNames(wrapper)).toEqual(['Citadelle', 'Parc A', 'Parc B', 'Parc C']);
      expect(header(wrapper, 'Surface').attributes('aria-sort')).toBe('descending');
    });

    it('trie les noms sans accents ni casse, avec ordre naturel des nombres', async () => {
      const wrapper = await mountWith([
        { ...payload.parks[0], parkId: '1', name: 'Parc 10' },
        { ...payload.parks[0], parkId: '2', name: 'Parc 2' },
        { ...payload.parks[0], parkId: '3', name: 'Écluse' }
      ]);

      await sortBy(wrapper, 'Parc');
      expect(parkNames(wrapper)).toEqual(['Écluse', 'Parc 2', 'Parc 10']);
    });

    it('trie les dates d’acquisition', async () => {
      const wrapper = await mountWith();

      await sortBy(wrapper, 'Acquisition');
      expect(parkNames(wrapper)).toEqual(['Parc C', 'Parc A', 'Parc B', 'Citadelle']);
    });

    it('garde les valeurs absentes en fin de tableau dans les deux sens', async () => {
      const wrapper = await mountWith([
        { ...payload.parks[0], parkId: '1', name: 'Sans min A', ndviMin: null },
        { ...payload.parks[1], parkId: '2', name: 'Min 0.2', ndviMin: 0.2 },
        { ...payload.parks[2], parkId: '3', name: 'Sans min B', ndviMin: null },
        { ...payload.parks[3], parkId: '4', name: 'Min 0.1', ndviMin: 0.1 },
        { ...payload.parks[0], parkId: '5', name: 'Sans min C', ndviMin: null }
      ]);
      const absents = ['Sans min A', 'Sans min B', 'Sans min C'];

      await sortBy(wrapper, 'Min');
      expect(parkNames(wrapper)).toEqual(['Min 0.1', 'Min 0.2', ...absents]);
      await sortBy(wrapper, 'Min');
      expect(parkNames(wrapper)).toEqual(['Min 0.2', 'Min 0.1', ...absents]);
    });

    it('annonce le tri aux lecteurs d’écran, sans message au chargement', async () => {
      const wrapper = await mountWith();
      const status = wrapper.find('p[role="status"]');
      expect(status.text()).toBe('');

      await sortBy(wrapper, 'Surface');
      expect(status.text()).toBe('Tableau trié par Surface (m²), ordre croissant');
      expect(detailTable(wrapper).find('caption').text()).toContain('triées par Surface (m²), ordre croissant');
    });
  });

  describe('répartition par catégorie NDVI', () => {
    it('reprend les seuils universels et le libellé de chaque niveau, du plus faible au plus élevé', async () => {
      const wrapper = await mountWith();

      expect(chartRows(wrapper).map((row) => row.label)).toEqual([
        '-1,0 à 0,1 : Eau, neige, roche, bâti',
        '0,1 à 0,2 : Sol nu ou végétation morte',
        '0,2 à 0,4 : Végétation très clairsemée',
        '0,4 à 0,7 : Végétation en croissance',
        '0,7 à 1,0 : Zone de saturation'
      ]);
    });

    it('compte les parcs de chaque catégorie, annonce l’unité aux lecteurs d’écran et dimensionne les barres sur la plus grande', async () => {
      const wrapper = await mountWith();

      const rows = chartRows(wrapper);
      expect(rows.map((row) => row.countText)).toEqual(['0 parc', '1 parc', '2 parcs', '1 parc', '0 parc']);
      expect(rows.map((row) => row.bar.element.style.width)).toEqual(['0%', '50%', '100%', '50%', '0%']);
    });

    it('colore chaque barre avec la couleur de sa catégorie', async () => {
      const wrapper = await mountWith();

      chartRows(wrapper).forEach((row, index) => {
        expect(row.bar.element.style.backgroundColor, row.label).toBe(cssColor(hexToRgba(NDVI_CATEGORIES[index].color, 0.5)));
      });
    });

    it('classe comme la colonne « Catégorie NDVI », y compris aux seuils arrondis à l’affichage', async () => {
      const means = [-0.05, -0.0004, 0, 0.0996, 0.1, 0.1996, 0.2, 0.4996, 0.5, 1.2];
      const wrapper = await mountWith(means.map((ndviMean, i) => (
        { ...payload.parks[0], parkId: String(i), name: `Parc ${i}`, ndviMean }
      )));

      const fromColumn = {};
      detailTable(wrapper).findAll('tbody tr').forEach((tr) => {
        const category = tr.findAll('td')[2].text();
        fromColumn[category] = (fromColumn[category] || 0) + 1;
      });
      chartRows(wrapper).forEach((row) => {
        const category = row.label.split(' : ')[1];
        expect(row.count, category).toBe(fromColumn[category] || 0);
      });
    });

    it('ne compte pas un parc sans NDVI moyen', async () => {
      const wrapper = await mountWith([
        { ...payload.parks[0], parkId: '1', name: 'Mesuré', ndviMean: 0.5 },
        { ...payload.parks[1], parkId: '2', name: 'Sans mesure', ndviMean: null }
      ]);

      const total = chartRows(wrapper).reduce((sum, row) => sum + row.count, 0);
      expect(total).toBe(1);
    });
  });

  describe('catégorie NDVI', () => {
    it('classe chaque parc selon les seuils universels, bornes comprises', async () => {
      const cases = [
        [-0.05, 'Eau, neige, roche, bâti'],
        [-0.001, 'Eau, neige, roche, bâti'],
        [0, 'Eau, neige, roche, bâti'],
        [0.099, 'Eau, neige, roche, bâti'],
        [0.1, 'Sol nu ou végétation morte'],
        [0.199, 'Sol nu ou végétation morte'],
        [0.2, 'Végétation très clairsemée'],
        [0.399, 'Végétation très clairsemée'],
        [0.4, 'Végétation en croissance'],
        [0.699, 'Végétation en croissance'],
        [0.7, 'Zone de saturation'],
        [1, 'Zone de saturation']
      ];
      const wrapper = await mountWith(cases.map(([ndviMean], i) => (
        { ...payload.parks[0], parkId: String(i), name: `Parc ${ndviMean}`, ndviMean }
      )));

      cases.forEach(([ndviMean, expected]) => {
        expect(categoryOf(wrapper, `Parc ${ndviMean}`), `NDVI ${ndviMean}`).toBe(expected);
      });
    });

    it('classe sur la valeur affichée : 0,700 n’est jamais rangé sous le seuil 0,7', async () => {
      const wrapper = await mountWith([
        { ...payload.parks[0], parkId: '1', name: 'Limite haute', ndviMean: 0.6996 },
        { ...payload.parks[1], parkId: '2', name: 'Limite basse', ndviMean: 0.1996 }
      ]);

      expect(rowOf(wrapper, 'Limite haute').text()).toContain('0,700');
      expect(categoryOf(wrapper, 'Limite haute')).toBe('Zone de saturation');
      expect(rowOf(wrapper, 'Limite basse').text()).toContain('0,200');
      expect(categoryOf(wrapper, 'Limite basse')).toBe('Végétation très clairsemée');
    });

    it('suit le mode daltonien mémorisé : pastilles et barres de la palette daltonienne', async () => {
      localStorage.setItem('colorblindMode', 'true');
      const wrapper = await mountWith([{ ...payload.parks[0], parkId: '1', name: 'Dense', ndviMean: 0.6 }]);

      const swatch = rowOf(wrapper, 'Dense').find('td span[aria-hidden="true"]');
      expect(swatch.element.style.backgroundColor).toBe(cssColor(hexToRgba(NDVI_CATEGORIES[3].colorDaltonien, 0.5)));
      expect(chartRows(wrapper)[3].bar.element.style.backgroundColor).toBe(cssColor(hexToRgba(NDVI_CATEGORIES[3].colorDaltonien, 0.5)));
    });

    it('affiche la couleur de la catégorie dans une pastille, sans pastille quand le NDVI est absent', async () => {
      const wrapper = await mountWith([
        { ...payload.parks[0], parkId: '1', name: 'Dense', ndviMean: 0.6 },
        { ...payload.parks[1], parkId: '2', name: 'Sans mesure', ndviMean: null }
      ]);

      const swatch = rowOf(wrapper, 'Dense').find('td span[aria-hidden="true"]');
      expect(swatch.element.style.backgroundColor).toBe(cssColor(hexToRgba(NDVI_CATEGORIES[3].color, 0.5)));
      expect(rowOf(wrapper, 'Sans mesure').find('td span[aria-hidden="true"]').exists()).toBe(false);
    });

    it('affiche N/A quand le NDVI moyen est absent', async () => {
      const wrapper = await mountWith([{ ...payload.parks[0], parkId: '1', name: 'Sans mesure', ndviMean: null }]);

      expect(categoryOf(wrapper, 'Sans mesure')).toBe('N/A');
    });

    it('se trie comme le NDVI moyen', async () => {
      const wrapper = await mountWith();

      await sortBy(wrapper, 'Catégorie NDVI');
      expect(parkNames(wrapper)).toEqual(['Parc C', 'Parc B', 'Parc A', 'Citadelle']);
      expect(header(wrapper, 'Catégorie NDVI').attributes('aria-sort')).toBe('ascending');

      await sortBy(wrapper, 'Catégorie NDVI');
      expect(parkNames(wrapper)).toEqual(['Citadelle', 'Parc A', 'Parc B', 'Parc C']);
    });
  });

  describe('filtre sur le nom du parc', () => {
    it('propose un champ de recherche libellé', async () => {
      const wrapper = await mountWith();

      expect(wrapper.find('label[for="ndviFiltreParc"]').text()).toBe('Filtrer par nom de parc');
      expect(wrapper.find('#ndviFiltreParc').attributes('type')).toBe('search');
    });

    it('filtre sur une partie du nom, sans tenir compte de la casse', async () => {
      const wrapper = await mountWith();

      await filterBy(wrapper, 'PARC');
      expect(parkNames(wrapper)).toEqual(['Parc A', 'Parc B', 'Parc C']);
    });

    it('ignore les accents, dans les deux sens', async () => {
      const wrapper = await mountWith([
        { ...payload.parks[0], parkId: '1', name: 'Écluse' },
        { ...payload.parks[1], parkId: '2', name: 'Parc 2' }
      ]);

      await filterBy(wrapper, 'ecluse');
      expect(parkNames(wrapper)).toEqual(['Écluse']);
      await filterBy(wrapper, 'ÉCLU');
      expect(parkNames(wrapper)).toEqual(['Écluse']);
    });

    it('exige tous les mots saisis, dans n’importe quel ordre', async () => {
      const wrapper = await mountWith([
        { ...payload.parks[0], parkId: '1', name: 'Jardins du Bois de Boulogne' },
        { ...payload.parks[1], parkId: '2', name: 'Bois de la Deûle' },
        { ...payload.parks[2], parkId: '3', name: 'Square Boulogne' }
      ]);

      await filterBy(wrapper, 'boulogne bois');
      expect(parkNames(wrapper)).toEqual(['Jardins du Bois de Boulogne']);
    });

    it('accepte l’apostrophe typographique des claviers mobiles', async () => {
      const wrapper = await mountWith([
        { ...payload.parks[0], parkId: '1', name: "Jardin de poche Chapelle d'Elocques" },
        { ...payload.parks[1], parkId: '2', name: 'Parc A' }
      ]);

      await filterBy(wrapper, 'd’elocques');
      expect(parkNames(wrapper)).toEqual(["Jardin de poche Chapelle d'Elocques"]);
    });

    it('annonce le nombre de parcs affichés aux lecteurs d’écran', async () => {
      const wrapper = await mountWith();
      const status = wrapper.find('p[role="status"]');
      expect(status.text()).toBe('');

      await filterBy(wrapper, 'parc');
      expect(status.text()).toBe('3 parcs affichés sur 4');
      await filterBy(wrapper, 'citadelle');
      expect(status.text()).toBe('1 parc affiché sur 4');
      await filterBy(wrapper, 'introuvable');
      expect(status.text()).toBe('0 parc affiché sur 4');
      expect(parkNames(wrapper)).toEqual([]);
    });

    it('se combine avec le tri et se vide pour retrouver tous les parcs', async () => {
      const wrapper = await mountWith();

      await filterBy(wrapper, 'parc');
      await sortBy(wrapper, 'Surface');
      expect(parkNames(wrapper)).toEqual(['Parc C', 'Parc B', 'Parc A']);

      await filterBy(wrapper, '');
      expect(parkNames(wrapper)).toEqual(['Parc C', 'Parc B', 'Parc A', 'Citadelle']);
    });

    it('garde le filtre quand on change d’année', async () => {
      const wrapper = await mountWith();
      await filterBy(wrapper, 'parc');

      await wrapper.find('#ndviAnnee').setValue('2019');
      await flushPromises();

      expect(wrapper.find('#ndviFiltreParc').element.value).toBe('parc');
      expect(parkNames(wrapper)).toEqual(['Parc A', 'Parc B', 'Parc C']);
    });
  });
});
