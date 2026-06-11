<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink } from 'vue-router'
import PageBackLink from '@/components/layout/PageBackLink.vue'
import SpeciesAutocompleteInput from '@/components/SpeciesAutocompleteInput.vue'
import TreePhotoUploadPicker from '@/components/TreePhotoUploadPicker.vue'
import TreeLocationMapPreview from '@/components/TreeLocationMapPreview.vue'
import { areLatLngInValidRange } from '@/composables/createTreeFormValidation'
import { useTreeLocationAutofill } from '@/composables/useTreeLocationAutofill'
import { useCreateTreeForm } from '@/composables/useCreateTreeForm'

const { t } = useI18n()
const {
  form,
  species,
  provinces,
  publicationStateOptions,
  mapVisibilityOptions,
  isLoadingMasters,
  mastersError,
  hasMasters,
  isSubmitting,
  fieldErrors,
  submitError,
  selectedPhotoFiles,
  loadMasters,
  submit,
} = useCreateTreeForm()

const showMapMarker = computed(() => areLatLngInValidRange(form))
const speciesAutocompleteRef = ref<InstanceType<typeof SpeciesAutocompleteInput> | null>(null)

interface CoordinatesPayload {
  latitude: string
  longitude: string
}

const { applyCoordinatesAndAutofillAddress } = useTreeLocationAutofill({
  form,
  provinces,
})

function onMapPickCoordinates(payload: CoordinatesPayload): void {
  void applyCoordinatesAndAutofillAddress(payload)
}

function onFirstPhotoGps(payload: CoordinatesPayload): void {
  void applyCoordinatesAndAutofillAddress(payload)
}

async function onSubmit(): Promise<void> {
  speciesAutocompleteRef.value?.commitSpeciesFromText()
  await submit()
}

onMounted(async () => {
  await loadMasters()
})
</script>

<template>
  <div class="tree-form-page">
    <header class="page-header tree-form-page__header">
      <PageBackLink :to="{ name: 'home' }">{{ t('navigation.home') }}</PageBackLink>
      <h1 class="page-header__title">{{ t('treeForm.title') }}</h1>
    </header>

    <p v-if="isLoadingMasters" class="status-note">{{ t('treeForm.loadingMasters') }}</p>
    <p v-if="mastersError" class="error" role="alert">{{ mastersError }}</p>

    <form
      v-if="!isLoadingMasters && hasMasters"
      class="tree-form"
      data-testid="tree-form"
      @submit.prevent="onSubmit"
    >
      <section
        class="tree-form-section"
        aria-labelledby="tree-form-species-heading"
      >
        <h2 id="tree-form-species-heading" class="tree-form-section__title">
          {{ t('treeForm.sections.speciesAndVisibility') }}
        </h2>
        <div class="tree-form-species-status-row">
          <div class="field species-field">
            <label class="form-label" for="speciesId">{{ t('treeForm.fields.species.label') }}</label>
            <SpeciesAutocompleteInput
              ref="speciesAutocompleteRef"
              input-id="speciesId"
              input-test-id="tree-form-species"
              v-model="form.speciesId"
              :species="species"
              required
              :aria-invalid="Boolean(fieldErrors.speciesId)"
              :placeholder="t('treeForm.fields.species.placeholder')"
            />
            <small v-if="fieldErrors.speciesId" class="field-error">{{
              fieldErrors.speciesId
            }}</small>
          </div>

          <div class="field">
            <label class="form-label" for="publicationState">{{
              t('treeForm.fields.publicationState.label')
            }}</label>
            <select id="publicationState" v-model="form.publicationState" class="form-control">
              <option v-for="item in publicationStateOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>

          <div class="field">
            <label class="form-label" for="publicMapVisibility">{{
              t('treeForm.fields.publicMapVisibility.label')
            }}</label>
            <select id="publicMapVisibility" v-model="form.publicMapVisibility" class="form-control">
              <option v-for="item in mapVisibilityOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
        </div>
      </section>

      <section
        class="tree-form-section tree-form-section--media"
        aria-labelledby="tree-form-media-heading"
      >
        <h2 id="tree-form-media-heading" class="tree-form-section__title">
          {{ t('treeForm.sections.media') }}
        </h2>
        <div class="tree-form-media-grid">
          <div class="tree-form-media-panel">
            <TreePhotoUploadPicker v-model="selectedPhotoFiles" @first-photo-gps="onFirstPhotoGps" />
          </div>

          <section
            class="tree-form-media-panel tree-form-map-slot"
            aria-labelledby="tree-form-map-heading"
          >
            <h3 id="tree-form-map-heading" class="tree-detail-panel__title">
              {{ t('treesDetail.map.title') }}
            </h3>
            <TreeLocationMapPreview
              :latitude="form.latitude"
              :longitude="form.longitude"
              :show-marker="showMapMarker"
              @pick-coordinates="onMapPickCoordinates"
            />
          </section>
        </div>
      </section>

      <section
        class="tree-form-section"
        aria-labelledby="tree-form-location-heading"
      >
        <h2 id="tree-form-location-heading" class="tree-form-section__title">
          {{ t('treeForm.sections.location') }}
        </h2>
        <div class="tree-form-location-row">
          <div class="field">
            <label class="form-label" for="provinceId">{{
              t('treeForm.fields.province.label')
            }}</label>
            <select
              id="provinceId"
              data-testid="tree-form-province"
              v-model="form.provinceId"
              class="form-control"
              required
              :aria-invalid="Boolean(fieldErrors.provinceId)"
            >
              <option disabled value="">{{ t('treeForm.fields.province.placeholder') }}</option>
              <option v-for="item in provinces" :key="item.id" :value="String(item.id)">
                {{ item.label }}
              </option>
            </select>
            <small v-if="fieldErrors.provinceId" class="field-error">{{
              fieldErrors.provinceId
            }}</small>
          </div>

          <div class="field">
            <label class="form-label" for="municipality">{{
              t('treeForm.fields.municipality.label')
            }}</label>
            <input
              id="municipality"
              data-testid="tree-form-municipality"
              v-model="form.municipality"
              class="form-control"
              type="text"
              maxlength="255"
              :placeholder="t('treeForm.fields.municipality.placeholder')"
            />
          </div>
        </div>

        <div class="field tree-form-field-block">
          <label class="form-label" for="description">{{
            t('treeForm.fields.description.label')
          }}</label>
          <textarea
            id="description"
            v-model="form.description"
            class="form-control form-textarea"
            rows="2"
            :placeholder="t('treeForm.fields.description.placeholder')"
            :aria-invalid="Boolean(fieldErrors.description)"
            maxlength="5000"
          />
          <small v-if="fieldErrors.description" class="field-error">{{
            fieldErrors.description
          }}</small>
        </div>
      </section>

      <section
        class="tree-form-section"
        aria-labelledby="tree-form-coordinates-heading"
      >
        <h2 id="tree-form-coordinates-heading" class="tree-form-section__title">
          {{ t('treeForm.sections.coordinates') }}
        </h2>
        <div class="tree-geo-row">
          <div class="field">
            <label class="form-label" for="latitude">{{ t('treeForm.fields.latitude.label') }}</label>
            <input
              id="latitude"
              data-testid="tree-form-latitude"
              v-model="form.latitude"
              class="form-control"
              type="number"
              step="any"
              min="-90"
              max="90"
              required
              :placeholder="t('treeForm.fields.latitude.placeholder')"
              :aria-invalid="Boolean(fieldErrors.latitude)"
            />
            <small v-if="fieldErrors.latitude" class="field-error">{{ fieldErrors.latitude }}</small>
          </div>

          <div class="field">
            <label class="form-label" for="longitude">{{
              t('treeForm.fields.longitude.label')
            }}</label>
            <input
              id="longitude"
              data-testid="tree-form-longitude"
              v-model="form.longitude"
              class="form-control"
              type="number"
              step="any"
              min="-180"
              max="180"
              required
              :placeholder="t('treeForm.fields.longitude.placeholder')"
              :aria-invalid="Boolean(fieldErrors.longitude)"
            />
            <small v-if="fieldErrors.longitude" class="field-error">{{
              fieldErrors.longitude
            }}</small>
          </div>

          <div class="field">
            <label class="form-label" for="altitude">{{ t('treeForm.fields.altitude.label') }}</label>
            <input
              id="altitude"
              v-model="form.altitude"
              class="form-control"
              type="number"
              step="any"
              :placeholder="t('treeForm.fields.altitude.placeholder')"
            />
          </div>
        </div>
      </section>

      <p v-if="submitError" class="error field-full" role="alert">{{ submitError }}</p>

      <div class="field-full actions page-actions-footer">
        <RouterLink class="btn btn-secondary" :to="{ name: 'home' }">
          {{ t('navigation.home') }}
        </RouterLink>
        <button
          class="btn btn-primary tree-form-submit"
          type="submit"
          data-testid="tree-form-submit"
          :disabled="isSubmitting"
        >
          {{ isSubmitting ? t('treeForm.submitting') : t('treeForm.submit') }}
        </button>
      </div>
    </form>
  </div>
</template>
