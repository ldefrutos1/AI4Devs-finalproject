import { HttpError, NetworkError } from '@/services/http/apiClient'

/** Mensajes de UI (i18n) para errores del flujo de enriquecimiento (HU-015). */
export interface EnrichmentErrorMessages {
  networkError: string
  unauthorized: string
  badRequest: string
  forbidden: string
  notFound: string
  badGateway: string
  serviceError: string
  unexpectedError: string
}

function isEnrichmentDownstreamMessage(error: HttpError): boolean {
  if (error.status === 502 || error.status === 503) {
    return true
  }
  if (error.status !== 500) {
    return false
  }
  const raw = error.problem as Record<string, unknown> | undefined
  const blob = [error.problem?.detail, raw?.message].filter((v) => typeof v === 'string').join(' ')
  return blob.includes('Connection refused')
}

/**
 * Convierte errores de `apiFetch` / `publicApiFetch` (Problem RFC 9457) en texto para la UI.
 * Los textos provienen del llamador (i18n); este módulo no hardcodea copy en español.
 */
export function mapEnrichmentError(error: unknown, messages: EnrichmentErrorMessages): string {
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
    if (isEnrichmentDownstreamMessage(error)) {
      return messages.badGateway
    }
    return error.problem?.detail?.trim() || messages.serviceError
  }

  return messages.unexpectedError
}
