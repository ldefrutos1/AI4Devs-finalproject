import { computed, createApp, nextTick } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import { createI18n } from 'vue-i18n'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { es } from '@/i18n/locales/es'
import { useTreeFormEnrichment } from '@/composables/useTreeFormEnrichment'

vi.mock('@/services/catalog/enrichmentService', () => ({
  fetchSpeciesEnrichment: vi.fn(),
  fetchTreeEnrichment: vi.fn(),
  updateSpeciesEnrichment: vi.fn(),
  updateTreeEnrichment: vi.fn(),
}))

vi.mock('@/services/ai/speciesEnrichmentSuggestionService', () => ({
  requestSpeciesEnrichmentSuggestion: vi.fn(),
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    hasRole: (role: string) => role === 'ADMIN',
  }),
}))

import {
  fetchSpeciesEnrichment,
  fetchTreeEnrichment,
  updateSpeciesEnrichment,
  updateTreeEnrichment,
} from '@/services/catalog/enrichmentService'
import { requestSpeciesEnrichmentSuggestion } from '@/services/ai/speciesEnrichmentSuggestionService'

function mountEnrichment(treeId: number | null, speciesId = '12') {
  let api!: ReturnType<typeof useTreeFormEnrichment>
  const app = createApp({
    setup() {
      api = useTreeFormEnrichment({
        treeId: computed(() => treeId),
        speciesId: computed(() => speciesId),
      })
      return () => null
    },
  })
  app.use(createI18n({ legacy: false, locale: 'es', messages: { es } }))
  app.mount(document.createElement('div'))
  return api
}

describe('useTreeFormEnrichment', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.mocked(fetchTreeEnrichment).mockResolvedValue({
      treeId: 42,
      tags: ['singular'],
      measurements: { heightM: 10 },
    })
    vi.mocked(updateTreeEnrichment).mockResolvedValue({
      treeId: 42,
      tags: ['singular'],
    })
    vi.mocked(fetchSpeciesEnrichment).mockResolvedValue({
      speciesId: 12,
      scientificName: 'Quercus ilex',
      commonName: 'Encina',
    })
    vi.mocked(updateSpeciesEnrichment).mockResolvedValue({
      speciesId: 12,
      scientificName: 'Quercus ilex',
    })
  })

  it('carga enriquecimiento del ejemplar cuando hay treeId', async () => {
    const api = mountEnrichment(42)
    await nextTick()
    await vi.waitFor(() => expect(fetchTreeEnrichment).toHaveBeenCalledWith(42, expect.any(AbortSignal)))

    expect(api.treeEnrichmentDraft.value?.tags).toEqual(['singular'])
  })

  it('validateBeforePersist bloquea cuando el panel reporta JSON inválido', () => {
    const api = mountEnrichment(42)
    api.onTreeEnrichmentDraftState({ dirty: true, valid: false, errorKey: 'invalidJson' })

    expect(api.validateBeforePersist()).toBe(false)
    expect(api.treeEnrichmentExpanded.value).toBe(true)
    expect(api.treeEnrichmentError.value).toContain('JSON')
  })

  it('persistTreeEnrichment envía PUT con el borrador', async () => {
    const api = mountEnrichment(42)
    await nextTick()
    await vi.waitFor(() => expect(api.treeEnrichmentDraft.value).not.toBeNull())

    api.treeEnrichmentDraft.value = {
      tags: ['protegido'],
      measurements: {},
      healthStatus: {},
      observations: [],
    }

    const ok = await api.persistTreeEnrichment()
    expect(ok).toBe(true)
    expect(updateTreeEnrichment).toHaveBeenCalledWith(
      42,
      expect.objectContaining({ tags: ['protegido'] }),
      expect.any(AbortSignal),
    )
  })

  it('carga enriquecimiento de especie al cambiar speciesId', async () => {
    const api = mountEnrichment(null, '12')
    await nextTick()
    await vi.waitFor(() =>
      expect(fetchSpeciesEnrichment).toHaveBeenCalledWith(12, expect.any(AbortSignal)),
    )
    expect(api.speciesEnrichment.value?.scientificName).toBe('Quercus ilex')
  })

  it('onSpeciesPopupOpen recarga enriquecimiento de especie', async () => {
    const api = mountEnrichment(null, '12')
    await api.onSpeciesPopupOpen()
    await nextTick()

    expect(fetchSpeciesEnrichment).toHaveBeenCalledWith(12, expect.any(AbortSignal))
    expect(api.speciesEnrichment.value?.scientificName).toBe('Quercus ilex')
  })

  it('canRequestSpeciesAiSuggestion es true para ADMIN sin enriquecimiento previo', async () => {
    const api = mountEnrichment(null, '12')
    await nextTick()
    await vi.waitFor(() => expect(api.speciesEnrichment.value).not.toBeNull())

    expect(api.canRequestSpeciesAiSuggestion.value).toBe(true)
  })

  it('canRequestSpeciesAiSuggestion es false si ya hay datos ampliados', async () => {
    vi.mocked(fetchSpeciesEnrichment).mockResolvedValueOnce({
      speciesId: 12,
      scientificName: 'Quercus ilex',
      commonName: 'Encina',
      synonyms: ['Quercus rotundifolia'],
    })
    const api = mountEnrichment(null, '12')
    await nextTick()
    await vi.waitFor(() => expect(api.speciesEnrichment.value?.synonyms?.length).toBe(1))

    expect(api.canRequestSpeciesAiSuggestion.value).toBe(false)
  })

  it('requestSpeciesAiSuggestion precarga payload y no persiste en catálogo', async () => {
    vi.mocked(requestSpeciesEnrichmentSuggestion).mockResolvedValueOnce({
      synonyms: ['Encina', 'Quercus ilex'],
      distribution: { continents: ['Europa'], countries: ['España'] },
      ecologicalData: { growthRate: 'moderate' },
    })

    const api = mountEnrichment(null, '12')
    await nextTick()
    await vi.waitFor(() => expect(api.canRequestSpeciesAiSuggestion.value).toBe(true))

    await api.requestSpeciesAiSuggestion()

    expect(requestSpeciesEnrichmentSuggestion).toHaveBeenCalledWith(
      { scientificName: 'Quercus ilex', commonName: 'Encina' },
      expect.any(AbortSignal),
    )
    expect(api.speciesAiSuggestionPayload.value).toMatchObject({
      synonyms: ['Encina', 'Quercus ilex'],
    })
    expect(updateSpeciesEnrichment).not.toHaveBeenCalled()
  })

  it('requestSpeciesAiSuggestion expone error de IA sin guardar', async () => {
    const { HttpError } = await import('@/services/http/apiClient')
    vi.mocked(requestSpeciesEnrichmentSuggestion).mockRejectedValueOnce(
      new HttpError(422, {
        title: 'Respuesta IA inválida',
        status: 422,
        detail: 'Respuesta IA inválida',
      }),
    )

    const api = mountEnrichment(null, '12')
    await nextTick()
    await vi.waitFor(() => expect(api.canRequestSpeciesAiSuggestion.value).toBe(true))

    await api.requestSpeciesAiSuggestion()

    expect(api.aiSuggestionError.value).toContain('Respuesta IA inválida')
    expect(api.speciesAiSuggestionPayload.value).toBeNull()
  })
})
