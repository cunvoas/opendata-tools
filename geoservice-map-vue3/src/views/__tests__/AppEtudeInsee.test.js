import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AppEtudeInsee from '../AppEtudeInsee.vue'

describe('AppEtudeInsee.vue', () => {
  it("ne contient qu'un titre et les liens vers l'étude et sa méthode", () => {
    const wrapper = mount(AppEtudeInsee)

    expect(wrapper.findAll('h1')).toHaveLength(1)
    const hrefs = wrapper.findAll('a').map((a) => a.attributes('href'))
    expect(hrefs).toEqual([
      'https://www.insee.fr/fr/statistiques/8558420',
      'https://www.insee.fr/fr/statistiques/8558420#methodes'
    ])
    wrapper.findAll('a').forEach((a) => expect(a.attributes('rel')).toContain('noopener'))
    expect(wrapper.findAll('p')).toHaveLength(0)
  })
})
