import { describe, it, expect } from 'vitest'
import fs from 'node:fs'
import path from 'node:path'

const html = fs.readFileSync(path.resolve(__dirname, '../../index.html'), 'utf-8')
const types = [...html.matchAll(/"@type":\s*"(\w+)"/g)].map((m) => m[1])

describe('index.html : données structurées communes à toutes les pages', () => {
  it("ne déclare plus d'Article ni de faux fil d'Ariane", () => {
    expect(types).not.toContain('Article')
    expect(types).not.toContain('BreadcrumbList')
  })

  it('garde la description de l\'application', () => {
    expect(types).toContain('WebApplication')
  })

  it('nomme les associations comme des organisations, avec leur graphie exacte', () => {
    expect(html).toContain('Entrelianes')
    expect(html).not.toContain('Entrelianne')
  })

  it("partage la carte de Lille avec ses dimensions et un texte alternatif", () => {
    expect(html).toMatch(/property="og:image" content="[^"]*og-carte-lille-1200x630\.jpg"/)
    expect(html).toContain('property="og:image:width" content="1200"')
    expect(html).toContain('property="og:image:height" content="630"')
    expect(html).toMatch(/property="og:image:alt" content="Carte de Lille/)
  })
})
