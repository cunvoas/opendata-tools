import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

describe('versionCheck', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.spyOn(globalThis, 'setInterval').mockImplementation(() => 0)
    vi.spyOn(console, 'log').mockImplementation(() => {})
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.resetModules()
    delete globalThis.fetch
    localStorage.clear()
  })

  async function loadAndInit(fetchMock) {
    globalThis.fetch = fetchMock
    const { initVersionCheck } = await import('../versionCheck.js')
    initVersionCheck()
    await new Promise(resolve => setTimeout(resolve, 10))
  }

  it('enregistre la version si aucune version précédente en localStorage', async () => {
    // localStorage vide → pas de lastVersion
    const mockFetch = vi.fn().mockResolvedValue({ json: async () => ({ version: '1.0.0' }) })
    await loadAndInit(mockFetch)
    expect(localStorage.getItem('app-version')).toBe('1.0.0')
  })

  it('ne recharge pas si la version est identique', async () => {
    localStorage.setItem('app-version', '1.0.0')
    const reloadSpy = vi.spyOn(window.location, 'reload').mockImplementation(() => {})
    const mockFetch = vi.fn().mockResolvedValue({ json: async () => ({ version: '1.0.0' }) })
    await loadAndInit(mockFetch)
    expect(reloadSpy).not.toHaveBeenCalled()
  })

  it('recharge si une nouvelle version est détectée', async () => {
    localStorage.setItem('app-version', '1.0.0')
    const reloadSpy = vi.spyOn(window.location, 'reload').mockImplementation(() => {})
    const mockFetch = vi.fn().mockResolvedValue({ json: async () => ({ version: '1.1.0' }) })
    await loadAndInit(mockFetch)
    expect(localStorage.getItem('app-version')).toBe('1.1.0')
    expect(reloadSpy).toHaveBeenCalled()
  })

  it('gère silencieusement une erreur fetch sans modifier localStorage', async () => {
    const mockFetch = vi.fn().mockRejectedValue(new Error('Network error'))
    await loadAndInit(mockFetch)
    expect(localStorage.getItem('app-version')).toBeNull()
  })
})
