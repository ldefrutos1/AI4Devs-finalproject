import { computed, createApp, nextTick, ref } from 'vue'
import { createI18n } from 'vue-i18n'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { es } from '@/i18n/locales/es'
import { useTreeChat } from '@/composables/useTreeChat'
import { AI_CHAT_MAX_CONTENT_LENGTH, AI_CHAT_MAX_MESSAGES } from '@/types/ai'

vi.mock('@/services/ai/chatMessageService', () => ({
  sendChatMessage: vi.fn(),
}))

import { sendChatMessage } from '@/services/ai/chatMessageService'
import { HttpError, NetworkError } from '@/services/http/apiClient'

const CONVERSATION_ID = '550e8400-e29b-41d4-a716-446655440000'

function mountTreeChat(initialTreeId: number | null = 42) {
  const treeId = ref(initialTreeId)
  let api!: ReturnType<typeof useTreeChat>
  const app = createApp({
    setup() {
      api = useTreeChat({ treeId: computed(() => treeId.value) })
      return () => null
    },
  })
  app.use(createI18n({ legacy: false, locale: 'es', messages: { es } }))
  app.mount(document.createElement('div'))
  return { api, treeId, unmount: () => app.unmount() }
}

describe('useTreeChat', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.stubGlobal('crypto', {
      randomUUID: vi.fn(() => CONVERSATION_ID),
    })
    vi.mocked(sendChatMessage).mockResolvedValue({
      conversationId: CONVERSATION_ID,
      message: {
        role: 'assistant',
        content: 'Respuesta orientativa.',
        createdAt: '2026-07-01T10:00:00Z',
      },
    })
  })

  it('openChat genera conversationId y deja el hilo vacío', () => {
    const { api } = mountTreeChat()

    api.openChat()

    expect(api.isOpen.value).toBe(true)
    expect(api.conversationId.value).toBe(CONVERSATION_ID)
    expect(api.messages.value).toEqual([])
    expect(api.draft.value).toBe('')
  })

  it('closeChat reinicia el hilo y conversationId', async () => {
    const { api } = mountTreeChat()
    api.openChat()
    api.draft.value = 'Hola'
    await api.sendMessage()

    api.closeChat()

    expect(api.isOpen.value).toBe(false)
    expect(api.conversationId.value).toBeNull()
    expect(api.messages.value).toEqual([])
    expect(api.draft.value).toBe('')
  })

  it('sendMessage acumula turnos user y assistant en el hilo', async () => {
    const { api } = mountTreeChat()
    api.openChat()
    api.draft.value = '¿Qué datos necesito?'

    await api.sendMessage()

    expect(api.messages.value).toEqual([
      { role: 'user', content: '¿Qué datos necesito?' },
      { role: 'assistant', content: 'Respuesta orientativa.' },
    ])
    expect(api.draft.value).toBe('')
    expect(api.error.value).toBe('')
  })

  it('sendMessage reenvía el hilo acumulado en cada turno', async () => {
    const { api } = mountTreeChat()
    api.openChat()

    api.draft.value = 'Primer mensaje'
    await api.sendMessage()

    vi.mocked(sendChatMessage).mockResolvedValueOnce({
      conversationId: CONVERSATION_ID,
      message: {
        role: 'assistant',
        content: 'Segunda respuesta.',
        createdAt: '2026-07-01T10:01:00Z',
      },
    })

    api.draft.value = 'Segundo mensaje'
    await api.sendMessage()

    expect(sendChatMessage).toHaveBeenLastCalledWith(
      {
        conversationId: CONVERSATION_ID,
        treeId: 42,
        messages: [
          { role: 'user', content: 'Primer mensaje' },
          { role: 'assistant', content: 'Respuesta orientativa.' },
          { role: 'user', content: 'Segundo mensaje' },
        ],
      },
      expect.any(AbortSignal),
    )
    expect(api.messages.value).toHaveLength(4)
  })

  it('ignora doble envío mientras hay petición en vuelo', async () => {
    let resolveSend!: (value: Awaited<ReturnType<typeof sendChatMessage>>) => void
    vi.mocked(sendChatMessage).mockImplementationOnce(
      () =>
        new Promise((resolve) => {
          resolveSend = resolve
        }),
    )

    const { api } = mountTreeChat()
    api.openChat()
    api.draft.value = 'Hola'

    const first = api.sendMessage()
    expect(api.isLoading.value).toBe(true)
    expect(api.canSendMessage.value).toBe(false)

    api.draft.value = 'Otro mensaje'
    await api.sendMessage()

    resolveSend({
      conversationId: CONVERSATION_ID,
      message: {
        role: 'assistant',
        content: 'Ok',
        createdAt: '2026-07-01T10:00:00Z',
      },
    })
    await first

    expect(sendChatMessage).toHaveBeenCalledTimes(1)
    expect(api.messages.value.filter((m) => m.role === 'user')).toHaveLength(1)
  })

  it('no envía mensajes vacíos o solo espacios', async () => {
    const { api } = mountTreeChat()
    api.openChat()
    api.draft.value = '   '

    await api.sendMessage()

    expect(sendChatMessage).not.toHaveBeenCalled()
    expect(api.canSendMessage.value).toBe(false)
  })

  it('bloquea envío cuando el hilo alcanza el máximo de turnos', async () => {
    const { api } = mountTreeChat()
    api.openChat()

    const fullThread = Array.from({ length: AI_CHAT_MAX_MESSAGES }, (_, index) => ({
      role: (index % 2 === 0 ? 'user' : 'assistant') as 'user' | 'assistant',
      content: `Turno ${index + 1}`,
    }))
    api.messages.value = [...fullThread]

    api.draft.value = 'Uno más'
    expect(api.isAtThreadLimit.value).toBe(true)
    expect(api.canSendMessage.value).toBe(false)

    await api.sendMessage()
    expect(sendChatMessage).not.toHaveBeenCalled()
  })

  it('respeta maxlength de contenido en canSendMessage', () => {
    const { api } = mountTreeChat()
    api.openChat()

    api.draft.value = 'a'.repeat(AI_CHAT_MAX_CONTENT_LENGTH)
    expect(api.canSendMessage.value).toBe(true)

    api.draft.value = 'a'.repeat(AI_CHAT_MAX_CONTENT_LENGTH + 1)
    expect(api.canSendMessage.value).toBe(false)
  })

  it('retryLastTurn reintenta el mismo turno tras 502 sin duplicar el mensaje user', async () => {
    vi.mocked(sendChatMessage)
      .mockRejectedValueOnce(new HttpError(502, { title: 'Bad Gateway', status: 502 }))
      .mockResolvedValueOnce({
        conversationId: CONVERSATION_ID,
        message: {
          role: 'assistant',
          content: 'Recuperado.',
          createdAt: '2026-07-01T10:02:00Z',
        },
      })

    const { api } = mountTreeChat()
    api.openChat()
    api.draft.value = 'Mensaje con fallo'

    await api.sendMessage()

    expect(api.canRetry.value).toBe(true)
    expect(api.messages.value).toEqual([{ role: 'user', content: 'Mensaje con fallo' }])

    await api.retryLastTurn()

    expect(sendChatMessage).toHaveBeenCalledTimes(2)
    expect(sendChatMessage).toHaveBeenLastCalledWith(
      {
        conversationId: CONVERSATION_ID,
        treeId: 42,
        messages: [{ role: 'user', content: 'Mensaje con fallo' }],
      },
      expect.any(AbortSignal),
    )
    expect(api.messages.value).toEqual([
      { role: 'user', content: 'Mensaje con fallo' },
      { role: 'assistant', content: 'Recuperado.' },
    ])
    expect(api.canRetry.value).toBe(false)
  })

  it('bloquea un nuevo envío mientras el último turno user sigue pendiente de respuesta', async () => {
    vi.mocked(sendChatMessage).mockRejectedValueOnce(
      new HttpError(429, {
        title: 'Too Many Requests',
        status: 429,
        detail: 'Espera antes de enviar otro mensaje.',
      }),
    )

    const { api } = mountTreeChat()
    api.openChat()
    api.draft.value = 'Primer mensaje'

    await api.sendMessage()

    expect(api.messages.value).toEqual([{ role: 'user', content: 'Primer mensaje' }])
    expect(api.canSendMessage.value).toBe(false)

    api.draft.value = 'Segundo mensaje'
    await api.sendMessage()

    expect(sendChatMessage).toHaveBeenCalledTimes(1)
    expect(api.messages.value).toEqual([{ role: 'user', content: 'Primer mensaje' }])
  })

  it('vuelve a permitir envío tras resolver el turno pendiente con retry 502', async () => {
    vi.mocked(sendChatMessage)
      .mockRejectedValueOnce(new HttpError(502, { title: 'Bad Gateway', status: 502 }))
      .mockResolvedValueOnce({
        conversationId: CONVERSATION_ID,
        message: {
          role: 'assistant',
          content: 'Recuperado.',
          createdAt: '2026-07-01T10:02:00Z',
        },
      })

    const { api } = mountTreeChat()
    api.openChat()
    api.draft.value = 'Mensaje con fallo'

    await api.sendMessage()
    expect(api.canSendMessage.value).toBe(false)

    await api.retryLastTurn()
    expect(api.canSendMessage.value).toBe(false)

    api.draft.value = 'Siguiente mensaje'
    expect(api.canSendMessage.value).toBe(true)
  })

  it('mapea error 429 sin habilitar reintento automático', async () => {
    vi.mocked(sendChatMessage).mockRejectedValueOnce(
      new HttpError(429, {
        title: 'Too Many Requests',
        status: 429,
        detail: 'Espera antes de enviar otro mensaje.',
      }),
    )

    const { api } = mountTreeChat()
    api.openChat()
    api.draft.value = 'Hola'

    await api.sendMessage()

    expect(api.error.value).toContain('Espera antes de enviar otro mensaje.')
    expect(api.canRetry.value).toBe(false)
  })

  it('mapea error 403', async () => {
    vi.mocked(sendChatMessage).mockRejectedValueOnce(
      new HttpError(403, {
        title: 'Forbidden',
        status: 403,
        detail: 'Rol no autorizado',
      }),
    )

    const { api } = mountTreeChat()
    api.openChat()
    api.draft.value = 'Hola'

    await api.sendMessage()

    expect(api.error.value).toContain('Rol no autorizado')
    expect(api.canRetry.value).toBe(false)
  })

  it('mapea error 401', async () => {
    vi.mocked(sendChatMessage).mockRejectedValueOnce(new HttpError(401))

    const { api } = mountTreeChat()
    api.openChat()
    api.draft.value = 'Hola'

    await api.sendMessage()

    expect(api.error.value).toContain('sesión')
    expect(api.canRetry.value).toBe(false)
  })

  it('mapea NetworkError', async () => {
    vi.mocked(sendChatMessage).mockRejectedValueOnce(new NetworkError())

    const { api } = mountTreeChat()
    api.openChat()
    api.draft.value = 'Hola'

    await api.sendMessage()

    expect(api.error.value).toContain('conectar')
    expect(api.canRetry.value).toBe(false)
  })

  it('cierra y reinicia el hilo al cambiar treeId', async () => {
    const { api, treeId } = mountTreeChat(42)
    api.openChat()
    api.draft.value = 'Hola'
    await api.sendMessage()

    treeId.value = 99
    await nextTick()

    expect(api.isOpen.value).toBe(false)
    expect(api.messages.value).toEqual([])
    expect(api.conversationId.value).toBeNull()
  })

  it('reinicia el hilo al desmontar la vista', async () => {
    const { api, unmount } = mountTreeChat()
    api.openChat()
    api.draft.value = 'Hola'
    await api.sendMessage()

    unmount()

    expect(api.messages.value).toEqual([])
    expect(api.conversationId.value).toBeNull()
  })
})
