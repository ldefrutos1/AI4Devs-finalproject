import { expect, type Page } from '@playwright/test'

/**
 * Espera a que el formulario de alta este listo (maestros cargados).
 * Si la API de catalog-service falla, el test falla con el mensaje de error de la UI
 * en lugar de un timeout opaco sobre tree-form.
 */
export async function expectCreateTreeFormReady(page: Page): Promise<void> {
  const form = page.getByTestId('tree-form')
  const mastersError = page.locator('.tree-form-page .error[role="alert"]')

  await expect(async () => {
    if (await mastersError.isVisible()) {
      const message = (await mastersError.textContent())?.trim() ?? '(sin mensaje)'
      throw new Error(
        `tree-form no visible: no se cargaron especies/provincias. UI: "${message}". ` +
          'Comprueba catalog-service (8081), api-gateway (8080), Redis y Mongo.',
      )
    }
    await expect(form).toBeVisible()
  }).toPass({ timeout: 10_000 })
}
