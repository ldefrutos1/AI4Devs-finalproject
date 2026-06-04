import { expect, type Page } from '@playwright/test'

/**
 * Credenciales del colaborador de prueba.
 * Se leen de entorno (E2E_USER / E2E_PASS) con valores por defecto
 * alineados al realm local `mtl` (usuario `colaborador`).
 */
export const collaborator = {
  username: process.env.E2E_USER ?? 'colaborador',
  password: process.env.E2E_PASS ?? 'colaborador_dev',
}

/**
 * Inicia sesion via UI pulsando el boton "Conectarse" de la barra lateral
 * (data-testid="nav-login"). Esto ejercita la funcionalidad de login de la SPA
 * y dispara el redirect OIDC a Keycloak; despues rellena el formulario de
 * Keycloak y espera el retorno autenticado a la home.
 *
 * Los selectores `#username`, `#password` y `#kc-login` son los ids estables
 * del tema de login de Keycloak.
 */
export async function loginAsCollaborator(page: Page): Promise<void> {
  await page.goto('/')

  // El boton de login aparece cuando la sesion OIDC esta inicializada (auth.isReady).
  const loginButton = page.getByTestId('nav-login')
  await loginButton.click()

  await page.locator('#username').waitFor({ state: 'visible' })
  await page.locator('#username').fill(collaborator.username)
  await page.locator('#password').fill(collaborator.password)
  await page.locator('#kc-login').click()

  // De vuelta en la SPA (la home), ya autenticado: el boton de login desaparece.
  await expect(page).toHaveURL(/\/(?:$|\?)/)
  await expect(loginButton).toHaveCount(0)
}
