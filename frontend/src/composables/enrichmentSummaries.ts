import type { SpeciesEnrichment, TreeEnrichmentReplaceRequest } from '@/types/enrichment'

export type SpeciesEnrichmentContentStatus = 'unknown' | 'empty' | 'hasData'

export interface TreeEnrichmentSummaryCounts {
  measurements: number
  tags: number
  observations: number
  hasHealthStatus: boolean
}

export function speciesEnrichmentHasDisplayContent(
  enrichment?: SpeciesEnrichment | null,
): boolean {
  if (!enrichment) {
    return false
  }
  const distribution = enrichment.distribution
  const hasDistribution = Boolean(
    distribution?.continents?.length ||
      distribution?.countries?.length ||
      distribution?.description?.trim(),
  )
  const hasEcological =
    enrichment.ecologicalData != null &&
    Object.keys(enrichment.ecologicalData).length > 0

  return Boolean(
    enrichment.synonyms?.length ||
      hasDistribution ||
      hasEcological ||
      enrichment.references?.length,
  )
}

export function resolveSpeciesEnrichmentContentStatus(
  enrichment: SpeciesEnrichment | null | undefined,
  options?: { loading?: boolean; speciesSelected?: boolean },
): SpeciesEnrichmentContentStatus {
  if (options?.loading) {
    return 'unknown'
  }
  if (!options?.speciesSelected) {
    return 'unknown'
  }
  if (!enrichment) {
    return 'unknown'
  }
  return speciesEnrichmentHasDisplayContent(enrichment) ? 'hasData' : 'empty'
}

export function countTreeEnrichmentSummary(
  payload?: TreeEnrichmentReplaceRequest | null,
): TreeEnrichmentSummaryCounts {
  if (!payload) {
    return { measurements: 0, tags: 0, observations: 0, hasHealthStatus: false }
  }
  return {
    measurements: Object.keys(payload.measurements ?? {}).length,
    tags: payload.tags?.length ?? 0,
    observations: payload.observations?.length ?? 0,
    hasHealthStatus: Object.keys(payload.healthStatus ?? {}).length > 0,
  }
}

export function treeEnrichmentHasDisplayContent(
  payload?: TreeEnrichmentReplaceRequest | null,
): boolean {
  const counts = countTreeEnrichmentSummary(payload)
  return (
    counts.measurements > 0 ||
    counts.tags > 0 ||
    counts.observations > 0 ||
    counts.hasHealthStatus
  )
}

export interface TreeEnrichmentSummaryPart {
  key: 'measurements' | 'tags' | 'observations' | 'healthStatus'
  count: number
}

export function buildTreeEnrichmentSummaryParts(
  payload?: TreeEnrichmentReplaceRequest | null,
): TreeEnrichmentSummaryPart[] {
  const counts = countTreeEnrichmentSummary(payload)
  const parts: TreeEnrichmentSummaryPart[] = []

  if (counts.measurements > 0) {
    parts.push({ key: 'measurements', count: counts.measurements })
  }
  if (counts.tags > 0) {
    parts.push({ key: 'tags', count: counts.tags })
  }
  if (counts.hasHealthStatus) {
    parts.push({ key: 'healthStatus', count: 1 })
  }
  if (counts.observations > 0) {
    parts.push({ key: 'observations', count: counts.observations })
  }

  return parts
}
