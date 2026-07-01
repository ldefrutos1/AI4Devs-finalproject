import { describe, expect, it } from 'vitest'
import { HttpError, NetworkError } from '@/services/http/apiClient'
import { isAiChatRetryableError, mapAiChatError } from '@/services/ai/aiChatErrors'

const messages = {
  networkError: 'network',
  unauthorized: 'unauthorized',
  badRequest: 'badRequest',
  forbidden: 'forbidden',
  tooManyRequests: 'tooManyRequests',
  badGateway: 'badGateway',
  serviceError: 'service',
  unexpectedError: 'unexpected',
}

describe('mapAiChatError', () => {
  it('mapea NetworkError', () => {
    expect(mapAiChatError(new NetworkError(), messages)).toBe('network')
  })

  it('mapea 401 al mensaje de unauthorized', () => {
    expect(mapAiChatError(new HttpError(401), messages)).toBe('unauthorized')
  })

  it('mapea 403 con detalle Problem Details', () => {
    const error = new HttpError(403, {
      title: 'Forbidden',
      status: 403,
      detail: 'Rol no autorizado para chat',
    })
    expect(mapAiChatError(error, messages)).toBe('Rol no autorizado para chat')
  })

  it('mapea 429 con detalle Problem Details', () => {
    const error = new HttpError(429, {
      title: 'Too Many Requests',
      status: 429,
      detail: 'Espera unos segundos antes de enviar otro mensaje.',
    })
    expect(mapAiChatError(error, messages)).toBe('Espera unos segundos antes de enviar otro mensaje.')
  })

  it('mapea 429 sin detalle al mensaje de tooManyRequests', () => {
    expect(mapAiChatError(new HttpError(429), messages)).toBe('tooManyRequests')
  })

  it('mapea 502 al mensaje de badGateway', () => {
    expect(mapAiChatError(new HttpError(502), messages)).toBe('badGateway')
  })

  it('mapea 502 con detalle Problem Details', () => {
    const error = new HttpError(502, {
      title: 'Bad Gateway',
      status: 502,
      detail: 'Proveedor IA no disponible',
    })
    expect(mapAiChatError(error, messages)).toBe('Proveedor IA no disponible')
  })
})

describe('isAiChatRetryableError', () => {
  it('devuelve true solo para HttpError 502', () => {
    expect(isAiChatRetryableError(new HttpError(502))).toBe(true)
    expect(isAiChatRetryableError(new HttpError(429))).toBe(false)
    expect(isAiChatRetryableError(new HttpError(401))).toBe(false)
    expect(isAiChatRetryableError(new NetworkError())).toBe(false)
    expect(isAiChatRetryableError(new Error('otro'))).toBe(false)
  })
})
