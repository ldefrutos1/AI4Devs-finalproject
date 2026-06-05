<script setup lang="ts">
import { computed, onMounted, ref, toRef } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import EditTreeGalleryPanel from '@/components/EditTreeGalleryPanel.vue'
import MtlConfirmDialog from '@/components/MtlConfirmDialog.vue'
import PageBackLink from '@/components/layout/PageBackLink.vue'
import SpeciesAutocompleteInput from '@/components/SpeciesAutocompleteInput.vue'
import TreeLocationMapPreview from '@/components/TreeLocationMapPreview.vue'
import { areLatLngInValidRange } from '@/composables/createTreeFormValidation'
import { useEditTreeForm } from '@/composables/useEditTreeForm'
import { useTreeCreateFlashFromRoute } from '@/composables/useTreeCreateFlashFromRoute'
import { useTreeLocationAutofill } from '@/composables/useTreeLocationAutofill'

const route = useRoute()
const { t } = useI18n()
const {
  successMessage: createSuccessMessage,
  warningMessage: createWarningMessage,
  applyFromRoute,
} = useTreeCreateFlashFromRoute()

const treeId = computed(() => {
  const rawId = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id
  const parsedId = Number(rawId)
  if (!Number.isInteger(parsedId) || parsedId <= 0) {
    return null
  }
  return parsedId
})

const {
  form,
  species,
  provinces,
  galleryPhotos,
  publicationStateOptions,
  mapVisibilityOptions,
  isLoading,
  loadError,
  isReady,
  isSubmitting,
  isDeleting,
  fieldErrors,
  submitError,
  deleteError,
  galleryPhotoError,
  isDeletingPhoto,
  isUploadingPhoto,
  canAddGalleryPhoto,
  initialize,
  submit,
  addGalleryPhoto,
  removeGalleryPhoto,
  removeTree,
} = useEditTreeForm(treeId)

const showMapMarker = computed(() => areLatLngInValidRange(form))
const deleteConfirmOpen = ref(false)

const { applyCoordinatesAndAutofillAddress } = useTreeLocationAutofill({
  form,
  provinces,
})

const speciesAutocompleteRef = ref<InstanceType<typeof SpeciesAutocompleteInput> | null>(null)

const editTreeGalleryOptions = {
  galleryPhotos,
  species,
  speciesId: toRef(form, 'speciesId'),
  isDeletingPhoto,
  isUploadingPhoto,
  canAddGalleryPhoto,
  addGalleryPhoto,
  removeGalleryPhoto,
}

const pageTitle = computed(() => {
  if (treeId.value) {
    return t('treeEdit.title', { id: treeId.value })
  }
  return t('treeEdit.titleInvalid')
})

interface CoordinatesPayload {
  latitude: string
  longitude: string
}

function onMapPickCoordinates(payload: CoordinatesPayload): void {
  void applyCoordinatesAndAutofillAddress(payload)
}

function openDeleteConfirm(): void {
  deleteConfirmOpen.value = true
}

async function onConfirmDelete(): Promise<void> {
  await removeTree()
}

async function onSubmit(): Promise<void> {
  speciesAutocompleteRef.value?.commitSpeciesFromText()
  await submit()
}

onMounted(async () => {
  applyFromRoute()
  await initialize()
})
</script>

<template>
  <div class="tree-form-page">
    <header class="page-header tree-form-page__header">
      <PageBackLink :to="{ name: 'mis-ejemplares' }">{{ t('treeEdit.backToList') }}</PageBackLink>
      <h1 class="page-header__title">{{ pageTitle }}</h1>
      <p class="page-header__description">{{ t('treeEdit.description') }}</p>
    </header>

    <output v-if="createSuccessMessage" class="success tree-form-page__flash" aria-live="polite">{{
      createSuccessMessage
    }}</output>
    <p v-if="createWarningMessage" class="error tree-form-page__flash" role="alert">
      {{ createWarningMessage }}
    </p>

    <p v-if="isLoading" class="status-note">{{ t('treeEdit.loading') }}</p>
    <p v-else-if="loadError" class="error" role="alert">{{ loadError }}</p>

    <form v-else-if="isReady" class="tree-form" @submit.prevent="onSubmit">
      <div class="field-full tree-form-species-status-row">
        <div class="field species-field">
          <label class="form-label" for="edit-speciesId">{{
            t('treeForm.fields.species.label')
          }}</label>
          <SpeciesAutocompleteInput
            ref="speciesAutocompleteRef"
            input-id="edit-speciesId"
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
          <label class="form-label" for="edit-publicationState">{{
            t('treeForm.fields.publicationState.label')
          }}</label>
          <select id="edit-publicationState" v-model="form.publicationState" class="form-control">
            <option v-for="item in publicationStateOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </div>

        <div class="field">
          <label class="form-label" for="edit-publicMapVisibility">{{
            t('treeForm.fields.publicMapVisibility.label')
          }}</label>
          <select
            id="edit-publicMapVisibility"
            v-model="form.publicMapVisibility"
            class="form-control"
          >
            <option v-for="item in mapVisibilityOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </div>
      </div>

      <div class="field-full tree-detail-visual-grid tree-edit-visual-grid">
        <EditTreeGalleryPanel :gallery="editTreeGalleryOptions" :gallery-photo-error="galleryPhotoError" />

        <section
          class="tree-detail-panel tree-form-map-slot"
          aria-labelledby="tree-edit-map-heading"
        >
          <h2 id="tree-edit-map-heading" class="tree-detail-panel__title">
            {{ t('treesDetail.map.title') }}
          </h2>
          <TreeLocationMapPreview
            :latitude="form.latitude"
            :longitude="form.longitude"
            :show-marker="showMapMarker"
            @pick-coordinates="onMapPickCoordinates"
          />
        </section>
      </div>

      <div class="field-full tree-form-location-row">
        <div class="field">
          <label class="form-label" for="edit-provinceId">{{
            t('treeForm.fields.province.label')
          }}</label>
          <select
            id="edit-provinceId"
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
          <label class="form-label" for="edit-municipality">{{
            t('treeForm.fields.municipality.label')
          }}</label>
          <input
            id="edit-municipality"
            v-model="form.municipality"
            class="form-control"
            type="text"
            maxlength="255"
            :placeholder="t('treeForm.fields.municipality.placeholder')"
          />
        </div>
      </div>

      <div class="field field-full tree-form-field-block">
        <label class="form-label" for="edit-description">{{
          t('treeForm.fields.description.label')
        }}</label>
        <textarea
          id="edit-description"
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

      <div class="field-full tree-geo-row">
        <div class="field">
          <label class="form-label" for="edit-latitude">{{
            t('treeForm.fields.latitude.label')
          }}</label>
          <input
            id="edit-latitude"
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
          <label class="form-label" for="edit-longitude">{{
            t('treeForm.fields.longitude.label')
          }}</label>
          <input
            id="edit-longitude"
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
          <label class="form-label" for="edit-altitude">{{
            t('treeForm.fields.altitude.label')
          }}</label>
          <input
            id="edit-altitude"
            v-model="form.altitude"
            class="form-control"
            type="number"
            step="any"
            :placeholder="t('treeForm.fields.altitude.placeholder')"
          />
        </div>
      </div>

      <p v-if="submitError" class="error field-full" role="alert">{{ submitError }}</p>

      <div class="field-full actions page-actions-footer">
        <RouterLink class="btn btn-secondary" :to="{ name: 'mis-ejemplares' }">
          {{ t('treeEdit.backToList') }}
        </RouterLink>
        <div class="page-actions-footer__end">
          <button
            type="button"
            class="btn btn-outline-danger"
            data-testid="tree-delete-button"
            :disabled="isSubmitting || isDeleting"
            @click="openDeleteConfirm"
          >
            {{ isDeleting ? t('treeEdit.deleting') : t('treeEdit.delete') }}
          </button>
          <button
            class="btn btn-primary tree-form-submit"
            type="submit"
            :disabled="isSubmitting || isDeleting"
          >
            {{ isSubmitting ? t('treeEdit.saving') : t('treeEdit.save') }}
          </button>
        </div>
      </div>
    </form>
    <p v-if="deleteError" class="error" role="alert">{{ deleteError }}</p>

    <MtlConfirmDialog
      v-model:open="deleteConfirmOpen"
      :title="t('treeEdit.deleteConfirm.title')"
      :message="t('treeEdit.deleteConfirm.message')"
      :cancel-label="t('treeEdit.deleteConfirm.cancel')"
      :confirm-label="t('treeEdit.deleteConfirm.confirm')"
      :confirm-danger="true"
      confirm-test-id="tree-delete-confirm"
      @confirm="onConfirmDelete"
    />
  </div>
</template>

<style scoped>
.tree-edit-visual-grid {
  width: 100%;
}

.tree-edit-visual-grid :deep(.tree-location-map-preview) {
  flex: 1;
  min-height: var(--tree-detail-media-h);
}
</style>
