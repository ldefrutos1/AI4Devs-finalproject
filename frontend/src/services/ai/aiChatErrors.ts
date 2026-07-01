import { HttpError, NetworkError } from '@/services/http/apiClient'

/** Mensajes de UI (i18n) para errores del flujo de chat IA (HU-010). */
export interface AiChatErrorMessages {
  networkError: string
  unauthorized: string
  badRequest: string
  forbidden: string
  tooManyRequests: string
  badGateway: string
  serviceError: string
  unexpectedError: string
}

/**
 * Convierte errores de `apiFetch` (Problem RFC 9457) en texto para la UI de chat IA.
 */
export function mapAiChatError(error: unknown, messages: AiChatErrorMessages): string {
  if (error instanceof NetworkError) {
    return messages.networkError
  }

  if (error instanceof HttpError) {
    if (error.status === 401) {
      return messages.unauthorized
    }
    if (error.status === 400) {
      return error.problem?.detail?.trim() || messages.badRequest
    }
    if (error.status === 403) {
      return error.problem?.detail?.trim() || messages.forbidden
    }
    if (error.status === 429) {
      return error.problem?.detail?.trim() || messages.tooManyRequests
    }
    if (error.status === 502 || error.status === 503) {
      return error.problem?.detail?.trim() || messages.badGateway
    }
    return error.problem?.detail?.trim() || messages.serviceError
  }

  return messages.unexpectedError
}

/** Indica si el error admite reintento manual del mismo turno (refinamiento HU-010: solo 502). */
export function isAiChatRetryableError(error: unknown): boolean {
  return error instanceof HttpError && error.status === 502
}
