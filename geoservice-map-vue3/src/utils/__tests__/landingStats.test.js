import { describe, it, expect, vi } from 'vitest'
import {
  resumeStats, chargerResume, urlStatsCommune, urlStatsEpci, lireDonneesIntegrees, integrerDonnees, COMMUNE_LILLE, EPCI_MEL
} from '../landingStats.js'

// Lille 2021 tel que publié : cinq tranches, seuils de commune dense.
const lille2021 = {
  nom: 'LILLE', insee: '59350', annee: '2021',
  stats: [
    { surface: '> 0<=3', habitants: 91445 }, { surface: '> 3<=7', habitants: 54092 },
    { surface: '> 7<=10', habitants: 16630 }, { surface: '> 10<=12', habitants: 5408 },
    { surface: '> 12 +', habitants: 38214 }
  ],
  seuils: [
    { surface: '0 <=  surface/habitant < 3 m²/hab.' }, { surface: '3 <=  surface/habitant < 7 m²/hab.' },
    { surface: '7 <=  surface/habitant < 10 m²/hab.' }, { surface: '10 <=  surface/habitant < 12 m²/hab.' },
    { surface: ' >= 12 m²/hab.' }
  ]
}

// EPCI « all » : trois niveaux OMS.
const mel2021 = {
  annee: '2021',
  stats: [{ habitants: 800 }, { habitants: 50 }, { habitants: 150 }]
}

describe('resumeStats', () => {
  it('commune : part sous le minimum, minimum OMS et première tranche', () => {
    const r = resumeStats(lille2021)

    expect(r.annee).toBe('2021')
    expect(r.habitants).toBe(205789)
    expect(r.sousMinimum).toBe(78.8)
    expect(r.minimumM2).toBe(10)
    expect(r.premiereBorne).toBe(3)
    expect(r.premiereTranche).toBe(44.4)
  })

  it('commune périurbaine : lit les bornes du JSON, pas des valeurs figées', () => {
    const peri = {
      annee: '2021',
      stats: [{ habitants: 10 }, { habitants: 10 }, { habitants: 10 }, { habitants: 10 }, { habitants: 60 }],
      seuils: [
        { surface: '0 <=  surface/habitant < 8 m²/hab.' }, {}, {}, { surface: '25 <=  surface/habitant < 45 m²/hab.' }, {}
      ]
    }
    const r = resumeStats(peri)

    expect(r.minimumM2).toBe(25)
    expect(r.premiereBorne).toBe(8)
    expect(r.sousMinimum).toBe(30)
  })

  it('EPCI : le premier niveau est « non respect des minima », sans détail de tranche', () => {
    const r = resumeStats(mel2021)

    expect(r.habitants).toBe(1000)
    expect(r.sousMinimum).toBe(80)
    expect(r.minimumM2).toBeNull()
    expect(r.premiereTranche).toBeNull()
  })

  it('renvoie null pour un JSON inexploitable ou sans habitant', () => {
    expect(resumeStats(null)).toBeNull()
    expect(resumeStats({})).toBeNull()
    expect(resumeStats({ stats: [{ habitants: 1 }] })).toBeNull()
    expect(resumeStats({ stats: [{ habitants: 0 }, { habitants: 0 }, { habitants: 0 }] })).toBeNull()
  })
})

describe('resumeStats : descripteur calculé par le serveur', () => {
  const avecDescripteur = (descripteur) => ({ ...lille2021, descripteur })

  it('reprend les deux m²/habitant du descripteur', () => {
    const r = resumeStats(avecDescripteur({ habitants: 205789, m2ParHabitantMoyen: 10.95, surfaceParcsOmsM2: 5000, m2ParHabitantGlobal: 52.64 }))

    expect(r.m2Moyen).toBe(10.95)
    expect(r.m2Global).toBe(52.64)
    // les tranches ne dépendent pas du descripteur
    expect(r.sousMinimum).toBe(78.8)
  })

  it("reste valable avec les anciens JSON, sans descripteur", () => {
    const r = resumeStats(lille2021)

    expect(r.m2Moyen).toBeNull()
    expect(r.m2Global).toBeNull()
    expect(r.habitants).toBe(205789)
  })

  it('ignore une valeur qui n\'est pas un nombre fini', () => {
    const r = resumeStats(avecDescripteur({ m2ParHabitantMoyen: '10,95', m2ParHabitantGlobal: Infinity }))

    expect(r.m2Moyen).toBeNull()
    expect(r.m2Global).toBeNull()
  })

  it('accepte une valeur nulle : zéro m² est une information, pas une absence', () => {
    const r = resumeStats(avecDescripteur({ m2ParHabitantMoyen: 0, m2ParHabitantGlobal: 0 }))

    expect(r.m2Moyen).toBe(0)
    expect(r.m2Global).toBe(0)
  })

  it("s'applique aussi aux EPCI « all »", () => {
    const r = resumeStats({ ...mel2021, descripteur: { m2ParHabitantMoyen: 11.49, m2ParHabitantGlobal: 29.79 } })

    expect(r.m2Moyen).toBe(11.49)
    expect(r.m2Global).toBe(29.79)
  })
})

describe('chargerResume', () => {
  it('résume le JSON téléchargé', async () => {
    const fetchFn = vi.fn().mockResolvedValue({ ok: true, json: async () => mel2021 })

    expect((await chargerResume('http://x', fetchFn)).sousMinimum).toBe(80)
    expect(fetchFn).toHaveBeenCalledWith('http://x')
  })

  it('renvoie null sur une erreur HTTP ou réseau, sans lever', async () => {
    expect(await chargerResume('u', vi.fn().mockResolvedValue({ ok: false }))).toBeNull()
    expect(await chargerResume('u', vi.fn().mockRejectedValue(new Error('réseau')))).toBeNull()
  })
})

describe('URL des statistiques', () => {
  it('suivent le même format que StatsGraph (mode dynamic des tests)', () => {
    expect(urlStatsCommune(COMMUNE_LILLE, 2021)).toMatch(/\/map\/stats\/city\/59350\?annee=2021$/)
    expect(urlStatsEpci(EPCI_MEL, 2021)).toMatch(/\/map\/stats\/com2co\/1\/all\?annee=2021$/)
  })
})

describe('chiffres intégrés au HTML', () => {
  const nettoyer = () => document.getElementById('landing-stats')?.remove()

  it('relit ce qui a été intégré', () => {
    nettoyer()
    integrerDonnees({ lille: { sousMinimum: 78.8 }, mel: { sousMinimum: 79.8 } })

    expect(lireDonneesIntegrees()).toEqual({ lille: { sousMinimum: 78.8 }, mel: { sousMinimum: 79.8 } })
    nettoyer()
  })

  it("réutilise la même balise au lieu d'en ajouter une à chaque intégration", () => {
    nettoyer()
    integrerDonnees({ a: 1 })
    integrerDonnees({ a: 2 })

    expect(document.querySelectorAll('script#landing-stats')).toHaveLength(1)
    expect(lireDonneesIntegrees()).toEqual({ a: 2 })
    nettoyer()
  })

  it("échappe « < » pour que le contenu ne puisse jamais fermer la balise script", () => {
    nettoyer()
    integrerDonnees({ texte: '</script><b>x' })

    expect(document.getElementById('landing-stats').textContent).not.toContain('<')
    expect(lireDonneesIntegrees().texte).toBe('</script><b>x')
    nettoyer()
  })

  it('renvoie un objet vide sans balise ou avec un contenu invalide', () => {
    nettoyer()
    expect(lireDonneesIntegrees()).toEqual({})

    const script = document.createElement('script')
    script.id = 'landing-stats'
    script.textContent = '{pas du json'
    document.head.appendChild(script)
    expect(lireDonneesIntegrees()).toEqual({})
    nettoyer()
  })
})

