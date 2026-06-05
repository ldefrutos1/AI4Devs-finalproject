<script setup lang="ts">
import { useTemplateRef } from 'vue'
import { useI18n } from 'vue-i18n'
import MtlConfirmDialog from '@/components/MtlConfirmDialog.vue'
import TreePhotoFullscreenViewer from '@/components/TreePhotoFullscreenViewer.vue'
import {
  EDIT_TREE_GALLERY_PHOTO_ACCEPT_MIME,
  useEditTreeGallery,
  type UseEditTreeGalleryBindings,
} from '@/composables/useEditTreeGallery'

const props = defineProps<{
  gallery: UseEditTreeGalleryBindings
  galleryPhotoError: string
}>()

const { t } = useI18n()
const photoFileInputRef = useTemplateRef<HTMLInputElement>('photoFileInput')
const {
  deletePhotoConfirmOpen,
  selectedPhotoIndex,
  isFullscreenOpen,
  hasGalleryPhotos,
  hasMultipleGalleryPhotos,
  selectedPhoto,
  selectedPhotoPosition,
  galleryAltText,
  showPreviousPhoto,
  showNextPhoto,
  openFullscreen,
  closeFullscreen,
  openDeletePhotoConfirm,
  onConfirmDeletePhoto,
  openPhotoFilePicker,
  onPhotoFileSelected,
} = useEditTreeGallery({ ...props.gallery, photoFileInputRef })

const { galleryPhotos, isDeletingPhoto, isUploadingPhoto, canAddGalleryPhoto } = props.gallery
</script>

<template>
  <section class="tree-detail-panel" aria-labelledby="tree-edit-gallery-heading">
    <h2 id="tree-edit-gallery-heading" class="tree-detail-panel__title">
      {{ t('treesDetail.gallery.title') }}
    </h2>
    <div class="tree-detail-gallery-frame">
      <button
        v-if="selectedPhoto"
        type="button"
        class="tree-detail-gallery-open-btn"
        :aria-label="t('treesDetail.gallery.openViewer')"
        @click="openFullscreen"
        @keydown.enter.prevent="openFullscreen"
        @keydown.space.prevent="openFullscreen"
      >
        <img
          class="tree-detail-gallery-image"
          :src="selectedPhoto.url"
          :alt="galleryAltText"
          draggable="false"
          @dblclick="openFullscreen"
        />
      </button>
      <output v-else class="muted tree-detail-gallery-empty">{{
        t('treesDetail.gallery.noPhotos')
      }}</output>
    </div>
    <div class="tree-detail-gallery-controls">
      <button
        v-if="hasMultipleGalleryPhotos"
        type="button"
        class="btn btn-secondary btn-sm"
        :disabled="isDeletingPhoto || isUploadingPhoto"
        @click="showPreviousPhoto"
      >
        {{ t('treesDetail.gallery.previous') }}
      </button>
      <span v-else class="tree-detail-gallery-controls-spacer" aria-hidden="true" />
      <div class="tree-detail-gallery-position">
        <span v-if="hasGalleryPhotos" class="muted">{{
          t('treesDetail.gallery.position', {
            current: selectedPhotoPosition,
            total: galleryPhotos.length,
          })
        }}</span>
        <span v-else class="muted">{{ t('treeEdit.gallery.noPhotosHint') }}</span>
        <div class="tree-detail-gallery-actions">
          <button
            type="button"
            class="btn btn-outline-danger btn-sm tree-gallery-icon-btn"
            :aria-label="t('treeEdit.gallery.deletePhoto')"
            :disabled="!selectedPhoto || isDeletingPhoto || isUploadingPhoto"
            @click="openDeletePhotoConfirm"
          >
            <svg
              class="tree-gallery-action-icon"
              xmlns="http://www.w3.org/2000/svg"
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
              aria-hidden="true"
            >
              <path d="M3 6h18" />
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" />
              <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            </svg>
          </button>
          <button
            type="button"
            class="btn btn-outline-primary btn-sm tree-gallery-icon-btn"
            :aria-label="t('treeEdit.gallery.addPhoto')"
            :disabled="!canAddGalleryPhoto"
            @click="openPhotoFilePicker"
          >
            <svg
              class="tree-gallery-action-icon"
              xmlns="http://www.w3.org/2000/svg"
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
              aria-hidden="true"
            >
              <path d="M12 5v14" />
              <path d="M5 12h14" />
            </svg>
          </button>
        </div>
        <input
          ref="photoFileInput"
          class="tree-gallery-file-input"
          type="file"
          :accept="EDIT_TREE_GALLERY_PHOTO_ACCEPT_MIME"
          :aria-label="t('treeEdit.gallery.addPhoto')"
          @change="onPhotoFileSelected"
        />
      </div>
      <button
        v-if="hasMultipleGalleryPhotos"
        type="button"
        class="btn btn-secondary btn-sm"
        :disabled="isDeletingPhoto || isUploadingPhoto"
        @click="showNextPhoto"
      >
        {{ t('treesDetail.gallery.next') }}
      </button>
      <span v-else class="tree-detail-gallery-controls-spacer" aria-hidden="true" />
    </div>
    <p v-if="galleryPhotoError" class="error tree-edit-gallery-error" role="alert">
      {{ galleryPhotoError }}
    </p>

    <TreePhotoFullscreenViewer
      v-if="isFullscreenOpen && hasGalleryPhotos"
      :photos="galleryPhotos"
      :initial-index="selectedPhotoIndex"
      :title="galleryAltText"
      @close="closeFullscreen"
    />

    <MtlConfirmDialog
      v-model:open="deletePhotoConfirmOpen"
      :title="t('treeEdit.gallery.deleteConfirm.title')"
      :message="t('treeEdit.gallery.deleteConfirm.message')"
      :cancel-label="t('treeEdit.gallery.deleteConfirm.cancel')"
      :confirm-label="t('treeEdit.gallery.deleteConfirm.confirm')"
      :confirm-danger="true"
      @confirm="onConfirmDeletePhoto"
    />
  </section>
</template>
