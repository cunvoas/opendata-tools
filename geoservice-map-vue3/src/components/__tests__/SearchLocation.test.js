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
    it('affiche 3 selects', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      expect(wrapper.findAll('select')).toHaveLength(3)
    })

    it('le select région est toujours activé', async () => {
      mockAxiosForAllCalls()
      const wrapper = mount(SearchLocation)
      expect(wrapper.findAll('select')[0].attributes('disabled')).toBeUndefined()
    })

    it('le select com2co est désactivé sans région', async () => {
      mockAxiosForAllCalls()
      // Pas de valeur par défaut : on force selectedRegion à null
      const wrapper = mount(SearchLocation)
      await wrapper.setData({ selectedRegion: null })
      expect(wrapper.findAll('select')[1].attributes('disabled')).toBeDefined()
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
        com2coName: 'MEL', cityId: '2878', cityName: 'LILLE (59350)',
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
  })
})
