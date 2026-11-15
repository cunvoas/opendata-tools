import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { ecrireAuProjet } from '../contact.js'

describe('ecrireAuProjet', () => {
  const locationOrigine = window.location

  beforeEach(() => {
    Object.defineProperty(window, 'location', { value: { href: '' }, writable: true, configurable: true })
  })
  afterEach(() => {
    Object.defineProperty(window, 'location', { value: locationOrigine, writable: true, configurable: true })
  })

  it('ouvre un message mailto avec le sujet encodé', () => {
    ecrireAuProjet("Isochrone: Demande d'accès")

    expect(window.location.href).toMatch(/^mailto:[^@\s]+@[^@\s?]+\?subject=/i)
    const sujet = decodeURIComponent(window.location.href.split('?subject=')[1])
    expect(sujet).toBe("Isochrone: Demande d'accès")
  })
})
