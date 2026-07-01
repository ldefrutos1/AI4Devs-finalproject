import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import { createMemoryHistory, createRouter } from 'vue-router'
import { es } from '@/i18n/locales/es'
import EditTreeView from '@/views/EditTreeView.vue'

vi.mock('@/services/catalog/catalogService', () => ({
  fetchSpecies: vi.fn(),
  fetchProvinces: vi.fn(),
}))

vi.mock('@/services/catalog/collaboratorTreesService', () => ({
  fetchCollaboratorTreeDetail: vi.fn(),
  updateCollaboratorTree: vi.fn(),
  deleteCollaboratorTree: vi.fn(),
}))

vi.mock('@/services/media/treeGalleryService', () => ({
  fetchTreePhotoGallery: vi.fn(),
  deleteTreePhoto: vi.fn(),
}))

vi.mock('@/services/media/treePhotoUploadSequence', () => ({
  ObjectStorageUploadError: class ObjectStorageUploadError extends Error {
    readonly status: number
    constructor(status: number, message: string) {
      super(message)
      this.name = 'ObjectStorageUploadError'
      this.status = status
    }
  },
  uploadPhotosForTree: vi.fn(),
}))

vi.mock('@/services/catalog/enrichmentService', () => ({
  fetchSpeciesEnrichment: vi.fn(),
  fetchTreeEnrichment: vi.fn(),
  updateSpeciesEnrichment: vi.fn(),
  updateTreeEnrichment: vi.fn(),
}))

vi.mock('@/services/ai/chatMessageService', () => ({
  sendChatMessage: vi.fn(),
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    hasRole: () => false,
  }),
}))

import { fetchProvinces, fetchSpecies } from '@/services/catalog/catalogService'
import { fetchCollaboratorTreeDetail } from '@/services/catalog/collaboratorTreesService'
import { fetchTreePhotoGallery } from '@/services/media/treeGalleryService'
import { fetchSpeciesEnrichment, fetchTreeEnrichment } from '@/services/catalog/enrichmentService'
import { sendChatMessage } from '@/services/ai/chatMessageService'
import { HttpError } from '@/services/http/apiClient'

const detailFixture = {
  treeId: 42,
  speciesId: 1,
  speciesLabel: 'Roble (Quercus robur)',
  provinceId: 2,
  provinceLabel: 'Madrid',
  municipality: 'Centro',
  description: 'Descripción',
  latitude: 40.4,
  longitude: -3.7,
  altitude: 650,
  publicationState: 'BORRADOR' as const,
  publicMapVisibility: 'PRIVADO' as const,
  createdByUserId: 9,
  createdAt: '2024-01-01T00:00:00Z',
  modifiedAt: '2024-01-02T00:00:00Z',
}

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

async function mountEditTreeView() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/mis-ejemplares', name: 'mis-ejemplares', component: { template: '<div />' } },
      { path: '/ejemplares/:id/edit', name: 'ejemplares-edit', component: EditTreeView },
    ],
  })
  await router.push('/ejemplares/42/edit')
  await router.isReady()

  const i18n = createI18n({ legacy: false, locale: 'es', messages: { es } })
  const wrapper = mount(EditTreeView, {
    global: {
      plugins: [router, i18n],
      stubs: {
        SpeciesAutocompleteInput: {
          template: '<input id="edit-speciesId" />',
        },
        TreeLocationMapPreview: true,
        EditTreeGalleryPanel: true,
        TreeEnrichmentPanel: true,
        SpeciesEnrichmentPopup: true,
      },
    },
  })
  await flushPromises()
  return wrapper
}

describe('EditTreeView chat integration', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.stubGlobal('crypto', {
      randomUUID: vi.fn(() => '550e8400-e29b-41d4-a716-446655440000'),
    })
    vi.mocked(fetchSpecies).mockResolvedValue([{ id: 1, label: 'Roble (Quercus robur)' }])
    vi.mocked(fetchProvinces).mockResolvedValue([{ id: 2, label: 'Madrid' }])
    vi.mocked(fetchCollaboratorTreeDetail).mockResolvedValue(detailFixture)
    vi.mocked(fetchTreePhotoGallery).mockResolvedValue([])
    vi.mocked(fetchSpeciesEnrichment).mockResolvedValue({
      speciesId: 1,
      scientificName: 'Quercus robur',
      commonName: 'Roble',
    })
    vi.mocked(fetchTreeEnrichment).mockResolvedValue({
      treeId: 42,
      tags: [],
      measurements: {},
    })
    vi.mocked(sendChatMessage).mockResolvedValue({
      conversationId: '550e8400-e29b-41d4-a716-446655440000',
      message: {
        role: 'assistant',
        content: 'Respuesta orientativa.',
        createdAt: '2026-07-01T10:00:00Z',
      },
    })
  })

  it('oculta el disparador mientras la ficha carga', async () => {
    let resolveDetail!: (value: typeof detailFixture) => void
    vi.mocked(fetchCollaboratorTreeDetail).mockImplementationOnce(
      () =>
        new Promise((resolve) => {
          resolveDetail = resolve
        }),
    )

    const wrapper = await mountEditTreeView()
    expect(wrapper.find('[data-testid="tree-chat-trigger"]').exists()).toBe(false)

    resolveDetail(detailFixture)
    await flushPromises()

    expect(wrapper.find('[data-testid="tree-chat-trigger"]').exists()).toBe(true)
  })

  it('muestra el disparador en la cabecera cuando la ficha está lista', async () => {
    const wrapper = await mountEditTreeView()

    const trigger = wrapper.get('[data-testid="tree-chat-trigger"]')
    expect(trigger.text()).toContain('Asistente IA')
    expect(trigger.classes()).toContain('btn-primary-soft')
    expect(trigger.classes()).toContain('tree-edit-page__assistant-trigger')
    expect(wrapper.find('.page-actions-footer [data-testid="tree-chat-trigger"]').exists()).toBe(false)
  })

  it('abre el diálogo y envía un turno con treeId de la ruta', async () => {
    const wrapper = await mountEditTreeView()

    await wrapper.get('[data-testid="tree-chat-trigger"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-testid="tree-chat-dialog"]').exists()).toBe(true)

    const input = wrapper.get('[data-testid="tree-chat-input"]')
    await input.setValue('¿Qué datos debo revisar?')
    await wrapper.get('[data-testid="tree-chat-composer"]').trigger('submit.prevent')
    await flushPromises()

    expect(sendChatMessage).toHaveBeenCalledWith(
      expect.objectContaining({
        treeId: 42,
        messages: [{ role: 'user', content: '¿Qué datos debo revisar?' }],
      }),
      expect.any(AbortSignal),
    )
    expect(wrapper.text()).toContain('Respuesta orientativa.')
  })

  it('reinicia el hilo al cerrar el diálogo', async () => {
    const wrapper = await mountEditTreeView()

    await wrapper.get('[data-testid="tree-chat-trigger"]').trigger('click')
    await flushPromises()
    await wrapper.get('[data-testid="tree-chat-input"]').setValue('Hola')
    await wrapper.get('[data-testid="tree-chat-composer"]').trigger('submit.prevent')
    await flushPromises()

    expect(wrapper.text()).toContain('Hola')

    await wrapper.get('[data-testid="tree-chat-close"]').trigger('click')
    await flushPromises()

    await wrapper.get('[data-testid="tree-chat-trigger"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-testid="tree-chat-message-user-0"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="tree-chat-empty"]').exists()).toBe(true)
  })

  it('muestra error 502 en el diálogo y permite reintentar el mismo turno', async () => {
    vi.mocked(sendChatMessage)
      .mockRejectedValueOnce(new HttpError(502, { title: 'Bad Gateway', status: 502 }))
      .mockResolvedValueOnce({
        conversationId: '550e8400-e29b-41d4-a716-446655440000',
        message: {
          role: 'assistant',
          content: 'Recuperado tras reintento.',
          createdAt: '2026-07-01T10:00:00Z',
        },
      })

    const wrapper = await mountEditTreeView()
    await wrapper.get('[data-testid="tree-chat-trigger"]').trigger('click')
    await flushPromises()
    await wrapper.get('[data-testid="tree-chat-input"]').setValue('Consulta con fallo')
    await wrapper.get('[data-testid="tree-chat-composer"]').trigger('submit.prevent')
    await flushPromises()

    expect(wrapper.get('[data-testid="tree-chat-error"]').text()).toContain('disponible')
    expect(wrapper.find('[data-testid="tree-chat-retry"]').exists()).toBe(true)

    await wrapper.get('[data-testid="tree-chat-retry"]').trigger('click')
    await flushPromises()

    expect(sendChatMessage).toHaveBeenCalledTimes(2)
    expect(wrapper.text()).toContain('Recuperado tras reintento.')
  })

  it('cerrar el chat no altera el formulario ni el botón Guardar', async () => {
    const wrapper = await mountEditTreeView()

    await wrapper.get('[data-testid="tree-chat-trigger"]').trigger('click')
    await flushPromises()
    await wrapper.get('[data-testid="tree-chat-input"]').setValue('Mensaje de prueba')
    await wrapper.get('[data-testid="tree-chat-composer"]').trigger('submit.prevent')
    await flushPromises()
    await wrapper.get('[data-testid="tree-chat-close"]').trigger('click')
    await flushPromises()

    const saveButton = wrapper.get('button.tree-form-submit[type="submit"]')
    expect(saveButton.text()).toContain('Guardar')
    expect(saveButton.attributes('disabled')).toBeUndefined()
    expect(wrapper.find('#edit-description').exists()).toBe(true)
  })
})
