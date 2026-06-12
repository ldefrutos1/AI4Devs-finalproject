/** Query `fromCreate` tras alta: mensaje flash en edición (se elimina con `router.replace`). */
export const TREE_CREATE_FLASH_QUERY = 'fromCreate' as const

/** Query `saved` tras edición exitosa: mensaje flash en listado de mis ejemplares. */
export const TREE_EDIT_SAVE_FLASH_QUERY = 'saved' as const

export type TreeCreateFlashValue = 'ok' | 'okPhotos' | 'photosWarning'

export function treeEditRouteAfterCreate(
  treeId: number,
  flash: TreeCreateFlashValue,
  enrichmentWarning?: string,
): {
  name: 'ejemplares-edit'
  params: { id: string }
  query: { fromCreate: TreeCreateFlashValue }
  state?: { enrichmentWarning?: string }
} {
  const route = {
    name: 'ejemplares-edit' as const,
    params: { id: String(treeId) },
    query: { [TREE_CREATE_FLASH_QUERY]: flash },
  }
  const trimmedWarning = enrichmentWarning?.trim()
  if (trimmedWarning) {
    return { ...route, state: { enrichmentWarning: trimmedWarning } }
  }
  return route
}

export function parseTreeCreateFlash(value: unknown): TreeCreateFlashValue | null {
  if (value === 'ok' || value === 'okPhotos' || value === 'photosWarning') {
    return value
  }
  return null
}
