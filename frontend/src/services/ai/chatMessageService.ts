import { apiFetch } from '@/services/http/apiClient'
import type { AiChatMessageRequest, AiChatMessageResponse } from '@/types/ai'

const AI_CHAT_MESSAGES_PATH = '/api/ai/chat/messages'

/**
 * Envía un turno de chat al asistente IA (HU-010).
 * Petición autenticada vía gateway; soporta cancelación con `AbortSignal`.
 */
export async function sendChatMessage(
  payload: AiChatMessageRequest,
  signal?: AbortSignal,
): Promise<AiChatMessageResponse> {
  return apiFetch<AiChatMessageResponse>(AI_CHAT_MESSAGES_PATH, {
    method: 'POST',
    body: JSON.stringify(payload),
    signal,
  })
}
