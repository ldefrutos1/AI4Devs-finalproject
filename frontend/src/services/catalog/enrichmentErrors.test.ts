import { describe, expect, it } from 'vitest'
import { HttpError, NetworkError } from '@/services/http/apiClient'
import { mapEnrichmentError } from '@/services/catalog/enrichmentErrors'

const messages = {
  networkError: 'NET',
  unauthorized: 'AUTH',
  badRequest: 'BAD',
  forbidden: 'FORBIDDEN',
  notFound: 'NOT_FOUND',
  badGateway: 'GATEWAY',
  serviceError: 'SERVICE',
  unexpectedError: 'UNEXPECTED',
}

describe('mapEnrichmentError', () => {
  it('mapea NetworkError', () => {
    expect(mapEnrichmentError(new NetworkError(), messages)).toBe('NET')
  })

  it('usa detail de Problem en 400 (p. ej. measurements inválidas)', () => {
    expect(
      mapEnrichmentError(
        new HttpError(400, {
          title: 'Bad Request',
          status: 400,
          detail: 'measurements.heightM must be a finite number',
        }),
        messages,
      ),
    ).toBe('measurements.heightM must be a finite number')
  })

  it('mapea 403 y 404 con fallback', () => {
    expect(
      mapEnrichmentError(new HttpError(403, { title: 'Forbidden', status: 403 }), messages),
    ).toBe('FORBIDDEN')
    expect(
      mapEnrichmentError(new HttpError(404, { title: 'Not Found', status: 404 }), messages),
    ).toBe('NOT_FOUND')
  })

  it('detecta servicio no disponible en 502', () => {
    expect(
      mapEnrichmentError(new HttpError(502, { title: 'Bad Gateway', status: 502 }), messages),
    ).toBe('GATEWAY')
  })

  it('devuelve unexpectedError para errores desconocidos', () => {
    expect(mapEnrichmentError(new Error('boom'), messages)).toBe('UNEXPECTED')
  })
})
