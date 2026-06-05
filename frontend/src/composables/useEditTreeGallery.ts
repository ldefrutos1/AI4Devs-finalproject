import { computed, ref, type ComputedRef, type Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { MasterListItem } from '@/types/catalog'
import type { TreePhotoGalleryItem } from '@/types/media'

export const EDIT_TREE_GALLERY_PHOTO_ACCEPT_MIME = 'image/jpeg,image/png,image/webp'

export interface UseEditTreeGalleryOptions {
  galleryPhotos: Ref<TreePhotoGalleryItem[]>
  species: Ref<MasterListItem[]>
  speciesId: Ref<string>
  isDeletingPhoto: Ref<boolean>
  isUploadingPhoto: Ref<boolean>
  canAddGalleryPhoto: ComputedRef<boolean>
  addGalleryPhoto: (file: File) => Promise<boolean>
  removeGalleryPhoto: (photoId: number) => Promise<boolean>
}

export function useEditTreeGallery(options: UseEditTreeGalleryOptions) {
  const { t } = useI18n()
  const {
    galleryPhotos,
    species,
    speciesId,
    isDeletingPhoto,
    isUploadingPhoto,
    canAddGalleryPhoto,
    addGalleryPhoto,
    removeGalleryPhoto,
  } = options

  const deletePhotoConfirmOpen = ref(false)
  const selectedPhotoIndex = ref(0)
  const isFullscreenOpen = ref(false)
  const photoFileInputRef = ref<HTMLInputElement | null>(null)

  const hasGalleryPhotos = computed(() => galleryPhotos.value.length > 0)
  const hasMultipleGalleryPhotos = computed(() => galleryPhotos.value.length > 1)

  const selectedPhoto = computed(() => {
    if (!galleryPhotos.value.length) {
      return null
    }
    const index = Math.min(Math.max(selectedPhotoIndex.value, 0), galleryPhotos.value.length - 1)
    return galleryPhotos.value[index] ?? null
  })

  const selectedPhotoPosition = computed(() =>
    selectedPhoto.value ? selectedPhotoIndex.value + 1 : 0,
  )

  const galleryAltText = computed(() => {
    const selected = species.value.find((item) => String(item.id) === speciesId.value)
    return selected?.label ?? t('treeEdit.galleryFallbackAlt')
  })

  function showPreviousPhoto(): void {
    if (!hasMultipleGalleryPhotos.value) {
      return
    }
    selectedPhotoIndex.value =
      (selectedPhotoIndex.value - 1 + galleryPhotos.value.length) % galleryPhotos.value.length
  }

  function showNextPhoto(): void {
    if (!hasMultipleGalleryPhotos.value) {
      return
    }
    selectedPhotoIndex.value = (selectedPhotoIndex.value + 1) % galleryPhotos.value.length
  }

  function openFullscreen(): void {
    if (!hasGalleryPhotos.value) {
      return
    }
    isFullscreenOpen.value = true
  }

  function closeFullscreen(): void {
    isFullscreenOpen.value = false
  }

  function openDeletePhotoConfirm(): void {
    if (!selectedPhoto.value || isDeletingPhoto.value || isUploadingPhoto.value) {
      return
    }
    deletePhotoConfirmOpen.value = true
  }

  async function onConfirmDeletePhoto(): Promise<void> {
    const photo = selectedPhoto.value
    if (!photo) {
      return
    }
    const deletedIndex = selectedPhotoIndex.value
    const ok = await removeGalleryPhoto(photo.id)
    if (!ok) {
      return
    }
    if (galleryPhotos.value.length === 0) {
      selectedPhotoIndex.value = 0
      return
    }
    selectedPhotoIndex.value = Math.min(deletedIndex, galleryPhotos.value.length - 1)
  }

  function openPhotoFilePicker(): void {
    if (!canAddGalleryPhoto.value) {
      return
    }
    photoFileInputRef.value?.click()
  }

  async function onPhotoFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file) {
      return
    }

    const previousCount = galleryPhotos.value.length
    const ok = await addGalleryPhoto(file)
    if (!ok) {
      return
    }
    if (galleryPhotos.value.length > previousCount) {
      selectedPhotoIndex.value = galleryPhotos.value.length - 1
    }
  }

  return {
    deletePhotoConfirmOpen,
    selectedPhotoIndex,
    isFullscreenOpen,
    photoFileInputRef,
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
  }
}
