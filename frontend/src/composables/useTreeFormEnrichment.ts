import { computed, ref, watch, type ComputedRef, type Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { isAbortError, useAbortableRequest } from '@/composables/useAbortableRequest'
import type { TreeEnrichmentDraftState } from '@/composables/enrichmentFormDraft'
import { speciesEnrichmentHasDisplayContent } from '@/composables/enrichmentSummaries'
import { useAiSuggestionErrorMapper } from '@/composables/useAiSuggestionErrorMapper'
import { useEnrichmentErrorMapper } from '@/composables/useEnrichmentErrorMapper'
import { useAuthStore } from '@/stores/auth'
import { requestSpeciesEnrichmentSuggestion } from '@/services/ai/speciesEnrichmentSuggestionService'
import {
  fetchSpeciesEnrichment,
  fetchTreeEnrichment,
  updateSpeciesEnrichment,
  updateTreeEnrichment,
} from '@/services/catalog/enrichmentService'
import type {
  SpeciesEnrichment,
  SpeciesEnrichmentReplaceRequest,
  TreeEnrichmentReplaceRequest,
} from '@/types/enrichment'

function parseSpeciesId(raw: string): number | null {
  const parsed = Number.parseInt(raw.trim(), 10)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

export interface UseTreeFormEnrichmentOptions {
  treeId: ComputedRef<number | null>
  speciesId: Ref<string> | ComputedRef<string>
}

export function useTreeFormEnrichment(options: UseTreeFormEnrichmentOptions) {
  const { t } = useI18n()
  const authStore = useAuthStore()
  const { toMessage } = useEnrichmentErrorMapper()
  const { toMessage: toAiMessage } = useAiSuggestionErrorMapper()
  const { runWithAbort } = useAbortableRequest()
  const { runWithAbort: runAiWithAbort, cancel: cancelAiSuggestion } = useAbortableRequest()

  const isAdmin = computed(() => authStore.hasRole('ADMIN'))
  const canEditSpeciesEnrichment = computed(() => isAdmin.value)

  const speciesPopupOpen = ref(false)
  const speciesEnrichment = ref<SpeciesEnrichment | null>(null)
  const isLoadingSpeciesEnrichment = ref(false)
  const speciesEnrichmentError = ref('')
  const isSavingSpeciesEnrichment = ref(false)
  const isLoadingAiSuggestion = ref(false)
  const aiSuggestionError = ref('')
  const speciesAiSuggestionPayload = ref<SpeciesEnrichmentReplaceRequest | null>(null)

  const canRequestSpeciesAiSuggestion = computed(() => {
    if (!canEditSpeciesEnrichment.value || isLoadingSpeciesEnrichment.value) {
      return false
    }
    const enrichment = speciesEnrichment.value
    if (!enrichment || speciesEnrichmentHasDisplayContent(enrichment)) {
      return false
    }
    return (
      Boolean(enrichment.scientificName?.trim()) && Boolean(enrichment.commonName?.trim())
    )
  })

  const treeEnrichmentDraft = ref<TreeEnrichmentReplaceRequest | null>(null)
  const isLoadingTreeEnrichment = ref(false)
  const treeEnrichmentError = ref('')
  const treeEnrichmentExpanded = ref(false)
  const mongoProjectionWarning = ref('')
  const treeEnrichmentDraftState = ref<TreeEnrichmentDraftState>({ dirty: false, valid: true })

  const hasTreeId = computed(() => options.treeId.value != null)
  const isTreeEnrichmentDirty = computed(() => treeEnrichmentDraftState.value.dirty)

  async function loadSpeciesEnrichment(): Promise<void> {
    const speciesId = parseSpeciesId(
      typeof options.speciesId === 'object' && 'value' in options.speciesId
        ? options.speciesId.value
        : String(options.speciesId),
    )
    if (!speciesId) {
      speciesEnrichment.value = null
      speciesEnrichmentError.value = ''
      return
    }

    isLoadingSpeciesEnrichment.value = true
    speciesEnrichmentError.value = ''
    try {
      speciesEnrichment.value = await runWithAbort((signal) =>
        fetchSpeciesEnrichment(speciesId, signal),
      )
    } catch (error: unknown) {
      if (isAbortError(error)) {
        return
      }
      speciesEnrichmentError.value = toMessage(error)
    } finally {
      isLoadingSpeciesEnrichment.value = false
    }
  }

  async function onSpeciesPopupOpen(): Promise<void> {
    aiSuggestionError.value = ''
    speciesAiSuggestionPayload.value = null
    await loadSpeciesEnrichment()
  }

  function clearSpeciesAiSuggestionState(): void {
    cancelAiSuggestion()
    aiSuggestionError.value = ''
    speciesAiSuggestionPayload.value = null
  }

  async function requestSpeciesAiSuggestion(): Promise<void> {
    const enrichment = speciesEnrichment.value
    const scientificName = enrichment?.scientificName?.trim() ?? ''
    const commonName = enrichment?.commonName?.trim() ?? ''
    if (!canRequestSpeciesAiSuggestion.value || !scientificName || !commonName) {
      return
    }

    isLoadingAiSuggestion.value = true
    aiSuggestionError.value = ''
    speciesAiSuggestionPayload.value = null
    try {
      const response = await runAiWithAbort((signal) =>
        requestSpeciesEnrichmentSuggestion({ scientificName, commonName }, signal),
      )
      speciesAiSuggestionPayload.value = {
        synonyms: response.synonyms,
        distribution: response.distribution,
        ecologicalData: response.ecologicalData,
        references: response.references,
      }
    } catch (error: unknown) {
      if (isAbortError(error)) {
        return
      }
      aiSuggestionError.value = toAiMessage(error)
    } finally {
      isLoadingAiSuggestion.value = false
    }
  }

  async function saveSpeciesEnrichment(
    payload: SpeciesEnrichmentReplaceRequest,
  ): Promise<boolean> {
    const speciesId = parseSpeciesId(
      typeof options.speciesId === 'object' && 'value' in options.speciesId
        ? options.speciesId.value
        : String(options.speciesId),
    )
    if (!speciesId) {
      return false
    }

    isSavingSpeciesEnrichment.value = true
    speciesEnrichmentError.value = ''
    try {
      speciesEnrichment.value = await runWithAbort((signal) =>
        updateSpeciesEnrichment(speciesId, payload, signal),
      )
      return true
    } catch (error: unknown) {
      if (isAbortError(error)) {
        return false
      }
      speciesEnrichmentError.value = toMessage(error)
      return false
    } finally {
      isSavingSpeciesEnrichment.value = false
    }
  }

  async function loadTreeEnrichment(): Promise<void> {
    const id = options.treeId.value
    if (!id) {
      treeEnrichmentDraft.value = null
      return
    }

    isLoadingTreeEnrichment.value = true
    treeEnrichmentError.value = ''
    try {
      const enrichment = await runWithAbort((signal) => fetchTreeEnrichment(id, signal))
      treeEnrichmentDraft.value = {
        measurements: enrichment.measurements,
        healthStatus: enrichment.healthStatus,
        tags: enrichment.tags,
        observations: enrichment.observations,
      }
    } catch (error: unknown) {
      if (isAbortError(error)) {
        return
      }
      treeEnrichmentError.value = toMessage(error)
      treeEnrichmentDraft.value = {
        measurements: {},
        healthStatus: {},
        tags: [],
        observations: [],
      }
    } finally {
      isLoadingTreeEnrichment.value = false
    }
  }

  function onTreeEnrichmentDraftState(state: TreeEnrichmentDraftState): void {
    treeEnrichmentDraftState.value = state
  }

  function validateBeforePersist(): boolean {
    if (!hasTreeId.value) {
      return true
    }
    if (treeEnrichmentDraftState.value.valid) {
      return true
    }
    const errorKey = treeEnrichmentDraftState.value.errorKey ?? 'invalidJson'
    treeEnrichmentError.value = t(`enrichment.validation.${errorKey}`)
    treeEnrichmentExpanded.value = true
    return false
  }

  async function persistTreeEnrichment(): Promise<boolean> {
    const id = options.treeId.value
    if (!id || !treeEnrichmentDraft.value) {
      return true
    }

    treeEnrichmentError.value = ''
    try {
      await runWithAbort((signal) => updateTreeEnrichment(id, treeEnrichmentDraft.value!, signal))
      return true
    } catch (error: unknown) {
      if (isAbortError(error)) {
        return false
      }
      treeEnrichmentError.value = toMessage(error)
      treeEnrichmentExpanded.value = true
      return false
    }
  }

  function setMongoProjectionWarning(message?: string | null): void {
    const trimmed = message?.trim() ?? ''
    mongoProjectionWarning.value = trimmed
    if (trimmed.length > 0) {
      treeEnrichmentExpanded.value = true
    }
  }

  function clearMongoProjectionWarning(): void {
    mongoProjectionWarning.value = ''
  }

  watch(
    () => options.treeId.value,
    (id) => {
      if (id) {
        void loadTreeEnrichment()
      } else {
        treeEnrichmentDraft.value = null
        treeEnrichmentError.value = ''
        treeEnrichmentDraftState.value = { dirty: false, valid: true }
      }
    },
    { immediate: true },
  )

  watch(
    () =>
      typeof options.speciesId === 'object' && 'value' in options.speciesId
        ? options.speciesId.value
        : String(options.speciesId),
    (rawSpeciesId) => {
      const speciesId = parseSpeciesId(rawSpeciesId)
      if (!speciesId) {
        speciesEnrichment.value = null
        speciesEnrichmentError.value = ''
        return
      }
      void loadSpeciesEnrichment()
    },
    { immediate: true },
  )

  watch(speciesPopupOpen, (isOpen) => {
    if (!isOpen) {
      clearSpeciesAiSuggestionState()
    }
  })

  return {
    isAdmin,
    canEditSpeciesEnrichment,
    canRequestSpeciesAiSuggestion,
    speciesPopupOpen,
    speciesEnrichment,
    isLoadingSpeciesEnrichment,
    speciesEnrichmentError,
    isSavingSpeciesEnrichment,
    isLoadingAiSuggestion,
    aiSuggestionError,
    speciesAiSuggestionPayload,
    requestSpeciesAiSuggestion,
    clearSpeciesAiSuggestionState,
    treeEnrichmentDraft,
    isLoadingTreeEnrichment,
    treeEnrichmentError,
    treeEnrichmentExpanded,
    mongoProjectionWarning,
    isTreeEnrichmentDirty,
    hasTreeId,
    onSpeciesPopupOpen,
    onTreeEnrichmentDraftState,
    validateBeforePersist,
    saveSpeciesEnrichment,
    loadTreeEnrichment,
    persistTreeEnrichment,
    setMongoProjectionWarning,
    clearMongoProjectionWarning,
  }
}
