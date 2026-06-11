export function publicationStateBadgeClass(state: string): string {
  if (state === 'PUBLICADO') {
    return 'mtl-badge mtl-badge--success'
  }
  if (state === 'BORRADOR') {
    return 'mtl-badge mtl-badge--draft'
  }
  return 'mtl-badge'
}

export function mapVisibilityBadgeClass(visibility: string): string {
  if (visibility === 'PUBLICO') {
    return 'mtl-badge mtl-badge--info'
  }
  if (visibility === 'PRIVADO') {
    return 'mtl-badge mtl-badge--muted'
  }
  return 'mtl-badge mtl-badge--muted'
}
