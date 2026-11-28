import { describe, it, expect } from 'vitest'
import {
  formaterNom, communeParInsee, epciParSiren, routeDepuisParametres, metaRoute, communesPourLiens
} from '../territoires.js'

describe('territoires publiés', () => {
  it('résout une commune par son code officiel géographique et un EPCI par son numéro Siren', () => {
    expect(communeParInsee('59350').nom).toBe('Lille')
    expect(epciParSiren('200093201').dossier).toBe('1')
    expect(communeParInsee('99999')).toBeNull()
  })

  it('classe les communes par population pour le maillage interne', () => {
    const communes = communesPourLiens(3)

    expect(communes).toHaveLength(3)
    expect(communes[0].insee).toBe('59350')
    expect(communes[0].population).toBeGreaterThanOrEqual(communes[1].population)
  })

  it('rend les noms en casse normale', () => {
    expect(formaterNom('LILLE')).toBe('Lille')
    expect(formaterNom('VILLENEUVE D ASCQ')).toBe('Villeneuve D Ascq')
  })
})

describe('routeDepuisParametres', () => {
  it('traduit un ancien lien à paramètres en route canonique', () => {
    expect(routeDepuisParametres({ city: '9,1,2878,50.63,3.05,Lille,city' })).toBe('/stats/59350')
    expect(routeDepuisParametres({ c2c: '1' })).toBe('/stats/epci/200093201')
  })

  it('renvoie null quand le territoire n’est pas publié', () => {
    expect(routeDepuisParametres({ c2c: '99999' })).toBeNull()
    expect(routeDepuisParametres({ city: '9,1,123456,50.63,3.05,Inconnue,city' })).toBeNull()
  })
})

describe('metaRoute', () => {
  it('titre et description d’une commune, avec son nom et « espaces verts »', () => {
    const { meta } = metaRoute({ name: 'statsCommune', path: '/stats/59350', params: { insee: '59350' } })

    expect(meta.title).toContain('Lille')
    expect(meta.title).toMatch(/espaces verts/i)
    expect(meta.title).not.toBe(meta.description)
    expect(meta.description).toMatch(/205[\s\u202f]789 habitants/)
    expect(meta.robots).toBeUndefined()
  })

  it('titre d’un EPCI sans numéro Siren seul', () => {
    const { meta } = metaRoute({ name: 'statsEpci', path: '/stats/epci/200093201', params: { siren: '200093201' } })

    expect(meta.title).toContain('Métropole Européenne de Lille')
    expect(meta.title).not.toContain('200093201')
  })

  it('garde hors index une route sans données', () => {
    const { meta } = metaRoute({ name: 'statsCommune', path: '/stats/99999', params: { insee: '99999' } })

    expect(meta.robots).toContain('noindex')
  })
})
