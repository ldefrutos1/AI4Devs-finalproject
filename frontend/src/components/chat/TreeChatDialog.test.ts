import { beforeAll, beforeEach, describe, expect, it } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import TreeChatDialog from '@/components/chat/TreeChatDialog.vue'
import { es } from '@/i18n/locales/es'
import { AI_CHAT_MAX_CONTENT_LENGTH } from '@/types/ai'

beforeAll(() => {
  if (typeof HTMLDialogElement === 'undefined') {
    return
  }
  const proto = HTMLDialogElement.prototype as HTMLDialogElement & {
    showModal?: () => void
  }
  if (typeof proto.showModal !== 'function') {
    proto.showModal = function (this: HTMLDialogElement) {
      this.setAttribute('open', '')
    }
  }
  if (typeof (proto as { close?: () => void }).close !== 'function') {
    ;(proto as { close: () => void }).close = function (this: HTMLDialogElement) {
      this.removeAttribute('open')
    }
  }
})

function mountDialog(props: Record<string, unknown> = {}) {
  const i18n = createI18n({
    legacy: false,
    locale: 'es',
    messages: { es },
  })
  return mount(TreeChatDialog, {
    props: {
      open: false,
      draft: '',
      messages: [],
      isLoading: false,
      error: '',
      canRetry: false,
      canSendMessage: false,
      isAtThreadLimit: false,
      maxContentLength: AI_CHAT_MAX_CONTENT_LENGTH,
      ...props,
    },
    global: {
      plugins: [i18n],
    },
  })
}

describe('TreeChatDialog', () => {
  beforeEach(() => {
    document.body.innerHTML = ''
  })

  it('muestra aviso orientativo y título accesible al abrir', async () => {
    const wrapper = mountDialog({ open: true })
    await flushPromises()

    const dialog = wrapper.get('[data-testid="tree-chat-dialog"]')
    expect(dialog.attributes('aria-labelledby')).toBeTruthy()
    expect(wrapper.text()).toContain('Asistente IA')
    expect(wrapper.text()).toContain('orientativas')
  })

  it('coloca Cerrar y Enviar en la misma fila de acciones', async () => {
    const wrapper = mountDialog({ open: true, canSendMessage: true, draft: 'Hola' })
    await flushPromises()

    const actions = wrapper.get('.mtl-tree-chat-composer-actions')
    expect(actions.classes()).toContain('mtl-form-dialog-actions')
    expect(actions.get('[data-testid="tree-chat-close"]').text()).toContain('Cerrar')
    expect(actions.get('[data-testid="tree-chat-send"]').text()).toContain('Enviar')
  })

  it('cierra con el botón Cerrar', async () => {
    const wrapper = mountDialog({ open: true })
    await flushPromises()

    await wrapper.get('[data-testid="tree-chat-close"]').trigger('click')
    await flushPromises()

    expect(wrapper.emitted('update:open')?.at(-1)).toEqual([false])
  })

  it('cierra con Esc (evento cancel del dialog)', async () => {
    const wrapper = mountDialog({ open: true })
    await flushPromises()

    await wrapper.get('[data-testid="tree-chat-dialog"]').trigger('cancel')
    await flushPromises()

    expect(wrapper.emitted('update:open')?.at(-1)).toEqual([false])
  })

  it('renderiza el hilo de mensajes user y assistant', async () => {
    const wrapper = mountDialog({
      open: true,
      messages: [
        { role: 'user', content: '¿Cómo registro coordenadas?' },
        { role: 'assistant', content: 'Usa el mapa en la ficha.' },
      ],
    })
    await flushPromises()

    expect(wrapper.get('[data-testid="tree-chat-message-user-0"]').text()).toContain(
      '¿Cómo registro coordenadas?',
    )
    expect(wrapper.get('[data-testid="tree-chat-message-assistant-1"]').text()).toContain(
      'Usa el mapa en la ficha.',
    )
    expect(wrapper.find('[data-testid="tree-chat-empty"]').exists()).toBe(false)
  })

  it('emite send al enviar con canSendMessage habilitado', async () => {
    const wrapper = mountDialog({
      open: true,
      draft: 'Hola',
      canSendMessage: true,
    })
    await flushPromises()

    const sendButton = wrapper.get('[data-testid="tree-chat-send"]')
    expect(sendButton.attributes('disabled')).toBeUndefined()

    await wrapper.get('[data-testid="tree-chat-composer"]').trigger('submit.prevent')
    await flushPromises()

    expect(wrapper.emitted('send')).toHaveLength(1)
  })

  it('deshabilita envío cuando canSendMessage es false o hay carga en vuelo', async () => {
    const wrapper = mountDialog({
      open: true,
      draft: 'Hola',
      canSendMessage: false,
      isLoading: true,
    })
    await flushPromises()

    const sendButton = wrapper.get('[data-testid="tree-chat-send"]')
    expect(sendButton.attributes('disabled')).toBeDefined()
    expect(sendButton.text()).toContain('Enviando')

    await sendButton.trigger('click')
    expect(wrapper.emitted('send')).toBeUndefined()
  })

  it('aplica maxlength al textarea según contrato', async () => {
    const wrapper = mountDialog({ open: true, maxContentLength: 2000 })
    await flushPromises()

    expect(wrapper.get('[data-testid="tree-chat-input"]').attributes('maxlength')).toBe('2000')
  })

  it('muestra error 502 con botón Reintentar', async () => {
    const wrapper = mountDialog({
      open: true,
      error: 'El asistente IA no está disponible.',
      canRetry: true,
    })
    await flushPromises()

    expect(wrapper.get('[data-testid="tree-chat-error"]').text()).toContain('no está disponible')
    expect(wrapper.find('[data-testid="tree-chat-retry"]').exists()).toBe(true)

    await wrapper.get('[data-testid="tree-chat-retry"]').trigger('click')
    expect(wrapper.emitted('retry')).toHaveLength(1)
  })

  it('muestra error 429 sin botón Reintentar', async () => {
    const wrapper = mountDialog({
      open: true,
      error: 'Has alcanzado el límite de mensajes.',
      canRetry: false,
    })
    await flushPromises()

    expect(wrapper.get('[data-testid="tree-chat-error"]').text()).toContain('límite')
    expect(wrapper.find('[data-testid="tree-chat-retry"]').exists()).toBe(false)
  })

  it('muestra error 403 sin botón Reintentar', async () => {
    const wrapper = mountDialog({
      open: true,
      error: 'No tienes permiso para usar el asistente de chat.',
      canRetry: false,
    })
    await flushPromises()

    expect(wrapper.get('[data-testid="tree-chat-error"]').text()).toContain('permiso')
    expect(wrapper.find('[data-testid="tree-chat-retry"]').exists()).toBe(false)
  })

  it('muestra aviso de límite de hilo y deshabilita el input', async () => {
    const wrapper = mountDialog({
      open: true,
      isAtThreadLimit: true,
    })
    await flushPromises()

    expect(wrapper.get('[data-testid="tree-chat-thread-limit"]').text()).toContain('máximo')
    expect(wrapper.get('[data-testid="tree-chat-input"]').attributes('disabled')).toBeDefined()
  })

  it('muestra estado de carga como burbuja del asistente', async () => {
    const wrapper = mountDialog({
      open: true,
      isLoading: true,
    })
    await flushPromises()

    const loading = wrapper.get('[data-testid="tree-chat-loading"]')
    expect(loading.classes()).toContain('mtl-tree-chat-bubble--assistant')
    expect(loading.text()).toContain('respondiendo')
  })
})
