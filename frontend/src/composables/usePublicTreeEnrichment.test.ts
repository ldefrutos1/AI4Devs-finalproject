import { computed, createApp, nextTick } from 'vue'
import { createI18n } from 'vue-i18n'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { es } from '@/i18n/locales/es'
import { usePublicTreeEnrichment } from '@/composables/usePublicTreeEnrichment'

vi.mock('@/services/catalog/enrichmentService', () => ({
  fetchPublicTreeEnrichment: vi.fn(),
}))

import { fetchPublicTreeEnrichment } from '@/services/catalog/enrichmentService'

function mountHook(treeId: number | null, treeDetail: ReturnType<typeof buildDetail> | null = null) {
  let api!: ReturnType<typeof usePublicTreeEnrichment>
  const app = createApp({
    setup() {
      api = usePublicTreeEnrichment({
        treeId: computed(() => treeId),
        treeDetail: computed(() => treeDetail),
      })
      return () => null
    },
  })
  app.use(createI18n({ legacy: false, locale: 'es', messages: { es } }))
  app.mount(document.createElement('div'))
  return api
}

function buildDetail() {
  return {
    treeId: 7,
    commonName: 'Encina',
    scientificName: 'Quercus ilex',
    province: 'Madrid',
    municipality: 'Centro',
    publicationState: 'PUBLICADO' as const,
    publicMapVisibility: 'PUBLICO' as const,
    description: 'Descripción',
    latitude: 40.4,
    longitude: -3.7,
    altitude: null,
  }
}

describe('usePublicTreeEnrichment', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(fetchPublicTreeEnrichment).mockResolvedValue({
      speciesEnrichment: {
        speciesId: 12,
        scientificName: 'Quercus ilex',
        synonyms: ['Encina'],
      },
      treeEnrichment: {
        treeId: 7,
        tags: ['monumental'],
        measurements: { heightM: 20 },
      },
    })
  })

  it('carga enriquecimiento público compuesto por treeId', async () => {
    const api = mountHook(7, buildDetail())
    await nextTick()
    await vi.waitFor(() => expect(fetchPublicTreeEnrichment).toHaveBeenCalledWith(7, expect.any(AbortSignal)))

    expect(api.treeEnrichmentDraft.value?.tags).toEqual(['monumental'])
    expect(api.displaySpeciesEnrichment.value?.synonyms).toEqual(['Encina'])
  })

  it('rellena nombres de especie desde detalle SQL si Mongo es parcial', async () => {
    vi.mocked(fetchPublicTreeEnrichment).mockResolvedValue({
      speciesEnrichment: { speciesId: 12 },
      treeEnrichment: null,
    })
    const api = mountHook(7, buildDetail())
    await nextTick()
    await vi.waitFor(() => expect(api.displaySpeciesEnrichment.value?.scientificName).toBe('Quercus ilex'))

    expect(api.displaySpeciesEnrichment.value?.commonName).toBe('Encina')
  })
})
