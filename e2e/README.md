# E2E (Playwright) - MyTreeLibrary

Pruebas extremo a extremo del **flujo de alta de ejemplar** del colaborador:
acceder a la app, iniciar sesion, dar de alta un ejemplar, consultar "mis arboles"
y borrar el ejemplar.

Servicios reales implicados: `catalog-service`, `media-service` (el borrado llama
en cascada a media), `api-gateway`, Keycloak, Postgres (PostGIS), Mongo y Kafka.

> Guía canónica (estrategia, variantes y CI): [docs/engineering/testing-e2e.md](../docs/engineering/testing-e2e.md).

## Estructura

- `playwright.config.ts` - configuracion; `baseURL` desde `BASE_URL`.
- `tests/` - specs del flujo.
- `fixtures/auth.ts` - helper de login OIDC por UI.
- `.env.example` - variables (`BASE_URL`, `E2E_USER`, `E2E_PASS`).

## Variante A - Entorno ya levantado (local)

El test **no** levanta infraestructura: asume el stack arriba.

1. Infra de apoyo + Keycloak: en `infra/compose`, `docker compose up -d`.
2. Microservicios (perfil `dev`): `catalog-service` (8081), `media-service` (8082),
   `api-gateway` (8080). Ver `services/README.md`.
3. Frontend: `npm run dev` en `frontend/` (5173).
4. Ejecutar las pruebas:

```bash
cd e2e
cp .env.example .env   # ajusta BASE_URL/credenciales si hace falta
npm install
npx playwright install --with-deps chromium
BASE_URL=http://localhost:5173 npm run e2e
```

Usuario por defecto: `colaborador` / `colaborador_dev` (realm `mtl`, rol `COLABORADOR`).

## Variante B - Self-contained en Docker

Construye y arranca todo el stack y ejecuta Playwright como contenedor en la red
`mtl`. Las DB son efimeras (se descartan al bajar el stack).

```bash
# desde la raiz del repo: primero construir los jars de los servicios
mvn -f services/pom.xml -pl catalog-service,media-service,api-gateway -am package -DskipTests

# levantar stack E2E (autocontenido) y ejecutar Playwright (sale con su exit code)
cd infra/compose
docker compose -f docker-compose.e2e.yml up --build \
  --abort-on-container-exit --exit-code-from playwright

# limpiar (incluye volumenes efimeros)
docker compose -f docker-compose.e2e.yml down -v
```

El informe HTML queda en `e2e/playwright-report/`.

## Relacion con `services/system-e2e-tests`

Son complementarios: el modulo Java valida seguridad y contrato HTTP por gateway;
este E2E valida el flujo de UI de punta a punta. No duplican intencion.
