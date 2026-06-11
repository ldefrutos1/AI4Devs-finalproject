import { describe, expect, it } from 'vitest'
import { mapVisibilityBadgeClass, publicationStateBadgeClass } from '@/utils/catalogBadgeClass'

describe('catalogBadgeClass', () => {
  it('publicationStateBadgeClass asigna clases por estado', () => {
    expect(publicationStateBadgeClass('PUBLICADO')).toBe('mtl-badge mtl-badge--success')
    expect(publicationStateBadgeClass('BORRADOR')).toBe('mtl-badge mtl-badge--draft')
    expect(publicationStateBadgeClass('OTRO')).toBe('mtl-badge')
  })

  it('mapVisibilityBadgeClass asigna clases por visibilidad', () => {
    expect(mapVisibilityBadgeClass('PUBLICO')).toBe('mtl-badge mtl-badge--info')
    expect(mapVisibilityBadgeClass('PRIVADO')).toBe('mtl-badge mtl-badge--muted')
    expect(mapVisibilityBadgeClass('OTRO')).toBe('mtl-badge mtl-badge--muted')
  })
})
