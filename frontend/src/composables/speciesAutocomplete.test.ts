import { describe, expect, it } from 'vitest'
import {
  filterSpeciesByLabel,
  findSpeciesByExactLabel,
  normalizeSpeciesAutocompleteValue,
  resolveSpeciesFromText,
} from '@/composables/speciesAutocomplete'

const species = [
  { id: 1, label: 'Encina (Quercus ilex)' },
  { id: 2, label: 'Olivo (Olea europaea)' },
  { id: 3, label: 'Encino americano (Quercus rubra)' },
]

describe('speciesAutocomplete', () => {
  it('normalizeSpeciesAutocompleteValue ignora acentos y mayúsculas', () => {
    expect(normalizeSpeciesAutocompleteValue('  Éncina ')).toBe('encina')
  })

  it('findSpeciesByExactLabel resuelve etiqueta completa', () => {
    const found = findSpeciesByExactLabel(species, 'Encina (Quercus ilex)')
    expect(found?.id).toBe(1)
  })

  it('filterSpeciesByLabel filtra por fragmento', () => {
    const filtered = filterSpeciesByLabel(species, 'olivo')
    expect(filtered).toHaveLength(1)
    expect(filtered[0]?.id).toBe(2)
  })

  describe('resolveSpeciesFromText', () => {
    it('devuelve empty con texto vacío', () => {
      expect(resolveSpeciesFromText(species, '', 'filter')).toEqual({ kind: 'empty' })
    })

    it('modo filter: resuelve coincidencia exacta', () => {
      const result = resolveSpeciesFromText(species, 'Encina (Quercus ilex)', 'filter')
      expect(result).toEqual({ kind: 'matched', item: species[0] })
    })

    it('modo filter: auto-selecciona una sola coincidencia parcial', () => {
      const result = resolveSpeciesFromText(species, 'olivo', 'filter')
      expect(result).toEqual({ kind: 'matched', item: species[1] })
    })

    it('modo filter: cleared_unresolved con varias coincidencias parciales', () => {
      const result = resolveSpeciesFromText(species, 'enc', 'filter')
      expect(result).toEqual({ kind: 'cleared_unresolved' })
    })

    it('modo filter: cleared_unresolved sin coincidencias', () => {
      const result = resolveSpeciesFromText(species, 'pino', 'filter')
      expect(result).toEqual({ kind: 'cleared_unresolved' })
    })

    it('modo form: unresolved con coincidencia parcial sin exacta', () => {
      const result = resolveSpeciesFromText(species, 'olivo', 'form')
      expect(result).toEqual({ kind: 'unresolved' })
    })

    it('modo form: matched con coincidencia exacta', () => {
      const result = resolveSpeciesFromText(species, 'Olivo (Olea europaea)', 'form')
      expect(result).toEqual({ kind: 'matched', item: species[1] })
    })
  })
})
