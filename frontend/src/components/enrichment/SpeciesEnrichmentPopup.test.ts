import { beforeAll, beforeEach, describe, expect, it } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import SpeciesEnrichmentPopup from '@/components/enrichment/SpeciesEnrichmentPopup.vue'
import { es } from '@/i18n/locales/es'

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

function mountPopup(props: Record<string, unknown> = {}) {
  const i18n = createI18n({
    legacy: false,
    locale: 'es',
    messages: { es },
  })
  return mount(SpeciesEnrichmentPopup, {
    props: {
      open: false,
      readonly: true,
      ...props,
    },
    global: {
      plugins: [i18n],
    },
  })
}

describe('SpeciesEnrichmentPopup', () => {
  beforeEach(() => {
    document.body.innerHTML = ''
  })

  it('deshabilita el icono cuando triggerDisabled es true', () => {
    const wrapper = mountPopup({ triggerDisabled: true })

    expect(wrapper.get('[data-testid="species-enrichment-trigger"]').attributes('disabled')).toBeDefined()
  })

  it('muestra badge de contenido en el disparador', () => {
    const wrapper = mountPopup({
      enrichment: { speciesId: 1, scientificName: 'Quercus ilex', synonyms: ['Encina'] },
    })

    const trigger = wrapper.get('[data-testid="species-enrichment-trigger"]')
    expect(trigger.attributes('aria-label')).toBe('Ver información ampliada de la especie')
    expect(wrapper.get('[data-testid="species-enrichment-content-badge"]').text()).toBe('Con datos')
  })

  it('muestra badge Sin datos cuando la especie no tiene enriquecimiento', () => {
    const wrapper = mountPopup({
      enrichment: { speciesId: 1, scientificName: 'Quercus ilex' },
    })

    expect(wrapper.get('[data-testid="species-enrichment-content-badge"]').text()).toBe('Sin datos')
  })

  it('abre el diálogo al pulsar el icono', async () => {
    const wrapper = mountPopup({
      enrichment: { speciesId: 1, scientificName: 'Quercus ilex', commonName: 'Encina' },
    })

    await wrapper.get('[data-testid="species-enrichment-trigger"]').trigger('click')
    await flushPromises()

    expect(wrapper.emitted('update:open')?.at(-1)).toEqual([true])
    expect(wrapper.find('[data-testid="species-enrichment-dialog"]').exists()).toBe(true)
  })

  it('muestra aviso de solo lectura para colaborador', async () => {
    const wrapper = mountPopup({
      open: true,
      readonly: true,
      enrichment: { speciesId: 1, scientificName: 'Quercus ilex' },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('Solo consulta')
    expect(wrapper.find('[data-testid="species-enrichment-save"]').exists()).toBe(false)
  })

  it('edita datos ecológicos y referencias en modo guiado', async () => {
    const wrapper = mountPopup({
      open: true,
      readonly: false,
      enrichment: { speciesId: 1, scientificName: 'Quercus ilex' },
    })
    await flushPromises()

    await wrapper.get('#species-enrichment-ecological-habitat').setValue('bosque, riberas')
    await wrapper.get('[data-testid="species-enrichment-references-add-reference"]').trigger('click')
    await wrapper.get('#species-enrichment-references-title-0').setValue('Flora Ibérica')
    await flushPromises()

    await wrapper.get('[data-testid="species-enrichment-save"]').trigger('click')
    await flushPromises()

    const saveEvents = wrapper.emitted('save')
    expect(saveEvents).toHaveLength(1)
    expect(saveEvents?.[0]?.[0]).toMatchObject({
      ecologicalData: { habitat: ['bosque', 'riberas'] },
      references: [{ title: 'Flora Ibérica' }],
    })
  })

  it('emite save con payload válido en modo edición ADMIN', async () => {
    const wrapper = mountPopup({
      open: true,
      readonly: false,
      enrichment: {
        speciesId: 1,
        scientificName: 'Quercus ilex',
        synonyms: ['Encina'],
      },
    })
    await flushPromises()

    await wrapper.get('[data-testid="species-enrichment-save"]').trigger('click')
    await flushPromises()

    const saveEvents = wrapper.emitted('save')
    expect(saveEvents).toHaveLength(1)
    expect(saveEvents?.[0]?.[0]).toMatchObject({
      synonyms: ['Encina'],
    })
  })

  it('muestra acción IA solo para ADMIN sin enriquecimiento previo', async () => {
    const wrapper = mountPopup({
      open: true,
      readonly: false,
      canRequestAiSuggestion: true,
      enrichment: {
        speciesId: 1,
        scientificName: 'Quercus ilex',
        commonName: 'Encina',
      },
    })
    await flushPromises()

    expect(wrapper.find('[data-testid="species-enrichment-ai-section"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="species-enrichment-ai-request"]').exists()).toBe(true)
  })

  it('oculta acción IA cuando canRequestAiSuggestion es false', async () => {
    const wrapper = mountPopup({
      open: true,
      readonly: false,
      canRequestAiSuggestion: false,
      enrichment: {
        speciesId: 1,
        scientificName: 'Quercus ilex',
        commonName: 'Encina',
        synonyms: ['Encina'],
      },
    })
    await flushPromises()

    expect(wrapper.find('[data-testid="species-enrichment-ai-section"]').exists()).toBe(false)
  })

  it('emite request-ai-suggestion y precarga campos con payload IA', async () => {
    const wrapper = mountPopup({
      open: true,
      readonly: false,
      canRequestAiSuggestion: true,
      enrichment: {
        speciesId: 1,
        scientificName: 'Quercus ilex',
        commonName: 'Encina',
      },
    })
    await flushPromises()

    await wrapper.get('[data-testid="species-enrichment-ai-request"]').trigger('click')
    expect(wrapper.emitted('request-ai-suggestion')).toHaveLength(1)

    await wrapper.setProps({
      aiSuggestionPayload: {
        synonyms: ['Encina', 'Quercus ilex'],
        distribution: { continents: ['Europa'], countries: ['España'] },
        ecologicalData: { habitat: ['bosque mediterráneo'] },
      },
    })
    await flushPromises()

    expect((wrapper.get('#species-enrichment-synonyms').element as HTMLTextAreaElement).value).toBe(
      'Encina\nQuercus ilex',
    )
    expect((wrapper.get('#species-enrichment-continents').element as HTMLInputElement).value).toBe(
      'Europa',
    )
    expect(wrapper.get('[data-testid="species-enrichment-ai-success"]').text()).toContain(
      'precargados',
    )
  })

  it('muestra error de consulta IA sin precargar', async () => {
    const wrapper = mountPopup({
      open: true,
      readonly: false,
      canRequestAiSuggestion: true,
      enrichment: {
        speciesId: 1,
        scientificName: 'Quercus ilex',
        commonName: 'Encina',
      },
      aiSuggestionError: 'La respuesta de la IA no superó la validación.',
    })
    await flushPromises()

    expect(wrapper.text()).toContain('validación')
    expect(wrapper.find('[data-testid="species-enrichment-ai-success"]').exists()).toBe(false)
  })
})
