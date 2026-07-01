import { beforeEach, describe, expect, it, vi } from 'vitest'
import { sendChatMessage } from '@/services/ai/chatMessageService'
import { apiFetch, HttpError } from '@/services/http/apiClient'

vi.mock('@/services/http/apiClient', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/services/http/apiClient')>()
  return {
    ...actual,
    apiFetch: vi.fn(),
  }
})

const apiFetchMock = vi.mocked(apiFetch)

describe('chatMessageService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('sendChatMessage envía POST autenticado al endpoint de chat IA', async () => {
    const conversationId = '550e8400-e29b-41d4-a716-446655440000'
    const response = {
      conversationId,
      message: {
        role: 'assistant' as const,
        content: 'Respuesta orientativa del asistente.',
        createdAt: '2026-07-01T10:00:00Z',
      },
    }
    apiFetchMock.mockResolvedValueOnce(response)

    const payload = {
      conversationId,
      treeId: 42,
      messages: [{ role: 'user' as const, content: '¿Qué datos necesito para esta ficha?' }],
    }

    const result = await sendChatMessage(payload, new AbortController().signal)

    expect(result).toEqual(response)
    expect(apiFetchMock).toHaveBeenCalledWith('/api/ai/chat/messages', {
      method: 'POST',
      body: JSON.stringify(payload),
      signal: expect.any(AbortSignal),
    })
  })

  it('sendChatMessage reenvía el hilo acumulado en el cuerpo', async () => {
    const conversationId = '550e8400-e29b-41d4-a716-446655440001'
    const payload = {
      conversationId,
      treeId: 7,
      messages: [
        { role: 'user' as const, content: 'Hola' },
        { role: 'assistant' as const, content: 'Hola, ¿en qué puedo ayudarte?' },
        { role: 'user' as const, content: '¿Cómo registro coordenadas?' },
      ],
    }
    apiFetchMock.mockResolvedValueOnce({
      conversationId,
      message: {
        role: 'assistant',
        content: 'Puedes usar el mapa en la ficha.',
        createdAt: '2026-07-01T10:01:00Z',
      },
    })

    await sendChatMessage(payload)

    expect(apiFetchMock).toHaveBeenCalledWith('/api/ai/chat/messages', {
      method: 'POST',
      body: JSON.stringify(payload),
      signal: undefined,
    })
  })

  it('propaga HttpError 401 del cliente HTTP', async () => {
    apiFetchMock.mockRejectedValueOnce(new HttpError(401))

    await expect(
      sendChatMessage({
        conversationId: '550e8400-e29b-41d4-a716-446655440000',
        treeId: 42,
        messages: [{ role: 'user', content: 'Hola' }],
      }),
    ).rejects.toBeInstanceOf(HttpError)
  })

  it('propaga HttpError 502 del cliente HTTP', async () => {
    apiFetchMock.mockRejectedValueOnce(new HttpError(502))

    await expect(
      sendChatMessage({
        conversationId: '550e8400-e29b-41d4-a716-446655440000',
        treeId: 42,
        messages: [{ role: 'user', content: 'Hola' }],
      }),
    ).rejects.toMatchObject({ status: 502 })
  })
})
