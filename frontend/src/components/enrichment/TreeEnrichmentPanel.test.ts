import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import TreeEnrichmentPanel from '@/components/enrichment/TreeEnrichmentPanel.vue'
import { es } from '@/i18n/locales/es'
import type { TreeEnrichmentReplaceRequest } from '@/types/enrichment'

function mountPanel(props: Record<string, unknown> = {}) {
  const i18n = createI18n({
    legacy: false,
    locale: 'es',
    messages: { es },
  })
  return mount(TreeEnrichmentPanel, {
    props: {
      modelValue: null,
      readonly: false,
      expanded: false,
      ...props,
    },
    global: {
      plugins: [i18n],
    },
  })
}

function mountPanelWithVModel(initial: TreeEnrichmentReplaceRequest) {
  let modelValue = initial
  const wrapper = mountPanel({
    expanded: true,
    modelValue,
    'onUpdate:modelValue': (value: TreeEnrichmentReplaceRequest) => {
      modelValue = value
      void wrapper.setProps({ modelValue: value })
    },
  })
  return wrapper
}

describe('TreeEnrichmentPanel', () => {
  it('renderiza el panel colapsable con título de sección', () => {
    const wrapper = mountPanel()

    expect(wrapper.find('[data-testid="tree-enrichment-panel"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Información ampliada del ejemplar')
  })

  it('muestra resumen vacío en la cabecera colapsada', () => {
    const wrapper = mountPanel({
      modelValue: { tags: [], measurements: {}, healthStatus: {}, observations: [] },
    })

    expect(wrapper.get('[data-testid="tree-enrichment-summary"]').text()).toBe('Sin datos ampliados')
  })

  it('muestra resumen con contadores en la cabecera colapsada', () => {
    const wrapper = mountPanel({
      modelValue: {
        tags: ['monumental'],
        measurements: { heightM: 12, crownDiameterM: 8 },
        healthStatus: { valoracion_general: 'bueno' },
        observations: [{ text: 'Nota' }],
      },
    })

    const summary = wrapper.get('[data-testid="tree-enrichment-summary"]').text()
    expect(summary).toContain('2 medida(s)')
    expect(summary).toContain('1 etiqueta(s)')
    expect(summary).toContain('Estado sanitario')
    expect(summary).toContain('1 observación(es)')
  })

  it('emite draft-state al editar y muestra badge de modificado', async () => {
    const wrapper = mountPanel({
      expanded: true,
      modelValue: { tags: [], measurements: {}, healthStatus: {}, observations: [] },
    })

    await wrapper.get('#tree-enrichment-tags').setValue('monumental')

    const stateEvents = wrapper.emitted('draft-state')
    expect(stateEvents?.at(-1)?.[0]).toMatchObject({ dirty: true, valid: true })
    expect(wrapper.text()).toContain('Modificado')
  })

  it('muestra badge de modificado al editar medidas con v-model', async () => {
    const wrapper = mountPanelWithVModel({
      tags: [],
      measurements: {},
      healthStatus: {},
      observations: [],
    })

    await wrapper.get('#tree-measurement-heightM').setValue('12')

    expect(wrapper.text()).toContain('Modificado')
    expect(wrapper.emitted('draft-state')?.at(-1)?.[0]).toMatchObject({ dirty: true, valid: true })
  })

  it('no muestra badge de modificado al enfocar estado sanitario vacío', async () => {
    const wrapper = mountPanelWithVModel({
      tags: [],
      measurements: {},
      healthStatus: {},
      observations: [],
    })

    await wrapper.get('#tree-enrichment-health-assessment').trigger('focus')

    expect(wrapper.text()).not.toContain('Modificado')
    expect(wrapper.emitted('draft-state')?.at(-1)?.[0]).toMatchObject({ dirty: false, valid: true })
  })

  it('emite estado sanitario desde el formulario guiado', async () => {
    const wrapper = mountPanelWithVModel({
      tags: [],
      measurements: {},
      healthStatus: {},
      observations: [],
    })

    await wrapper.get('#tree-enrichment-health-assessment').setValue('bueno')

    const updates = wrapper.emitted('update:modelValue')
    const lastPayload = updates?.at(-1)?.[0] as { healthStatus?: Record<string, unknown> }
    expect(lastPayload.healthStatus?.valoracion_general).toBe('bueno')
  })

  it('emite draft-state inválido con JSON erróneo', async () => {
    const wrapper = mountPanel({
      expanded: true,
      modelValue: { tags: [], measurements: {}, healthStatus: {}, observations: [] },
    })

    await wrapper.get('[data-testid="enrichment-field-mode-toggle"]').trigger('click')
    await wrapper.get('#tree-enrichment-health').setValue('{ invalid')

    const stateEvents = wrapper.emitted('draft-state')
    expect(stateEvents?.at(-1)?.[0]).toMatchObject({
      dirty: true,
      valid: false,
      errorKey: 'invalidJson',
    })
  })

  it('emite update:modelValue al editar etiquetas', async () => {
    const wrapper = mountPanel({
      expanded: true,
      modelValue: { tags: [], measurements: {}, healthStatus: {}, observations: [] },
    })

    await wrapper.get('#tree-enrichment-tags').setValue('monumental, protegido')

    const updates = wrapper.emitted('update:modelValue')
    expect(updates).toBeTruthy()
    const lastPayload = updates?.at(-1)?.[0] as { tags?: string[] }
    expect(lastPayload.tags).toEqual(['monumental', 'protegido'])
  })

  it('muestra solo el aviso vacío en solo lectura sin contenido', () => {
    const wrapper = mountPanel({
      expanded: true,
      readonly: true,
      modelValue: null,
    })

    expect(wrapper.get('[data-testid="tree-enrichment-empty"]').text()).toContain(
      'Todavía no hay datos ampliados para este ejemplar',
    )
    expect(wrapper.find('#tree-enrichment-tags').exists()).toBe(false)
    expect(wrapper.find('.mtl-enrichment-measurements-group').exists()).toBe(false)
    expect(wrapper.find('.mtl-enrichment-observations-group').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('Solo consulta')
  })

  it('muestra solo el error en solo lectura cuando falla la carga', () => {
    const wrapper = mountPanel({
      expanded: true,
      readonly: true,
      modelValue: null,
      error: 'No se encontró el enriquecimiento solicitado.',
    })

    expect(wrapper.text()).toContain('No se encontró el enriquecimiento solicitado.')
    expect(wrapper.find('[data-testid="tree-enrichment-empty"]').exists()).toBe(false)
    expect(wrapper.find('#tree-enrichment-tags').exists()).toBe(false)
  })

  it('añade observaciones en modo edición', async () => {
    const wrapper = mountPanel({
      expanded: true,
      modelValue: { tags: [], measurements: {}, healthStatus: {}, observations: [] },
    })

    expect(wrapper.find('.mtl-enrichment-observations-group.mtl-enrichment-guided-section').exists()).toBe(
      true,
    )
    await wrapper.get('[data-testid="tree-enrichment-add-observation"]').trigger('click')

    expect(wrapper.find('[data-testid="tree-enrichment-observation-0"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Añadir otra observación')
  })
})
