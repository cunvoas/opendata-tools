import { describe, it, expect, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import HeaderAsso from '../HeaderAsso.vue'

const globalStubs = {
  global: {
    stubs: {
      'router-link': {
        props: ['to'],
        template: '<a :href="to"><slot /></a>'
      },
      'router-view': true
    }
  }
}

describe('HeaderAsso.vue', () => {
  it('expose le lien d\'évitement, main#contenu et le footer', () => {
    const wrapper = mount(HeaderAsso, globalStubs)

    const skip = wrapper.find('a.skip-link')
    expect(skip.exists()).toBe(true)
    expect(skip.attributes('href')).toBe('#contenu')
    expect(skip.text()).toContain('Aller au contenu principal')

    const main = wrapper.find('main#contenu')
    expect(main.exists()).toBe(true)
    expect(main.attributes('tabindex')).toBe('-1')

    expect(wrapper.find('header').exists()).toBe(true)
    expect(wrapper.find('footer').exists()).toBe(true)
  })

  it('le footer contient une seconde navigation étiquetée', () => {
    const wrapper = mount(HeaderAsso, globalStubs)
    const footerNav = wrapper.find('footer nav[aria-label="Plan du site"]')
    expect(footerNav.exists()).toBe(true)
    expect(footerNav.findAll('a').length).toBeGreaterThanOrEqual(4)
  })

  it('la navigation principale est étiquetée et le titre de site n\'est plus un h3 orphelin', () => {
    const wrapper = mount(HeaderAsso, globalStubs)
    expect(wrapper.find('header nav[aria-label="Navigation principale"]').exists()).toBe(true)
    expect(wrapper.find('header h3').exists()).toBe(false)
    expect(wrapper.find('header p').exists()).toBe(true)
  })
})

describe('HeaderAsso.vue : feature flag NDVI', () => {
  afterEach(() => {
    vi.unstubAllEnvs()
    vi.resetModules()
  })

  async function mountWithFlag(flag) {
    vi.stubEnv('VITE_FEATURE_NDVI', flag)
    vi.resetModules()
    const { default: Header } = await import('../HeaderAsso.vue')
    return mount(Header, globalStubs)
  }

  it('affiche les liens NDVI (navigation principale et plan du site) quand le flag est actif', async () => {
    const wrapper = await mountWithFlag('true')

    expect(wrapper.findAll('a[href="/ndvi"]')).toHaveLength(2)
    expect(wrapper.find('header nav a[href="/ndvi"]').exists()).toBe(true)
    expect(wrapper.find('footer nav a[href="/ndvi"]').exists()).toBe(true)
  })

  it('masque les deux liens NDVI quand le flag est inactif, sans toucher aux autres', async () => {
    const wrapper = await mountWithFlag('false')

    expect(wrapper.find('a[href="/ndvi"]').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('NDVI')
    expect(wrapper.find('header nav a[href="/stats"]').exists()).toBe(true)
    expect(wrapper.find('footer nav a[href="/stats"]').exists()).toBe(true)
  })
})
