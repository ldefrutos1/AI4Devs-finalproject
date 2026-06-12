import { computed, ref, watch, type ComputedRef } from 'vue'
import { isAbortError, useAbortableRequest } from '@/composables/useAbortableRequest'
import { useEnrichmentErrorMapper } from '@/composables/useEnrichmentErrorMapper'
import { fetchPublicTreeEnrichment } from '@/services/catalog/enrichmentService'
import type { PublicTreeDetail } from '@/types/catalog'
import type {
  SpeciesEnrichment,
  TreeEnrichmentReplaceRequest,
} from '@/types/enrichment'

function toTreeEnrichmentDraft(
  treeEnrichment?: {
    measurements?: TreeEnrichmentReplaceRequest['measurements']
    healthStatus?: TreeEnrichmentReplaceRequest['healthStatus']
    tags?: string[]
    observations?: TreeEnrichmentReplaceRequest['observations']
  } | null,
): TreeEnrichmentReplaceRequest | null {
  if (!treeEnrichment) {
    return null
  }
  return {
    measurements: treeEnrichment.measurements,
    healthStatus: treeEnrichment.healthStatus,
    tags: treeEnrichment.tags,
    observations: treeEnrichment.observations,
  }
}

export interface UsePublicTreeEnrichmentOptions {
  treeId: ComputedRef<number | null>
  /** Detalle SQL público; se usa para rellenar nombres si el bloque Mongo es parcial. */
  treeDetail: ComputedRef<PublicTreeDetail | null>
}

export function usePublicTreeEnrichment(options: UsePublicTreeEnrichmentOptions) {
  const { toMessage } = useEnrichmentErrorMapper()
  const { runWithAbort } = useAbortableRequest()

  const speciesEnrichment = ref<SpeciesEnrichment | null>(null)
  const treeEnrichmentDraft = ref<TreeEnrichmentReplaceRequest | null>(null)
  const isLoadingEnrichment = ref(false)
  const enrichmentError = ref('')
  const speciesPopupOpen = ref(false)
  const treeEnrichmentExpanded = ref(false)

  const displaySpeciesEnrichment = computed<SpeciesEnrichment | null>(() => {
    const detail = options.treeDetail.value
    const enrichment = speciesEnrichment.value
    if (!detail && !enrichment) {
      return null
    }
    return {
      speciesId: enrichment?.speciesId ?? 0,
      scientificName: enrichment?.scientificName ?? detail?.scientificName,
      commonName: enrichment?.commonName ?? detail?.commonName,
      synonyms: enrichment?.synonyms,
      distribution: enrichment?.distribution,
      ecologicalData: enrichment?.ecologicalData,
      references: enrichment?.references,
    }
  })

  async function loadPublicEnrichment(): Promise<void> {
    const id = options.treeId.value
    if (!id) {
      speciesEnrichment.value = null
      treeEnrichmentDraft.value = null
      enrichmentError.value = ''
      return
    }

    isLoadingEnrichment.value = true
    enrichmentError.value = ''
    try {
      const response = await runWithAbort((signal) => fetchPublicTreeEnrichment(id, signal))
      speciesEnrichment.value = response.speciesEnrichment ?? null
      treeEnrichmentDraft.value = toTreeEnrichmentDraft(response.treeEnrichment)
    } catch (error: unknown) {
      if (isAbortError(error)) {
        return
      }
      enrichmentError.value = toMessage(error)
      speciesEnrichment.value = null
      treeEnrichmentDraft.value = null
    } finally {
      isLoadingEnrichment.value = false
    }
  }

  watch(
    () => options.treeId.value,
    (id) => {
      if (id) {
        void loadPublicEnrichment()
      } else {
        speciesEnrichment.value = null
        treeEnrichmentDraft.value = null
        enrichmentError.value = ''
      }
    },
    { immediate: true },
  )

  return {
    speciesEnrichment,
    displaySpeciesEnrichment,
    treeEnrichmentDraft,
    isLoadingEnrichment,
    enrichmentError,
    speciesPopupOpen,
    treeEnrichmentExpanded,
    loadPublicEnrichment,
  }
}
