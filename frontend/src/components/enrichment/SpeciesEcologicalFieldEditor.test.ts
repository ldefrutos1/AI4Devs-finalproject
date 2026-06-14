import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import SpeciesEcologicalFieldEditor from '@/components/enrichment/SpeciesEcologicalFieldEditor.vue'
import { es } from '@/i18n/locales/es'

function mountEditor(props: Record<string, unknown> = {}) {
  const i18n = createI18n({
    legacy: false,
    locale: 'es',
    messages: { es },
  })
  return mount(SpeciesEcologicalFieldEditor, {
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

describe('SpeciesEcologicalFieldEditor', () => {
  it('agrupa los campos guiados en una sección a ancho completo', () => {
    const wrapper = mountEditor()

    expect(wrapper.find('.mtl-enrichment-guided-section').exists()).toBe(true)
    expect(wrapper.find('.field-full').exists()).toBe(true)
    expect(wrapper.find('#species-enrichment-ecological-habitat').exists()).toBe(true)
    expect(wrapper.find('#species-enrichment-ecological-altitude-min').exists()).toBe(true)
    expect(wrapper.find('#species-enrichment-ecological-climate').exists()).toBe(true)
  })
})
