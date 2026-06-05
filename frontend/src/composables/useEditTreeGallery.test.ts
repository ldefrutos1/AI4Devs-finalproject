import { computed, createApp, nextTick, ref } from 'vue'
import { createI18n } from 'vue-i18n'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { es } from '@/i18n/locales/es'
import { useEditTreeGallery } from '@/composables/useEditTreeGallery'
import type { TreePhotoGalleryItem } from '@/types/media'

function photo(id: number): TreePhotoGalleryItem {
  return {
    id,
    url: `https://example.test/${id}.jpg`,
    order: id,
    isPrimary: id === 1,
    mimeType: 'image/jpeg',
    width: 800,
    height: 600,
    category: 'PRIVATE',
  }
}

function mountGallery(photos: TreePhotoGalleryItem[] = [photo(1), photo(2), photo(3)]) {
  const galleryPhotos = ref<TreePhotoGalleryItem[]>([...photos])
  const species = ref([{ id: 10, label: 'Roble' }])
  const speciesId = ref('10')
  const isDeletingPhoto = ref(false)
  const isUploadingPhoto = ref(false)
  const canAddGalleryPhoto = computed(() => galleryPhotos.value.length < 5)
  const photoFileInputRef = ref<HTMLInputElement | null>(null)
  const addGalleryPhoto = vi.fn().mockResolvedValue(true)
  const removeGalleryPhoto = vi.fn().mockResolvedValue(true)

  let api!: ReturnType<typeof useEditTreeGallery>
  const app = createApp({
    setup() {
      api = useEditTreeGallery({
        galleryPhotos,
        species,
        speciesId,
        isDeletingPhoto,
        isUploadingPhoto,
        canAddGalleryPhoto,
        photoFileInputRef,
        addGalleryPhoto,
        removeGalleryPhoto,
      })
      return () => null
    },
  })
  app.use(
    createI18n({
      legacy: false,
      locale: 'es',
      messages: { es },
    }),
  )
  app.mount(document.createElement('div'))

  return {
    api,
    galleryPhotos,
    addGalleryPhoto,
    removeGalleryPhoto,
    unmount: () => app.unmount(),
  }
}

describe('useEditTreeGallery', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('navega circularmente entre fotos', async () => {
    const { api, unmount } = mountGallery()
    expect(api.selectedPhotoIndex.value).toBe(0)
    expect(api.selectedPhoto.value?.id).toBe(1)

    api.showNextPhoto()
    expect(api.selectedPhotoIndex.value).toBe(1)

    api.showNextPhoto()
    api.showNextPhoto()
    expect(api.selectedPhotoIndex.value).toBe(0)

    api.showPreviousPhoto()
    expect(api.selectedPhotoIndex.value).toBe(2)
    unmount()
  })

  it('no navega si solo hay una foto', () => {
    const { api, unmount } = mountGallery([photo(1)])
    api.showNextPhoto()
    expect(api.selectedPhotoIndex.value).toBe(0)
    api.showPreviousPhoto()
    expect(api.selectedPhotoIndex.value).toBe(0)
    unmount()
  })

  it('ajusta el indice tras borrar la foto seleccionada', async () => {
    const { api, galleryPhotos, removeGalleryPhoto, unmount } = mountGallery()
    api.selectedPhotoIndex.value = 2
    removeGalleryPhoto.mockImplementation(async () => {
      galleryPhotos.value = galleryPhotos.value.filter((item) => item.id !== 3)
      return true
    })

    await api.onConfirmDeletePhoto()
    await nextTick()

    expect(removeGalleryPhoto).toHaveBeenCalledWith(3)
    expect(api.selectedPhotoIndex.value).toBe(1)
    expect(api.selectedPhoto.value?.id).toBe(2)
    unmount()
  })

  it('selecciona la ultima foto tras anadir una nueva', async () => {
    const { api, galleryPhotos, addGalleryPhoto, unmount } = mountGallery([photo(1)])
    addGalleryPhoto.mockImplementation(async () => {
      galleryPhotos.value = [...galleryPhotos.value, photo(2)]
      return true
    })

    const input = document.createElement('input')
    const file = new File(['x'], 'nueva.jpg', { type: 'image/jpeg' })
    Object.defineProperty(input, 'files', { value: [file], configurable: true })

    await api.onPhotoFileSelected({ target: input } as unknown as Event)
    await nextTick()

    expect(addGalleryPhoto).toHaveBeenCalledWith(file)
    expect(api.selectedPhotoIndex.value).toBe(1)
    expect(api.selectedPhoto.value?.id).toBe(2)
    unmount()
  })

  it('usa etiqueta de especie para el alt de la galeria', () => {
    const { api, unmount } = mountGallery()
    expect(api.galleryAltText.value).toBe('Roble')
    unmount()
  })
})
