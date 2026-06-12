<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import EnrichmentFieldModeToggle from '@/components/enrichment/EnrichmentFieldModeToggle.vue'
import { TREE_HEALTH_STATUS_JSON_PLACEHOLDER } from '@/composables/enrichmentFormDraft'
import {
  createEmptyHealthLesionDraft,
  createEmptyHealthStatusGuidedDraft,
  parseHealthStatusJsonField,
  serializeHealthStatusGuidedDraft,
  type EnrichmentFieldEditMode,
  type HealthStatusGuidedDraft,
} from '@/composables/enrichmentGuidedForms'

const props = withDefaults(
  defineProps<{
    fieldId?: string
    readonly?: boolean
  }>(),
  {
    fieldId: 'tree-enrichment-health',
    readonly: false,
  },
)

const model = defineModel<string>({ default: '' })

const { t } = useI18n()
const mode = ref<EnrichmentFieldEditMode>('guided')
const guidedDraft = ref<HealthStatusGuidedDraft>(createEmptyHealthStatusGuidedDraft())
const jsonText = ref('')
const inlineError = ref('')
let syncingFromModel = false

function syncFromModel(value: string): void {
  syncingFromModel = true
  const parsed = parseHealthStatusJsonField(value)
  if (!parsed.ok) {
    mode.value = 'json'
    jsonText.value = value
    guidedDraft.value = createEmptyHealthStatusGuidedDraft()
    inlineError.value = t('enrichment.validation.invalidJson')
    syncingFromModel = false
    return
  }

  mode.value = props.readonly && !parsed.fitsGuided ? 'json' : parsed.mode
  guidedDraft.value = parsed.value.guided
  jsonText.value = value.trim().length > 0 ? value : serializeHealthStatusGuidedDraft(parsed.value.guided)
  inlineError.value = ''
  syncingFromModel = false
}

function emitFromGuided(): void {
  if (syncingFromModel || props.readonly) {
    return
  }
  const next = serializeHealthStatusGuidedDraft(guidedDraft.value)
  if (next !== model.value) {
    model.value = next
  }
  inlineError.value = ''
}

function emitFromJson(): void {
  if (syncingFromModel || props.readonly) {
    return
  }
  const parsed = parseHealthStatusJsonField(jsonText.value)
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
    const parsed = parseHealthStatusJsonField(jsonText.value)
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
      : serializeHealthStatusGuidedDraft(guidedDraft.value)
})

function addLesion(): void {
  if (props.readonly) {
    return
  }
  guidedDraft.value.lesiones.push(createEmptyHealthLesionDraft())
}

function removeLesion(index: number): void {
  if (props.readonly) {
    return
  }
  guidedDraft.value.lesiones.splice(index, 1)
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
      v-if="showGuided || (!showJsonReadonly && !showJsonEmptyReadonly)"
      class="mtl-enrichment-guided-section"
      :aria-labelledby="`${fieldId}-section-legend`"
    >
      <legend :id="`${fieldId}-section-legend`" class="mtl-enrichment-guided-section__legend">
        <span class="mtl-enrichment-guided-section__legend-label">
          {{ t('enrichment.tree.fields.healthStatus') }}
        </span>
        <EnrichmentFieldModeToggle v-if="!readonly" v-model:mode="mode" />
      </legend>

      <div v-if="showGuided" class="mtl-enrichment-guided-section__fields">
        <div class="mtl-enrichment-guided-section__row">
          <div class="field">
            <label class="form-label" :for="`${fieldId}-assessment`">
              {{ t('enrichment.guided.health.assessment') }}
            </label>
            <input
              :id="`${fieldId}-assessment`"
              v-model="guidedDraft.valoracionGeneral"
              class="form-control"
              type="text"
              :readonly="readonly"
            />
          </div>

          <div class="field">
            <label class="form-label" :for="`${fieldId}-revision`">
              {{ t('enrichment.guided.health.lastRevision') }}
            </label>
            <input
              :id="`${fieldId}-revision`"
              v-model="guidedDraft.ultimaRevision"
              class="form-control"
              type="date"
              :readonly="readonly"
            />
          </div>
        </div>

        <div class="field">
          <label class="form-label" :for="`${fieldId}-pests`">
            {{ t('enrichment.guided.health.pests') }}
          </label>
          <input
            :id="`${fieldId}-pests`"
            v-model="guidedDraft.plagasDetectadasText"
            class="form-control"
            type="text"
            :readonly="readonly"
          />
          <small v-if="!readonly" class="form-text">{{ t('enrichment.guided.listHint') }}</small>
        </div>

        <div class="mtl-enrichment-nested-group">
          <span class="form-label">{{ t('enrichment.guided.health.lesions') }}</span>

          <p v-if="guidedDraft.lesiones.length === 0" class="status-note">
            {{ t('common.emptyValue') }}
          </p>

          <div v-else class="mtl-enrichment-nested-stack">
            <article
              v-for="(lesion, index) in guidedDraft.lesiones"
              :key="index"
              class="mtl-enrichment-nested-card"
              :data-testid="`${fieldId}-lesion-${index}`"
            >
              <header class="mtl-enrichment-nested-card__header">
                <span class="mtl-enrichment-nested-card__badge">
                  {{ t('enrichment.guided.health.lesionTitle', { index: index + 1 }) }}
                </span>
                <button
                  v-if="!readonly"
                  type="button"
                  class="btn btn-secondary btn-sm"
                  @click="removeLesion(index)"
                >
                  {{ t('enrichment.guided.health.removeLesion') }}
                </button>
              </header>
              <div class="mtl-enrichment-nested-card__fields">
                <div class="mtl-enrichment-guided-section__row">
                  <div class="field">
                    <label class="form-label" :for="`${fieldId}-lesion-type-${index}`">
                      {{ t('enrichment.guided.health.lesionType') }}
                    </label>
                    <input
                      :id="`${fieldId}-lesion-type-${index}`"
                      v-model="lesion.tipo"
                      class="form-control"
                      type="text"
                      :readonly="readonly"
                    />
                  </div>
                  <div class="field">
                    <label class="form-label" :for="`${fieldId}-lesion-side-${index}`">
                      {{ t('enrichment.guided.health.lesionSide') }}
                    </label>
                    <input
                      :id="`${fieldId}-lesion-side-${index}`"
                      v-model="lesion.lado"
                      class="form-control"
                      type="text"
                      :readonly="readonly"
                    />
                  </div>
                </div>
                <div class="field">
                  <label class="form-label" :for="`${fieldId}-lesion-desc-${index}`">
                    {{ t('enrichment.guided.health.lesionDescription') }}
                  </label>
                  <textarea
                    :id="`${fieldId}-lesion-desc-${index}`"
                    v-model="lesion.descripcion"
                    class="form-control form-textarea"
                    rows="2"
                    :readonly="readonly"
                  />
                </div>
              </div>
            </article>
          </div>

          <button
            v-if="!readonly"
            type="button"
            class="btn btn-secondary btn-sm mtl-enrichment-nested-group__add"
            :data-testid="`${fieldId}-add-lesion`"
            @click="addLesion"
          >
            {{ t('enrichment.guided.health.addLesion') }}
          </button>
        </div>
      </div>

      <div
        v-else-if="!showJsonReadonly && !showJsonEmptyReadonly"
        class="mtl-enrichment-guided-section__fields mtl-enrichment-json-advanced"
      >
        <textarea
          :id="fieldId"
          v-model="jsonText"
          class="form-control form-textarea mtl-enrichment-json-advanced__textarea"
          rows="8"
          spellcheck="false"
          :placeholder="TREE_HEALTH_STATUS_JSON_PLACEHOLDER"
        />
        <small class="form-text">{{ t('enrichment.common.jsonHint') }}</small>
        <p v-if="inlineError" class="field-error" role="alert">{{ inlineError }}</p>
      </div>
    </fieldset>

    <pre v-if="showJsonReadonly" class="mtl-enrichment-json-preview">{{ jsonText }}</pre>
    <textarea
      v-else-if="showJsonEmptyReadonly"
      :id="fieldId"
      class="form-control form-textarea"
      rows="5"
      :value="t('common.emptyValue')"
      readonly
    />
  </div>
</template>
