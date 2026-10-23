import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';

vi.mock('axios', () => ({
  default: { get: vi.fn(() => Promise.resolve({ data: {} })) }
}));

import Isochrone from '../Isochrone.vue';

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
