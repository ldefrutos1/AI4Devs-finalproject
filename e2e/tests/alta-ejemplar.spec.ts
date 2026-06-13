import { expect, test } from '@playwright/test'
import { loginAsCollaborator } from '../fixtures/auth'
import { expectCreateTreeFormReady } from '../fixtures/treeForm'

/**
 * E2E del flujo de alta de ejemplar (colaborador):
 *  1. Acceder a la app.
 *  2. Iniciar sesion pulsando "Conectarse" (login OIDC contra Keycloak).
 *  3. Dar de alta un ejemplar.
 *  4. Consultar "mis arboles" y localizar el recien creado.
 *  5. Borrar el ejemplar y verificar que desaparece de la lista.
 *
 * Servicios reales: catalog-service (+ media-service en el borrado), gateway,
 * Keycloak, Postgres y Mongo.
 */

// Coordenadas validas (Madrid) para el alta.
const LATITUDE = '40.4168'
const LONGITUDE = '-3.7038'

test('alta, consulta y borrado de un ejemplar por un colaborador', async ({ page }) => {
  // Municipio unico para identificar de forma inequivoca el ejemplar creado.
  const municipality = `E2E ${Date.now()}`

  await test.step('1-2. Acceder e iniciar sesion como colaborador', async () => {
    await loginAsCollaborator(page)
  })

  let treeId = ''

  await test.step('3. Dar de alta un ejemplar', async () => {
    await page.goto('/ejemplares/new')

    // El formulario solo se renderiza cuando los maestros (especies/provincias) han cargado.
    await expectCreateTreeFormReady(page)

    // Especie: abrir el autocompletado y elegir la primera sugerencia disponible.
    await page.getByTestId('tree-form-species').click()
    const firstSpecies = page.locator('.species-autocomplete-list .species-autocomplete-item').first()
    await expect(firstSpecies).toBeVisible()
    await firstSpecies.click()

    // Provincia: primera opcion real (la opcion 0 es el placeholder deshabilitado).
    await page.getByTestId('tree-form-province').selectOption({ index: 1 })

    await page.getByTestId('tree-form-municipality').fill(municipality)
    await page.getByTestId('tree-form-latitude').fill(LATITUDE)
    await page.getByTestId('tree-form-longitude').fill(LONGITUDE)

    await page.getByTestId('tree-form-submit').click()

    // Tras el alta, la app redirige a la edicion del nuevo ejemplar.
    await page.waitForURL(/\/ejemplares\/\d+\/edit/)
    const match = page.url().match(/\/ejemplares\/(\d+)\/edit/)
    expect(match, 'no se pudo extraer el id del ejemplar de la URL').not.toBeNull()
    treeId = match![1]
  })

  await test.step('4. Consultar "mis arboles" y localizar el ejemplar', async () => {
    await page.goto('/mis-ejemplares')

    await expect(page.getByTestId('my-trees-results-count')).toBeVisible()

    // El orden por defecto es modificado_en,desc: el recien creado aparece el primero.
    const card = page.locator(`[data-testid="my-trees-card"][data-tree-id="${treeId}"]`)
    await expect(card).toBeVisible()
    await expect(card).toContainText(municipality)
  })

  await test.step('5. Borrar el ejemplar y verificar que desaparece', async () => {
    const card = page.locator(`[data-testid="my-trees-card"][data-tree-id="${treeId}"]`)
    await card.getByTestId('my-trees-card-edit-link').click()

    await page.waitForURL(new RegExp(`/ejemplares/${treeId}/edit`))

    await page.getByTestId('tree-delete-button').click()
    await page.getByTestId('tree-delete-confirm').click()

    // El borrado redirige de vuelta al listado.
    await page.waitForURL(/\/mis-ejemplares/)

    await expect(
      page.locator(`[data-testid="my-trees-card"][data-tree-id="${treeId}"]`),
    ).toHaveCount(0)
  })
})
