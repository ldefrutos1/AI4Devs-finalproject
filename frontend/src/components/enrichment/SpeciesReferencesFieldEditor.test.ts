import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import SpeciesReferencesFieldEditor from '@/components/enrichment/SpeciesReferencesFieldEditor.vue'
import { es } from '@/i18n/locales/es'

function mountEditor(props: Record<string, unknown> = {}) {
  const i18n = createI18n({
    legacy: false,
    locale: 'es',
    messages: { es },
  })
  return mount(SpeciesReferencesFieldEditor, {
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

describe('SpeciesReferencesFieldEditor', () => {
  it('muestra estado vacío con CTA para la primera referencia', () => {
    const wrapper = mountEditor()

    expect(wrapper.find('.mtl-enrichment-guided-section').exists()).toBe(true)
    expect(wrapper.get('[data-testid="species-enrichment-references-empty"]').text()).toContain(
      'Todavía no hay referencias bibliográficas',
    )
    expect(wrapper.get('[data-testid="species-enrichment-references-add-reference"]').text()).toBe(
      'Añadir primera referencia',
    )
  })

  it('apila referencias y ofrece añadir otra al final', async () => {
    const wrapper = mountEditor()

    await wrapper.get('[data-testid="species-enrichment-references-add-reference"]').trigger('click')

    expect(wrapper.find('[data-testid="species-enrichment-references-empty"]').exists()).toBe(false)
    expect(wrapper.get('[data-testid="species-enrichment-references-reference-0"]').text()).toContain(
      'Referencia 1',
    )
    expect(wrapper.get('[data-testid="species-enrichment-references-add-reference"]').text()).toBe(
      'Añadir otra referencia',
    )
  })
})
