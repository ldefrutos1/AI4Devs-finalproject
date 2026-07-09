# E2E (Playwright) - MyTreeLibrary

Pruebas extremo a extremo de los siguientes flujos:
- **flujo de alta de ejemplar** del colaborador: iniciar sesion, dar de alta un ejemplar, consultar "mis arboles" y borrar el ejemplar.
- **administracion de maestros** (rol ADMIN): iniciar sesion como administrador, acceder a `/admin/masters`, crear una especie de prueba y borrarla comprobando que desaparece del listado.
- **consulta publica** (visitante sin sesion): acceder al listado publico de ejemplares, verificar que carga con al menos una ficha y abrir el detalle comprobando especie y ubicacion.


> Guía canónica (estrategia, variantes y CI): [docs/engineering/testing-e2e.md](../docs/engineering/testing-e2e.md).

## Estructura

- `playwright.config.ts` - configuracion; `baseURL` desde `BASE_URL`.
- `tests/` - specs del flujo.
- `fixtures/auth.ts` - helper de login OIDC por UI.
- `.env.example` - variables (`BASE_URL`, `E2E_USER`, `E2E_PASS`,`E2E_ADMIN_USER`,`E2E_ADMIN_PASS`).

## Variante A - Entorno ya levantado (local)

El test **no** levanta infraestructura: asume el stack arriba.

```bash
npm install
npx playwright install --with-deps chromium
BASE_URL=http://localhost:5173 npm run e2e
```

```powershell
npm install
npx playwright install --with-deps chromium
$env:BASE_URL = 'http://localhost:5173'
npm run e2e
```

Atajo (desde la raíz del repo): `.\scripts\dev\test-e2e.ps1 -Local`

## Variante B - Self-contained en Docker

Construye y arranca todo el stack y ejecuta Playwright como contenedor en la red `mtl`. Las DB son efimeras (se descartan al bajar el stack).

```bash
# desde la raiz del repo: primero construir los jars de los servicios
mvn -f services/pom.xml -pl catalog-service,media-service,ai-assistant-service,api-gateway -am package -DskipTests

# levantar stack E2E (infra + apps en background; esperar healthchecks)
cd infra/compose
docker compose -f docker-compose.e2e.yml up -d --build --wait \
  postgres mongo kafka redis keycloak catalog-service media-service \
  ai-assistant-service api-gateway frontend

# pruebas en serie (no usar up --abort-on-container-exit: Maven abortaría antes de Playwright)
docker compose -f docker-compose.e2e.yml run --rm system-e2e-tests
docker compose -f docker-compose.e2e.yml run --rm playwright

# limpiar (incluye volumenes efimeros)
docker compose -f docker-compose.e2e.yml down -v
```

```powershell
# desde la raiz del repo: primero construir los jars de los servicios
mvn -f services/pom.xml -pl catalog-service,media-service,ai-assistant-service,api-gateway -am package -DskipTests

# levantar stack E2E (infra + apps en background; esperar healthchecks)
Set-Location infra/compose
docker compose -f docker-compose.e2e.yml up -d --build --wait `
  postgres mongo kafka redis keycloak catalog-service media-service `
  ai-assistant-service api-gateway frontend

# pruebas en serie
docker compose -f docker-compose.e2e.yml run --rm system-e2e-tests
docker compose -f docker-compose.e2e.yml run --rm playwright

# limpiar (incluye volumenes efimeros)
docker compose -f docker-compose.e2e.yml down -v
```

Atajo (desde la raíz del repo): `.\scripts\dev\test-e2e.ps1`

### Orden en Docker (variante B)

1. **`system-e2e-tests`** — contenedor Maven (`mvn verify` del módulo Java). Si falla, **Playwright no arranca**.
2. **`playwright`** — `npm ci`, `wait-on` y `npx playwright test`. Los logs de cada spec salen por consola (`list`); el informe HTML se escribe en disco.

El informe HTML queda en `e2e/playwright-report/` (no es lo que ves en consola). Para abrirlo tras una ejecución local:

```bash
cd e2e && npm run report
```

## Relacion con `services/system-e2e-tests`

Son complementarios: el modulo Java valida seguridad y contrato HTTP por gateway; este E2E valida el flujo de UI de punta a punta. No duplican intencion.
