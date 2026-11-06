import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';
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
