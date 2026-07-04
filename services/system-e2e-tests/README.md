# system-e2e-tests

HTTP contra el **API Gateway** y microservicios reales (JWT Keycloak, sin WireMock).

- **Diseño del módulo:** [testing-java.md](../../docs/engineering/testing-java.md) §2.1.2  

## Requisitos

Infra Compose (Postgres, Keycloak) y microservicios Maven en perfil **`dev`** según la clase IT. Sin stack levantado los tests quedan deshabilitados o fallan con 401/502.

El `iss` del token debe coincidir con `MTL_JWT_ISSUER_URI` (por defecto `http://localhost:8180/realms/mtl`); no mezclar `localhost` y `127.0.0.1` al obtener el token.

### Stack Maven por clase

| Clase / patrón | Servicios `dev` (además de Compose + **api-gateway** **8080**) |
|----------------|----------------------------------------------------------------|
| `Hu001Scenario02…` – `04…` | **catalog-service** **8081** |
| `Hu005Task03MastersReadGatewayE2EIT` | **catalog-service** **8081** |
| `Hu005Hu008CollaboratorTreeLifecycleGatewayE2EIT` | **catalog-service** **8081**, **media-service** **8082** |
| `Hu010Scenario01…` | **catalog-service** **8081** (alta borrador para `treeId`), **ai-assistant-service** **8084** (`stub` por defecto) |
| `Hu010Scenario03…` | **ai-assistant-service** **8084** (ruta `/api/ai/**` en gateway) |

Arranque por flujo: [local-setup-guide.md](../../docs/onboarding/local-setup-guide.md). Verificación manual HU-010: [frontend/README.md](../../frontend/README.md) (apartado HU-010).

## Variables de entorno

| Variable | Uso |
|----------|-----|
| `MTL_E2E_TOKEN_COLABORADOR` | Token manual (preferido) |
| `MTL_E2E_ACCESS_TOKEN` | Legacy (misma prioridad que colaborador si falta el anterior) |
| `MTL_E2E_AUTO_KEYCLOAK_TOKEN=true` | Token automático vía Admin API; activa `directAccessGrants` en `mtl-spa` solo durante el IT (dev) |
| `MTL_E2E_RUN_SECURITY=true` | Habilita esc. 3 sin token en env (stack arriba) |
| `MTL_E2E_GATEWAY_BASE_URL` | Default `http://127.0.0.1:8080` |
| `MTL_KEYCLOAK_BASE_URL` | Default derivado de `MTL_JWT_ISSUER_URI` |
| `MTL_KEYCLOAK_REALM` | Opcional; si falta, se deduce del sufijo `/realms/<realm>` de `MTL_JWT_ISSUER_URI` |
| `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` | Para modo automático (compose: `admin` / `admin_dev_password`) |

Sin token ni `MTL_E2E_AUTO_KEYCLOAK_TOKEN`, los `*GatewayE2EIT` quedan deshabilitados (`@EnabledIf`); `mvn verify` sigue en verde (smoke unitario).

## Ejecución

Desde `services/`:

```powershell
$env:MTL_E2E_AUTO_KEYCLOAK_TOKEN = "true"
mvn -pl system-e2e-tests "-Dit.test=Hu001Scenario*" verify          # HU-001 esc. 2–4
mvn -pl system-e2e-tests "-Dit.test=Hu010Scenario*" verify          # HU-010 esc. 1 y 3 (Java)
mvn -pl system-e2e-tests "-Dit.test=Hu005Task03*" verify          # HU-005 TASK-005-03
mvn -pl system-e2e-tests verify                                      # todos los IT habilitados
```

```bash
export MTL_E2E_TOKEN_COLABORADOR="<token>"
mvn -pl system-e2e-tests verify
```

## Clases y escenarios

| Clase | Comprueba |
|-------|-----------|
| `Hu001Scenario02…` | Esc. 2: COLABORADOR → species **200**; eco `X-Correlation-Id` |
| `Hu001Scenario03…` | Esc. 3: sin Bearer / Bearer inválido / Bearer **expirado** (este último requiere `MTL_E2E_AUTO_KEYCLOAK_TOKEN=true`) → **401** Problem + correlación |
| `Hu001Scenario04…` | Esc. 4: COLABORADOR → `families`, `species/1` → **403** Problem + correlación (id `1` en semilla) |
| `Hu005Task03MastersReadGatewayE2EIT` | **HU-005** TASK-005-03: maestros species/provinces, búsqueda `q` / `unaccent` |
| `Hu005Hu008CollaboratorTreeLifecycleGatewayE2EIT` | **HU-005** TASK-005-06 + **HU-008** TASK-008-02/07: alta `POST` → listado `GET` → borrado `DELETE` (204). Complementa Playwright `e2e/`. Requiere **media-service** |
| `Hu010Scenario01…` | **HU-010** esc. 1 (Java): `POST /api/ai/chat/messages` con `treeId` → **200** respuesta orientativa |
| `Hu010Scenario03…` | **HU-010** esc. 3 (Java): sin Bearer / Bearer inválido → **401** Problem + correlación |


Pruebas **manuales** de API con Postman (token OIDC, maestros catálogo): [api-manual-testing-postman.md](../../docs/engineering/api-manual-testing-postman.md) — no forma parte de este módulo Maven.
