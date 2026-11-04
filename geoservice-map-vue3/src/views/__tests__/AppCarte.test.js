import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import AppCarte from '../AppCarte.vue';
import { hexToRgba } from '../../utils/color.js';
import { NDVI_CATEGORIES } from '../../utils/ndvi.js';

function mountAppCarte() {
  return mount(AppCarte, {
    global: {
      stubs: {
        Isochrone: {
          template: '<div data-stub="isochrone" @colorblind-mode-changed="$emit(\'colorblind-mode-changed\', false)" />'
        },
        SearchLocation: {
          template: '<div data-stub="search-location" />'
        },
        RouterLink: { template: '<a><slot /></a>' }
      }
    }
  });
}

const ndviRows = (wrapper) => wrapper.findAll('tr[id^="legend-ndvi-"]');
const isHidden = (row) => (row.attributes('style') ?? '').includes('display: none');

describe('AppCarte', () => {
  it('expose swatchColor à l\'instance (hexToRgba utilisable par le template)', () => {
    const wrapper = mountAppCarte();
    expect(typeof wrapper.vm.swatchColor).toBe('function');
    expect(wrapper.vm.swatchColor('#0000e8', 0.4)).toBe('rgba(0, 0, 232, 0.4)');
  });

  it('les pastilles de légende utilisent la transparence de la carte (rgba)', () => {
    const wrapper = mountAppCarte();
    const swatches = wrapper.findAll('td div[style*="rgba"]');
    expect(swatches.length).toBeGreaterThanOrEqual(5);
    expect(swatches[0].attributes('style')).toContain('rgba(0, 0, 232, 0.4)');
    expect(wrapper.html()).toContain('border border-gray-600');
  });

  it('contient les repères a11y de la légende', () => {
    const wrapper = mountAppCarte();
    expect(wrapper.find('h1').exists()).toBe(true);
    expect(wrapper.find('caption').text()).toBe('Légende de la carte');
    expect(wrapper.find('th').exists()).toBe(true);
  });

  it('masque la légende NDVI tant que le mode NDVI de la carte n’est pas actif', () => {
    const wrapper = mountAppCarte();

    expect(ndviRows(wrapper)).toHaveLength(NDVI_CATEGORIES.length);
    expect(ndviRows(wrapper).every(isHidden)).toBe(true);
  });

  it('affiche la légende NDVI (seuils, texte, couleurs de la grille) quand le mode NDVI est actif', async () => {
    const wrapper = mountAppCarte();
    const isochrone = wrapper.findComponent('[data-stub="isochrone"]');

    isochrone.vm.$emit('ndvi-mode-changed', true);
    await wrapper.vm.$nextTick();

    const rows = ndviRows(wrapper);
    expect(rows.some(isHidden)).toBe(false);
    expect(rows.map((row) => row.find('th').text())).toEqual(
      NDVI_CATEGORIES.map((category) => `NDVI ${category.range} : ${category.description}`)
    );
    rows.forEach((row, index) => {
      // même transparence que les polygones de la carte (fillOpacity 0,5)
      expect(row.find('td div').attributes('style')).toContain(hexToRgba(NDVI_CATEGORIES[index].color, 0.5));
    });

    isochrone.vm.$emit('ndvi-mode-changed', false);
    await wrapper.vm.$nextTick();
    expect(ndviRows(wrapper).every(isHidden)).toBe(true);
  });

  it('la légende NDVI suit le mode daltonien de la carte', async () => {
    const wrapper = mountAppCarte();
    const isochrone = wrapper.findComponent('[data-stub="isochrone"]');

    isochrone.vm.$emit('colorblind-mode-changed', true);
    isochrone.vm.$emit('ndvi-mode-changed', true);
    await wrapper.vm.$nextTick();

    ndviRows(wrapper).forEach((row, index) => {
      expect(row.find('td div').attributes('style')).toContain(hexToRgba(NDVI_CATEGORIES[index].colorDaltonien, 0.5));
    });
  });
});
