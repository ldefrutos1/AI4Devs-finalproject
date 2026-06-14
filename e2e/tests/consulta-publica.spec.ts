import { expect, test } from '@playwright/test'
import { expectPublicTreesListReady } from '../fixtures/publicBrowse'

/**
 * E2E de consulta publica (visitante sin autenticacion):
 *  1. Acceder al listado publico de ejemplares.
 *  2. Verificar que carga el listado (conteo y al menos una ficha).
 *  3. Abrir el detalle de un ejemplar publicado y comprobar especie y ubicacion.
 *
 * Requiere al menos una ficha PUBLICADO + PUBLICO (semilla Flyway V3 o datos previos).
 */

test('consulta publica del catalogo sin iniciar sesion', async ({ page }) => {
  await test.step('1. Acceder a /ejemplares sin login', async () => {
    await page.goto('/ejemplares')

    await expect(page).toHaveURL(/\/ejemplares/)
    await expect(page.getByTestId('nav-login')).toBeVisible()
  })

  let treeId = ''
  let speciesTitle = ''
  let locationLine = ''

  await test.step('2. Verificar que carga el listado publico', async () => {
    await expectPublicTreesListReady(page)

    const resultsCount = page.getByTestId('public-trees-results-count')
    await expect(resultsCount).toBeVisible()
    await expect(resultsCount).toHaveText(/\d+ resultado/)

    const countText = (await resultsCount.textContent()) ?? ''
    const match = countText.match(/(\d+)/)
    expect(match, 'no se pudo leer el total de resultados del listado').not.toBeNull()
    expect(Number(match![1])).toBeGreaterThan(0)

    const firstCard = page.getByTestId('public-trees-card').first()
    await expect(firstCard).toBeVisible()

    treeId = (await firstCard.getAttribute('data-tree-id')) ?? ''
    expect(treeId, 'la tarjeta del listado debe exponer data-tree-id').not.toBe('')

    speciesTitle = ((await firstCard.getByTestId('public-trees-card-title').textContent()) ?? '').trim()
    expect(speciesTitle.length).toBeGreaterThan(0)

    locationLine = ((await firstCard.getByTestId('public-trees-card-location').textContent()) ?? '').trim()
    expect(locationLine.length).toBeGreaterThan(0)
  })

  await test.step('3. Abrir detalle y verificar especie y ubicacion', async () => {
    const card = page.locator(`[data-testid="public-trees-card"][data-tree-id="${treeId}"]`)
    await card.getByTestId('public-trees-card-detail-link').click()

    await page.waitForURL(new RegExp(`/ejemplares/${treeId}(?:\\?|$)`))

    await expect(page.getByTestId('public-tree-detail')).toBeVisible()

    const detailSpecies = page.getByTestId('public-tree-detail-species')
    const detailSpeciesText = ((await detailSpecies.textContent()) ?? '').trim()
    expect(detailSpeciesText.length).toBeGreaterThan(0)
    expect(speciesTitle).toContain(detailSpeciesText)

    const detailMunicipality = page.getByTestId('public-tree-detail-municipality')
    const detailProvince = page.getByTestId('public-tree-detail-province')
    await expect(detailMunicipality).toBeVisible()
    await expect(detailProvince).toBeVisible()

    const municipality = ((await detailMunicipality.textContent()) ?? '').trim()
    const province = ((await detailProvince.textContent()) ?? '').trim()
    expect(municipality.length).toBeGreaterThan(0)
    expect(province.length).toBeGreaterThan(0)
    expect(locationLine).toContain(municipality)
    expect(locationLine).toContain(province)
  })
})
