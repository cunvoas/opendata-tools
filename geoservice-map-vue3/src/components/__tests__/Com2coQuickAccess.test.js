import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import Com2coQuickAccess from '../Com2coQuickAccess.vue'

const locationWithCom2co = {
  regionId: '9',
  com2coId: '1',
  com2coName: 'Métropole Européenne de Lille',
  cityId: '2878',
  cityName: 'Lille'
}

describe('Com2coQuickAccess.vue', () => {
  describe('rendu conditionnel', () => {
    it('n\'affiche rien si currentLocation est null', () => {
      const wrapper = mount(Com2coQuickAccess, { props: { currentLocation: null } })
      expect(wrapper.find('div').exists()).toBe(false)
    })

    it('n\'affiche rien si com2coId est absent', () => {
      const wrapper = mount(Com2coQuickAccess, {
        props: { currentLocation: { regionId: '9' } }
      })
      expect(wrapper.find('div').exists()).toBe(false)
    })

    it('affiche le composant si com2coId est présent', () => {
      const wrapper = mount(Com2coQuickAccess, { props: { currentLocation: locationWithCom2co } })
      expect(wrapper.find('div').exists()).toBe(true)
    })
  })

  describe('titre', () => {
    it('affiche le nom de la com2co dans le titre', () => {
      const wrapper = mount(Com2coQuickAccess, { props: { currentLocation: locationWithCom2co } })
      expect(wrapper.find('h3').text()).toContain('Métropole Européenne de Lille')
    })

    it('affiche un libellé par défaut si com2coName est absent', () => {
      const wrapper = mount(Com2coQuickAccess, {
        props: { currentLocation: { com2coId: '1' } }
      })
      expect(wrapper.find('h3').text()).toContain('Communauté de commune')
    })
  })

  describe('boutons graphTypes', () => {
    it('affiche 3 boutons', () => {
      const wrapper = mount(Com2coQuickAccess, { props: { currentLocation: locationWithCom2co } })
      expect(wrapper.findAll('button')).toHaveLength(3)
    })

    it('les boutons ont les bons libellés', () => {
      const wrapper = mount(Com2coQuickAccess, { props: { currentLocation: locationWithCom2co } })
      const labels = wrapper.findAll('button').map(b => b.text())
      expect(labels).toContain('Dense')
      expect(labels).toContain('Périurbain')
      expect(labels).toContain('Densités confondues')
    })
  })

  describe('sélection d\'un type de graphe', () => {
    it('émet graph-type-selected avec locType=com2co et graphType correct', async () => {
      const wrapper = mount(Com2coQuickAccess, { props: { currentLocation: locationWithCom2co } })
      await wrapper.findAll('button')[0].trigger('click') // Dense → urbans
      const emitted = wrapper.emitted('graph-type-selected')
      expect(emitted).toBeTruthy()
      expect(emitted[0][0]).toMatchObject({
        locType: 'com2co',
        graphType: 'urbans',
        com2coId: '1'
      })
    })

    it('émet graph-type-selected avec graphType=suburbs pour Périurbain', async () => {
      const wrapper = mount(Com2coQuickAccess, { props: { currentLocation: locationWithCom2co } })
      await wrapper.findAll('button')[1].trigger('click')
      const emitted = wrapper.emitted('graph-type-selected')
      expect(emitted[0][0].graphType).toBe('suburbs')
    })

    it('émet graph-type-selected avec graphType=all pour Densités confondues', async () => {
      const wrapper = mount(Com2coQuickAccess, { props: { currentLocation: locationWithCom2co } })
      await wrapper.findAll('button')[2].trigger('click')
      expect(wrapper.emitted('graph-type-selected')[0][0].graphType).toBe('all')
    })

    it('met à jour selectedType après clic', async () => {
      const wrapper = mount(Com2coQuickAccess, { props: { currentLocation: locationWithCom2co } })
      await wrapper.findAll('button')[0].trigger('click')
      expect(wrapper.vm.selectedType).toBe('urbans')
    })

    it('applique la classe active sur le bouton sélectionné', async () => {
      const wrapper = mount(Com2coQuickAccess, { props: { currentLocation: locationWithCom2co } })
      const buttons = wrapper.findAll('button')
      await buttons[0].trigger('click')
      expect(buttons[0].classes()).toContain('bg-emerald-800')
      expect(buttons[1].classes()).not.toContain('bg-emerald-800')
    })

    it('enrichit l\'objet location avec les données de currentLocation', async () => {
      const wrapper = mount(Com2coQuickAccess, { props: { currentLocation: locationWithCom2co } })
      await wrapper.findAll('button')[0].trigger('click')
      const emitted = wrapper.emitted('graph-type-selected')[0][0]
      expect(emitted.regionId).toBe('9')
      expect(emitted.cityId).toBe('2878')
    })

    it('gère window._paq si présent', async () => {
      const pushSpy = vi.fn()
      const paqArray = { push: pushSpy }
      // Rendre _paq détectable par Array.isArray via un proxy tableau
      const realArray = []
      realArray.push = pushSpy
      vi.stubGlobal('_paq', realArray)
      const wrapper = mount(Com2coQuickAccess, { props: { currentLocation: locationWithCom2co } })
      await wrapper.findAll('button')[0].trigger('click')
      expect(pushSpy).toHaveBeenCalled()
      vi.unstubAllGlobals()
    })
  })
})
