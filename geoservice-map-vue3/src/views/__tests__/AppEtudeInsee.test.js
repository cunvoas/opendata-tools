import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AppEtudeInsee from '../AppEtudeInsee.vue'

const RouterLinkStub = { props: ['to'], template: '<a :href="to"><slot /></a>' }
const monter = () => mount(AppEtudeInsee, { global: { stubs: { RouterLink: RouterLinkStub } } })

describe('AppEtudeInsee.vue', () => {
  it('répond en tête de page à la question « pourquoi les chiffres diffèrent »', () => {
    const wrapper = monter()

    expect(wrapper.findAll('h1')).toHaveLength(1)
    expect(wrapper.find('h1').text()).toContain('pourquoi les chiffres diffèrent')
    expect(wrapper.findAll('p')[0].text()).toContain('Réponse courte')
    expect(wrapper.findAll('p')[0].text()).toContain('ni la même population ni le même accès')
  })

  it("borne le chiffre de l'INSEE dans la même phrase que son périmètre", () => {
    const texte = monter().text().replace(/\s/g, ' ')

    expect(texte).toMatch(/51,9 % de la population du grand centre urbain de Lille \(et non de la commune seule\) vivait à moins de 300 m .* en 2019/)
  })

  it('compare les deux méthodes dans un tableau sourcé et daté', () => {
    const wrapper = monter()

    const lignes = wrapper.findAll('tbody tr')
    expect(lignes).toHaveLength(8)
    expect(wrapper.find('table').text()).toContain('72 grands centres urbains')
    expect(lignes.at(-1).text()).toContain('Insee Première n° 2049')
  })

  it("cite les sources de l'OMS et de l'INSEE avec des liens externes sûrs", () => {
    const wrapper = monter()
    const liens = wrapper.findAll('a').filter((a) => /who\.int|villes-sante\.com|insee\.fr/.test(a.attributes('href') ?? ''))

    expect(liens.length).toBeGreaterThanOrEqual(5)
    liens.forEach((a) => expect(a.attributes('rel')).toContain('noopener'))
  })

  it("attribue les seuils à leur source sans affirmer de valeurs non sourcées", () => {
    const texte = monter().text().replace(/\s/g, ' ')

    expect(texte).toContain('circulaire ministérielle du 8 février 1973')
    expect(texte).toContain('selon le Réseau français des Villes-Santé')
    expect(texte).toContain('forêts urbaines et des espaces boisés périurbains')
    // 12 m² et 45 m² « conseillés » : aucune source ne les établit, ils ne doivent pas figurer sur la page
    expect(texte).not.toMatch(/12 conseillés|45 conseillés/)
    expect(texte).not.toMatch(/\b(12|45) ?m²/)
  })

  it("cite les sources de l'OMS, du réseau Villes-Santé et de l'INSEE avec des liens externes sûrs", () => {
    const wrapper = monter()
    const liens = wrapper.findAll('a').filter((a) => /who\.int|villes-sante\.com/.test(a.attributes('href') ?? ''))

    expect(liens.map((a) => a.attributes('href'))).toEqual([
      'https://www.who.int/europe/publications/i/item/WHO-EURO-2016-3352-43111-60341',
      'https://www.who.int/europe/publications/i/item/9789289052498',
      'https://villes-sante.com/wp-content/uploads/2023/02/Espaces-verts-urbains.pdf'
    ])
    liens.forEach((a) => expect(a.attributes('rel')).toContain('noopener'))
  })

  it("renvoie vers les chiffres de l'accueil", () => {
    const lien = monter().findAll('a').find((a) => a.attributes('href') === '/#insee')

    expect(lien).toBeTruthy()
  })
})
