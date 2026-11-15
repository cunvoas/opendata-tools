import { describe, it, expect, beforeEach } from 'vitest'
import { applyRouteMeta, SITE_URL } from '../seoMeta.js'

function documentWithMeta() {
  document.head.innerHTML = `
    <meta name="description" content="ancienne" />
    <meta property="og:title" content="ancien" />
    <meta property="og:description" content="ancienne" />
    <meta property="og:url" content="ancienne" />
    <meta name="twitter:title" content="ancien" />
    <meta name="twitter:description" content="ancienne" />
    <meta name="robots" content="index, follow" />`
  return document
}

const content = (selector) => document.querySelector(selector).getAttribute('content')

describe('applyRouteMeta', () => {
  beforeEach(() => documentWithMeta())

  it('reporte titre, description et URL de la route sur title, Open Graph et Twitter', () => {
    applyRouteMeta(document, { path: '/stats', meta: { title: 'Titre', description: 'Description' } })

    expect(document.title).toBe('Titre')
    expect(content('meta[property="og:title"]')).toBe('Titre')
    expect(content('meta[name="twitter:title"]')).toBe('Titre')
    expect(content('meta[name="description"]')).toBe('Description')
    expect(content('meta[property="og:description"]')).toBe('Description')
    expect(content('meta[name="twitter:description"]')).toBe('Description')
    expect(content('meta[property="og:url"]')).toBe(`${SITE_URL}/stats`)
  })

  it('la racine garde une URL se terminant par /', () => {
    applyRouteMeta(document, { path: '/', meta: { title: 'Accueil' } })

    expect(content('meta[property="og:url"]')).toBe(`${SITE_URL}/`)
  })

  it('sans meta de route, laisse titre et description intacts et ne lève pas', () => {
    expect(() => applyRouteMeta(document, { path: '/x' })).not.toThrow()
    expect(content('meta[name="description"]')).toBe('ancienne')
  })

  it('ignore une balise absente du document', () => {
    document.head.innerHTML = ''

    expect(() => applyRouteMeta(document, { path: '/', meta: { title: 'T', description: 'D' } })).not.toThrow()
  })

  it('applique la balise robots de la route puis la réinitialise sur la route suivante', () => {
    applyRouteMeta(document, { path: '/etude-insee', meta: { title: 'T', robots: 'noindex, follow' } })
    expect(content('meta[name="robots"]')).toBe('noindex, follow')

    applyRouteMeta(document, { path: '/carte', meta: { title: 'T' } })
    expect(content('meta[name="robots"]')).toBe('index, follow')
  })
})
