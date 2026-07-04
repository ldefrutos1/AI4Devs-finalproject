<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useSpeciesAutocomplete } from '@/composables/useSpeciesAutocomplete'
import type { SpeciesResolveMode, SpeciesResolveResult } from '@/composables/speciesAutocomplete'
import type { MasterListItem } from '@/types/catalog'

const props = withDefaults(
  defineProps<{
    inputId: string
    modelValue: string
    species: MasterListItem[]
    placeholder?: string
    inputClass?: string
    required?: boolean
    ariaInvalid?: boolean
    inputTestId?: string
  }>(),
  {
    inputClass: 'form-control',
    required: false,
    ariaInvalid: false,
    inputTestId: undefined,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
  filterTextInput: []
}>()

const speciesRef = computed(() => props.species)
const speciesId = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value),
})

const {
  speciesAutocompleteText,
  showSpeciesSuggestions,
  speciesHighlightIndex,
  filteredSpecies,
  onSpeciesInput,
  onSpeciesFocus,
  onSpeciesBlur,
  selectSpecies,
  highlightNextSpecies,
  highlightPreviousSpecies,
  confirmHighlightedSpecies,
  dismissSpeciesSuggestions,
  syncTextFromSpeciesId,
  commitSpeciesFromText,
} = useSpeciesAutocomplete(speciesRef, speciesId)

defineExpose({
  commitSpeciesFromText(mode?: SpeciesResolveMode): SpeciesResolveResult['kind'] {
    return commitSpeciesFromText(mode)
  },
})

watch(
  () => props.modelValue,
  () => {
    syncTextFromSpeciesId()
  },
)

watch(
  () => props.species,
  () => {
    syncTextFromSpeciesId()
  },
)

onMounted(() => {
  syncTextFromSpeciesId()
})

function onSpeciesInputWithEmit(event: Event): void {
  onSpeciesInput(event)
  emit('filterTextInput')
}
</script>

<template>
  <div class="species-autocomplete">
    <input
      :id="inputId"
      :data-testid="inputTestId"
      :value="speciesAutocompleteText"
      :class="inputClass"
      type="text"
      :required="required"
      :placeholder="placeholder"
      :aria-invalid="ariaInvalid || undefined"
      autocomplete="off"
      @input="onSpeciesInputWithEmit"
      @keydown.down.prevent="highlightNextSpecies"
      @keydown.up.prevent="highlightPreviousSpecies"
      @keydown.page-down.prevent="highlightNextSpecies"
      @keydown.page-up.prevent="highlightPreviousSpecies"
      @keydown.enter.prevent="confirmHighlightedSpecies"
      @keydown.esc.prevent="dismissSpeciesSuggestions"
      @focus="onSpeciesFocus"
      @blur="onSpeciesBlur"
    />
    <ul
      v-if="showSpeciesSuggestions && filteredSpecies.length > 0"
      class="species-autocomplete-list"
      role="listbox"
      :aria-labelledby="inputId"
    >
      <li
        v-for="(item, index) in filteredSpecies"
        :key="item.id"
        class="species-autocomplete-item"
        :class="{ 'species-autocomplete-item-active': speciesHighlightIndex === index }"
        role="option"
        @mousedown.prevent="selectSpecies(item)"
      >
        {{ item.label }}
      </li>
    </ul>
  </div>
</template>

<style scoped>
.species-autocomplete {
  position: relative;
  width: 100%;
}

.species-autocomplete > .form-control {
  width: 100%;
}

.species-autocomplete-list {
  position: absolute;
  z-index: 20;
  top: calc(100% + 2px);
  left: 0;
  right: 0;
  max-height: 12rem;
  margin: 0;
  padding: 0;
  list-style: none;
  overflow-y: auto;
  border: 1px solid var(--border-color, #ced4da);
  border-radius: 0.375rem;
  background: var(--surface-bg, #fff);
  box-shadow: 0 0.25rem 0.5rem rgba(0, 0, 0, 0.08);
}

.species-autocomplete-item {
  padding: 0.5rem 0.75rem;
  cursor: pointer;
}

.species-autocomplete-item:hover {
  background: var(--muted-bg, #f8f9fa);
}

.species-autocomplete-item-active {
  background: var(--muted-bg, #f8f9fa);
}
</style>
