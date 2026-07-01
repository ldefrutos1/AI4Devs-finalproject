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

/** Rol de un turno conversacional (`POST /api/ai/chat/messages`, HU-010). */
export type ChatRole = 'user' | 'assistant'

/** Un turno del hilo conversacional (HU-010). */
export interface AiChatTurn {
  role: ChatRole
  content: string
}

/** Entrada de `POST /api/ai/chat/messages` (HU-010). */
export interface AiChatMessageRequest {
  conversationId: string
  treeId: number
  messages: AiChatTurn[]
}

/** Respuesta del asistente para un turno procesado (HU-010). */
export interface AiChatAssistantMessage {
  role: 'assistant'
  content: string
  createdAt: string
}

/** Salida de `POST /api/ai/chat/messages` (HU-010). */
export interface AiChatMessageResponse {
  conversationId: string
  message: AiChatAssistantMessage
}

/** Máximo de turnos por petición (OpenAPI HU-010). */
export const AI_CHAT_MAX_MESSAGES = 20

/** Máximo de caracteres por `content` (OpenAPI HU-010). */
export const AI_CHAT_MAX_CONTENT_LENGTH = 2000
