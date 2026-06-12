import type {
  BibliographicReference,
  FieldObservation,
  SpeciesEnrichment,
  SpeciesEnrichmentReplaceRequest,
  TreeEnrichmentReplaceRequest,
  TreeMeasurements,
} from '@/types/enrichment'

/** Claves de medidas alineadas con el ejemplo OpenAPI (`TreeMeasurements`). */
export const TREE_MEASUREMENT_FIELD_KEYS = [
  'heightM',
  'trunkDiameterCm',
  'crownDiameterM',
  'trunkPerimeterCm',
] as const

export type TreeMeasurementFieldKey = (typeof TREE_MEASUREMENT_FIELD_KEYS)[number]

export function formatSynonymsLines(synonyms?: string[]): string {
  return (synonyms ?? []).join('\n')
}

export function parseSynonymsLines(text: string): string[] {
  return text
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.length > 0)
}

export function formatTagsComma(tags?: string[]): string {
  return (tags ?? []).join(', ')
}

export function parseTagsComma(text: string): string[] {
  return text
    .split(',')
    .map((tag) => tag.trim())
    .filter((tag) => tag.length > 0)
}

export function stringifyJsonField(value: unknown): string {
  if (value === undefined || value === null) {
    return ''
  }
  if (
    typeof value === 'object' &&
    !Array.isArray(value) &&
    Object.keys(value as Record<string, unknown>).length === 0
  ) {
    return ''
  }
  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return ''
  }
}

export function isJsonObjectFieldEffectivelyEmpty(text: string): boolean {
  const trimmed = text.trim()
  if (trimmed.length === 0) {
    return true
  }
  const objectResult = parseJsonObjectField(trimmed)
  return objectResult.ok && Object.keys(objectResult.value).length === 0
}

export type JsonParseResult<T> = { ok: true; value: T } | { ok: false; errorKey: 'invalidJson' }

export function parseJsonObjectField(text: string): JsonParseResult<Record<string, unknown>> {
  const trimmed = text.trim()
  if (trimmed.length === 0) {
    return { ok: true, value: {} }
  }
  try {
    const parsed: unknown = JSON.parse(trimmed)
    if (typeof parsed !== 'object' || parsed === null || Array.isArray(parsed)) {
      return { ok: false, errorKey: 'invalidJson' }
    }
    return { ok: true, value: parsed as Record<string, unknown> }
  } catch {
    return { ok: false, errorKey: 'invalidJson' }
  }
}

export function parseJsonArrayField<T>(text: string): JsonParseResult<T[]> {
  const trimmed = text.trim()
  if (trimmed.length === 0) {
    return { ok: true, value: [] }
  }
  try {
    const parsed: unknown = JSON.parse(trimmed)
    if (!Array.isArray(parsed)) {
      return { ok: false, errorKey: 'invalidJson' }
    }
    return { ok: true, value: parsed as T[] }
  } catch {
    return { ok: false, errorKey: 'invalidJson' }
  }
}

export function parseMeasurementInput(raw: string): number | undefined {
  const trimmed = raw.trim()
  if (trimmed.length === 0) {
    return undefined
  }
  const value = Number(trimmed)
  if (!Number.isFinite(value)) {
    return undefined
  }
  return value
}

export function buildMeasurementsFromInputs(
  inputs: Partial<Record<TreeMeasurementFieldKey, string>>,
): TreeMeasurements {
  const measurements: TreeMeasurements = {}
  for (const key of TREE_MEASUREMENT_FIELD_KEYS) {
    const parsed = parseMeasurementInput(inputs[key] ?? '')
    if (parsed !== undefined) {
      measurements[key] = parsed
    }
  }
  return measurements
}

export function measurementInputsFromRecord(
  measurements?: TreeMeasurements,
): Record<TreeMeasurementFieldKey, string> {
  const inputs = {} as Record<TreeMeasurementFieldKey, string>
  for (const key of TREE_MEASUREMENT_FIELD_KEYS) {
    const value = measurements?.[key]
    inputs[key] = value !== undefined && Number.isFinite(value) ? String(value) : ''
  }
  return inputs
}

export function speciesEnrichmentToReplaceRequest(
  enrichment: SpeciesEnrichment,
): SpeciesEnrichmentReplaceRequest {
  return {
    synonyms: enrichment.synonyms,
    distribution: enrichment.distribution,
    ecologicalData: enrichment.ecologicalData,
    references: enrichment.references,
  }
}

export function createEmptyObservation(): FieldObservation {
  return { date: '', text: '', author: '' }
}

export interface TreeEnrichmentFormDraft {
  measurementInputs: Record<TreeMeasurementFieldKey, string>
  tagsText: string
  healthStatusJson: string
  observations: FieldObservation[]
}

export function createEmptyTreeEnrichmentDraft(): TreeEnrichmentFormDraft {
  return {
    measurementInputs: measurementInputsFromRecord(),
    tagsText: '',
    healthStatusJson: '',
    observations: [],
  }
}

export function treeEnrichmentToFormDraft(
  enrichment?: TreeEnrichmentReplaceRequest | null,
): TreeEnrichmentFormDraft {
  if (!enrichment) {
    return createEmptyTreeEnrichmentDraft()
  }
  return {
    measurementInputs: measurementInputsFromRecord(enrichment.measurements),
    tagsText: formatTagsComma(enrichment.tags),
    healthStatusJson: stringifyJsonField(enrichment.healthStatus),
    observations: enrichment.observations?.length
      ? enrichment.observations.map((item) => ({ ...item }))
      : [],
  }
}

export type TreeEnrichmentDraftBuildResult =
  | { ok: true; payload: TreeEnrichmentReplaceRequest }
  | { ok: false; errorKey: 'invalidJson' | 'invalidMeasurement' }

export function buildTreeEnrichmentReplaceRequest(
  draft: TreeEnrichmentFormDraft,
): TreeEnrichmentDraftBuildResult {
  for (const key of TREE_MEASUREMENT_FIELD_KEYS) {
    const raw = draft.measurementInputs[key] ?? ''
    if (raw.trim().length === 0) {
      continue
    }
    const parsed = parseMeasurementInput(raw)
    if (parsed === undefined) {
      return { ok: false, errorKey: 'invalidMeasurement' }
    }
  }

  const healthResult = parseJsonObjectField(draft.healthStatusJson)
  if (!healthResult.ok) {
    return { ok: false, errorKey: 'invalidJson' }
  }

  const observations = draft.observations
    .map((item) => ({
      date: item.date?.trim() || undefined,
      text: item.text?.trim() || undefined,
      author: item.author?.trim() || undefined,
      conditions: item.conditions,
    }))
    .filter((item) => item.date || item.text || item.author)

  return {
    ok: true,
    payload: {
      measurements: buildMeasurementsFromInputs(draft.measurementInputs),
      healthStatus: healthResult.value,
      tags: parseTagsComma(draft.tagsText),
      observations,
    },
  }
}

export type TreeEnrichmentDraftState = {
  dirty: boolean
  valid: boolean
  errorKey?: 'invalidJson' | 'invalidMeasurement'
}

export const EMPTY_TREE_ENRICHMENT_PAYLOAD: TreeEnrichmentReplaceRequest = {
  tags: [],
  measurements: {},
  healthStatus: {},
  observations: [],
}

export function isTreeHealthStatusJsonDraftPristine(text: string): boolean {
  if (isJsonObjectFieldEffectivelyEmpty(text)) {
    return true
  }
  return text.trim() === TREE_HEALTH_STATUS_JSON_TEMPLATE.trim()
}

export function normalizeTreeEnrichmentDraftForDirtyCheck(
  draft: TreeEnrichmentFormDraft,
): TreeEnrichmentFormDraft {
  return {
    measurementInputs: { ...draft.measurementInputs },
    tagsText: draft.tagsText,
    healthStatusJson: isTreeHealthStatusJsonDraftPristine(draft.healthStatusJson)
      ? ''
      : draft.healthStatusJson,
    observations: draft.observations.map((item) => ({ ...item })),
  }
}

export function buildTreeEnrichmentPayloadFromRequest(
  request?: TreeEnrichmentReplaceRequest | null,
): TreeEnrichmentReplaceRequest {
  const result = buildTreeEnrichmentReplaceRequest(treeEnrichmentToFormDraft(request))
  return result.ok ? result.payload : { ...EMPTY_TREE_ENRICHMENT_PAYLOAD }
}

export function cloneTreeEnrichmentPayload(
  request?: TreeEnrichmentReplaceRequest | null,
): TreeEnrichmentReplaceRequest {
  if (!request) {
    return { ...EMPTY_TREE_ENRICHMENT_PAYLOAD }
  }
  return {
    measurements: { ...(request.measurements ?? {}) },
    healthStatus: { ...(request.healthStatus ?? {}) },
    tags: [...(request.tags ?? [])],
    observations: (request.observations ?? []).map((item) => ({ ...item })),
  }
}

export function resolveTreeEnrichmentDraftState(
  draft: TreeEnrichmentFormDraft,
  baseline?: TreeEnrichmentReplaceRequest | null,
): TreeEnrichmentDraftState {
  const actualBuild = buildTreeEnrichmentReplaceRequest(draft)
  const normalizedDraft = normalizeTreeEnrichmentDraftForDirtyCheck(draft)
  const normalizedBuild = buildTreeEnrichmentReplaceRequest(normalizedDraft)
  const baselinePayload = buildTreeEnrichmentPayloadFromRequest(baseline)

  const dirty = normalizedBuild.ok
    ? JSON.stringify(normalizedBuild.payload) !== JSON.stringify(baselinePayload)
    : JSON.stringify(normalizedDraft) !==
      JSON.stringify(
        normalizeTreeEnrichmentDraftForDirtyCheck(treeEnrichmentToFormDraft(baseline)),
      )

  if (!actualBuild.ok) {
    if (normalizedBuild.ok) {
      return { dirty, valid: true }
    }
    return { dirty, valid: false, errorKey: actualBuild.errorKey }
  }

  return {
    dirty: JSON.stringify(actualBuild.payload) !== JSON.stringify(baselinePayload),
    valid: true,
  }
}

/** Ejemplo JSON visible como placeholder del estado sanitario. */
export const TREE_HEALTH_STATUS_JSON_PLACEHOLDER = `{
  "valoracion_general": "bueno",
  "plagas_detectadas": ["Tortrix viridana"],
  "lesiones": [
    {
      "tipo": "cavidad basal",
      "descripcion": "Oquedad en la base del tronco",
      "lado": "norte"
    }
  ],
  "ultima_revision": "2024-09-15"
}`

/** Plantilla de estructura JSON para estado sanitario (sin valores). */
export const TREE_HEALTH_STATUS_JSON_TEMPLATE = `{
  "valoracion_general": ,
  "plagas_detectadas": ,
  "lesiones": [
    {
      "tipo": ,
      "descripcion": ,
      "lado": 
    }
  ],
  "ultima_revision": 
}`

export function applyTreeHealthStatusJsonTemplateIfEmpty(draft: TreeEnrichmentFormDraft): void {
  if (!isJsonObjectFieldEffectivelyEmpty(draft.healthStatusJson)) {
    return
  }
  draft.healthStatusJson = TREE_HEALTH_STATUS_JSON_TEMPLATE
}

/** Ejemplo JSON visible como placeholder cuando el campo está vacío. */
export const SPECIES_ECOLOGICAL_DATA_JSON_PLACEHOLDER = `{
  "habitat": ["bosque caducifolio", "riberas"],
  "altitudMinM": 0,
  "altitudMaxM": 1500,
  "clima": ["atlántico", "continental"]
}`

/** Ejemplo JSON visible como placeholder cuando el campo está vacío. */
export const SPECIES_REFERENCES_JSON_PLACEHOLDER = `[
  {
    "title": "Flora Ibérica",
    "authors": ["Castroviejo, S."],
    "source": "Real Jardín Botánico, CSIC",
    "year": 1993,
    "url": "https://www.floraiberica.es"
  }
]`

/** Plantilla de estructura JSON para datos ecológicos (sin valores; contrato HTTP, camelCase). */
export const SPECIES_ECOLOGICAL_DATA_JSON_TEMPLATE = `{
  "habitat": ,
  "altitudMinM": ,
  "altitudMaxM": ,
  "clima": 
}`

/** Plantilla de estructura JSON para referencias bibliográficas (sin valores). */
export const SPECIES_REFERENCES_JSON_TEMPLATE = `[
  {
    "title": ,
    "authors": ,
    "source": ,
    "year": ,
    "url": 
  }
]`

export interface SpeciesEnrichmentFormDraft {
  synonymsText: string
  distributionContinents: string
  distributionCountries: string
  distributionDescription: string
  ecologicalDataJson: string
  referencesJson: string
}

export function applySpeciesJsonTemplateIfEmpty(
  draft: SpeciesEnrichmentFormDraft,
  field: 'ecologicalDataJson' | 'referencesJson',
): void {
  if (draft[field].trim().length > 0) {
    return
  }
  draft[field] =
    field === 'ecologicalDataJson'
      ? SPECIES_ECOLOGICAL_DATA_JSON_TEMPLATE
      : SPECIES_REFERENCES_JSON_TEMPLATE
}

export function speciesEnrichmentToFormDraft(
  enrichment?: SpeciesEnrichment | null,
): SpeciesEnrichmentFormDraft {
  if (!enrichment) {
    return {
      synonymsText: '',
      distributionContinents: '',
      distributionCountries: '',
      distributionDescription: '',
      ecologicalDataJson: '',
      referencesJson: '',
    }
  }
  return {
    synonymsText: formatSynonymsLines(enrichment.synonyms),
    distributionContinents: formatTagsComma(enrichment.distribution?.continents),
    distributionCountries: formatTagsComma(enrichment.distribution?.countries),
    distributionDescription: enrichment.distribution?.description ?? '',
    ecologicalDataJson: stringifyJsonField(enrichment.ecologicalData),
    referencesJson: stringifyJsonField(enrichment.references),
  }
}

export type SpeciesEnrichmentDraftBuildResult =
  | { ok: true; payload: SpeciesEnrichmentReplaceRequest }
  | { ok: false; errorKey: 'invalidJson' }

export function buildSpeciesEnrichmentReplaceRequest(
  draft: SpeciesEnrichmentFormDraft,
): SpeciesEnrichmentDraftBuildResult {
  const ecologicalResult = parseJsonObjectField(draft.ecologicalDataJson)
  if (!ecologicalResult.ok) {
    return { ok: false, errorKey: 'invalidJson' }
  }

  const referencesResult = parseJsonArrayField<BibliographicReference>(draft.referencesJson)
  if (!referencesResult.ok) {
    return { ok: false, errorKey: 'invalidJson' }
  }

  const distribution = {
    continents: parseTagsComma(draft.distributionContinents),
    countries: parseTagsComma(draft.distributionCountries),
    description: draft.distributionDescription.trim() || undefined,
  }

  const hasDistribution =
    distribution.continents.length > 0 ||
    distribution.countries.length > 0 ||
    Boolean(distribution.description)

  return {
    ok: true,
    payload: {
      synonyms: parseSynonymsLines(draft.synonymsText),
      distribution: hasDistribution ? distribution : undefined,
      ecologicalData: ecologicalResult.value,
      references: referencesResult.value,
    },
  }
}
