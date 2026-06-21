import { ref } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useGalleryPhotoDisplayUrls } from '@/composables/useGalleryPhotoDisplayUrls'
import { apiFetchBlob } from '@/services/http/apiClient'
import type { TreePhotoGalleryItem } from '@/types/media'

vi.mock('@/services/http/apiClient', () => ({
  apiFetchBlob: vi.fn(),
}))

const apiFetchBlobMock = vi.mocked(apiFetchBlob)

function photo(id: number): TreePhotoGalleryItem {
  return {
    id,
    url: `/api/media/trees/1/photos/${id}/content`,
    isPrimary: id === 1,
    order: id,
    mimeType: 'image/jpeg',
    width: 100,
    height: 100,
    category: 'PUBLIC',
  }
}

describe('useGalleryPhotoDisplayUrls', () => {
  beforeEach(() => {
    apiFetchBlobMock.mockReset()
    vi.spyOn(URL, 'createObjectURL').mockImplementation(() => 'blob:mock')
    vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('carga blobs autenticados y expone urlFor por id', async () => {
    apiFetchBlobMock.mockResolvedValue(new Blob(['x'], { type: 'image/jpeg' }))
    const photos = ref<TreePhotoGalleryItem[]>([photo(1), photo(2)])

    const { urlFor } = useGalleryPhotoDisplayUrls(photos)

    await vi.waitFor(() => {
      expect(urlFor(photo(1))).toBe('blob:mock')
    })

    expect(apiFetchBlobMock).toHaveBeenCalledWith('/api/media/trees/1/photos/1/content', {
      signal: expect.any(AbortSignal),
    })
    expect(urlFor(photo(2))).toBe('blob:mock')
  })

  it('revoca URLs al cambiar la galería', async () => {
    apiFetchBlobMock.mockResolvedValue(new Blob(['x'], { type: 'image/jpeg' }))
    const photos = ref<TreePhotoGalleryItem[]>([photo(1)])

    useGalleryPhotoDisplayUrls(photos)
    await vi.waitFor(() => expect(URL.createObjectURL).toHaveBeenCalled())

    photos.value = [photo(2)]
    await vi.waitFor(() => expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:mock'))
  })
})
