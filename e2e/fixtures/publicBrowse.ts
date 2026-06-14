import { expect, type Page } from '@playwright/test'

/**
 * Espera a que el listado publico de ejemplares este listo.
 * Si la API falla, el test falla con el mensaje de error de la UI
 * en lugar de un timeout opaco.
 */
export async function expectPublicTreesListReady(page: Page): Promise<void> {
  const resultsCount = page.getByTestId('public-trees-results-count')
  const listError = page.locator('.catalog-page .error[role="alert"]')
  const emptyState = page.getByTestId('public-trees-empty')

  await expect(async () => {
    if (await listError.isVisible()) {
      const message = (await listError.textContent())?.trim() ?? '(sin mensaje)'
      throw new Error(
        `listado publico no visible: fallo al cargar /api/catalog/public/trees. UI: "${message}". ` +
          'Comprueba catalog-service (8081), api-gateway (8080) y Postgres (Flyway V3 si no hay fichas).',
      )
    }
    const hasCount = await resultsCount.isVisible()
    const hasEmpty = await emptyState.isVisible()
    if (!hasCount && !hasEmpty) {
      throw new Error('listado publico aun cargando (sin conteo ni estado vacio).')
    }
  }).toPass({ timeout: 10_000 })
}
