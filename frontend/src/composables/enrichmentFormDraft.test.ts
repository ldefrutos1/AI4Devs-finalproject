import { describe, expect, it } from 'vitest'
import {
  applySpeciesJsonTemplateIfEmpty,
  applyTreeHealthStatusJsonTemplateIfEmpty,
  buildSpeciesEnrichmentReplaceRequest,
  buildTreeEnrichmentReplaceRequest,
  parseSynonymsLines,
  parseTagsComma,
  resolveTreeEnrichmentDraftState,
  speciesEnrichmentToFormDraft,
  treeEnrichmentToFormDraft,
} from '@/composables/enrichmentFormDraft'

describe('enrichmentFormDraft', () => {
  it('parsea sinónimos y etiquetas desde texto', () => {
    expect(parseSynonymsLines('  A \n\nB ')).toEqual(['A', 'B'])
    expect(parseTagsComma(' monumental , protegido , ')).toEqual(['monumental', 'protegido'])
  })

  it('construye borrador de ejemplar con medidas válidas', () => {
    const draft = treeEnrichmentToFormDraft({
      measurements: { heightM: 18.5 },
      tags: ['singular'],
      healthStatus: { valoracion_general: 'bueno' },
      observations: [{ date: '2024-01-01', text: 'Nota', author: 'Ana' }],
    })

    const result = buildTreeEnrichmentReplaceRequest({
      ...draft,
      measurementInputs: { ...draft.measurementInputs, heightM: '20' },
    })

    expect(result.ok).toBe(true)
    if (result.ok) {
      expect(result.payload.measurements?.heightM).toBe(20)
      expect(result.payload.tags).toEqual(['singular'])
    }
  })

  it('rechaza medidas no finitas en ejemplar', () => {
    const draft = treeEnrichmentToFormDraft(null)
    const result = buildTreeEnrichmentReplaceRequest({
      ...draft,
      measurementInputs: { ...draft.measurementInputs, heightM: 'no-numeric' },
    })

    expect(result).toEqual({ ok: false, errorKey: 'invalidMeasurement' })
  })

  it('construye borrador de especie desde enriquecimiento', () => {
    const draft = speciesEnrichmentToFormDraft({
      speciesId: 12,
      scientificName: 'Quercus ilex',
      synonyms: ['Encina'],
      distribution: { continents: ['Europa'], countries: ['España'], description: 'Península' },
      ecologicalData: { habitat: ['bosque'] },
      references: [{ title: 'Flora Ibérica', year: 1993 }],
    })

    const result = buildSpeciesEnrichmentReplaceRequest(draft)

    expect(result.ok).toBe(true)
    if (result.ok) {
      expect(result.payload.synonyms).toEqual(['Encina'])
      expect(result.payload.distribution?.countries).toEqual(['España'])
      expect(result.payload.references?.[0]?.title).toBe('Flora Ibérica')
    }
  })

  it('resolveTreeEnrichmentDraftState detecta cambios semánticos respecto a la baseline', () => {
    const baseline = {
      tags: ['singular'],
      measurements: {},
      healthStatus: {},
      observations: [],
    }
    const draft = treeEnrichmentToFormDraft(baseline)

    expect(resolveTreeEnrichmentDraftState(draft, baseline)).toEqual({ dirty: false, valid: true })

    draft.measurementInputs.heightM = '12'
    expect(resolveTreeEnrichmentDraftState(draft, baseline)).toEqual({ dirty: true, valid: true })

    draft.healthStatusJson = '{ invalid'
    expect(resolveTreeEnrichmentDraftState(draft, baseline)).toEqual({
      dirty: true,
      valid: false,
      errorKey: 'invalidJson',
    })
  })

  it('resolveTreeEnrichmentDraftState ignora la plantilla JSON sin editar del estado sanitario', () => {
    const baseline = {
      tags: [],
      measurements: {},
      healthStatus: {},
      observations: [],
    }
    const draft = treeEnrichmentToFormDraft(baseline)

    applyTreeHealthStatusJsonTemplateIfEmpty(draft)

    expect(resolveTreeEnrichmentDraftState(draft, baseline)).toEqual({ dirty: false, valid: true })
  })

  it('aplica plantilla de estado sanitario solo si el campo está vacío', () => {
    const draft = treeEnrichmentToFormDraft({ healthStatus: {}, measurements: {}, observations: [] })
    expect(draft.healthStatusJson).toBe('')

    applyTreeHealthStatusJsonTemplateIfEmpty(draft)
    expect(draft.healthStatusJson).toContain('"valoracion_general":')
    expect(draft.healthStatusJson).not.toContain('"bueno"')

    draft.healthStatusJson = '{ "valoracion_general": "regular" }'
    applyTreeHealthStatusJsonTemplateIfEmpty(draft)
    expect(draft.healthStatusJson).toBe('{ "valoracion_general": "regular" }')
  })

  it('aplica plantilla JSON solo en campos vacíos al enfocar', () => {
    const draft = speciesEnrichmentToFormDraft(null)

    applySpeciesJsonTemplateIfEmpty(draft, 'ecologicalDataJson')
    expect(draft.ecologicalDataJson).toContain('"habitat":')
    expect(draft.ecologicalDataJson).not.toContain('bosque')

    applySpeciesJsonTemplateIfEmpty(draft, 'referencesJson')
    expect(draft.referencesJson).toContain('"title":')

    draft.ecologicalDataJson = '{ "habitat": ["ribera"] }'
    applySpeciesJsonTemplateIfEmpty(draft, 'ecologicalDataJson')
    expect(draft.ecologicalDataJson).toBe('{ "habitat": ["ribera"] }')
  })

  it('rechaza JSON inválido en datos ecológicos', () => {
    const draft = speciesEnrichmentToFormDraft(null)
    const result = buildSpeciesEnrichmentReplaceRequest({
      ...draft,
      ecologicalDataJson: '{ invalid',
    })

    expect(result).toEqual({ ok: false, errorKey: 'invalidJson' })
  })
})
