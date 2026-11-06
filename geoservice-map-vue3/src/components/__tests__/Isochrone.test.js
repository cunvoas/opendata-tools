import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';

vi.mock('axios', () => ({
  default: { get: vi.fn(() => Promise.resolve({ data: {} })) }
}));

vi.mock('../../utils/config.js', async (importOriginal) => ({
  ...(await importOriginal()),
  features: { ndvi: true }
}));

import Isochrone from '../Isochrone.vue';
import { features } from '../../utils/config.js';
import { NDVI_CATEGORIES, NDVI_CONTOUR_DALTONIEN } from '../../utils/ndvi.js';

const stubs = {
  'l-map': { template: '<div class="l-map-stub"><slot /></div>' },
  'l-tile-layer': true,
  'l-control-layers': true,
  'l-control-scale': true,
  'l-geo-json': true,
  'l-control': { template: '<div><slot /></div>' }
};

function mountIsochrone() {
  return mount(Isochrone, { global: { stubs } });
}

describe('Isochrone - bouton accessibilité (mode daltonien)', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('remplace la case à cocher par un bouton pastille aria-pressed', () => {
    const wrapper = mountIsochrone();
    expect(wrapper.find('#cbColorblind').exists()).toBe(false);

    const button = wrapper.find('button.a11y-toggle');
    expect(button.exists()).toBe(true);
    expect(button.text()).toContain('Mode daltonien');
    expect(button.attributes('aria-pressed')).toBe('false');
    expect(button.attributes('type')).toBe('button');
  });

  it('bascule le mode au clic : état, classe active, localStorage, événement', async () => {
    const wrapper = mountIsochrone();
    const button = wrapper.find('button.a11y-toggle');

    await button.trigger('click');
    expect(button.attributes('aria-pressed')).toBe('true');
    expect(button.classes()).toContain('a11y-toggle--active');
    expect(localStorage.getItem('colorblindMode')).toBe('true');
    expect(wrapper.emitted('colorblind-mode-changed').at(-1)).toEqual([true]);

    await button.trigger('click');
    expect(button.attributes('aria-pressed')).toBe('false');
    expect(button.classes()).not.toContain('a11y-toggle--active');
    expect(localStorage.getItem('colorblindMode')).toBe('false');
    expect(wrapper.emitted('colorblind-mode-changed').at(-1)).toEqual([false]);
  });

  it('reste le seul contrôle de mode (pas de doublon)', () => {
    const wrapper = mountIsochrone();
    expect(wrapper.findAll('button.a11y-toggle')).toHaveLength(1);
    expect(wrapper.find('label[for="cbColorblind"]').exists()).toBe(false);
  });
});

describe('Isochrone - mode et détail NDVI', () => {
  beforeEach(() => {
    localStorage.clear();
    const detail = document.createElement('div');
    detail.id = 'dataDetail';
    document.body.appendChild(detail);
  });

  afterEach(() => {
    document.getElementById('dataDetail')?.remove();
    features.ndvi = true;
  });

  const feature = {
    properties: {
      name: 'Citadelle',
      ndviEnabled: true,
      ndviMean: 0.593,
      ndviMin: 0.1,
      ndviMax: 0.9,
      ndviStddev: 0.3,
      ndviAcquisitionDate: '2025-06-10'
    }
  };

  it('active le bouton NDVI sans ajouter d’entrée de calque', async () => {
    const wrapper = mountIsochrone();
    const button = wrapper.find('button.ndvi-toggle');

    expect(button.exists()).toBe(true);
    expect(button.attributes('aria-pressed')).toBe('false');
    await button.trigger('click');
    expect(button.attributes('aria-pressed')).toBe('true');
    expect(wrapper.findAll('input[type="checkbox"]')).toHaveLength(4);
  });

  it('colore les parcs selon le NDVI et laisse les parcs sans mesure en neutre', () => {
    const wrapper = mountIsochrone();
    wrapper.vm.ndviMode = true;

    expect(wrapper.vm.computeParcStyle(feature).fillColor).toBe('#006837');
    expect(wrapper.vm.computeParcStyle({ properties: { ndviEnabled: true } }).fillColor).toBe('#959595');
  });

  it('colore chaque parc avec la couleur de sa catégorie de la grille NDVI', () => {
    const wrapper = mountIsochrone();
    wrapper.vm.ndviMode = true;

    NDVI_CATEGORIES.forEach((category) => {
      const ndviMean = Number.isFinite(category.min) ? category.min : -0.2;
      const style = wrapper.vm.computeParcStyle({ properties: { ndviEnabled: true, ndviMean } });
      expect(style.fillColor, category.range).toBe(category.color);
    });
  });

  it('prévient la page quand le mode NDVI change, pour sa légende', async () => {
    const wrapper = mountIsochrone();
    const button = wrapper.find('button.ndvi-toggle');

    await button.trigger('click');
    await button.trigger('click');

    expect(wrapper.emitted('ndvi-mode-changed')).toEqual([[true], [false]]);
  });

  it('restyle le calque des parcs à chaque changement de mode NDVI', async () => {
    const setStyle = vi.fn();
    const geoJsonStub = { template: '<div />', data: () => ({ leafletObject: { setStyle } }) };
    const wrapper = mount(Isochrone, { global: { stubs: { ...stubs, 'l-geo-json': geoJsonStub } } });
    const button = wrapper.find('button.ndvi-toggle');
    const dense = NDVI_CATEGORIES.at(-1).color;

    await button.trigger('click');
    expect(setStyle).toHaveBeenCalledTimes(1);
    expect(setStyle.mock.calls[0][0](feature).fillColor).toBe(dense);

    await button.trigger('click');
    expect(setStyle).toHaveBeenCalledTimes(2);
    expect(setStyle.mock.calls[1][0](feature).fillColor).not.toBe(dense);
  });

  it('masque le bouton NDVI et toute information NDVI quand le feature flag est inactif', () => {
    features.ndvi = false;
    const wrapper = mountIsochrone();

    expect(wrapper.find('button.ndvi-toggle').exists()).toBe(false);
    expect(wrapper.vm.buildNdviHtml(feature.properties)).toBe('');
  });

  it('affiche les valeurs NDVI au clic dans le panneau Détail des données', () => {
    const wrapper = mountIsochrone();
    const handlers = {};
    const layer = {
      bindTooltip: vi.fn(),
      setStyle: vi.fn(),
      on: vi.fn((event, handler) => { handlers[event] = handler; })
    };

    wrapper.vm.onDetailPark(feature, layer);
    handlers.click();

    const html = document.getElementById('dataDetail').innerHTML;
    expect(html).toContain('Citadelle');
    expect(html).toContain('<b>NDVI moyen : 0,593</b>');
    expect(html).toContain('(min 0,100, max 0,900)');
    expect(html).toContain('<i>0,5 à 1,0 :</i>');
    expect(html).toContain('Végétation dense et saine');
  });

  it('affiche « pas encore calculé » sans valeur implicite pour un parc sans NDVI', () => {
    const wrapper = mountIsochrone();
    const properties = { name: 'Parc sans mesure', ndviEnabled: true, ndviMean: null };

    expect(wrapper.vm.buildNdviHtml(properties)).toContain('NDVI : pas encore calculé');
    expect(wrapper.vm.buildNdviHtml(properties)).not.toContain('0,000');
  });

  it('affiche le picto et le libellé de chaque seuil NDVI, en couleur de carte (daltonien compris)', () => {
    localStorage.clear();
    const wrapper = mountIsochrone();

    NDVI_CATEGORIES.forEach((category) => {
      const ndviMean = Number.isFinite(category.min) ? category.min : -0.2;
      const html = wrapper.vm.buildNdviHtml({ ndviEnabled: true, ndviMean });
      expect(html, category.range).toContain(`<i>${category.range} :</i>`);
      expect(html, category.range).toContain(`background:${category.color}`);
    });

    wrapper.vm.colorblindMode = true;
    const daltonien = wrapper.vm.buildNdviHtml({ ndviEnabled: true, ndviMean: 0.6 });
    expect(daltonien).toContain(`background:${NDVI_CATEGORIES.at(-1).colorDaltonien}`);
  });

  it('n’affiche aucune mention NDVI quand la feature est désactivée', () => {
    const wrapper = mountIsochrone();
    expect(wrapper.vm.buildNdviHtml({ ndviEnabled: false, ndviMean: 0.5 })).toBe('');
  });
});

describe('Isochrone - changement d’EPCI', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('met à jour le lien partageable sans erreur quand l’EPCI change', () => {
    localStorage.setItem('location-selected', JSON.stringify({ locType: 'com2co', regionId: '9', com2coId: 2, cityId: null }));
    const wrapper = mountIsochrone();

    expect(() => wrapper.vm.updateCom2coIfChanged({ com2coId: 2 })).not.toThrow();
    expect(wrapper.vm.shareableUrl).toEqual(expect.stringContaining('2'));
  });
});

describe('Isochrone - sélection des calques en mode NDVI', () => {
  const coches = (wrapper) => ({
    parcs: wrapper.find('#cbParcs').element.checked,
    isochrones: wrapper.find('#cbIsochrones').element.checked,
    carres: wrapper.find('#cbCarre').element.checked,
    cadastre: wrapper.find('#cbCadastre').element.checked
  });

  beforeEach(() => {
    localStorage.clear();
  });

  it('coche Parcs et décoche les autres calques à l’activation du mode NDVI', async () => {
    const wrapper = mountIsochrone();
    await wrapper.find('#cbIsochrones').setValue(true);

    await wrapper.find('button.ndvi-toggle').trigger('click');

    expect(coches(wrapper)).toEqual({ parcs: true, isochrones: false, carres: false, cadastre: false });
  });

  it('restaure la sélection d’avant NDVI à la sortie du mode, et efface la mémoire', async () => {
    const wrapper = mountIsochrone();
    await wrapper.find('#cbIsochrones').setValue(true);
    await wrapper.find('#cbCadastre').setValue(true);
    const avant = coches(wrapper);
    const bouton = wrapper.find('button.ndvi-toggle');

    await bouton.trigger('click');
    await bouton.trigger('click');

    expect(coches(wrapper)).toEqual(avant);
    expect(localStorage.getItem('layers-avant-ndvi')).toBeNull();
  });

  it('mémorise la sélection dans localStorage pendant le mode NDVI', async () => {
    const wrapper = mountIsochrone();
    await wrapper.find('#cbCarre').setValue(false);

    await wrapper.find('button.ndvi-toggle').trigger('click');

    expect(JSON.parse(localStorage.getItem('layers-avant-ndvi'))).toEqual({
      showParcs: false, showIsochrones: false, showCarre: false, showCadastre: false
    });
  });

  it('repart de la sélection nominale à chaque arrivée sur la carte, mémoire NDVI effacée', () => {
    localStorage.setItem('layers-avant-ndvi', JSON.stringify({
      showParcs: false, showIsochrones: true, showCarre: false, showCadastre: true
    }));

    const wrapper = mountIsochrone();

    expect(coches(wrapper)).toEqual({ parcs: false, isochrones: false, carres: true, cadastre: false });
    expect(localStorage.getItem('layers-avant-ndvi')).toBeNull();
    expect(wrapper.find('button.ndvi-toggle').attributes('aria-pressed')).toBe('false');
  });
});

describe('Isochrone - couleurs NDVI en mode daltonien', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('en mode daltonien, les parcs NDVI ont la palette daltonienne et un contour sombre', () => {
    const wrapper = mountIsochrone();
    wrapper.vm.ndviMode = true;
    wrapper.vm.colorblindMode = true;

    const style = wrapper.vm.computeParcStyle({ properties: { ndviEnabled: true, ndviMean: 0.6 } });

    expect(style.fillColor).toBe(NDVI_CATEGORIES[4].colorDaltonien);
    expect(style.color).toBe(NDVI_CONTOUR_DALTONIEN);
  });

  it('hors mode daltonien, le contour NDVI reprend la couleur du parc', () => {
    const wrapper = mountIsochrone();
    wrapper.vm.ndviMode = true;

    const style = wrapper.vm.computeParcStyle({ properties: { ndviEnabled: true, ndviMean: 0.6 } });

    expect(style.color).toBe(NDVI_CATEGORIES[4].color);
    expect(style.fillColor).toBe(NDVI_CATEGORIES[4].color);
  });
});

describe('Isochrone - requête des parcs (mode dynamic)', () => {
  it('sans paramètres, reprend les bornes courantes au lieu de « undefined »', () => {
    const wrapper = mountIsochrone();
    const log = vi.spyOn(console, 'log');
    wrapper.vm.bounds = { _southWest: { lat: 50.6, lng: 2.9 }, _northEast: { lat: 50.7, lng: 3.1 } };

    wrapper.vm.callGeoJsonParcs();

    expect(log).toHaveBeenCalledWith(expect.stringContaining('/map/park/outline?swLat=50.6&swLng=2.9&neLat=50.7&neLng=3.1&annee=2021'));
    expect(log).not.toHaveBeenCalledWith(expect.stringContaining('undefined'));
  });
});
