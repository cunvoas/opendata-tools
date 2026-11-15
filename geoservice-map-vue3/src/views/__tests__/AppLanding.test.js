import { describe, it, expect, vi, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import AppLanding from '../AppLanding.vue'
import { ecrireAuProjet } from '../../utils/contact.js'

vi.mock('../../utils/contact.js', () => ({ ecrireAuProjet: vi.fn() }))

const RouterLinkStub = { props: ['to'], template: '<a :href="to"><slot /></a>' }
const monter = () => mount(AppLanding, { global: { stubs: { RouterLink: RouterLinkStub } } })

const lille = {
  nom: 'LILLE', annee: '2021',
  stats: [{ habitants: 91445 }, { habitants: 54092 }, { habitants: 16630 }, { habitants: 5408 }, { habitants: 38214 }],
  seuils: [
    { surface: '0 <=  surface/habitant < 3 m²/hab.' }, {}, {}, { surface: '10 <=  surface/habitant < 12 m²/hab.' }, {}
  ]
}
const mel = { annee: '2021', stats: [{ habitants: 800 }, { habitants: 50 }, { habitants: 150 }] }

function simulerReseau(reponses) {
  vi.stubGlobal('fetch', vi.fn(async (url) => {
    const json = url.includes('/com2co/') ? reponses.mel : reponses.lille
    return json ? { ok: true, json: async () => json } : { ok: false }
  }))
}

describe('AppLanding.vue', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    document.head.querySelectorAll('script[type="application/ld+json"], script#landing-stats').forEach((s) => s.remove())
  })

  it('porte un seul H1 sur la question de recherche et mène à la carte et aux statistiques', async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const h1 = wrapper.findAll('h1')
    expect(h1).toHaveLength(1)
    expect(h1[0].text()).toContain('m² de parcs et jardins par habitant')
    const liens = wrapper.findAll('a').map((a) => a.attributes('href'))
    expect(liens).toContain('/carte')
    expect(liens).toContain('/stats')
  })

  it('calcule les chiffres depuis les JSON publiés et signale que la page est prête', async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const texte = wrapper.text().replace(/\s/g, ' ')
    expect(texte).toContain('78,8')
    expect(texte).toContain('moins de 10 m² de parc accessible à pied')
    expect(texte).toContain('44,4')
    expect(texte).toContain('80')
    expect(texte).toContain('Données 2021')
    expect(wrapper.attributes('data-landing-ready')).toBe('true')
  })

  it("n'affiche aucun chiffre quand les JSON sont injoignables, et ne se déclare pas prête", async () => {
    simulerReseau({ lille: null, mel: null })
    const wrapper = monter()
    await flushPromises()

    expect(wrapper.text()).toContain('Les chiffres ne sont pas disponibles')
    expect(wrapper.find('section[aria-labelledby="chiffres-cles"]').text()).not.toContain('%')
    expect(wrapper.attributes('data-landing-ready')).toBe('false')
    expect(wrapper.find('h1').exists()).toBe(true)
  })

  it("n'affiche que la carte disponible quand une seule source répond, sans se déclarer prête", async () => {
    simulerReseau({ lille, mel: null })
    const wrapper = monter()
    await flushPromises()

    const chiffres = wrapper.find('section[aria-labelledby="chiffres-cles"]').text()
    expect(chiffres).toContain('Lille')
    expect(chiffres).not.toContain('Métropole Européenne de Lille')
    expect(wrapper.attributes('data-landing-ready')).toBe('false')
  })

  it("présente la comparaison avec l'INSEE et son étude", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    expect(wrapper.find('table').text()).toContain('72 grands centres urbains')
    const lienInsee = wrapper.findAll('a').find((a) => a.attributes('href')?.includes('insee.fr/fr/statistiques/8558420'))
    expect(lienInsee).toBeTruthy()
    expect(lienInsee.attributes('rel')).toContain('noopener')
  })

  it('ajoute son JSON-LD (page et jeu de données) au document puis le retire en quittant la page', async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const script = document.getElementById('json-ld-accueil')
    expect(script).not.toBeNull()
    const graphe = JSON.parse(script.textContent)['@graph']
    const page = graphe.find((n) => n['@type'] === 'WebPage')
    expect(page.author.map((a) => a.name)).toContain('Entrelianes')

    const jeu = graphe.find((n) => n['@type'] === 'Dataset')
    expect(jeu.name).toContain('Métropole Européenne de Lille')
    expect(jeu.temporalCoverage).toBe('2015/2021')
    expect(jeu.distribution[0].contentUrl).toMatch(/stats_c2c_1_all_2021\.json$|\/map\/stats\/com2co\/1\/all/)
    // licence des données non arrêtée : rien n'est affirmé
    expect(jeu.license).toBeUndefined()

    wrapper.unmount()
    expect(document.getElementById('json-ld-accueil')).toBeNull()
  })

  it("raconte la genèse de l'outil avec les faits fournis par l'équipe", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const texte = wrapper.find('section[aria-labelledby="genese"]').text().replace(/\s/g, ' ')
    expect(texte).toContain("LM Oxygène et Deûl'Air")
    expect(texte).toContain('17 m² par habitant annoncés par la ville')
    expect(texte).toContain('Lille, Hellemmes et Lomme, puis toutes les communes de la MEL')
    expect(texte).toContain('Sciences Po Lille')
    expect(texte).toContain('municipales de 2026')
    expect(texte).toContain("L'outil est né d'un chiffre qui ne collait pas")
  })

  it('affiche les chiffres du projet et la date à laquelle ils ont été arrêtés', async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const texte = wrapper.find('section[aria-labelledby="projet-en-chiffres"]').text().replace(/\s/g, ' ')
    expect(texte).toMatch(/1 679 ?parcs référencés, dont 828 vérifiés/)
    expect(texte).toMatch(/2 259 ?entrées de parc saisies/)
    expect(texte).toMatch(/14 ?associations/)
    expect(texte).toMatch(/13 760 819 ?carreaux de 200 m/)
    expect(texte).toContain('Au 12 novembre 2026')
    expect(texte).toContain('1 253 intercommunalités')
    expect(texte).toContain('34 723 communes')
  })

  it("distingue les territoires disponibles de ceux dont les parcs sont décrits", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    expect(wrapper.text().replace(/\s/g, ' ')).toContain("les résultats n'existent que là où des associations ont décrit les parcs")
  })

  it("attribue les seuils à leur source sans affirmer de valeurs non sourcées", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const texte = wrapper.text().replace(/\s/g, ' ')
    expect(texte).toContain('circulaire ministérielle du 8 février 1973')
    expect(texte).toContain('selon le Réseau français des Villes-Santé')
    expect(texte).toContain('forêts urbaines et des espaces boisés périurbains')
    // 12 m² et 45 m² « conseillés » : aucune source ne les établit, ils ne doivent pas figurer sur la page
    expect(texte).not.toMatch(/12 conseillés|45 conseillés/)
    expect(texte).not.toMatch(/\b(12|45) ?m²/)
  })

  it("cite les trois sources de l'OMS et du réseau Villes-Santé avec des liens externes sûrs", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const liens = wrapper.findAll('a').filter((a) => /who\.int|villes-sante\.com/.test(a.attributes('href') ?? ''))
    expect(liens.map((a) => a.attributes('href'))).toEqual([
      'https://www.who.int/europe/publications/i/item/WHO-EURO-2016-3352-43111-60341',
      'https://www.who.int/europe/publications/i/item/9789289052498',
      'https://villes-sante.com/wp-content/uploads/2023/02/Espaces-verts-urbains.pdf'
    ])
    liens.forEach((a) => expect(a.attributes('rel')).toContain('noopener'))
  })

  it('annonce la licence AGPLv3', async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const lien = wrapper.findAll('a').find((a) => a.attributes('href') === 'https://www.gnu.org/licenses/agpl-3.0.html')
    expect(lien?.text()).toBe('licence AGPLv3')
  })

  it("le bouton « Décrire les parcs de mon territoire » ouvre un message au projet", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const bouton = wrapper.findAll('button').find((b) => b.text() === 'Décrire les parcs de mon territoire')
    await bouton.trigger('click')

    expect(ecrireAuProjet).toHaveBeenCalledWith(expect.stringContaining('Décrire les parcs'))
  })

  it("présente la simulation d'ajout de parcs et ses méthodes en langage courant", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const histoire = wrapper.find('section[aria-labelledby="genese"]')
    const texte = histoire.text().replace(/\s/g, ' ')
    expect(texte).toContain('Depuis le printemps 2026')
    expect(texte).toContain("simuler l'ajout de parcs")
    expect(texte).toContain('recherche opérationnelle')

    const methodes = histoire.findAll('li').map((li) => li.text().replace(/\s/g, ' '))
    expect(methodes).toHaveLength(3)
    expect(methodes[0]).toContain('programmation par contraintes')
    expect(methodes[1]).toContain('algorithme génétique')
    expect(methodes[2]).toContain('algorithme glouton')
    // lecture grand public : chaque méthode a un nom courant avant le terme technique
    expect(methodes[0]).toMatch(/^Les règles d'abord \(programmation par contraintes\)/)
    expect(texte).toContain('Encore faut-il savoir où poser ces parcs')
  })

  it("ne fait pas de la simulation une promesse au public : réservée au système de gestion", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    expect(wrapper.text().replace(/\s/g, ' ')).toContain("n'est disponible que dans notre système de gestion")
  })

  it("décrit le mode manuel de l'été 2026 et signale que son évaluation est simplifiée", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const histoire = wrapper.find('section[aria-labelledby="genese"]')

    const texte = histoire.text().replace(/\s/g, ' ')
    expect(texte).toContain("Autre nouveauté, ajoutée pendant l'été 2026 : un mode manuel")
    expect(texte).toContain('on dessine soi-même un futur parc')
    expect(texte).toContain('d\'un simple cercle ou d\'un contour précis')
    expect(texte).toMatch(/Un clic sur « ?Évaluer ? »/)
    // aucune méthode d'étude principale n'est attribuée au mode manuel : la simplification est dite
    expect(texte).toContain('cette évaluation est simplifiée')
    expect(texte).toContain("à vol d'oiseau")
    expect(texte).toContain('sans calcul d\'itinéraire depuis des entrées')
    expect(texte).toContain('version bêta')
  })

  it("place le mode manuel après la simulation automatique de l'histoire", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const texte = wrapper.find('section[aria-labelledby="genese"]').text()
    expect(texte.indexOf('Depuis le printemps 2026')).toBeGreaterThan(-1)
    expect(texte.indexOf("pendant l'été 2026")).toBeGreaterThan(texte.indexOf('Depuis le printemps 2026'))
  })

  it("titre la section sur la genèse de l'outil, pas sur l'histoire des associations", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const titre = wrapper.find('#genese')
    expect(titre.text()).toBe("La genèse de l'outil")
    expect(wrapper.text()).not.toContain('Notre histoire')
  })

  it("raconte la genèse d'une seule coulée, sans intertitre qui coupe la lecture", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const genese = wrapper.find('section[aria-labelledby="genese"]')
    expect(genese.findAll('h3')).toHaveLength(0)
    const texte = genese.text().replace(/\s/g, ' ')
    const ordre = ['municipales de 2026', 'Depuis le printemps 2026', 'Encore faut-il savoir où poser ces parcs', 'Autre nouveauté', 'Une précision importante']
    const positions = ordre.map((phrase) => texte.indexOf(phrase))
    positions.forEach((position, i) => expect(position, ordre[i]).toBeGreaterThan(-1))
    expect([...positions].sort((a, b) => a - b)).toEqual(positions)
  })

  it("date la conférence du 15 janvier 2026 et renvoie à deux comptes rendus de presse", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const genese = wrapper.find('section[aria-labelledby="genese"]')
    const date = genese.find('time')
    expect(date.attributes('datetime')).toBe('2026-01-15')
    expect(date.text()).toBe('15 janvier 2026')

    const liens = genese.findAll('a').filter((a) => /mediacites\.fr|aefinfo\.fr/.test(a.attributes('href') ?? ''))
    expect(liens.map((a) => [a.text(), a.attributes('href')])).toEqual([
      ['Médiacités', 'https://www.mediacites.fr/reportage/lille/2026/01/16/a-lille-les-candidats-sopposent-a-nouveau-sur-lamenagement-de-la-friche-saint-sauveur/'],
      ['AEF info', 'https://www.aefinfo.fr/depeche/744403-a-lille-lopposition-remet-en-question-le-modele-de-densification-urbaine']
    ])
    liens.forEach((a) => {
      expect(a.attributes('rel')).toContain('noopener')
      expect(a.attributes('target')).toBe('_blank')
    })
    // les liens renvoient à des comptes rendus de la conférence : ils n'affirment rien sur le contenu des articles
    expect(genese.text().replace(/\s/g, ' ')).toContain('Médiacités et AEF info en ont rendu compte.')
    // neutralité : aucun candidat n'est nommé ni cité, ni dans le texte ni par un lien vers un réseau social
    expect(wrapper.text()).not.toMatch(/Spillebout|Deslandes|Baly|Addouche/)
    expect(wrapper.findAll('a').some((a) => /x\.com|twitter\.com/.test(a.attributes('href') ?? ''))).toBe(false)
  })

  it("présente la conférence comme le démarrage des débats publics, sans parler de série", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const texte = wrapper.find('section[aria-labelledby="genese"]').text().replace(/\s/g, ' ')
    expect(texte).toContain('Elle a marqué le démarrage des débats publics entre les candidats aux municipales de 2026 à Lille')
    expect(wrapper.text()).not.toMatch(/série/i)
  })

  it("porte le nom officiel de la MEL dans le H1 et « espaces verts » dans les titres de section", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    expect(wrapper.find('h1').text().replace(/\s/g, ' ')).toBe('Combien de m² de parcs et jardins par habitant dans la Métropole Européenne de Lille ?')
    const titres = wrapper.findAll('h2').map((h) => h.text().replace(/\s/g, ' '))
    expect(titres.filter((titre) => /espaces verts/i.test(titre)).length).toBeGreaterThanOrEqual(2)
    expect(titres).toContain("La genèse de l'outil")
    expect(wrapper.text().replace(/\s/g, ' ')).toContain('Lille et de la Métropole Européenne de Lille (MEL)')
  })

  it("invite à consulter la carte et décrit ce qu'elle affiche pour un carreau", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const section = wrapper.find('section[aria-labelledby="chez-vous"]')
    const texte = section.text().replace(/\s/g, ' ')
    expect(section.find('h2').text().replace(/\s/g, ' ')).toBe('Combien de m² de parc autour de chez vous ?')
    expect(texte).toContain('saisissez votre adresse')
    expect(texte).toContain('la surface de parc accessible à pied')
    expect(texte).toContain("le nombre d'habitants avec lesquels elle est partagée")
    expect(texte).toContain('m² par habitant')
    expect(texte).toContain('la liste des parcs concernés')
    expect(section.find('a').attributes('href')).toBe('/carte')
    // aucun seuil non sourcé dans l'invitation
    expect(texte).not.toMatch(/\b(12|45) ?m²/)
  })

  it("illustre la page par la carte de Lille, avec texte alternatif et dimensions réservées", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const image = wrapper.find('section[aria-labelledby="chez-vous"] img')
    expect(image.attributes('src')).toMatch(/og-carte-lille-1200x630\.jpg$/)
    expect(image.attributes('alt')).toContain('Carte de Lille')
    expect(image.attributes('width')).toBe('1200')
    expect(image.attributes('height')).toBe('630')
    expect(image.attributes('loading')).toBe('lazy')
    expect(wrapper.find('figcaption').text()).toContain('OpenStreetMap')
  })

  it("cite le titre de la conférence, qui porte Saint-Sauveur et la densification", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const texte = wrapper.find('section[aria-labelledby="genese"]').text().replace(/\s/g, ' ')
    expect(texte).toContain('De la friche Saint-Sauveur aux Rives de la Haute-Deûle : quelles alternatives à la densification urbaine ?')
  })

  it("s'affiche avec ses chiffres dès le premier rendu quand ils sont intégrés au HTML, sans « Chargement… »", async () => {
    const script = document.createElement('script')
    script.type = 'application/json'
    script.id = 'landing-stats'
    script.textContent = JSON.stringify({
      lille: { annee: '2021', habitants: 1000, sousMinimum: 50.5, minimumM2: 10, premiereBorne: 3, premiereTranche: 20 },
      mel: { annee: '2021', habitants: 5000, sousMinimum: 60.1, minimumM2: null, premiereBorne: null, premiereTranche: null }
    })
    document.head.appendChild(script)
    simulerReseau({ lille: null, mel: null })

    const wrapper = monter()
    // avant toute attente : le premier rendu contient déjà les chiffres
    const chiffres = wrapper.find('section[aria-labelledby="chiffres-cles"]').text()
    expect(chiffres).toContain('50,5')
    expect(chiffres).toContain('60,1')
    expect(chiffres).not.toContain('Chargement des chiffres')
    expect(wrapper.attributes('data-landing-ready')).toBe('true')

    // un échec réseau ne les efface pas
    await flushPromises()
    expect(wrapper.find('section[aria-labelledby="chiffres-cles"]').text()).toContain('50,5')
  })

  it("met à jour les chiffres intégrés avec ceux du réseau et les réintègre pour le pré-rendu", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const integres = JSON.parse(document.getElementById('landing-stats').textContent)
    expect(integres.lille.sousMinimum).toBe(78.8)
    expect(integres.mel.sousMinimum).toBe(80)
    expect(wrapper.attributes('data-landing-ready')).toBe('true')
  })

  it("affiche la moyenne pondérée et le ratio global quand le serveur les fournit", async () => {
    const lilleAvec = { ...lille, descripteur: { habitants: 205789, m2ParHabitantMoyen: 11.5, surfaceParcsOmsM2: 3400000, m2ParHabitantGlobal: 16.5 } }
    const melAvec = { ...mel, descripteur: { habitants: 1000, m2ParHabitantMoyen: 9.2, surfaceParcsOmsM2: 20000, m2ParHabitantGlobal: 20 } }
    simulerReseau({ lille: lilleAvec, mel: melAvec })
    const wrapper = monter()
    await flushPromises()

    const chiffres = wrapper.find('section[aria-labelledby="chiffres-cles"]').text().replace(/\s/g, ' ')
    expect(chiffres).toContain('En moyenne, un habitant dispose de 11,5 m² de parc accessible à pied')
    expect(chiffres).toContain('pondérée par la population')
    expect(chiffres).toContain('comptées une seule fois, rapportées à la population prise en compte : 16,5 m² par habitant')
    expect(chiffres).toContain('9,2 m²')
    expect(chiffres).toContain('20 m² par habitant')
  })

  it("n'affiche aucune ligne de m² par habitant tant que les JSON n'ont pas de descripteur", async () => {
    simulerReseau({ lille, mel })
    const wrapper = monter()
    await flushPromises()

    const chiffres = wrapper.find('section[aria-labelledby="chiffres-cles"]').text().replace(/\s/g, ' ')
    expect(chiffres).not.toContain('En moyenne, un habitant dispose')
    expect(chiffres).not.toContain('comptées une seule fois')
    // les pourcentages restent affichés
    expect(chiffres).toContain('78,8')
  })

  it("garde les m² par habitant dans les chiffres intégrés au HTML pour le pré-rendu", async () => {
    const lilleAvec = { ...lille, descripteur: { m2ParHabitantMoyen: 11.5, m2ParHabitantGlobal: 16.5 } }
    simulerReseau({ lille: lilleAvec, mel })
    const wrapper = monter()
    await flushPromises()

    const integres = JSON.parse(document.getElementById('landing-stats').textContent)
    expect(integres.lille.m2Moyen).toBe(11.5)
    expect(integres.lille.m2Global).toBe(16.5)
    expect(integres.mel.m2Moyen).toBeNull()
  })
})

