import { describe, expect, it } from 'vitest'
import {
  createEmptyHealthStatusGuidedDraft,
  ecologicalGuidedToObject,
  ecologicalObjectToGuided,
  healthStatusGuidedToObject,
  healthStatusObjectToGuided,
  parseEcologicalJsonField,
  parseHealthStatusJsonField,
  parseReferencesJsonField,
  referenceGuidedToObject,
  referencesGuidedToArray,
  serializeEcologicalGuidedDraft,
  serializeHealthStatusGuidedDraft,
  serializeReferencesGuidedDraft,
} from '@/composables/enrichmentGuidedForms'

describe('enrichmentGuidedForms', () => {
  it('convierte estado sanitario entre JSON y borrador guiado', () => {
    const object = {
      valoracion_general: 'bueno',
      plagas_detectadas: ['Tortrix viridana'],
      lesiones: [{ tipo: 'cavidad', descripcion: 'Base', lado: 'norte' }],
      ultima_revision: '2024-09-15',
    }

    const guided = healthStatusObjectToGuided(object)
    expect(guided.valoracionGeneral).toBe('bueno')
    expect(guided.plagasDetectadasText).toBe('Tortrix viridana')
    expect(guided.lesiones).toHaveLength(1)
    expect(healthStatusGuidedToObject(guided)).toEqual(object)
    expect(serializeHealthStatusGuidedDraft(guided)).toContain('"valoracion_general"')
  })

  it('parseHealthStatusJsonField usa modo guiado para objetos compatibles', () => {
    const result = parseHealthStatusJsonField(
      JSON.stringify({ valoracion_general: 'regular' }, null, 2),
    )

    expect(result.ok).toBe(true)
    if (result.ok) {
      expect(result.mode).toBe('guided')
      expect(result.value.guided.valoracionGeneral).toBe('regular')
    }
  })

  it('parseHealthStatusJsonField usa modo JSON para claves desconocidas', () => {
    const result = parseHealthStatusJsonField(
      JSON.stringify({ valoracion_general: 'bueno', notas_extra: 'x' }),
    )

    expect(result.ok).toBe(true)
    if (result.ok) {
      expect(result.mode).toBe('json')
      expect(result.fitsGuided).toBe(false)
    }
  })

  it('convierte datos ecológicos entre JSON y borrador guiado', () => {
    const object = {
      habitat: ['bosque', 'riberas'],
      altitudMinM: 0,
      altitudMaxM: 1500,
      clima: ['atlántico'],
    }

    const guided = ecologicalObjectToGuided(object)
    expect(guided.habitatText).toBe('bosque, riberas')
    expect(ecologicalGuidedToObject(guided)).toEqual(object)
    expect(serializeEcologicalGuidedDraft(guided)).toContain('"altitudMaxM"')
  })

  it('parseEcologicalJsonField detecta JSON inválido', () => {
    expect(parseEcologicalJsonField('{ invalid').ok).toBe(false)
  })

  it('convierte referencias entre JSON y borrador guiado', () => {
    const guided = [
      {
        title: 'Flora Ibérica',
        authorsText: 'Castroviejo, S.',
        source: 'RJB',
        year: '1993',
        url: 'https://example.com',
      },
    ]

    const array = referencesGuidedToArray(guided)
    expect(array).toHaveLength(1)
    expect(referenceGuidedToObject(guided[0])).toMatchObject({
      title: 'Flora Ibérica',
      year: 1993,
    })

    const parsed = parseReferencesJsonField(serializeReferencesGuidedDraft(guided))
    expect(parsed.ok).toBe(true)
    if (parsed.ok) {
      expect(parsed.value.guided[0]?.title).toBe('Flora Ibérica')
    }
  })

  it('estado sanitario vacío serializa a cadena vacía', () => {
    expect(serializeHealthStatusGuidedDraft(createEmptyHealthStatusGuidedDraft())).toBe('')
  })
})
