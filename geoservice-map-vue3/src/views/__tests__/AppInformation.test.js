import { describe, it, expect, vi, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AppInformation from '../AppInformation.vue'
import { features } from '../../utils/config.js'
import { NDVI_CATEGORIES } from '../../utils/ndvi.js'
import { hexToRgba } from '../../utils/color.js'

vi.mock('../../utils/config.js', async (importOriginal) => ({
  ...(await importOriginal()),
  features: { ndvi: true }
}))

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

describe('AppInformation.vue - encart NDVI du thésaurus', () => {
  const encart = (wrapper) => wrapper.find('section[aria-labelledby="thesaurus-ndvi"]')
  const tableSeuils = (wrapper) => encart(wrapper)
    .findAll('table')
    .find((t) => t.find('caption').exists() && t.find('caption').text().includes('Seuils NDVI par type de surface'))
  const cssColor = (hex) => {
    const probe = document.createElement('span')
    probe.style.backgroundColor = hex
    return probe.style.backgroundColor
  }

  afterEach(() => {
    features.ndvi = true
  })

  it('affiche l’encart NDVI avec les seuils de la grille partagée', () => {
    const wrapper = mount(AppInformation)
    const section = encart(wrapper)

    expect(section.exists()).toBe(true)
    expect(section.find('h3').text()).toBe('Indices de végétation')
    const table = tableSeuils(wrapper)
    expect(table).toBeTruthy()
    const rows = table.findAll('tbody tr')
    expect(rows).toHaveLength(NDVI_CATEGORIES.length)
    rows.forEach((row, index) => {
      expect(row.find('th').text()).toBe(NDVI_CATEGORIES[index].range)
      expect(row.findAll('td')[0].text()).toBe(NDVI_CATEGORIES[index].description)
    })
  })

  it('n’affiche pas l’encart quand le feature flag NDVI est inactif', () => {
    features.ndvi = false
    const wrapper = mount(AppInformation)

    expect(encart(wrapper).exists()).toBe(false)
  })

  it('montre les deux palettes en pastilles décoratives à 50 %, comme la carte : le texte reste l’information', () => {
    const wrapper = mount(AppInformation)
    const rows = tableSeuils(wrapper).findAll('tbody tr')

    expect(rows).toHaveLength(NDVI_CATEGORIES.length)
    rows.forEach((row, index) => {
      const pastilles = row.findAll('span[aria-hidden="true"]')
      expect(pastilles).toHaveLength(2)
      expect(pastilles[0].element.style.backgroundColor).toBe(cssColor(hexToRgba(NDVI_CATEGORIES[index].color, 0.5)))
      expect(pastilles[1].element.style.backgroundColor).toBe(cssColor(hexToRgba(NDVI_CATEGORIES[index].colorDaltonien, 0.5)))
    })
  })

  it('détaille les trois indices NDVI, GNDVI et NDRE (nom complet, mesure, utilité) et lie « red edge »', () => {
    const wrapper = mount(AppInformation)
    const section = encart(wrapper)
    const table = section.find('table[aria-label="NDVI, GNDVI et NDRE : ce que chacun mesure"]')
    expect(table.exists()).toBe(true)
    expect(table.findAll('thead th').map((th) => th.text())).toEqual([
      'Indice', 'Nom complet', "Ce qu'il mesure spécifiquement", 'Utilité pour notre étude'
    ])

    const codes = table.findAll('tbody tr').map((row) => row.find('th').text())
    expect(codes).toEqual(['NDVI', 'GNDVI', 'NDRE'])
    const noms = table.findAll('tbody tr').map((row) => row.findAll('td')[0].text())
    expect(noms).toEqual([
      'Normalized Difference Vegetation Index',
      'Green Normalized Difference Vegetation Index',
      'Normalized Difference Red Edge Index'
    ])
    table.findAll('tbody tr').forEach((row) => {
      const cells = row.findAll('td')
      expect(cells[1].text()).not.toBe('')
      expect(cells[2].text()).not.toBe('')
    })

    const lienRedEdge = section.find('a[href="https://fr.wikipedia.org/wiki/Red-edge"]')
    expect(lienRedEdge.exists()).toBe(true)
    expect(lienRedEdge.attributes('target')).toBe('_blank')
  })
})
