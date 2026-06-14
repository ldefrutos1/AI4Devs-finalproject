/** Tipos alineados con OpenAPI (HU-015): enriquecimiento Mongo vía catalog-service. */

export interface SpeciesDistribution {
  continents?: string[]
  countries?: string[]
  description?: string
}

export interface BibliographicReference {
  title?: string
  authors?: string[]
  source?: string
  year?: number
  url?: string
}

/** Respuesta de `GET`/`PUT` `/api/catalog/species/{speciesId}/enrichment`. */
export interface SpeciesEnrichment {
  speciesId: number
  scientificName?: string
  commonName?: string
  synonyms?: string[]
  distribution?: SpeciesDistribution
  ecologicalData?: Record<string, unknown>
  references?: BibliographicReference[]
}

/** Cuerpo de `PUT` `/api/catalog/species/{speciesId}/enrichment`. */
export interface SpeciesEnrichmentReplaceRequest {
  synonyms?: string[]
  distribution?: SpeciesDistribution
  ecologicalData?: Record<string, unknown>
  references?: BibliographicReference[]
}

/** Medidas físicas del ejemplar; claves en camelCase según contrato HTTP. */
export type TreeMeasurements = Record<string, number>

export interface FieldObservation {
  date?: string
  text?: string
  author?: string
  conditions?: Record<string, unknown>
}

/** Respuesta de `GET`/`PUT` `/api/catalog/trees/{treeId}/enrichment`. */
export interface TreeEnrichment {
  treeId: number
  speciesId?: number
  measurements?: TreeMeasurements
  healthStatus?: Record<string, unknown>
  tags?: string[]
  observations?: FieldObservation[]
}

/** Cuerpo de `PUT` `/api/catalog/trees/{treeId}/enrichment`. */
export interface TreeEnrichmentReplaceRequest {
  measurements?: TreeMeasurements
  healthStatus?: Record<string, unknown>
  tags?: string[]
  observations?: FieldObservation[]
}

/** Respuesta de `GET` `/api/catalog/public/trees/{treeId}/enrichment`. */
export interface PublicTreeEnrichment {
  speciesEnrichment?: SpeciesEnrichment | null
  treeEnrichment?: TreeEnrichment | null
}
