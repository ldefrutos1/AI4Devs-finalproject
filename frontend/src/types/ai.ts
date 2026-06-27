import type {
  BibliographicReference,
  SpeciesDistribution,
  SpeciesEnrichmentReplaceRequest,
} from '@/types/enrichment'

/** Entrada de `POST /api/ai/species/enrichment-suggestions` (HU-016). */
export interface AiSpeciesEnrichmentSuggestionRequest {
  scientificName: string
  commonName: string
}

/** Salida orientativa de la consulta IA (HU-016); forma estructural de SpeciesEnrichmentReplaceRequest; ecologicalData según ADR-0007 regla 10. */
export type AiSpeciesEnrichmentSuggestionResponse = SpeciesEnrichmentReplaceRequest & {
  synonyms?: string[]
  distribution?: SpeciesDistribution
  ecologicalData?: Record<string, unknown>
  references?: BibliographicReference[]
}
