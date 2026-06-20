import { HttpError, NetworkError } from '@/services/http/apiClient'

/** Mensajes de UI (i18n) para errores del flujo de consulta IA (HU-016). */
export interface AiSuggestionErrorMessages {
  networkError: string
  unauthorized: string
  badRequest: string
  forbidden: string
  notFound: string
  unprocessableEntity: string
  badGateway: string
  serviceError: string
  unexpectedError: string
}

/**
 * Convierte errores de `apiFetch` (Problem RFC 9457) en texto para la UI de consulta IA.
 */
export function mapAiSuggestionError(error: unknown, messages: AiSuggestionErrorMessages): string {
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
    if (error.status === 404) {
      return error.problem?.detail?.trim() || messages.notFound
    }
    if (error.status === 422) {
      return error.problem?.detail?.trim() || messages.unprocessableEntity
    }
    if (error.status === 502 || error.status === 503) {
      return error.problem?.detail?.trim() || messages.badGateway
    }
    return error.problem?.detail?.trim() || messages.serviceError
  }

  return messages.unexpectedError
}
