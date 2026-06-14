import { expect, type Page } from '@playwright/test'

export type E2eCredentials = {
  username: string
  password: string
}

/**
 * Credenciales del colaborador de prueba.
 * Se leen de entorno (E2E_USER / E2E_PASS) con valores por defecto
 * alineados al realm local `mtl` (usuario `colaborador`).
 */
export const collaborator: E2eCredentials = {
  username: process.env.E2E_USER ?? 'colaborador',
  password: process.env.E2E_PASS ?? 'colaborador_dev',
}

/**
 * Credenciales del administrador de prueba (maestros taxonomicos).
 * Realm local `mtl`: `admin_mtl` / `admin_mtl_dev`.
 */
export const admin: E2eCredentials = {
  username: process.env.E2E_ADMIN_USER ?? 'admin_mtl',
  password: process.env.E2E_ADMIN_PASS ?? 'admin_mtl_dev',
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
export async function loginAs(page: Page, creds: E2eCredentials): Promise<void> {
  await page.goto('/')

  const loginButton = page.getByTestId('nav-login')
  await loginButton.click()

  await page.locator('#username').waitFor({ state: 'visible' })
  await page.locator('#username').fill(creds.username)
  await page.locator('#password').fill(creds.password)
  await page.locator('#kc-login').click()

  await expect(page).toHaveURL(/\/(?:$|\?)/)
  await expect(loginButton).toHaveCount(0)
}

export async function loginAsCollaborator(page: Page): Promise<void> {
  await loginAs(page, collaborator)
}

export async function loginAsAdmin(page: Page): Promise<void> {
  await loginAs(page, admin)
}
