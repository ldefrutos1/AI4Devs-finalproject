import { describe, expect, it } from 'vitest'
import {
  buildTreeEnrichmentSummaryParts,
  resolveSpeciesEnrichmentContentStatus,
  speciesEnrichmentHasDisplayContent,
  treeEnrichmentHasDisplayContent,
} from '@/composables/enrichmentSummaries'

describe('enrichmentSummaries', () => {
  it('detecta contenido ampliado de especie', () => {
    expect(speciesEnrichmentHasDisplayContent(null)).toBe(false)
    expect(
      speciesEnrichmentHasDisplayContent({ speciesId: 1, synonyms: ['Encina'] }),
    ).toBe(true)
    expect(
      speciesEnrichmentHasDisplayContent({
        speciesId: 1,
        distribution: { countries: ['España'] },
      }),
    ).toBe(true)
    expect(speciesEnrichmentHasDisplayContent({ speciesId: 1 })).toBe(false)
  })

  it('resuelve estado de badge de especie', () => {
    expect(
      resolveSpeciesEnrichmentContentStatus(null, { speciesSelected: false }),
    ).toBe('unknown')
    expect(
      resolveSpeciesEnrichmentContentStatus(
        { speciesId: 1, synonyms: ['Encina'] },
        { speciesSelected: true },
      ),
    ).toBe('hasData')
    expect(
      resolveSpeciesEnrichmentContentStatus({ speciesId: 1 }, { speciesSelected: true }),
    ).toBe('empty')
  })

  it('construye partes del resumen del ejemplar', () => {
    expect(
      buildTreeEnrichmentSummaryParts({
        measurements: { heightM: 12, crownDiameterM: 8 },
        tags: ['monumental', 'ribera'],
        healthStatus: { valoracion_general: 'bueno' },
        observations: [{ text: 'Nota' }],
      }),
    ).toEqual([
      { key: 'measurements', count: 2 },
      { key: 'tags', count: 2 },
      { key: 'healthStatus', count: 1 },
      { key: 'observations', count: 1 },
    ])
    expect(treeEnrichmentHasDisplayContent(null)).toBe(false)
    expect(
      treeEnrichmentHasDisplayContent({
        tags: ['singular'],
        measurements: {},
        healthStatus: {},
        observations: [],
      }),
    ).toBe(true)
  })
})
