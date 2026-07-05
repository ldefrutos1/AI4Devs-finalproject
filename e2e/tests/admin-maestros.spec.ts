import { expect, test } from '@playwright/test'
import {
  adminSpeciesRow,
  expectAdminMastersReady,
  expectSpeciesModalClosed,
  filterAdminSpeciesByLabel,
} from '../fixtures/adminMasters'
import { loginAsAdmin } from '../fixtures/auth'

/**
 * E2E de administracion de maestros taxonomicos (rol ADMIN):
 *  1. Iniciar sesion como administrador.
 *  2. Acceder a /admin/masters.
 *  3. Crear una especie de prueba (genero existente de semilla).
 *  4. Verificar que aparece en la tabla.
 *  5. Borrarla y comprobar que desaparece.
 *
 * Servicios reales: catalog-service, api-gateway, Keycloak, Postgres.
 */

test('alta y borrado de una especie en maestros admin', async ({ page }) => {
  const suffix = String(Date.now())
  const scientificName = `E2e species ${suffix}`
  const commonName = `Especie E2E ${suffix}`

  await test.step('1. Iniciar sesion como administrador', async () => {
    await loginAsAdmin(page)
  })

  await test.step('2. Ir a maestros admin', async () => {
    await page.goto('/admin/masters')
    await expect(page).toHaveURL(/\/admin\/masters/)
    await expectAdminMastersReady(page)
  })

  await test.step('3. Crear especie de prueba', async () => {
    const speciesModal = page.getByTestId('admin-masters-species-modal')

    await page.getByTestId('admin-masters-create-species').click()
    await expect(speciesModal).toHaveAttribute('open', '')

    await page.getByTestId('admin-masters-species-genus').selectOption({ index: 1 })
    await page.getByTestId('admin-masters-species-scientific').fill(scientificName)
    await page.getByTestId('admin-masters-species-common').fill(commonName)
    await page.getByTestId('admin-masters-species-submit').click()

    await expectSpeciesModalClosed(page)
    await expect(page.getByTestId('admin-masters-status-message')).toBeVisible()
  })

  await test.step('4. Verificar que la especie aparece en la tabla', async () => {
    // Refresca opciones del autocomplete de filtro (fetch unpaged al montar la vista).
    await page.reload()
    await expectAdminMastersReady(page)
    await filterAdminSpeciesByLabel(page, scientificName)

    const row = adminSpeciesRow(page, scientificName)
    await expect(row).toHaveCount(1)
    await expect(row).toContainText(scientificName)
  })

  await test.step('5. Borrar la especie y verificar ausencia', async () => {
    const row = adminSpeciesRow(page, scientificName)
    await row.getByTestId('admin-masters-delete').click()
    await page.getByTestId('admin-masters-confirm-delete').click()

    await expect(row).toHaveCount(0)
  })
})
