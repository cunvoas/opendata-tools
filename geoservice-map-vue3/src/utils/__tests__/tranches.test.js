import { describe, it, expect } from 'vitest'
import { bornesTranche, libelleTranche, formaterPourcentage } from '../tranches.js'

describe('libelleTranche', () => {
  it('traduit la tranche basse « > 0<=3 » sans notation mathématique', () => {
    expect(libelleTranche({ surface: '> 0<=3' })).toBe('Moins de 3 m² par habitant')
    expect(libelleTranche({ surface: '> 0<=3' })).not.toMatch(/<=|</)
  })

  it('traduit les tranches intermédiaires et la tranche ouverte', () => {
    expect(libelleTranche({ surface: '> 3<=7' })).toBe('De 3 à 7 m² par habitant')
    expect(libelleTranche({ surface: '> 12 +' })).toBe('12 m² ou plus par habitant')
  })

  it('lit les bornes numériques des JSON v2 sans passer par le texte', () => {
    expect(libelleTranche({ borneMin: 3, borneMax: 7 })).toBe('De 3 à 7 m² par habitant')
    expect(libelleTranche({ borneMin: 12 })).toBe('12 m² ou plus par habitant')
    expect(bornesTranche({ borneMin: 0, borneMax: 3 })).toEqual({ min: 0, max: 3 })
  })

  it('humanise les libellés en prose des EPCI', () => {
    expect(libelleTranche({ surface: 'Non respect des minima OMS' })).toBe('Sous les minima de l’OMS')
    expect(libelleTranche({ surface: 'Préconisé par l\'OMS' })).toBe('Niveau préconisé par l’OMS')
  })
})

describe('formaterPourcentage', () => {
  it('donne des parts dont la somme reste à 100 %', () => {
    const tranches = [91445, 54092, 16630, 5408, 38214]
    const total = tranches.reduce((somme, valeur) => somme + valeur, 0)
    const parts = tranches.map((valeur) => Number(formaterPourcentage(valeur, total).replace(',', '.').replace(' %', '')))

    expect(formaterPourcentage(91445, total)).toBe('44,4 %')
    const somme = parts.reduce((a, b) => a + b, 0)
    expect(somme).toBeGreaterThan(99.9)
    expect(somme).toBeLessThan(100.1)
  })
})
