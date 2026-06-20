import { beforeEach, describe, expect, it, vi } from 'vitest'
import { requestSpeciesEnrichmentSuggestion } from '@/services/ai/speciesEnrichmentSuggestionService'
import { apiFetch } from '@/services/http/apiClient'

vi.mock('@/services/http/apiClient', () => ({
  apiFetch: vi.fn(),
}))

const apiFetchMock = vi.mocked(apiFetch)

describe('speciesEnrichmentSuggestionService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('requestSpeciesEnrichmentSuggestion envía POST autenticado al endpoint IA', async () => {
    const response = {
      synonyms: ['Encina'],
      distribution: { continents: ['Europa'] },
    }
    apiFetchMock.mockResolvedValueOnce(response)

    const result = await requestSpeciesEnrichmentSuggestion(
      { scientificName: 'Quercus ilex', commonName: 'Encina' },
      new AbortController().signal,
    )

    expect(result).toEqual(response)
    expect(apiFetchMock).toHaveBeenCalledWith('/api/ai/species/enrichment-suggestions', {
      method: 'POST',
      body: JSON.stringify({ scientificName: 'Quercus ilex', commonName: 'Encina' }),
      signal: expect.any(AbortSignal),
    })
  })
})
