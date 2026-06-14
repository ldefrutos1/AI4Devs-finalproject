<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import EnrichmentFieldModeToggle from '@/components/enrichment/EnrichmentFieldModeToggle.vue'
import { SPECIES_ECOLOGICAL_DATA_JSON_PLACEHOLDER } from '@/composables/enrichmentFormDraft'
import {
  createEmptyEcologicalGuidedDraft,
  parseEcologicalJsonField,
  serializeEcologicalGuidedDraft,
  type EcologicalGuidedDraft,
  type EnrichmentFieldEditMode,
} from '@/composables/enrichmentGuidedForms'

const props = withDefaults(
  defineProps<{
    fieldId?: string
    readonly?: boolean
  }>(),
  {
    fieldId: 'species-enrichment-ecological',
    readonly: false,
  },
)

const model = defineModel<string>({ default: '' })

const { t } = useI18n()
const mode = ref<EnrichmentFieldEditMode>('guided')
const guidedDraft = ref<EcologicalGuidedDraft>(createEmptyEcologicalGuidedDraft())
const jsonText = ref('')
const inlineError = ref('')
let syncingFromModel = false

function syncFromModel(value: string): void {
  syncingFromModel = true
  const parsed = parseEcologicalJsonField(value)
  if (!parsed.ok) {
    mode.value = 'json'
    jsonText.value = value
    guidedDraft.value = createEmptyEcologicalGuidedDraft()
    inlineError.value = t('enrichment.validation.invalidJson')
    syncingFromModel = false
    return
  }

  mode.value = props.readonly && !parsed.fitsGuided ? 'json' : parsed.mode
  guidedDraft.value = parsed.value.guided
  jsonText.value = value.trim().length > 0 ? value : serializeEcologicalGuidedDraft(parsed.value.guided)
  inlineError.value = ''
  syncingFromModel = false
}

function emitFromGuided(): void {
  if (syncingFromModel || props.readonly) {
    return
  }
  const next = serializeEcologicalGuidedDraft(guidedDraft.value)
  if (next !== model.value) {
    model.value = next
  }
  inlineError.value = ''
}

function emitFromJson(): void {
  if (syncingFromModel || props.readonly) {
    return
  }
  const parsed = parseEcologicalJsonField(jsonText.value)
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
    const parsed = parseEcologicalJsonField(jsonText.value)
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
      : serializeEcologicalGuidedDraft(guidedDraft.value)
})

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
          {{ t('enrichment.species.fields.ecologicalData') }}
        </span>
        <EnrichmentFieldModeToggle v-if="!readonly" v-model:mode="mode" />
      </legend>

      <div class="mtl-enrichment-guided-section__fields">
        <div class="field">
          <label class="form-label" :for="`${fieldId}-habitat`">
            {{ t('enrichment.guided.ecological.habitat') }}
          </label>
          <input
            :id="`${fieldId}-habitat`"
            v-model="guidedDraft.habitatText"
            class="form-control"
            type="text"
            :readonly="readonly"
          />
          <small v-if="!readonly" class="form-text">{{ t('enrichment.guided.listHint') }}</small>
        </div>

        <div class="mtl-enrichment-guided-section__row">
          <div class="field">
            <label class="form-label" :for="`${fieldId}-altitude-min`">
              {{ t('enrichment.guided.ecological.altitudeMinM') }}
            </label>
            <input
              :id="`${fieldId}-altitude-min`"
              v-model="guidedDraft.altitudMinM"
              class="form-control"
              type="text"
              inputmode="decimal"
              :readonly="readonly"
            />
          </div>

          <div class="field">
            <label class="form-label" :for="`${fieldId}-altitude-max`">
              {{ t('enrichment.guided.ecological.altitudeMaxM') }}
            </label>
            <input
              :id="`${fieldId}-altitude-max`"
              v-model="guidedDraft.altitudMaxM"
              class="form-control"
              type="text"
              inputmode="decimal"
              :readonly="readonly"
            />
          </div>
        </div>

        <div class="field">
          <label class="form-label" :for="`${fieldId}-climate`">
            {{ t('enrichment.guided.ecological.climate') }}
          </label>
          <input
            :id="`${fieldId}-climate`"
            v-model="guidedDraft.climaText"
            class="form-control"
            type="text"
            :readonly="readonly"
          />
          <small v-if="!readonly" class="form-text">{{ t('enrichment.guided.listHint') }}</small>
        </div>
      </div>
    </fieldset>

    <template v-else>
      <div class="mtl-enrichment-json-field__header">
        <label class="form-label" :for="fieldId">{{ t('enrichment.species.fields.ecologicalData') }}</label>
        <EnrichmentFieldModeToggle v-if="!readonly" v-model:mode="mode" />
      </div>
      <textarea
        :id="fieldId"
        v-model="jsonText"
        class="form-control form-textarea"
        rows="4"
        spellcheck="false"
        :placeholder="SPECIES_ECOLOGICAL_DATA_JSON_PLACEHOLDER"
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
