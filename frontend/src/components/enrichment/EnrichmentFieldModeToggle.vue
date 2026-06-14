<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { EnrichmentFieldEditMode } from '@/composables/enrichmentGuidedForms'

const mode = defineModel<EnrichmentFieldEditMode>('mode', { required: true })

const { t } = useI18n()

function toggleMode(): void {
  mode.value = mode.value === 'guided' ? 'json' : 'guided'
}
</script>

<template>
  <button
    type="button"
    class="btn btn-secondary btn-sm mtl-enrichment-mode-toggle"
    data-testid="enrichment-field-mode-toggle"
    @click="toggleMode"
  >
    {{
      mode === 'guided'
        ? t('enrichment.common.advancedJsonMode')
        : t('enrichment.common.guidedMode')
    }}
  </button>
</template>
