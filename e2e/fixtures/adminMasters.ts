import { expect, type Page } from '@playwright/test'

const speciesModalSelector = '[data-testid="admin-masters-species-modal"]'

/**
 * Espera a que la pantalla de maestros admin este lista para interactuar.
 */
export async function expectAdminMastersReady(page: Page): Promise<void> {
  const createButton = page.getByTestId('admin-masters-create-species')
  const pageError = page.locator('.admin-masters-page .error[role="alert"]')

  await expect(async () => {
    if (await pageError.isVisible()) {
      const message = (await pageError.textContent())?.trim() ?? '(sin mensaje)'
      throw new Error(
        `admin maestros no visible: fallo al cargar maestros. UI: "${message}". ` +
          'Comprueba catalog-service (8081), api-gateway (8080) y rol ADMIN en Keycloak.',
      )
    }
    await expect(createButton).toBeVisible()
  }).toPass({ timeout: 15_000 })
}

/**
 * Espera a que el modal de alta/edicion de especie se cierre tras guardar.
 * El `<dialog>` sigue en el DOM; comprobar ausencia del atributo `open`.
 */
export async function expectSpeciesModalClosed(page: Page): Promise<void> {
  const speciesModal = page.locator(speciesModalSelector)

  await expect(async () => {
    const formError = speciesModal.locator('.error[role="alert"]')
    if (await formError.isVisible()) {
      const message = (await formError.textContent())?.trim() ?? '(sin mensaje)'
      throw new Error(`Modal de especie abierto con error: "${message}"`)
    }
    await expect(speciesModal).not.toHaveAttribute('open', '')
  }).toPass({ timeout: 15_000 })
}

/**
 * Filtra la tabla por etiqueta exacta de especie (el formulario hace commit al pulsar Aplicar).
 * Tras crear una especie conviene recargar la pagina para refrescar las opciones del autocomplete.
 */
export async function filterAdminSpeciesByLabel(page: Page, label: string): Promise<void> {
  await page.getByTestId('admin-masters-filter-species').fill(label)
  await page.getByTestId('admin-masters-filter-apply').click()

  await expect(async () => {
    const listError = page.locator('.admin-masters-page .error[role="alert"]')
    if (await listError.isVisible()) {
      const message = (await listError.textContent())?.trim() ?? '(sin mensaje)'
      throw new Error(`Filtro de especies falló: "${message}"`)
    }
    await expect(page.getByTestId('admin-masters-table')).toBeVisible()
  }).toPass({ timeout: 15_000 })
}

/** Localiza una fila de especie en la tabla admin por texto estable del alta E2E. */
export function adminSpeciesRow(page: Page, marker: string) {
  return page.getByTestId('admin-masters-row').filter({ hasText: marker })
}
