import type { BibliographicReference } from '@/types/enrichment'
import {
  isJsonObjectFieldEffectivelyEmpty,
  isTreeHealthStatusJsonDraftPristine,
  parseJsonArrayField,
  parseJsonObjectField,
  parseTagsComma,
  SPECIES_ECOLOGICAL_DATA_JSON_TEMPLATE,
  SPECIES_REFERENCES_JSON_TEMPLATE,
  stringifyJsonField,
} from '@/composables/enrichmentFormDraft'

export type EnrichmentFieldEditMode = 'guided' | 'json'

export const HEALTH_STATUS_OBJECT_KEYS = [
  'valoracion_general',
  'plagas_detectadas',
  'lesiones',
  'ultima_revision',
] as const

export const HEALTH_LESION_OBJECT_KEYS = ['tipo', 'descripcion', 'lado'] as const

export interface HealthLesionGuidedDraft {
  tipo: string
  descripcion: string
  lado: string
}

export interface HealthStatusGuidedDraft {
  valoracionGeneral: string
  plagasDetectadasText: string
  lesiones: HealthLesionGuidedDraft[]
  ultimaRevision: string
}

export const ECOLOGICAL_OBJECT_KEYS = ['habitat', 'altitudMinM', 'altitudMaxM', 'clima'] as const

export interface EcologicalGuidedDraft {
  habitatText: string
  altitudMinM: string
  altitudMaxM: string
  climaText: string
}

export const REFERENCE_OBJECT_KEYS = ['title', 'authors', 'source', 'year', 'url'] as const

export interface ReferenceGuidedDraft {
  title: string
  authorsText: string
  source: string
  year: string
  url: string
}

export function createEmptyHealthLesionDraft(): HealthLesionGuidedDraft {
  return { tipo: '', descripcion: '', lado: '' }
}

export function createEmptyHealthStatusGuidedDraft(): HealthStatusGuidedDraft {
  return {
    valoracionGeneral: '',
    plagasDetectadasText: '',
    lesiones: [],
    ultimaRevision: '',
  }
}

export function createEmptyEcologicalGuidedDraft(): EcologicalGuidedDraft {
  return {
    habitatText: '',
    altitudMinM: '',
    altitudMaxM: '',
    climaText: '',
  }
}

export function createEmptyReferenceGuidedDraft(): ReferenceGuidedDraft {
  return {
    title: '',
    authorsText: '',
    source: '',
    year: '',
    url: '',
  }
}

function objectKeysMatchAllowed(
  value: Record<string, unknown>,
  allowedKeys: readonly string[],
): boolean {
  return Object.keys(value).every((key) => allowedKeys.includes(key))
}

export function healthStatusGuidedFitsObject(value: Record<string, unknown>): boolean {
  if (!objectKeysMatchAllowed(value, HEALTH_STATUS_OBJECT_KEYS)) {
    return false
  }
  const lesions = value.lesiones
  if (lesions === undefined) {
    return true
  }
  if (!Array.isArray(lesions)) {
    return false
  }
  return lesions.every((item) => {
    if (typeof item !== 'object' || item === null || Array.isArray(item)) {
      return false
    }
    return objectKeysMatchAllowed(item as Record<string, unknown>, HEALTH_LESION_OBJECT_KEYS)
  })
}

export function ecologicalGuidedFitsObject(value: Record<string, unknown>): boolean {
  return objectKeysMatchAllowed(value, ECOLOGICAL_OBJECT_KEYS)
}

export function referenceGuidedFitsObject(value: BibliographicReference): boolean {
  return objectKeysMatchAllowed(value as Record<string, unknown>, REFERENCE_OBJECT_KEYS)
}

export function referencesGuidedFitsArray(value: BibliographicReference[]): boolean {
  return value.every((item) => referenceGuidedFitsObject(item))
}

export function healthStatusObjectToGuided(value: Record<string, unknown>): HealthStatusGuidedDraft {
  const pests = value.plagas_detectadas
  return {
    valoracionGeneral:
      typeof value.valoracion_general === 'string' ? value.valoracion_general : '',
    plagasDetectadasText: Array.isArray(pests)
      ? pests.map((item) => String(item)).join(', ')
      : '',
    lesiones: Array.isArray(value.lesiones)
      ? value.lesiones.map((item) => {
          const lesion = item as Record<string, unknown>
          return {
            tipo: typeof lesion.tipo === 'string' ? lesion.tipo : '',
            descripcion: typeof lesion.descripcion === 'string' ? lesion.descripcion : '',
            lado: typeof lesion.lado === 'string' ? lesion.lado : '',
          }
        })
      : [],
    ultimaRevision:
      typeof value.ultima_revision === 'string' ? value.ultima_revision : '',
  }
}

export function healthStatusGuidedToObject(
  draft: HealthStatusGuidedDraft,
): Record<string, unknown> {
  const result: Record<string, unknown> = {}
  const assessment = draft.valoracionGeneral.trim()
  if (assessment.length > 0) {
    result.valoracion_general = assessment
  }

  const pests = parseTagsComma(draft.plagasDetectadasText)
  if (pests.length > 0) {
    result.plagas_detectadas = pests
  }

  const lesions = draft.lesiones
    .map((item) => ({
      tipo: item.tipo.trim() || undefined,
      descripcion: item.descripcion.trim() || undefined,
      lado: item.lado.trim() || undefined,
    }))
    .filter((item) => item.tipo || item.descripcion || item.lado)
    .map((item) => ({
      tipo: item.tipo,
      descripcion: item.descripcion,
      lado: item.lado,
    }))
  if (lesions.length > 0) {
    result.lesiones = lesions
  }

  const revisionDate = draft.ultimaRevision.trim()
  if (revisionDate.length > 0) {
    result.ultima_revision = revisionDate
  }

  return result
}

export function ecologicalObjectToGuided(value: Record<string, unknown>): EcologicalGuidedDraft {
  const habitat = value.habitat
  const climate = value.clima
  return {
    habitatText: Array.isArray(habitat) ? habitat.map((item) => String(item)).join(', ') : '',
    altitudMinM:
      value.altitudMinM !== undefined && Number.isFinite(Number(value.altitudMinM))
        ? String(value.altitudMinM)
        : '',
    altitudMaxM:
      value.altitudMaxM !== undefined && Number.isFinite(Number(value.altitudMaxM))
        ? String(value.altitudMaxM)
        : '',
    climaText: Array.isArray(climate) ? climate.map((item) => String(item)).join(', ') : '',
  }
}

export function ecologicalGuidedToObject(draft: EcologicalGuidedDraft): Record<string, unknown> {
  const result: Record<string, unknown> = {}
  const habitat = parseTagsComma(draft.habitatText)
  if (habitat.length > 0) {
    result.habitat = habitat
  }

  const minAltitude = draft.altitudMinM.trim()
  if (minAltitude.length > 0) {
    const parsed = Number(minAltitude)
    if (Number.isFinite(parsed)) {
      result.altitudMinM = parsed
    }
  }

  const maxAltitude = draft.altitudMaxM.trim()
  if (maxAltitude.length > 0) {
    const parsed = Number(maxAltitude)
    if (Number.isFinite(parsed)) {
      result.altitudMaxM = parsed
    }
  }

  const climate = parseTagsComma(draft.climaText)
  if (climate.length > 0) {
    result.clima = climate
  }

  return result
}

export function referenceObjectToGuided(value: BibliographicReference): ReferenceGuidedDraft {
  return {
    title: value.title ?? '',
    authorsText: Array.isArray(value.authors) ? value.authors.join(', ') : '',
    source: value.source ?? '',
    year: value.year !== undefined && Number.isFinite(value.year) ? String(value.year) : '',
    url: value.url ?? '',
  }
}

export function referenceGuidedToObject(draft: ReferenceGuidedDraft): BibliographicReference {
  const result: BibliographicReference = {}
  const title = draft.title.trim()
  if (title.length > 0) {
    result.title = title
  }

  const authors = parseTagsComma(draft.authorsText)
  if (authors.length > 0) {
    result.authors = authors
  }

  const source = draft.source.trim()
  if (source.length > 0) {
    result.source = source
  }

  const yearText = draft.year.trim()
  if (yearText.length > 0) {
    const parsed = Number.parseInt(yearText, 10)
    if (Number.isFinite(parsed)) {
      result.year = parsed
    }
  }

  const url = draft.url.trim()
  if (url.length > 0) {
    result.url = url
  }

  return result
}

export function referencesArrayToGuided(value: BibliographicReference[]): ReferenceGuidedDraft[] {
  return value.length > 0 ? value.map(referenceObjectToGuided) : []
}

export function referencesGuidedToArray(drafts: ReferenceGuidedDraft[]): BibliographicReference[] {
  return drafts
    .map(referenceGuidedToObject)
    .filter(
      (item) =>
        item.title ||
        item.authors?.length ||
        item.source ||
        item.year !== undefined ||
        item.url,
    )
}

export function stringifyJsonObjectOrEmpty(value: Record<string, unknown>): string {
  if (Object.keys(value).length === 0) {
    return ''
  }
  return stringifyJsonField(value)
}

export function stringifyJsonArrayOrEmpty<T>(value: T[]): string {
  if (value.length === 0) {
    return ''
  }
  return stringifyJsonField(value)
}

export type JsonFieldParseResult<T> =
  | { ok: true; value: T; fitsGuided: boolean; mode: EnrichmentFieldEditMode }
  | { ok: false; errorKey: 'invalidJson' }

export function parseHealthStatusJsonField(json: string): JsonFieldParseResult<{
  guided: HealthStatusGuidedDraft
}> {
  const trimmed = json.trim()
  if (
    trimmed.length === 0 ||
    isJsonObjectFieldEffectivelyEmpty(trimmed) ||
    isTreeHealthStatusJsonDraftPristine(trimmed)
  ) {
    return {
      ok: true,
      value: { guided: createEmptyHealthStatusGuidedDraft() },
      fitsGuided: true,
      mode: 'guided',
    }
  }

  const parsed = parseJsonObjectField(trimmed)
  if (!parsed.ok) {
    return { ok: false, errorKey: 'invalidJson' }
  }

  const fitsGuided = healthStatusGuidedFitsObject(parsed.value)
  return {
    ok: true,
    value: { guided: healthStatusObjectToGuided(parsed.value) },
    fitsGuided,
    mode: fitsGuided ? 'guided' : 'json',
  }
}

export function parseEcologicalJsonField(json: string): JsonFieldParseResult<{
  guided: EcologicalGuidedDraft
}> {
  const trimmed = json.trim()
  if (
    trimmed.length === 0 ||
    isJsonObjectFieldEffectivelyEmpty(trimmed) ||
    trimmed === SPECIES_ECOLOGICAL_DATA_JSON_TEMPLATE.trim()
  ) {
    return {
      ok: true,
      value: { guided: createEmptyEcologicalGuidedDraft() },
      fitsGuided: true,
      mode: 'guided',
    }
  }

  const parsed = parseJsonObjectField(trimmed)
  if (!parsed.ok) {
    return { ok: false, errorKey: 'invalidJson' }
  }

  const fitsGuided = ecologicalGuidedFitsObject(parsed.value)
  return {
    ok: true,
    value: { guided: ecologicalObjectToGuided(parsed.value) },
    fitsGuided,
    mode: fitsGuided ? 'guided' : 'json',
  }
}

export function parseReferencesJsonField(json: string): JsonFieldParseResult<{
  guided: ReferenceGuidedDraft[]
}> {
  const trimmed = json.trim()
  if (
    trimmed.length === 0 ||
    trimmed === '[]' ||
    trimmed === SPECIES_REFERENCES_JSON_TEMPLATE.trim()
  ) {
    return {
      ok: true,
      value: { guided: [] },
      fitsGuided: true,
      mode: 'guided',
    }
  }

  const parsed = parseJsonArrayField<BibliographicReference>(trimmed)
  if (!parsed.ok) {
    return { ok: false, errorKey: 'invalidJson' }
  }

  const fitsGuided = referencesGuidedFitsArray(parsed.value)
  return {
    ok: true,
    value: { guided: referencesArrayToGuided(parsed.value) },
    fitsGuided,
    mode: fitsGuided ? 'guided' : 'json',
  }
}

export function serializeHealthStatusGuidedDraft(draft: HealthStatusGuidedDraft): string {
  return stringifyJsonObjectOrEmpty(healthStatusGuidedToObject(draft))
}

export function serializeEcologicalGuidedDraft(draft: EcologicalGuidedDraft): string {
  return stringifyJsonObjectOrEmpty(ecologicalGuidedToObject(draft))
}

export function serializeReferencesGuidedDraft(drafts: ReferenceGuidedDraft[]): string {
  return stringifyJsonArrayOrEmpty(referencesGuidedToArray(drafts))
}
