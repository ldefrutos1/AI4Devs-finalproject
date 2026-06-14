import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  fetchPublicTreeEnrichment,
  fetchSpeciesEnrichment,
  fetchTreeEnrichment,
  updateSpeciesEnrichment,
  updateTreeEnrichment,
} from '@/services/catalog/enrichmentService'
import { apiFetch, publicApiFetch } from '@/services/http/apiClient'

vi.mock('@/services/http/apiClient', () => ({
  apiFetch: vi.fn(),
  publicApiFetch: vi.fn(),
}))

const apiFetchMock = vi.mocked(apiFetch)
const publicApiFetchMock = vi.mocked(publicApiFetch)

describe('enrichmentService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('fetchSpeciesEnrichment consulta por speciesId', async () => {
    const enrichment = { speciesId: 12, scientificName: 'Quercus ilex', commonName: 'Encina' }
    apiFetchMock.mockResolvedValueOnce(enrichment)

    const result = await fetchSpeciesEnrichment(12)

    expect(result).toEqual(enrichment)
    expect(apiFetchMock).toHaveBeenCalledWith('/api/catalog/species/12/enrichment', {
      signal: undefined,
    })
  })

  it('updateSpeciesEnrichment envía PUT con cuerpo JSON', async () => {
    const payload = { synonyms: ['Quercus rotundifolia'] }
    const response = { speciesId: 12, ...payload, scientificName: 'Quercus ilex' }
    apiFetchMock.mockResolvedValueOnce(response)

    await updateSpeciesEnrichment(12, payload)

    expect(apiFetchMock).toHaveBeenCalledWith('/api/catalog/species/12/enrichment', {
      method: 'PUT',
      body: JSON.stringify(payload),
      signal: undefined,
    })
  })

  it('fetchTreeEnrichment consulta por treeId', async () => {
    const enrichment = { treeId: 42, speciesId: 12, tags: ['singular'] }
    apiFetchMock.mockResolvedValueOnce(enrichment)

    const result = await fetchTreeEnrichment(42)

    expect(result).toEqual(enrichment)
    expect(apiFetchMock).toHaveBeenCalledWith('/api/catalog/trees/42/enrichment', {
      signal: undefined,
    })
  })

  it('updateTreeEnrichment envía PUT con cuerpo JSON', async () => {
    const payload = {
      measurements: { heightM: 18.5 },
      tags: ['it-enrichment'],
    }
    apiFetchMock.mockResolvedValueOnce({ treeId: 42, ...payload })

    await updateTreeEnrichment(42, payload)

    expect(apiFetchMock).toHaveBeenCalledWith('/api/catalog/trees/42/enrichment', {
      method: 'PUT',
      body: JSON.stringify(payload),
      signal: undefined,
    })
  })

  it('fetchPublicTreeEnrichment usa publicApiFetch sin autenticación', async () => {
    const enrichment = {
      speciesEnrichment: { speciesId: 3, scientificName: 'Pinus pinea' },
      treeEnrichment: { treeId: 7, tags: ['public'] },
    }
    publicApiFetchMock.mockResolvedValueOnce(enrichment)

    const result = await fetchPublicTreeEnrichment(7)

    expect(result).toEqual(enrichment)
    expect(publicApiFetchMock).toHaveBeenCalledWith('/api/catalog/public/trees/7/enrichment', {
      signal: undefined,
    })
    expect(apiFetchMock).not.toHaveBeenCalled()
  })

  it('propaga AbortSignal en operaciones autenticadas', async () => {
    const controller = new AbortController()
    apiFetchMock.mockResolvedValue({ treeId: 1 })

    await fetchTreeEnrichment(1, controller.signal)

    expect(apiFetchMock).toHaveBeenCalledWith('/api/catalog/trees/1/enrichment', {
      signal: controller.signal,
    })
  })

  it('propaga AbortSignal en lectura pública', async () => {
    const controller = new AbortController()
    publicApiFetchMock.mockResolvedValue({})

    await fetchPublicTreeEnrichment(5, controller.signal)

    expect(publicApiFetchMock).toHaveBeenCalledWith('/api/catalog/public/trees/5/enrichment', {
      signal: controller.signal,
    })
  })
})
