import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import StatsGraph from '../StatsGraph.vue';

const stubs = { Bar: true, Pie: true, StatsTableAccessibility: true };

describe('StatsGraph - source des données en mode dynamic', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.resolve({ ok: false, status: 404 })));
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('ville : lit les stats via /map/stats/city de l’API locale', async () => {
    const wrapper = mount(StatsGraph, { global: { stubs } });

    await wrapper.vm.processLocation({ locType: 'city', cityInsee: '59350', com2coId: 1, regionId: 9 });

    expect(fetch).toHaveBeenLastCalledWith(`http://localhost:8980/isochrone/map/stats/city/59350?annee=${wrapper.vm.annee}`);
  });

  it('EPCI : lit les stats via /map/stats/com2co/<id>/<type> de l’API locale', async () => {
    const wrapper = mount(StatsGraph, { global: { stubs } });

    await wrapper.vm.processLocation({ locType: 'com2co', com2coId: 1, regionId: 9, graphType: 'suburbs' });

    expect(fetch).toHaveBeenLastCalledWith(`http://localhost:8980/isochrone/map/stats/com2co/1/suburbs?annee=${wrapper.vm.annee}`);
  });
});

describe('StatsGraph - sélecteur d’année', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.resolve({ ok: false, status: 404 })));
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('propose les millésimes publiés, 2021 par défaut', () => {
    const wrapper = mount(StatsGraph, { props: { location: {} }, global: { stubs } });
    const select = wrapper.find('#anneeStats');

    expect(select.element.value).toBe('2021');
    expect(select.findAll('option').map((option) => option.text())).toEqual(['2021', '2019', '2017', '2015']);
    expect(wrapper.find('label[for="anneeStats"]').text()).toBe('Année');
  });

  it('recharge les statistiques du même territoire pour le millésime choisi', async () => {
    const wrapper = mount(StatsGraph, {
      props: { location: { locType: 'city', cityInsee: '59350' } },
      global: { stubs }
    });
    await flushPromises();

    await wrapper.find('#anneeStats').setValue('2019');
    await flushPromises();

    expect(fetch).toHaveBeenLastCalledWith('http://localhost:8980/isochrone/map/stats/city/59350?annee=2019');
  });
});

describe('StatsGraph - info-bulles', () => {
  const label = (wrapper, options) =>
    options.plugins.tooltip.callbacks.label.call(wrapper.vm, {
      parsed: options === wrapper.vm.myOptions ? 0 : { y: 0 },
      label: 'Faible',
      dataset: { data: [0, 0] }
    });

  it('n’affiche pas NaN% quand le jeu de données ne contient que des zéros', () => {
    const wrapper = mount(StatsGraph, { props: { location: {} }, global: { stubs } });

    expect(label(wrapper, wrapper.vm.barOptions)).toBe('0 habitants (0.0%)');
    expect(label(wrapper, wrapper.vm.myOptions)).toBe('Faible: 0 habitants (0.0%)');
  });
});
