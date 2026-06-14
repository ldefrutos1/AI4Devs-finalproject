<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import HealthStatusFieldEditor from '@/components/enrichment/HealthStatusFieldEditor.vue'
import {
  buildTreeEnrichmentReplaceRequest,
  cloneTreeEnrichmentPayload,
  createEmptyObservation,
  resolveTreeEnrichmentDraftState,
  TREE_MEASUREMENT_FIELD_KEYS,
  treeEnrichmentToFormDraft,
  type TreeEnrichmentFormDraft,
  type TreeEnrichmentDraftState,
} from '@/composables/enrichmentFormDraft'
import type { TreeEnrichmentReplaceRequest } from '@/types/enrichment'
import {
  buildTreeEnrichmentSummaryParts,
  treeEnrichmentHasDisplayContent,
} from '@/composables/enrichmentSummaries'
const props = withDefaults(
  defineProps<{
    modelValue?: TreeEnrichmentReplaceRequest | null
    readonly?: boolean
    loading?: boolean
    error?: string
    validationError?: string
  }>(),
  {
    modelValue: null,
    readonly: false,
    loading: false,
    error: '',
    validationError: '',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: TreeEnrichmentReplaceRequest]
  'draft-state': [state: TreeEnrichmentDraftState]
}>()

const expanded = defineModel<boolean>('expanded', { default: false })

const { t } = useI18n()
const draft = ref<TreeEnrichmentFormDraft>(treeEnrichmentToFormDraft(null))
const localValidationError = ref('')

const displayError = computed(
  () => props.validationError || localValidationError.value || props.error,
)

const measurementFieldLabels: Record<(typeof TREE_MEASUREMENT_FIELD_KEYS)[number], string> = {
  heightM: 'enrichment.tree.fields.heightM',
  trunkDiameterCm: 'enrichment.tree.fields.trunkDiameterCm',
  crownDiameterM: 'enrichment.tree.fields.crownDiameterM',
  trunkPerimeterCm: 'enrichment.tree.fields.trunkPerimeterCm',
}

let syncingFromProps = false
let skippingBaselineRefresh = false

const baselinePayload = ref<TreeEnrichmentReplaceRequest>(
  cloneTreeEnrichmentPayload(props.modelValue),
)

function captureBaseline(): void {
  baselinePayload.value = cloneTreeEnrichmentPayload(props.modelValue)
}

function syncDraftFromModel(refreshBaseline: boolean): void {
  syncingFromProps = true
  draft.value = treeEnrichmentToFormDraft(props.modelValue)
  if (refreshBaseline) {
    captureBaseline()
  }
  localValidationError.value = ''
  syncingFromProps = false
  publishDraftState()
}

function publishDraftState(): void {
  if (props.readonly) {
    emit('draft-state', { dirty: false, valid: true })
    return
  }
  emit('draft-state', resolveTreeEnrichmentDraftState(draft.value, baselinePayload.value))
}

function emitDraftUpdate(): void {
  if (props.readonly || syncingFromProps) {
    return
  }
  const state = resolveTreeEnrichmentDraftState(draft.value, baselinePayload.value)
  publishDraftState()
  const result = buildTreeEnrichmentReplaceRequest(draft.value)
  if (!result.ok) {
    localValidationError.value = state.dirty
      ? t(`enrichment.validation.${result.errorKey}`)
      : ''
    return
  }
  localValidationError.value = ''
  if (!state.dirty) {
    return
  }
  skippingBaselineRefresh = true
  emit('update:modelValue', result.payload)
}

const isDraftDirty = computed(
  () => resolveTreeEnrichmentDraftState(draft.value, baselinePayload.value).dirty,
)

watch(
  () => props.modelValue,
  () => {
    if (skippingBaselineRefresh) {
      skippingBaselineRefresh = false
      publishDraftState()
      return
    }
    syncDraftFromModel(true)
  },
  { deep: true, immediate: true },
)

watch(
  draft,
  () => {
    emitDraftUpdate()
  },
  { deep: true },
)

function addObservation(): void {
  if (props.readonly) {
    return
  }
  draft.value.observations.push(createEmptyObservation())
}

function removeObservation(index: number): void {
  if (props.readonly) {
    return
  }
  draft.value.observations.splice(index, 1)
}

const hasContent = computed(() => treeEnrichmentHasDisplayContent(props.modelValue))

const summaryParts = computed(() => buildTreeEnrichmentSummaryParts(props.modelValue))

const collapsedSummaryText = computed(() => {
  if (props.loading) {
    return t('enrichment.tree.summaryLoading')
  }
  if (!hasContent.value) {
    return t('enrichment.tree.summaryEmpty')
  }
  return summaryParts.value
    .map((part) =>
      t(`enrichment.tree.summaryParts.${part.key}`, { count: part.count }),
    )
    .join(' · ')
})

const showReadonlyEmptyOnly = computed(
  () => props.readonly && !props.loading && !hasContent.value && !displayError.value,
)

const showFormFields = computed(
  () => !props.readonly || (hasContent.value && !props.loading),
)

function onDetailsToggle(event: Event): void {
  const target = event.target as HTMLDetailsElement
  expanded.value = target.open
}

</script>

<template>
  <details
    :open="expanded"
    class="mtl-enrichment-panel"
    data-testid="tree-enrichment-panel"
    @toggle="onDetailsToggle"
  >
    <summary class="mtl-enrichment-panel__summary">
      <span class="mtl-enrichment-panel__summary-text">
        <span class="mtl-enrichment-panel__summary-label">{{ t('enrichment.tree.sectionTitle') }}</span>
        <span class="mtl-enrichment-panel__summary-meta" data-testid="tree-enrichment-summary">
          {{ collapsedSummaryText }}
        </span>
      </span>
      <span
        v-if="isDraftDirty && !readonly"
        class="mtl-badge mtl-badge--info mtl-enrichment-panel__dirty-badge"
      >
        {{ t('enrichment.tree.dirtyBadge') }}
      </span>
    </summary>

    <div class="mtl-enrichment-panel__body">
      <p v-if="!readonly" class="mtl-enrichment-panel__save-hint">
        {{ t('enrichment.tree.saveHint') }}
      </p>
      <p v-if="readonly && showFormFields" class="mtl-enrichment-panel__hint">
        {{ t('enrichment.tree.readOnlyNotice') }}
      </p>

      <p v-if="loading" class="status-note">{{ t('enrichment.tree.loading') }}</p>
      <p v-if="displayError" class="error" role="alert">{{ displayError }}</p>

      <div v-if="!loading" class="tree-form mtl-enrichment-tree-form">
        <p
          v-if="showReadonlyEmptyOnly"
          class="status-note field-full"
          data-testid="tree-enrichment-empty"
        >
          {{ t('enrichment.tree.empty') }}
        </p>

        <template v-else-if="showFormFields">
        <div
          class="field field-full mtl-enrichment-measurements-group"
          role="group"
          aria-labelledby="tree-enrichment-measurements-legend"
        >
          <span id="tree-enrichment-measurements-legend" class="form-label">
            {{ t('enrichment.tree.fields.measurements') }}
          </span>
          <div class="mtl-enrichment-measurements-grid">
            <div v-for="key in TREE_MEASUREMENT_FIELD_KEYS" :key="key" class="field">
              <label class="form-label" :for="`tree-measurement-${key}`">
                {{ t(measurementFieldLabels[key]) }}
              </label>
              <input
                :id="`tree-measurement-${key}`"
                v-model="draft.measurementInputs[key]"
                class="form-control"
                type="text"
                inputmode="decimal"
                :readonly="readonly"
              />
            </div>
          </div>
        </div>

        <div class="field field-full">
          <label class="form-label" for="tree-enrichment-tags">
            {{ t('enrichment.tree.fields.tags') }}
          </label>
          <input
            id="tree-enrichment-tags"
            v-model="draft.tagsText"
            class="form-control"
            type="text"
            :readonly="readonly"
          />
          <small v-if="!readonly" class="form-text">
            {{ t('enrichment.tree.fields.tagsHint') }}
          </small>
        </div>

        <HealthStatusFieldEditor
          v-model="draft.healthStatusJson"
          field-id="tree-enrichment-health"
          :readonly="readonly"
        />

        <fieldset
          class="mtl-enrichment-guided-section field-full mtl-enrichment-observations-group"
          aria-labelledby="tree-enrichment-observations-legend"
        >
          <legend id="tree-enrichment-observations-legend" class="mtl-enrichment-guided-section__legend">
            <span class="mtl-enrichment-guided-section__legend-label">
              {{ t('enrichment.tree.fields.observations') }}
            </span>
          </legend>

          <div
            v-if="draft.observations.length === 0"
            class="mtl-enrichment-nested-group__empty"
            data-testid="tree-enrichment-observations-empty"
          >
            <p class="status-note">{{ t('enrichment.tree.fields.observationsEmpty') }}</p>
            <button
              v-if="!readonly"
              type="button"
              class="btn btn-secondary btn-sm"
              data-testid="tree-enrichment-add-observation"
              @click="addObservation"
            >
              {{ t('enrichment.tree.fields.addFirstObservation') }}
            </button>
          </div>

          <div v-else class="mtl-enrichment-nested-stack">
            <article
              v-for="(observation, index) in draft.observations"
              :key="index"
              class="mtl-enrichment-nested-card"
              :data-testid="`tree-enrichment-observation-${index}`"
            >
              <header class="mtl-enrichment-nested-card__header">
                <span class="mtl-enrichment-nested-card__badge">
                  {{ t('enrichment.tree.fields.observationLabel', { index: index + 1 }) }}
                </span>
                <button
                  v-if="!readonly"
                  type="button"
                  class="btn btn-secondary btn-sm"
                  @click="removeObservation(index)"
                >
                  {{ t('enrichment.tree.fields.removeObservation') }}
                </button>
              </header>
              <div class="mtl-enrichment-nested-card__fields">
                <div class="mtl-enrichment-guided-section__row">
                  <div class="field">
                    <label class="form-label" :for="`tree-observation-date-${index}`">
                      {{ t('enrichment.tree.fields.observationDate') }}
                    </label>
                    <input
                      :id="`tree-observation-date-${index}`"
                      v-model="observation.date"
                      class="form-control"
                      type="date"
                      :readonly="readonly"
                    />
                  </div>
                  <div class="field">
                    <label class="form-label" :for="`tree-observation-author-${index}`">
                      {{ t('enrichment.tree.fields.observationAuthor') }}
                    </label>
                    <input
                      :id="`tree-observation-author-${index}`"
                      v-model="observation.author"
                      class="form-control"
                      type="text"
                      :readonly="readonly"
                    />
                  </div>
                </div>
                <div class="field">
                  <label class="form-label" :for="`tree-observation-text-${index}`">
                    {{ t('enrichment.tree.fields.observationText') }}
                  </label>
                  <textarea
                    :id="`tree-observation-text-${index}`"
                    v-model="observation.text"
                    class="form-control form-textarea"
                    rows="3"
                    :readonly="readonly"
                  />
                </div>
              </div>
            </article>

            <button
              v-if="!readonly"
              type="button"
              class="btn btn-secondary btn-sm mtl-enrichment-nested-group__add"
              data-testid="tree-enrichment-add-observation"
              @click="addObservation"
            >
              {{ t('enrichment.tree.fields.addAnotherObservation') }}
            </button>
          </div>
        </fieldset>
        </template>
      </div>
    </div>
  </details>
</template>
