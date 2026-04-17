import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import Autocomplete from '../Autocomplete.vue'

const makeFetchItems = (items = []) => vi.fn().mockResolvedValue(items)

const sampleItems = [
  { id: '2.123, 48.456', label: '12 Rue de la Paix, Paris', score: 0.95 },
  { id: '2.456, 48.789', label: '14 Rue de la Paix, Paris', score: 0.80 }
]

describe('Autocomplete.vue', () => {
  describe('rendu conditionnel', () => {
    it('affiche l\'input si displaySearchAddress=true', () => {
      const wrapper = mount(Autocomplete, {
        props: { fetchItems: makeFetchItems(), displaySearchAddress: true }
      })
      expect(wrapper.find('input').exists()).toBe(true)
    })

    it('masque tout si displaySearchAddress=false', () => {
      const wrapper = mount(Autocomplete, {
        props: { fetchItems: makeFetchItems(), displaySearchAddress: false }
      })
      expect(wrapper.find('input').exists()).toBe(false)
    })
  })

  describe('placeholder et disabled', () => {
    it('applique le placeholder', () => {
      const wrapper = mount(Autocomplete, {
        props: { fetchItems: makeFetchItems(), placeholder: 'Chercher une adresse', displaySearchAddress: true }
      })
      expect(wrapper.find('input').attributes('placeholder')).toBe('Chercher une adresse')
    })

    it('désactive l\'input si disabled=true', () => {
      const wrapper = mount(Autocomplete, {
        props: { fetchItems: makeFetchItems(), disabled: true, displaySearchAddress: true }
      })
      expect(wrapper.find('input').attributes('disabled')).toBeDefined()
    })
  })

  describe('dropdown', () => {
    it('n\'affiche pas la liste au départ', () => {
      const wrapper = mount(Autocomplete, {
        props: { fetchItems: makeFetchItems(), displaySearchAddress: true }
      })
      expect(wrapper.find('ul').exists()).toBe(false)
    })

    it('affiche les suggestions après saisie', async () => {
      const fetchItems = makeFetchItems(sampleItems)
      const wrapper = mount(Autocomplete, {
        props: { fetchItems, displaySearchAddress: true }
      })
      const input = wrapper.find('input')
      await input.setValue('Rue de la Paix')
      await input.trigger('input')
      // Attendre la résolution de la promesse fetchItems
      await vi.waitFor(() => expect(wrapper.findAll('li')).toHaveLength(2))
    })

    it('affiche le score si présent', async () => {
      const fetchItems = makeFetchItems(sampleItems)
      const wrapper = mount(Autocomplete, {
        props: { fetchItems, displaySearchAddress: true }
      })
      await wrapper.find('input').trigger('input')
      // Simuler les résultats directement via le state
      await wrapper.setData({ items: sampleItems, showDropdown: true })
      const scores = wrapper.findAll('li span.text-xs')
      expect(scores[0].text()).toBe('95%')
    })
  })

  describe('sélection d\'un item', () => {
    it('émet location-selected avec les bonnes coordonnées', async () => {
      const wrapper = mount(Autocomplete, {
        props: { fetchItems: makeFetchItems(sampleItems), displaySearchAddress: true }
      })
      await wrapper.setData({ items: sampleItems, showDropdown: true })
      await wrapper.findAll('li')[0].trigger('click')

      const emitted = wrapper.emitted('location-selected')
      expect(emitted).toBeTruthy()
      expect(emitted[0][0]).toEqual({ locType: 'address', lonX: '2.123', latY: '48.456' })
    })

    it('émet update:modelValue avec l\'id de l\'item', async () => {
      const wrapper = mount(Autocomplete, {
        props: { fetchItems: makeFetchItems(sampleItems), displaySearchAddress: true }
      })
      await wrapper.setData({ items: sampleItems, showDropdown: true })
      await wrapper.findAll('li')[0].trigger('click')

      const emitted = wrapper.emitted('update:modelValue')
      expect(emitted[0][0]).toBe('2.123, 48.456')
    })

    it('ferme le dropdown après sélection', async () => {
      const wrapper = mount(Autocomplete, {
        props: { fetchItems: makeFetchItems(sampleItems), displaySearchAddress: true }
      })
      await wrapper.setData({ items: sampleItems, showDropdown: true })
      await wrapper.findAll('li')[0].trigger('click')
      expect(wrapper.find('ul').exists()).toBe(false)
    })

    it('met à jour le champ texte avec le label sélectionné', async () => {
      const wrapper = mount(Autocomplete, {
        props: { fetchItems: makeFetchItems(sampleItems), displaySearchAddress: true }
      })
      await wrapper.setData({ items: sampleItems, showDropdown: true })
      await wrapper.findAll('li')[0].trigger('click')
      expect(wrapper.find('input').element.value).toBe('12 Rue de la Paix, Paris')
    })
  })

  describe('watcher modelValue', () => {
    it('met à jour le champ search quand modelValue change vers un id connu', async () => {
      const wrapper = mount(Autocomplete, {
        props: { fetchItems: makeFetchItems(sampleItems), displaySearchAddress: true, modelValue: null }
      })
      await wrapper.setData({ items: sampleItems })
      await wrapper.setProps({ modelValue: '2.123, 48.456' })
      expect(wrapper.find('input').element.value).toBe('12 Rue de la Paix, Paris')
    })

    it('vide le champ si modelValue ne correspond à aucun item', async () => {
      const wrapper = mount(Autocomplete, {
        props: { fetchItems: makeFetchItems(sampleItems), displaySearchAddress: true, modelValue: '2.123, 48.456' }
      })
      await wrapper.setData({ items: sampleItems })
      await wrapper.setProps({ modelValue: 'unknown-id' })
      expect(wrapper.find('input').element.value).toBe('')
    })
  })

  describe('filteredItems', () => {
    it('filtre les items selon la saisie (insensible à la casse)', async () => {
      const wrapper = mount(Autocomplete, {
        props: { fetchItems: makeFetchItems(), displaySearchAddress: true }
      })
      await wrapper.setData({ items: sampleItems, showDropdown: true, search: '12 rue' })
      const lis = wrapper.findAll('li')
      expect(lis).toHaveLength(1)
      expect(lis[0].text()).toContain('12 Rue de la Paix')
    })
  })
})
