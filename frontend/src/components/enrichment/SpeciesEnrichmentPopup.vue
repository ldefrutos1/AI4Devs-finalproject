<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, useId, useTemplateRef, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import SpeciesEcologicalFieldEditor from '@/components/enrichment/SpeciesEcologicalFieldEditor.vue'
import SpeciesReferencesFieldEditor from '@/components/enrichment/SpeciesReferencesFieldEditor.vue'
import {
  buildSpeciesEnrichmentReplaceRequest,
  speciesEnrichmentToFormDraft,
  type SpeciesEnrichmentFormDraft,
} from '@/composables/enrichmentFormDraft'
import {
  resolveSpeciesEnrichmentContentStatus,
  speciesEnrichmentHasDisplayContent,
} from '@/composables/enrichmentSummaries'
import type { SpeciesEnrichment, SpeciesEnrichmentReplaceRequest } from '@/types/enrichment'

const props = withDefaults(
  defineProps<{
    /** Deshabilita el icono (p. ej. sin especie seleccionada). */
    triggerDisabled?: boolean
    enrichment?: SpeciesEnrichment | null
    readonly?: boolean
    loading?: boolean
    saving?: boolean
    error?: string
    validationError?: string
  }>(),
  {
    triggerDisabled: false,
    enrichment: null,
    readonly: true,
    loading: false,
    saving: false,
    error: '',
    validationError: '',
  },
)

const emit = defineEmits<{
  open: []
  save: [payload: SpeciesEnrichmentReplaceRequest]
}>()

const open = defineModel<boolean>('open', { default: false })

const { t } = useI18n()
const dialogRef = useTemplateRef<HTMLDialogElement>('dialogRef')
const titleId = useId()
const errorId = useId()
const draft = ref<SpeciesEnrichmentFormDraft>(speciesEnrichmentToFormDraft(null))
const localValidationError = ref('')

const displayError = computed(
  () => props.validationError || localValidationError.value || props.error,
)

const speciesContentStatus = computed(() =>
  resolveSpeciesEnrichmentContentStatus(props.enrichment, {
    loading: props.loading,
    speciesSelected: !props.triggerDisabled,
  }),
)

const speciesContentBadge = computed(() => {
  switch (speciesContentStatus.value) {
    case 'hasData':
      return { label: t('enrichment.species.badgeWithData'), className: 'mtl-badge--info' }
    case 'empty':
      return { label: t('enrichment.species.badgeEmpty'), className: 'mtl-badge--muted' }
    default:
      return null
  }
})

function syncDraftFromProps(): void {
  draft.value = speciesEnrichmentToFormDraft(props.enrichment)
  localValidationError.value = ''
}

function syncDialogToModel(isDialogOpen: boolean): void {
  const el = dialogRef.value
  if (!el) {
    return
  }
  if (isDialogOpen) {
    if (!el.open) {
      try {
        el.showModal()
      } catch {
        el.setAttribute('open', '')
      }
    }
  } else if (el.open) {
    el.close()
  }
}

watch(
  open,
  async (isOpen) => {
    if (isOpen) {
      syncDraftFromProps()
      emit('open')
    }
    await nextTick()
    syncDialogToModel(isOpen)
  },
  { flush: 'post', immediate: true },
)

watch(
  () => props.enrichment,
  () => {
    if (open.value) {
      syncDraftFromProps()
    }
  },
)

function onTriggerClick(): void {
  if (props.triggerDisabled) {
    return
  }
  open.value = true
}

function onCloseClick(): void {
  open.value = false
}

function onDialogCancel(ev: Event): void {
  ev.preventDefault()
  open.value = false
}

function onSaveClick(): void {
  const result = buildSpeciesEnrichmentReplaceRequest(draft.value)
  if (!result.ok) {
    localValidationError.value = t(`enrichment.validation.${result.errorKey}`)
    return
  }
  localValidationError.value = ''
  emit('save', result.payload)
}

function displayReadonlyText(value: string | undefined | null): string {
  const trimmed = value?.trim() ?? ''
  return trimmed.length > 0 ? trimmed : t('common.emptyValue')
}

onBeforeUnmount(() => {
  const el = dialogRef.value
  if (el?.open) {
    el.close()
  }
})
</script>

<template>
  <button
    type="button"
    class="mtl-enrichment-species-trigger"
    data-testid="species-enrichment-trigger"
    :disabled="triggerDisabled"
    :aria-label="t('enrichment.species.triggerLabel')"
    :title="
      triggerDisabled
        ? t('enrichment.species.triggerDisabledHint')
        : t('enrichment.species.triggerLabel')
    "
    @click="onTriggerClick"
  >
    <svg
      class="mtl-enrichment-species-trigger__icon"
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5" />
      <path
        d="M12 10v5M12 7.5h.01"
        stroke="currentColor"
        stroke-width="1.75"
        stroke-linecap="round"
      />
    </svg>
    <span
      v-if="speciesContentBadge"
      class="mtl-badge mtl-enrichment-species-trigger__badge"
      :class="speciesContentBadge.className"
      data-testid="species-enrichment-content-badge"
    >
      {{ speciesContentBadge.label }}
    </span>
  </button>

  <dialog
    ref="dialogRef"
    class="mtl-form-dialog mtl-form-dialog--species"
    aria-modal="true"
    :aria-labelledby="titleId"
    :aria-describedby="displayError ? errorId : undefined"
    data-testid="species-enrichment-dialog"
    @cancel="onDialogCancel"
  >
    <div class="mtl-form-dialog-panel mtl-enrichment-dialog-panel" @click.stop>
      <div class="mtl-enrichment-dialog-scroll">
        <h3 :id="titleId" class="mtl-form-dialog-title">{{ t('enrichment.species.dialogTitle') }}</h3>
        <p v-if="readonly" class="mtl-enrichment-dialog-intro">
          {{ t('enrichment.species.readOnlyNotice') }}
        </p>

        <p v-if="loading" class="status-note">{{ t('enrichment.species.loading') }}</p>
        <p v-else-if="displayError" :id="errorId" class="error" role="alert">{{ displayError }}</p>

        <div v-if="!loading" class="tree-form mtl-enrichment-species-form">
        <div class="mtl-enrichment-dialog-meta">
          <div class="field">
            <label class="form-label" for="species-enrichment-scientific-name">
              {{ t('enrichment.species.fields.scientificName') }}
            </label>
            <input
              id="species-enrichment-scientific-name"
              class="form-control"
              type="text"
              :value="displayReadonlyText(enrichment?.scientificName)"
              readonly
            />
          </div>
          <div class="field">
            <label class="form-label" for="species-enrichment-common-name">
              {{ t('enrichment.species.fields.commonName') }}
            </label>
            <input
              id="species-enrichment-common-name"
              class="form-control"
              type="text"
              :value="displayReadonlyText(enrichment?.commonName)"
              readonly
            />
          </div>
        </div>

        <p
          v-if="!speciesEnrichmentHasDisplayContent(enrichment) && readonly && !displayError"
          class="status-note field-full"
          data-testid="species-enrichment-empty"
        >
          {{ t('enrichment.species.empty') }}
        </p>

        <div class="field field-full">
          <label class="form-label" for="species-enrichment-synonyms">
            {{ t('enrichment.species.fields.synonyms') }}
          </label>
          <textarea
            v-if="!readonly"
            id="species-enrichment-synonyms"
            v-model="draft.synonymsText"
            class="form-control form-textarea"
            rows="2"
            :aria-describedby="'species-enrichment-synonyms-hint'"
          />
          <textarea
            v-else
            id="species-enrichment-synonyms"
            class="form-control form-textarea"
            rows="2"
            :value="displayReadonlyText(draft.synonymsText)"
            readonly
          />
          <small v-if="!readonly" id="species-enrichment-synonyms-hint" class="form-text">
            {{ t('enrichment.species.fields.synonymsHint') }}
          </small>
        </div>

        <div class="field">
          <label class="form-label" for="species-enrichment-continents">
            {{ t('enrichment.species.fields.distributionContinents') }}
          </label>
          <input
            v-if="!readonly"
            id="species-enrichment-continents"
            v-model="draft.distributionContinents"
            class="form-control"
            type="text"
          />
          <input
            v-else
            id="species-enrichment-continents"
            class="form-control"
            type="text"
            :value="displayReadonlyText(draft.distributionContinents)"
            readonly
          />
        </div>

        <div class="field">
          <label class="form-label" for="species-enrichment-countries">
            {{ t('enrichment.species.fields.distributionCountries') }}
          </label>
          <input
            v-if="!readonly"
            id="species-enrichment-countries"
            v-model="draft.distributionCountries"
            class="form-control"
            type="text"
          />
          <input
            v-else
            id="species-enrichment-countries"
            class="form-control"
            type="text"
            :value="displayReadonlyText(draft.distributionCountries)"
            readonly
          />
        </div>

        <div class="field field-full">
          <label class="form-label" for="species-enrichment-distribution-desc">
            {{ t('enrichment.species.fields.distributionDescription') }}
          </label>
          <textarea
            v-if="!readonly"
            id="species-enrichment-distribution-desc"
            v-model="draft.distributionDescription"
            class="form-control form-textarea"
            rows="2"
          />
          <textarea
            v-else
            id="species-enrichment-distribution-desc"
            class="form-control form-textarea"
            rows="2"
            :value="displayReadonlyText(draft.distributionDescription)"
            readonly
          />
        </div>

        <SpeciesEcologicalFieldEditor
          v-model="draft.ecologicalDataJson"
          :readonly="readonly"
        />

        <SpeciesReferencesFieldEditor
          v-model="draft.referencesJson"
          :readonly="readonly"
        />
        </div>
      </div>

      <div v-if="!loading" class="mtl-form-dialog-actions mtl-enrichment-dialog-actions">
        <button type="button" class="btn btn-secondary" @click="onCloseClick">
          {{ t('enrichment.species.close') }}
        </button>
        <button
          v-if="!readonly"
          type="button"
          class="btn btn-primary tree-form-submit"
          data-testid="species-enrichment-save"
          :disabled="saving || loading"
          @click="onSaveClick"
        >
          {{ saving ? t('enrichment.species.saving') : t('enrichment.species.save') }}
        </button>
      </div>
    </div>
  </dialog>
</template>
