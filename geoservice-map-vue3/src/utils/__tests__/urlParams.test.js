import { describe, it, expect, beforeEach } from 'vitest'
import {
  getUrlParams,
  buildUrlWithParams,
  hasGeographicalParams,
  buildShareableUrl,
  removeGeographicalParams,
  isActive
} from '../urlParams.js'

// Helper to set window.location.search
function setSearch(query) {
  Object.defineProperty(window, 'location', {
    value: { ...window.location, search: query, pathname: '/' },
    writable: true,
    configurable: true
  })
}

describe('urlParams', () => {
  beforeEach(() => {
    setSearch('')
  })

  // ─── getUrlParams ────────────────────────────────────────────────────────────

  describe('getUrlParams', () => {
    it('retourne des nulls si aucun paramètre', () => {
      const result = getUrlParams()
      expect(result).toEqual({ regionId: null, c2cId: null, city: null })
    })

    it('extrait regionId', () => {
      setSearch('?region=9')
      const result = getUrlParams()
      expect(result.regionId).toBe('9')
      expect(result.c2cId).toBeNull()
    })

    it('extrait c2cId via le paramètre c2c', () => {
      setSearch('?c2c=3')
      expect(getUrlParams().c2cId).toBe('3')
    })

    it('extrait c2cId via le paramètre com2co', () => {
      setSearch('?com2co=5')
      expect(getUrlParams().c2cId).toBe('5')
    })

    it('extrait les données city complètes', () => {
      setSearch('?city=9,1,2878,50.6349747,3.046428,Lille,city')
      const { city } = getUrlParams()
      expect(city).toMatchObject({
        regionId: '9',
        com2coId: '1',
        cityId: '2878',
        latY: 50.6349747,
        lonX: 3.046428,
        name: 'Lille',
        locType: 'city'
      })
    })

    it('retourne city null si coordonnées invalides', () => {
      setSearch('?city=9,1,2878,invalid,invalid,Lille,city')
      expect(getUrlParams().city).toBeNull()
    })

    it('extrait region + c2c + city ensemble', () => {
      setSearch('?region=9&c2c=1&city=9,1,2878,50.63,3.04,Lille,city')
      const result = getUrlParams()
      expect(result.regionId).toBe('9')
      expect(result.c2cId).toBe('1')
      expect(result.city).not.toBeNull()
    })
  })

  // ─── buildUrlWithParams ──────────────────────────────────────────────────────

  describe('buildUrlWithParams', () => {
    it('retourne le pathname seul si aucune option', () => {
      expect(buildUrlWithParams()).toBe('/')
    })

    it('ajoute le paramètre region', () => {
      const url = buildUrlWithParams({ regionId: '9', keepExisting: false })
      expect(url).toBe('/?region=9')
    })

    it('ajoute le paramètre c2c', () => {
      const url = buildUrlWithParams({ c2cId: '2', keepExisting: false })
      expect(url).toBe('/?c2c=2')
    })

    it('ajoute le paramètre city formaté correctement', () => {
      const url = buildUrlWithParams({
        city: { regionId: '9', com2coId: '1', cityId: '2878', latY: 50.63, lonX: 3.04, name: 'Lille', locType: 'city' },
        keepExisting: false
      })
      expect(url).toContain('city=9%2C1%2C2878%2C50.63%2C3.04%2CLille%2Ccity')
    })

    it('ne conserve pas les paramètres géographiques existants avec keepExisting', () => {
      setSearch('?region=5&other=foo')
      const url = buildUrlWithParams({ regionId: '9', keepExisting: true })
      expect(url).toContain('region=9')
      expect(url).toContain('other=foo')
      // l'ancienne region ne doit pas être dupliquée
      expect(url.match(/region=/g)).toHaveLength(1)
    })
  })

  // ─── hasGeographicalParams ───────────────────────────────────────────────────

  describe('hasGeographicalParams', () => {
    it('retourne false si aucun paramètre géographique', () => {
      setSearch('?foo=bar')
      expect(hasGeographicalParams()).toBe(false)
    })

    it('retourne false si aucun paramètre du tout', () => {
      expect(hasGeographicalParams()).toBe(false)
    })
  })

  // ─── buildShareableUrl ───────────────────────────────────────────────────────

  describe('buildShareableUrl', () => {
    it('retourne la defaultLoc si locationData est null', () => {
      const result = buildShareableUrl(null)
      // defaultLoc est un objet, pas une URL
      expect(typeof result).toBe('object')
      expect(result.cityName).toBe('LILLE')
    })

    it('construit une URL avec les données de localisation', () => {
      const loc = {
        regionId: '9', com2coId: '1', cityId: '2878',
        latY: 50.63, lonX: 3.04, cityName: 'Lille', locType: 'city'
      }
      const url = buildShareableUrl(loc)
      expect(typeof url).toBe('string')
      expect(url).toContain('city=')
      expect(url).toContain('region=9')
    })
  })

  // ─── removeGeographicalParams ────────────────────────────────────────────────

  describe('removeGeographicalParams', () => {
    it('supprime tous les paramètres géographiques', () => {
      setSearch('?region=9&c2c=1&city=foo&other=bar')
      const url = removeGeographicalParams()
      expect(url).not.toContain('region=')
      expect(url).not.toContain('c2c=')
      expect(url).not.toContain('city=')
      expect(url).toContain('other=bar')
    })

    it('retourne le pathname seul si aucun autre paramètre', () => {
      setSearch('?region=9')
      expect(removeGeographicalParams()).toBe('/')
    })
  })

  // ─── isActive ────────────────────────────────────────────────────────────────

  describe('isActive', () => {
    it('retourne true si le 1er janvier est dans la plage (format YYYYMMDD)', () => {
      expect(isActive(2026, '20260101', '20261231')).toBe(true)
    })

    it('retourne true si le 1er janvier de 2027 est dans la plage', () => {
      expect(isActive(2027, '20260101', '20270131')).toBe(true)
    })

    it('retourne false si l\'année est antérieure à dateDebut', () => {
      expect(isActive(2025, '20260101', '20270101')).toBe(false)
    })

    it('retourne false si le 1er janvier est égal à dateFin (exclu)', () => {
      expect(isActive(2027, '20260101', '20270101')).toBe(false)
    })

    it('accepte le format YYYY-MM-DD', () => {
      expect(isActive(2026, '2026-01-01', '2026-12-31')).toBe(true)
    })

    it('accepte une année passée en string', () => {
      expect(isActive('2026', '20260101', '20261231')).toBe(true)
    })

    it('retourne false pour une plage passée', () => {
      expect(isActive(2030, '20200101', '20210101')).toBe(false)
    })
  })
})
