<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import EnrichmentFieldModeToggle from '@/components/enrichment/EnrichmentFieldModeToggle.vue'
import { SPECIES_REFERENCES_JSON_PLACEHOLDER } from '@/composables/enrichmentFormDraft'
import {
  createEmptyReferenceGuidedDraft,
  parseReferencesJsonField,
  serializeReferencesGuidedDraft,
  type EnrichmentFieldEditMode,
  type ReferenceGuidedDraft,
} from '@/composables/enrichmentGuidedForms'

const props = withDefaults(
  defineProps<{
    fieldId?: string
    readonly?: boolean
  }>(),
  {
    fieldId: 'species-enrichment-references',
    readonly: false,
  },
)

const model = defineModel<string>({ default: '' })

const { t } = useI18n()
const mode = ref<EnrichmentFieldEditMode>('guided')
const guidedDraft = ref<ReferenceGuidedDraft[]>([])
const jsonText = ref('')
const inlineError = ref('')
let syncingFromModel = false

function syncFromModel(value: string): void {
  syncingFromModel = true
  const parsed = parseReferencesJsonField(value)
  if (!parsed.ok) {
    mode.value = 'json'
    jsonText.value = value
    guidedDraft.value = []
    inlineError.value = t('enrichment.validation.invalidJson')
    syncingFromModel = false
    return
  }

  mode.value = props.readonly && !parsed.fitsGuided ? 'json' : parsed.mode
  guidedDraft.value = parsed.value.guided
  jsonText.value = value.trim().length > 0 ? value : serializeReferencesGuidedDraft(parsed.value.guided)
  inlineError.value = ''
  syncingFromModel = false
}

function emitFromGuided(): void {
  if (syncingFromModel || props.readonly) {
    return
  }
  const next = serializeReferencesGuidedDraft(guidedDraft.value)
  if (next !== model.value) {
    model.value = next
  }
  inlineError.value = ''
}

function emitFromJson(): void {
  if (syncingFromModel || props.readonly) {
    return
  }
  const parsed = parseReferencesJsonField(jsonText.value)
  if (!parsed.ok) {
    inlineError.value = t('enrichment.validation.invalidJson')
    if (jsonText.value !== model.value) {
      model.value = jsonText.value
    }
    return
  }
  inlineError.value = ''
  if (jsonText.value !== model.value) {
    model.value = jsonText.value
  }
}

watch(
  () => model.value,
  (value) => {
    syncFromModel(value ?? '')
  },
  { immediate: true },
)

watch(
  guidedDraft,
  () => {
    if (mode.value === 'guided') {
      emitFromGuided()
    }
  },
  { deep: true },
)

watch(jsonText, () => {
  if (mode.value === 'json') {
    emitFromJson()
  }
})

watch(mode, (next, previous) => {
  if (syncingFromModel || props.readonly || next === previous) {
    return
  }
  if (next === 'guided') {
    const parsed = parseReferencesJsonField(jsonText.value)
    if (parsed.ok) {
      guidedDraft.value = parsed.value.guided
      inlineError.value = ''
      emitFromGuided()
    }
    return
  }
  jsonText.value =
    model.value.trim().length > 0
      ? model.value
      : serializeReferencesGuidedDraft(guidedDraft.value)
})

function addReference(): void {
  if (props.readonly) {
    return
  }
  guidedDraft.value.push(createEmptyReferenceGuidedDraft())
}

function removeReference(index: number): void {
  if (props.readonly) {
    return
  }
  guidedDraft.value.splice(index, 1)
}

const showGuided = computed(() => mode.value === 'guided')
const showJsonReadonly = computed(
  () => props.readonly && mode.value === 'json' && jsonText.value.trim().length > 0,
)
const showJsonEmptyReadonly = computed(
  () => props.readonly && mode.value === 'json' && jsonText.value.trim().length === 0,
)
</script>

<template>
  <div class="mtl-enrichment-json-field field field-full">
    <fieldset
      v-if="showGuided"
      class="mtl-enrichment-guided-section"
      :aria-labelledby="`${fieldId}-section-legend`"
    >
      <legend :id="`${fieldId}-section-legend`" class="mtl-enrichment-guided-section__legend">
        <span class="mtl-enrichment-guided-section__legend-label">
          {{ t('enrichment.species.fields.references') }}
        </span>
        <EnrichmentFieldModeToggle v-if="!readonly" v-model:mode="mode" />
      </legend>

      <div class="mtl-enrichment-references-group">
      <div
        v-if="guidedDraft.length === 0"
        class="mtl-enrichment-references-group__empty"
        data-testid="species-enrichment-references-empty"
      >
        <p class="status-note">{{ t('enrichment.guided.references.empty') }}</p>
        <button
          v-if="!readonly"
          type="button"
          class="btn btn-secondary btn-sm"
          :data-testid="`${fieldId}-add-reference`"
          @click="addReference"
        >
          {{ t('enrichment.guided.references.addFirstReference') }}
        </button>
      </div>

      <div v-else class="mtl-enrichment-references-stack">
        <article
          v-for="(reference, index) in guidedDraft"
          :key="index"
          class="mtl-enrichment-reference-card"
          :data-testid="`${fieldId}-reference-${index}`"
        >
          <header class="mtl-enrichment-reference-card__header">
            <span class="mtl-enrichment-reference-card__badge">
              {{ t('enrichment.guided.references.referenceLabel', { index: index + 1 }) }}
            </span>
            <button
              v-if="!readonly"
              type="button"
              class="btn btn-secondary btn-sm"
              @click="removeReference(index)"
            >
              {{ t('enrichment.guided.references.removeReference') }}
            </button>
          </header>

          <div class="mtl-enrichment-reference-card__fields">
            <div class="field">
              <label class="form-label" :for="`${fieldId}-title-${index}`">
                {{ t('enrichment.guided.references.title') }}
              </label>
              <input
                :id="`${fieldId}-title-${index}`"
                v-model="reference.title"
                class="form-control"
                type="text"
                :readonly="readonly"
              />
            </div>

            <div class="field">
              <label class="form-label" :for="`${fieldId}-authors-${index}`">
                {{ t('enrichment.guided.references.authors') }}
              </label>
              <input
                :id="`${fieldId}-authors-${index}`"
                v-model="reference.authorsText"
                class="form-control"
                type="text"
                :readonly="readonly"
              />
              <small v-if="!readonly" class="form-text">{{ t('enrichment.guided.listHint') }}</small>
            </div>

            <div class="mtl-enrichment-reference-card__row">
              <div class="field">
                <label class="form-label" :for="`${fieldId}-source-${index}`">
                  {{ t('enrichment.guided.references.source') }}
                </label>
                <input
                  :id="`${fieldId}-source-${index}`"
                  v-model="reference.source"
                  class="form-control"
                  type="text"
                  :readonly="readonly"
                />
              </div>
              <div class="field">
                <label class="form-label" :for="`${fieldId}-year-${index}`">
                  {{ t('enrichment.guided.references.year') }}
                </label>
                <input
                  :id="`${fieldId}-year-${index}`"
                  v-model="reference.year"
                  class="form-control"
                  type="text"
                  inputmode="numeric"
                  :readonly="readonly"
                />
              </div>
            </div>

            <div class="field">
              <label class="form-label" :for="`${fieldId}-url-${index}`">
                {{ t('enrichment.guided.references.url') }}
              </label>
              <input
                :id="`${fieldId}-url-${index}`"
                v-model="reference.url"
                class="form-control"
                type="url"
                :readonly="readonly"
              />
            </div>
          </div>
        </article>

        <button
          v-if="!readonly"
          type="button"
          class="btn btn-secondary btn-sm mtl-enrichment-references-group__add-more"
          :data-testid="`${fieldId}-add-reference`"
          @click="addReference"
        >
          {{ t('enrichment.guided.references.addAnotherReference') }}
        </button>
      </div>
      </div>
    </fieldset>

    <template v-else>
      <div class="mtl-enrichment-json-field__header">
        <label class="form-label" :for="fieldId">{{ t('enrichment.species.fields.references') }}</label>
        <EnrichmentFieldModeToggle v-if="!readonly" v-model:mode="mode" />
      </div>
      <textarea
        :id="fieldId"
        v-model="jsonText"
        class="form-control form-textarea"
        rows="4"
        spellcheck="false"
        :placeholder="SPECIES_REFERENCES_JSON_PLACEHOLDER"
      />
      <small class="form-text">{{ t('enrichment.common.jsonHint') }}</small>
      <p v-if="inlineError" class="field-error" role="alert">{{ inlineError }}</p>
    </template>

    <pre v-if="showJsonReadonly" class="mtl-enrichment-json-preview">{{ jsonText }}</pre>
    <textarea
      v-else-if="showJsonEmptyReadonly"
      :id="fieldId"
      class="form-control form-textarea"
      rows="4"
      :value="t('common.emptyValue')"
      readonly
    />
  </div>
</template>
