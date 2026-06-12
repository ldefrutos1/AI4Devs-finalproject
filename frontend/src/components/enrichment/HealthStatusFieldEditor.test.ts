import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import HealthStatusFieldEditor from '@/components/enrichment/HealthStatusFieldEditor.vue'
import { es } from '@/i18n/locales/es'

function mountEditor(props: Record<string, unknown> = {}) {
  const i18n = createI18n({
    legacy: false,
    locale: 'es',
    messages: { es },
  })
  return mount(HealthStatusFieldEditor, {
    props: {
      modelValue: '',
      readonly: false,
      ...props,
    },
    global: {
      plugins: [i18n],
    },
  })
}

describe('HealthStatusFieldEditor', () => {
  it('muestra campos guiados por defecto', () => {
    const wrapper = mountEditor()

    expect(wrapper.find('.mtl-enrichment-guided-section').exists()).toBe(true)
    expect(wrapper.find('#tree-enrichment-health-assessment').exists()).toBe(true)
    expect(wrapper.text()).toContain('Modo JSON avanzado')
  })

  it('serializa cambios guiados al modelValue', async () => {
    const wrapper = mountEditor()

    await wrapper.get('#tree-enrichment-health-assessment').setValue('bueno')

    const updates = wrapper.emitted('update:modelValue')
    expect(updates).toBeTruthy()
    const lastValue = updates?.at(-1)?.[0] as string
    expect(lastValue).toContain('"valoracion_general": "bueno"')
  })

  it('abre modo JSON avanzado y valida inline', async () => {
    const wrapper = mountEditor()

    await wrapper.get('[data-testid="enrichment-field-mode-toggle"]').trigger('click')
    await wrapper.get('#tree-enrichment-health').setValue('{ invalid')

    expect(wrapper.text()).toContain('El JSON indicado no es válido')
  })

  it('carga datos existentes en modo guiado', () => {
    const wrapper = mountEditor({
      modelValue: JSON.stringify({ valoracion_general: 'regular' }, null, 2),
    })

    expect((wrapper.get('#tree-enrichment-health-assessment').element as HTMLInputElement).value).toBe(
      'regular',
    )
  })
})
