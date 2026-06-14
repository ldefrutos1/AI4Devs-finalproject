import { apiFetch, publicApiFetch } from '@/services/http/apiClient'
import type {
  PublicTreeEnrichment,
  SpeciesEnrichment,
  SpeciesEnrichmentReplaceRequest,
  TreeEnrichment,
  TreeEnrichmentReplaceRequest,
} from '@/types/enrichment'

const CATALOG = '/api/catalog'

export async function fetchSpeciesEnrichment(
  speciesId: number,
  signal?: AbortSignal,
): Promise<SpeciesEnrichment> {
  return apiFetch<SpeciesEnrichment>(`${CATALOG}/species/${speciesId}/enrichment`, { signal })
}

export async function updateSpeciesEnrichment(
  speciesId: number,
  payload: SpeciesEnrichmentReplaceRequest,
  signal?: AbortSignal,
): Promise<SpeciesEnrichment> {
  return apiFetch<SpeciesEnrichment>(`${CATALOG}/species/${speciesId}/enrichment`, {
    method: 'PUT',
    body: JSON.stringify(payload),
    signal,
  })
}

export async function fetchTreeEnrichment(
  treeId: number,
  signal?: AbortSignal,
): Promise<TreeEnrichment> {
  return apiFetch<TreeEnrichment>(`${CATALOG}/trees/${treeId}/enrichment`, { signal })
}

export async function updateTreeEnrichment(
  treeId: number,
  payload: TreeEnrichmentReplaceRequest,
  signal?: AbortSignal,
): Promise<TreeEnrichment> {
  return apiFetch<TreeEnrichment>(`${CATALOG}/trees/${treeId}/enrichment`, {
    method: 'PUT',
    body: JSON.stringify(payload),
    signal,
  })
}

export async function fetchPublicTreeEnrichment(
  treeId: number,
  signal?: AbortSignal,
): Promise<PublicTreeEnrichment> {
  return publicApiFetch<PublicTreeEnrichment>(`${CATALOG}/public/trees/${treeId}/enrichment`, {
    signal,
  })
}
