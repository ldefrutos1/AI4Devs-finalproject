import { onUnmounted, ref, watch, type Ref } from 'vue'
import { apiFetchBlob } from '@/services/http/apiClient'
import type { TreePhotoGalleryItem } from '@/types/media'

function revokeObjectUrls(urls: Record<number, string>): void {
  for (const url of Object.values(urls)) {
    URL.revokeObjectURL(url)
  }
}

/**
 * URLs de objeto para mostrar fotos de galería con JWT cuando haga falta (borradores / PRIVATE).
 * `<img src="/api/.../content">` no envía Bearer; este composable usa `apiFetchBlob`.
 */
export function useGalleryPhotoDisplayUrls(photos: Ref<readonly TreePhotoGalleryItem[]>) {
  const displayUrls = ref<Record<number, string>>({})
  let loadGeneration = 0

  async function loadForPhotos(
    items: readonly TreePhotoGalleryItem[],
    signal?: AbortSignal,
  ): Promise<void> {
    const generation = ++loadGeneration
    revokeObjectUrls(displayUrls.value)
    displayUrls.value = {}

    const next: Record<number, string> = {}
    await Promise.all(
      items.map(async (photo) => {
        try {
          const blob = await apiFetchBlob(photo.url, { signal })
          if (blob !== null && blob.size > 0) {
            next[photo.id] = URL.createObjectURL(blob)
          }
        } catch {
          /* miniatura / foto opcional */
        }
      }),
    )

    if (signal?.aborted || generation !== loadGeneration) {
      revokeObjectUrls(next)
      return
    }
    displayUrls.value = next
  }

  watch(
    photos,
    (items, _prev, onCleanup) => {
      const controller = new AbortController()
      onCleanup(() => controller.abort())
      void loadForPhotos(items, controller.signal)
    },
    { immediate: true, deep: true },
  )

  onUnmounted(() => {
    loadGeneration += 1
    revokeObjectUrls(displayUrls.value)
    displayUrls.value = {}
  })

  function urlFor(photo: TreePhotoGalleryItem | null | undefined): string {
    if (!photo) {
      return ''
    }
    return displayUrls.value[photo.id] ?? ''
  }

  return { displayUrls, urlFor }
}
