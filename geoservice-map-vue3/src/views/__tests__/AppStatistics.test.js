import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AppStatistics from '../AppStatistics.vue'

const stubs = {
  SearchLocation: true,
  Com2coQuickAccess: true,
  RouterLink: { props: ['to'], template: '<a :href="to"><slot /></a>' },
  StatsGraph: { props: ['location'], template: '<div class="graph">{{ location.locType }}:{{ location.cityInsee || location.com2coId }}</div>' }
}

const monter = (route) => mount(AppStatistics, {
  global: { stubs, mocks: { $route: route } }
})

describe('AppStatistics.vue', () => {
  beforeEach(() => localStorage.clear())

  it('titre la page avec le nom de la commune et lui passe son code Insee', () => {
    const wrapper = monter({ name: 'statsCommune', params: { insee: '59350' }, fullPath: '/stats/59350' })

    expect(wrapper.find('h1').text()).toBe('Espaces verts à Lille')
    expect(wrapper.find('.graph').text()).toBe('city:59350')
  })

  it('lit une intercommunalité par son dossier déclaré dans l’index EPCI', () => {
    const wrapper = monter({ name: 'statsEpci', params: { siren: '200093201' }, fullPath: '/stats/epci/200093201' })

    expect(wrapper.find('h1').text()).toContain('Métropole Européenne de Lille')
    expect(wrapper.find('.graph').text()).toBe('com2co:1')
  })

  it('liste les territoires couverts quand l’adresse ne désigne aucun territoire publié', () => {
    const wrapper = monter({ name: 'statsCommune', params: { insee: '99999' }, fullPath: '/stats/99999' })

    expect(wrapper.text()).toContain("Ce territoire n'est pas couvert")
    expect(wrapper.findAll('a').some((a) => a.attributes('href') === '/stats/59350')).toBe(true)
  })
})
