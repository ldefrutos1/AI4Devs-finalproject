# Convenciones y buenas prácticas — E2E de UI (Playwright)

Documento **canónico** para las pruebas E2E de navegador (Playwright) que ejercitan el flujo de producto extremo a extremo a través del SPA. Complementa, sin sustituir:

- **Tests unitarios/componente frontend (Vitest):** [testing-frontend.md](testing-frontend.md).
- **E2E de backend (gateway + microservicios reales, HTTP/JWT):** [testing-java.md](testing-java.md) §2.1 y el módulo [services/system-e2e-tests](../../services/system-e2e-tests/README.md). El **mismo ciclo de vida del ejemplar** (alta → consulta → borrado) está cubierto por HTTP en `EjemplarLifecycleGatewayE2EIT`, independiente de la UI.

Reparto de responsabilidades (qué demuestra cada capa, evitando duplicar): [testing-java.md](testing-java.md) §2.1.1. Regla corta de calidad: [quality-and-testing.mdc](../../.cursor/rules/quality-and-testing.mdc).

## 1. Alcance y ubicación

- Carpeta: **`e2e/`** (proyecto Node independiente del SPA; no se mezcla con Vitest). Guía operativa: [e2e/README.md](../../e2e/README.md).
- Cubre el **flujo del colaborador** de punta a punta por la UI: acceder, **iniciar sesión** (botón "Conectarse" → OIDC en Keycloak), **dar de alta** un ejemplar, **consultar "mis árboles"** y **borrar** el ejemplar.
- Servicios reales implicados: `catalog-service` (alta/listado/borrado), `media-service` (el borrado llama en cascada a media), `api-gateway`, Keycloak, Postgres (PostGIS), Mongo (dependencia futura del catálogo híbrido) y Kafka (el alta emite `EJEMPLAR_CREADO`; no se verifica el correo, no hay consumidor en el stack).
- **Qué NO duplicar aquí:** asserts de contrato API (401/403, paginación) que ya cubren `system-e2e-tests` y los IT; este E2E valida el **camino de usuario** en el navegador (OIDC, router, UX de sesión y CRUD visible).

## 2. Dos variantes (misma suite, distinta orquestación)

La URL del SPA se toma de `BASE_URL` (ver [e2e/playwright.config.ts](../../e2e/playwright.config.ts)), de modo que la misma suite sirve para ambas.

### Variante A — Contra entorno ya levantado (local / depuración)

El test **no** levanta infraestructura: asume el stack arriba (infra + microservicios en perfil `dev` + `npm run dev` del frontend) y solo ejecuta Playwright contra `http://localhost:5173`. Usuario del realm `mtl`: `colaborador` / `colaborador_dev`. Es la vía barata para iterar y depurar specs.

### Variante B — Self-contained en Docker (la que corre en CI/PR)

Construye y arranca un **stack mínimo y efímero** y ejecuta Playwright como contenedor en la red `mtl`. Definición: [infra/compose/docker-compose.e2e.yml](../../infra/compose/docker-compose.e2e.yml). Claves de diseño:

- **DB efímeras:** Postgres/Mongo/Kafka en `tmpfs` (RAM) y Keycloak con **H2 en memoria** (importa el realm [mtl-e2e](../../infra/compose/init/keycloak-e2e/mtl-e2e-realm.json)). Cada ejecución parte de cero.
- **Coherencia de issuer:** el navegador (contenedor Playwright) y los backends usan el **mismo** `http://keycloak:8080/realms/mtl-e2e`, evitando el problema de doble hostname de Keycloak.
- **Imágenes (Opción A):** los jars se compilan antes con Maven y los Dockerfiles solo los empaquetan (JRE 21). El frontend se sirve con Nginx y hace proxy de `/api` al gateway.
- **"En memoria" real:** H2 no es viable para `catalog-service` (PostGIS, `unaccent`, SQL nativo); el equivalente práctico es Postgres/Mongo **efímeros** en `tmpfs`.

## 3. Selectores y login

- Selectores estables por **`data-testid`** en las vistas implicadas (`tree-form*`, `my-trees-*`, `tree-delete-button`, `tree-delete-confirm`, `nav-login`); evitan acoplarse al copy i18n. El formulario de Keycloak se localiza por sus ids estables (`#username`, `#password`, `#kc-login`).
- El **login es por UI** (Authorization Code + PKCE; `directAccessGrants` desactivado): el helper [e2e/fixtures/auth.ts](../../e2e/fixtures/auth.ts) pulsa "Conectarse" y completa el formulario de Keycloak.

## 4. Integración continua

- **Por defecto (cada PR/push a `main`/`develop`):** [.github/workflows/ci.yml](../../.github/workflows/ci.yml) — `mvn test` en `services/` y `npm test` en `frontend/` (sin Docker).
- **E2E Playwright (opcional, pesado):** [.github/workflows/e2e-playwright.yml](../../.github/workflows/e2e-playwright.yml) — solo **`workflow_dispatch`** (manual desde Actions). Compila jars, levanta el stack e2e (`--abort-on-container-exit --exit-code-from playwright`), publica `playwright-report` y hace `down -v`.
- **Bloqueo de merge:** marcar como **required** las verificaciones de **CI** (Java + frontend). El E2E Playwright solo si el equipo decide exigirlo de forma explícita.

## 5. Ejecutar

Comandos concretos de ambas variantes: [e2e/README.md](../../e2e/README.md). Atajo local (Windows/PowerShell): `scripts/dev/test-e2e.ps1` (Docker autocontenido por defecto; `-Local` contra entorno levantado) — ver [scripts/README.md](../../scripts/README.md).

## 6. Cuándo ampliar la suite

- Mantener el E2E de UI **enfocado** a caminos de usuario y contrato del navegador; no convertirlo en cobertura de API (eso vive en `system-e2e-tests` y los IT).
- Specs **idempotentes**: cada uno crea sus propios datos (p. ej. municipio único) y los limpia; el stack efímero se descarta al bajar.
- Añadir un spec nuevo cuando una HU introduzca un **flujo de UI** crítico no cubierto (no por cada endpoint).
