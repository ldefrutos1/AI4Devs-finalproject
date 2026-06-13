import { defineConfig, devices } from '@playwright/test'

/**
 * Configuracion Playwright para el E2E del flujo de alta de ejemplar.
 *
 * La URL de la SPA se toma de BASE_URL para soportar las dos variantes:
 *  - Entorno levantado (local): BASE_URL=http://localhost:5173
 *  - Self-contained Docker (CI): BASE_URL=http://frontend:80 (red docker `mtl`)
 *
 * Las credenciales del colaborador de prueba se inyectan por entorno
 * (E2E_USER / E2E_PASS) para no fijar secretos en el repositorio.
 */
const baseURL = process.env.BASE_URL ?? 'http://localhost:5173'
const isCI = Boolean(process.env.CI)

export default defineConfig({
  testDir: './tests',
  outputDir: './test-results',
  fullyParallel: false,
  forbidOnly: isCI,
  retries: isCI ? 1 : 0,
  workers: 1,
  timeout: 60_000,
  expect: { timeout: 10_000 },
  reporter: isCI
    ? [['html', { outputFolder: 'playwright-report', open: 'never' }], ['line']]
    : [['html', { outputFolder: 'playwright-report', open: 'never' }], ['list']],
  use: {
    baseURL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'on',
    actionTimeout: 15_000,
    navigationTimeout: 30_000,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
})
