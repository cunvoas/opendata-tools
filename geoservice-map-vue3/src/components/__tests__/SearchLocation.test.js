import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import SearchLocation from '../SearchLocation.vue'

// ─── Données de mock ─────────────────────────────────────────────────────────

const mockRegions = [
  { id: '9', name: 'Hauts-de-France' },
  { id: '11', name: 'Île-de-France' }
]

const mockCom2cos = [
  { id: '1', name: 'Métropole Européenne de Lille' },
  { id: '2', name: 'Communauté Urbaine Dunkerque' }
]

const mockCities = [
  { id: '2878', name: 'LILLE', inseeCode: '59350', lonX: '3.046986424', latY: '50.631855028' },
  { id: '100', name: 'ROUBAIX', inseeCode: '59512', lonX: '3.178', latY: '50.694' }
]

// ─── Mock axios ───────────────────────────────────────────────────────────────

vi.mock('axios', () => ({
  default: {
    get: vi.fn()
  }
}))

import axios from 'axios'

function mockAxiosForAllCalls() {
  axios.get.mockImplementation((url) => {
    if (url.includes('regions.json')) return Promise.resolve({ data: mockRegions })
    if (url.includes('com2cos_')) return Promise.resolve({ data: mockCom2cos })
    if (url.includes('cities_')) return Promise.resolve({ data: mockCities })
    if (url.includes('api-adresse')) return Promise.resolve({ data: { features: [] } })
    return Promise.reject(new Error(`Unmatched URL: ${url}`))
  })
}

// ─── Setup localStorage ───────────────────────────────────────────────────────

beforeEach(() => {
  localStorage.clear()
  axios.get.mockReset()
  vi.spyOn(console, 'log').mockImplementation(() => {})
  vi.spyOn(console, 'error').mockImplementation(() => {})
})

afterEach(() => {
  vi.restoreAllMocks()
})

// ─── Tests ────────────────────────────────────────────────────────────────────

describe('SearchLocation.vue', () => {
  describe('rendu initial', () => {
    it('affiche 3 champs de sélection avec datalist', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      expect(wrapper.findAll('select')).toHaveLength(0)
      expect(wrapper.find('#selRegion').attributes('list')).toBe('regionsList')
      expect(wrapper.find('#selCom2co').attributes('list')).toBe('com2cosList')
      expect(wrapper.find('#selCity').attributes('list')).toBe('citiesList')
    })

    it('le champ région est toujours activé', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      expect(wrapper.find('#selRegion').attributes('disabled')).toBeUndefined()
    })

    it('le champ com2co est désactivé sans région', async () => {
      mockAxiosForAllCalls()
      // Pas de valeur par défaut : on force selectedRegion à null
      const wrapper = mount(SearchLocation)
      await wrapper.setData({ selectedRegion: null })
      expect(wrapper.find('#selCom2co').attributes('disabled')).toBeDefined()
    })
  })

  describe('chargement initial depuis les valeurs par défaut (Lille)', () => {
    it('charge les régions au montage', async () => {
      mockAxiosForAllCalls()
      mount(SearchLocation)
      await flushPromises()
      expect(axios.get).toHaveBeenCalledWith(expect.stringContaining('regions.json'))
    })

    it('charge les com2cos après les régions', async () => {
      mockAxiosForAllCalls()
      mount(SearchLocation)
      await flushPromises()
      expect(axios.get).toHaveBeenCalledWith(expect.stringContaining('com2cos_'))
    })

    it('charge les villes après les com2cos', async () => {
      mockAxiosForAllCalls()
      mount(SearchLocation)
      await flushPromises()
      expect(axios.get).toHaveBeenCalledWith(expect.stringContaining('cities_'))
    })

    it('préremplit les champs avec Hauts-de-France, MEL et Lille par défaut', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await flushPromises()
      expect(wrapper.find('#selRegion').element.value).toBe('Hauts-de-France')
      expect(wrapper.find('#selCom2co').element.value).toBe('Métropole Européenne de Lille')
      expect(wrapper.find('#selCity').element.value).toBe('Lille (59350)')
    })

    it('émet update-location avec les données de Lille par défaut', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await flushPromises()
      const emitted = wrapper.emitted('update-location')
      expect(emitted).toBeTruthy()
      const lastEmit = emitted[emitted.length - 1][0]
      expect(lastEmit.cityId).toBe('2878')
      expect(lastEmit.locType).toBe('city')
    })

    it('sauvegarde dans localStorage', async () => {
      mockAxiosForAllCalls()
      mount(SearchLocation)
      await flushPromises()
      const saved = JSON.parse(localStorage.getItem('location-selected'))
      expect(saved).not.toBeNull()
      expect(saved.cityId).toBe('2878')
    })
  })

  describe('chargement depuis localStorage', () => {
    it('restore la sélection depuis localStorage et émet update-location', async () => {
      const savedLoc = {
        locType: 'city', regionId: '9', com2coId: '1',
        com2coName: 'MEL', cityId: '2878', cityName: 'Lille (59350)',
        cityInsee: '59350', lonX: '3.046986424', latY: '50.631855028'
      }
      localStorage.setItem('location-selected', JSON.stringify(savedLoc))
      mockAxiosForAllCalls()

      const wrapper = mount(SearchLocation)
      await flushPromises()

      const emitted = wrapper.emitted('update-location')
      expect(emitted).toBeTruthy()
      // Le premier emit vient du localStorage restore
      expect(emitted[0][0]).toMatchObject({ cityId: '2878', regionId: '9' })
    })

    it('préremplit région, EPCI et commune quand les ids des fichiers sont numériques', async () => {
      localStorage.setItem('location-selected', JSON.stringify({
        locType: 'city', regionId: '9', com2coId: '1',
        cityId: '2878', cityName: 'Lille (59350)', cityInsee: '59350'
      }))
      axios.get.mockImplementation((url) => {
        if (url.includes('regions.json')) return Promise.resolve({ data: [{ id: 9, name: 'Hauts-de-France' }] })
        if (url.includes('com2cos_')) return Promise.resolve({ data: [{ id: 1, name: 'Métropole Européenne de Lille' }] })
        if (url.includes('cities_')) return Promise.resolve({ data: [{ id: 2878, name: 'LILLE', inseeCode: '59350', lonX: '3.05', latY: '50.63' }] })
        return Promise.resolve({ data: { features: [] } })
      })

      const wrapper = mount(SearchLocation)
      await flushPromises()

      expect(wrapper.find('#selRegion').element.value).toBe('Hauts-de-France')
      expect(wrapper.find('#selCom2co').element.value).toBe('Métropole Européenne de Lille')
      expect(wrapper.find('#selCity').element.value).toBe('Lille (59350)')
    })
  })

  describe('erreurs fetch', () => {
    it('gère l\'échec du fetch des régions sans planter', async () => {
      axios.get.mockRejectedValue(new Error('Network error'))
      const wrapper = mount(SearchLocation)
      await flushPromises()
      expect(wrapper.vm.regions).toEqual([])
    })

    it('gère l\'échec du fetch des com2cos', async () => {
      axios.get.mockImplementation((url) => {
        if (url.includes('regions.json')) return Promise.resolve({ data: mockRegions })
        return Promise.reject(new Error('Network error'))
      })
      const wrapper = mount(SearchLocation)
      await flushPromises()
      expect(wrapper.vm.com2cos).toEqual([])
    })
  })

  describe('handleLocationSelected', () => {
    it('enrichit la loc avec les données contextuelles et émet update-location', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await flushPromises()

      // Simuler une sélection d'adresse par Autocomplete
      await wrapper.setData({
        selectedRegion: '9',
        selectedCom2co: '1',
        selectedCom2coName: 'MEL',
        selectedCity: '2878',
        selectedCityName: 'LILLE',
        selectedCityInseeCode: '59350'
      })

      wrapper.vm.handleLocationSelected({ locType: 'address', lonX: '3.1', latY: '50.7' })

      const emitted = wrapper.emitted('update-location')
      const lastEmit = emitted[emitted.length - 1][0]
      expect(lastEmit.locType).toBe('address')
      expect(lastEmit.regionId).toBe('9')
      expect(lastEmit.com2coId).toBe('1')
      expect(lastEmit.lonX).toBe('3.1')
    })
  })

  describe('city-selected', () => {
    it('est émis au choix d’une commune, pas au chargement initial', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await flushPromises()
      expect(wrapper.emitted('city-selected')).toBeUndefined()

      await wrapper.find('#selCity').setValue('ROUBAIX (59512)')

      expect(wrapper.emitted('city-selected')).toHaveLength(1)
      const location = wrapper.emitted('city-selected')[0][0]
      expect(location).toMatchObject({ regionId: '9', com2coId: '1', cityId: '100', cityInsee: '59512' })
      expect(location.cityName.trim()).toBe('Roubaix (59512)')
    })

    it('trie les villes alphabétiquement sans modifier l’ordre source', async () => {
      axios.get.mockImplementation((url) => {
        if (url.includes('regions.json')) return Promise.resolve({ data: mockRegions })
        if (url.includes('com2cos_')) return Promise.resolve({ data: mockCom2cos })
        if (url.includes('cities_')) return Promise.resolve({ data: [...mockCities].reverse() })
        return Promise.resolve({ data: { features: [] } })
      })
      const wrapper = mount(SearchLocation)
      await flushPromises()

      expect(wrapper.vm.sortedCities.map(c => c.name)).toEqual(['LILLE', 'ROUBAIX'])
      expect(wrapper.vm.cities.map(c => c.name)).toEqual(['ROUBAIX', 'LILLE'])
    })

    it('retrouve une commune malgré casse et accents', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await flushPromises()

      await wrapper.find('#selCity').setValue('roubaix (59512)')

      expect(wrapper.emitted('city-selected')[0][0].cityId).toBe('100')
    })

    it('ignore une saisie inconnue et restaure la commune sélectionnée', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await flushPromises()

      await wrapper.find('#selCity').setValue('VILLE INCONNUE')

      expect(wrapper.emitted('city-selected')).toBeUndefined()
      expect(wrapper.vm.citySearch).toBe('Lille (59350)')
    })
  })

  describe('com2co-selected', () => {
    it('est émis au choix d’un EPCI, sans attendre la commune', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await flushPromises()
      expect(wrapper.emitted('com2co-selected')).toBeUndefined()

      await wrapper.find('#selCom2co').setValue('Communauté Urbaine Dunkerque')
      await flushPromises()

      expect(wrapper.emitted('com2co-selected')).toEqual([
        [{ regionId: '9', com2coId: '2', com2coName: 'Communauté Urbaine Dunkerque' }]
      ])
    })

    it('mémorise l’EPCI choisi dans localStorage, même sans commune', async () => {
      mockAxiosForAllCalls()
      const allCalls = axios.get.getMockImplementation()
      // Dunkerque n'a pas Lille : la commune précédente n'est donc plus dans la liste.
      axios.get.mockImplementation((url) => url.includes('cities_2.json')
        ? Promise.resolve({ data: [{ id: '900', name: 'DUNKERQUE', inseeCode: '59183', lonX: '2.37', latY: '51.03' }] })
        : allCalls(url))
      const wrapper = mount(SearchLocation)
      await flushPromises()

      await wrapper.find('#selCom2co').setValue('Communauté Urbaine Dunkerque')
      await flushPromises()

      const saved = JSON.parse(localStorage.getItem('location-selected'))
      expect(saved).toMatchObject({ regionId: '9', com2coId: '2', cityId: null })
      expect(wrapper.vm.citySearch).toBe('')
      expect(wrapper.find('#selCity').element.value).toBe('')
    })

    it('résout une région saisie et recharge les EPCI', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await flushPromises()
      axios.get.mockClear()

      await wrapper.find('#selRegion').setValue('Île-de-France')
      await flushPromises()

      expect(wrapper.vm.selectedRegion).toBe('11')
      expect(wrapper.vm.selectedCom2co).toBeNull()
      expect(wrapper.vm.com2coSearch).toBe('')
      expect(wrapper.vm.citySearch).toBe('')
      expect(wrapper.find('#selCom2co').element.value).toBe('')
      expect(wrapper.find('#selCity').element.value).toBe('')
      expect(axios.get).toHaveBeenCalledWith(expect.stringContaining('com2cos_11'))
    })

    it('trie régions et EPCI alphabétiquement sans modifier l’ordre source', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await flushPromises()

      expect(wrapper.vm.sortedRegions.map(r => r.name)).toEqual(['Hauts-de-France', 'Île-de-France'])
      expect(wrapper.vm.sortedCom2cos.map(c => c.name)).toEqual([
        'Communauté Urbaine Dunkerque',
        'Métropole Européenne de Lille'
      ])
      expect(wrapper.vm.com2cos[0].name).toBe('Métropole Européenne de Lille')
    })
  })

  describe('fetchAddresses', () => {
    it('ne fait pas d\'appel API si query < 3 caractères', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await flushPromises()
      axios.get.mockClear()
      wrapper.vm.fetchAddresses('ab')
      // Attendre plus que le délai debounce (350ms)
      await new Promise(resolve => setTimeout(resolve, 400))
      await flushPromises()
      const adresseCall = axios.get.mock.calls.find(c => c[0].includes('api-adresse'))
      expect(adresseCall).toBeUndefined()
    }, 2000)

    it('ne fait pas d\'appel API si aucune ville sélectionnée', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await wrapper.setData({ selectedCity: null, selectedCityInseeCode: null })
      axios.get.mockClear()
      wrapper.vm.fetchAddresses('rue de la paix')
      await new Promise(resolve => setTimeout(resolve, 400))
      await flushPromises()
      const adresseCall = axios.get.mock.calls.find(c => c[0].includes('api-adresse'))
      expect(adresseCall).toBeUndefined()
    }, 2000)

    it('appelle l\'API adresse avec le bon inseeCode', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await flushPromises()

      axios.get.mockImplementation((url) => {
        if (url.includes('api-adresse')) return Promise.resolve({
          data: {
            features: [{
              geometry: { coordinates: ['3.05', '50.63'] },
              properties: { label: '1 Rue de la Paix, Lille', score: 0.9 }
            }]
          }
        })
        return Promise.resolve({ data: [] })
      })

      await wrapper.setData({ selectedCityInseeCode: '59350', selectedCity: '2878' })
      wrapper.vm.fetchAddresses('rue de la paix')
      // Attendre le délai debounce + exécution
      await new Promise(resolve => setTimeout(resolve, 400))
      await flushPromises()
      const adresseCall = axios.get.mock.calls.find(c => c[0].includes('api-adresse'))
      expect(adresseCall).toBeDefined()
      expect(adresseCall[0]).toContain('59350')
    }, 2000)

    it('encode les caractères réservés de la requête (&, ?, #)', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      await flushPromises()

      await wrapper.setData({ selectedCityInseeCode: '59350', selectedCity: '2878' })
      wrapper.vm.fetchAddresses('Rue A & B?')
      await new Promise(resolve => setTimeout(resolve, 400))
      await flushPromises()

      const adresseCall = axios.get.mock.calls.find(c => c[0].includes('api-adresse'))
      expect(adresseCall[0]).toContain('q=Rue%20A%20%26%20B%3F')
      expect(adresseCall[0]).not.toContain('q=Rue A & B?')
    }, 2000)
  })
})
