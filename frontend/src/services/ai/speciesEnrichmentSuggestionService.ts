import { apiFetch } from '@/services/http/apiClient'
import type {
  AiSpeciesEnrichmentSuggestionRequest,
  AiSpeciesEnrichmentSuggestionResponse,
} from '@/types/ai'

const AI_SPECIES_ENRICHMENT_SUGGESTIONS = '/api/ai/species/enrichment-suggestions'

export async function requestSpeciesEnrichmentSuggestion(
  payload: AiSpeciesEnrichmentSuggestionRequest,
  signal?: AbortSignal,
): Promise<AiSpeciesEnrichmentSuggestionResponse> {
  return apiFetch<AiSpeciesEnrichmentSuggestionResponse>(AI_SPECIES_ENRICHMENT_SUGGESTIONS, {
    method: 'POST',
    body: JSON.stringify(payload),
    signal,
  })
}
