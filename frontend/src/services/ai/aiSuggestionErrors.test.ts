import { describe, expect, it } from 'vitest'
import { HttpError, NetworkError } from '@/services/http/apiClient'
import { mapAiSuggestionError } from '@/services/ai/aiSuggestionErrors'

const messages = {
  networkError: 'network',
  unauthorized: 'unauthorized',
  badRequest: 'badRequest',
  forbidden: 'forbidden',
  notFound: 'notFound',
  unprocessableEntity: 'unprocessable',
  badGateway: 'badGateway',
  serviceError: 'service',
  unexpectedError: 'unexpected',
}

describe('mapAiSuggestionError', () => {
  it('mapea NetworkError', () => {
    expect(mapAiSuggestionError(new NetworkError(), messages)).toBe('network')
  })

  it('mapea 422 con detalle Problem Details', () => {
    const error = new HttpError(422, {
      title: 'Respuesta IA inválida',
      status: 422,
      detail: 'JSON IA inválido',
    })
    expect(mapAiSuggestionError(error, messages)).toBe('JSON IA inválido')
  })

  it('mapea 502 al mensaje de badGateway', () => {
    expect(mapAiSuggestionError(new HttpError(502), messages)).toBe('badGateway')
  })
})
