import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import AppCarte from '../AppCarte.vue';

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
});
