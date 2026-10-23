import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AppInformation from '../AppInformation.vue'

describe('AppInformation.vue', () => {
  it('les boutons de bascule Détail/Diagramme exposent aria-pressed', async () => {
    const wrapper = mount(AppInformation)
    const buttons = wrapper.findAll('button').filter(b => ['Détail', 'Diagramme'].includes(b.text()))
    expect(buttons).toHaveLength(2)

    expect(buttons[0].attributes('aria-pressed')).toBe('true')
    expect(buttons[1].attributes('aria-pressed')).toBe('false')

    await buttons[1].trigger('click')
    const after = wrapper.findAll('button').filter(b => ['Détail', 'Diagramme'].includes(b.text()))
    expect(after[0].attributes('aria-pressed')).toBe('false')
    expect(after[1].attributes('aria-pressed')).toBe('true')
  })

  it('les cartes de thésaurus ouvrent leur modale via un vrai bouton', async () => {
    const wrapper = mount(AppInformation)
    const isoBtn = wrapper.findAll('h3 button').find(b => b.text() === 'Isochrone')
    expect(isoBtn).toBeTruthy()

    await isoBtn.trigger('click')
    const modal = wrapper.find('div.fixed.inset-0')
    expect(modal.exists()).toBe(true)
    expect(modal.text()).toContain("indicateurs d'accessibilité spatiale")

    const closeBtn = wrapper.findAll('button').find(b => b.attributes('aria-label') === 'Fermer la fenêtre')
    expect(closeBtn).toBeTruthy()
    await closeBtn.trigger('click')
    expect(wrapper.find('div.fixed.inset-0').exists()).toBe(false)
  })

  it('le diagramme SVG est annoncé par role="img"', async () => {
    const wrapper = mount(AppInformation)
    const diagramBtn = wrapper.findAll('button').find(b => b.text() === 'Diagramme')
    await diagramBtn.trigger('click')

    const svg = wrapper.find('svg')
    expect(svg.exists()).toBe(true)
    expect(svg.attributes('role')).toBe('img')
    expect(svg.attributes('aria-label')).toBeTruthy()
  })
})
