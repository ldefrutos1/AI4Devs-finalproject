# Pruebas manuales de API con Postman (desarrollo local)

Guía para obtener un **access token** del realm **`mtl`** y llamar al **API Gateway** (puerto **8080** por defecto), igual que la SPA. No sustituye los tests automatizados (`system-e2e-tests`, IT, Playwright).

**Referencias:**

- Usuarios de prueba y OIDC en SPA: [jwt-gateway-strategy.md](../security/jwt-gateway-strategy.md) §4.
- Realm importado: [infra/compose/init/keycloak/mtl-realm.json](../../infra/compose/init/keycloak/mtl-realm.json).

---

## Redirect URI y Keycloak

El error **`Invalid parameter: redirect_uri`** aparece cuando la **Callback URL** de Postman no está en **Valid redirect URIs** del cliente **`mtl-spa`** (coincidencia estricta).

| Situación                                              | Callback URL (Postman y Keycloak)              |
| ------------------------------------------------------ | ---------------------------------------------- |
| Postman **escritorio**, “Authorize using browser”      | `https://oauth.pstmn.io/v1/callback`           |
| Postman **web** o callback reciente                    | `https://oauth.pstmn.io/v1/browser-callback`   |

En el realm import del repo ya figuran ambas URIs y `https://oauth.pstmn.io` en **Web origins**. Si el realm es antiguo (volumen previo), añádelas en consola: **Clients → mtl-spa → Save**.

---

## Obtener el token en Postman

1. Petición de prueba (p. ej. `GET {{gateway}}/api/catalog/species`).
2. **Authorization** → **OAuth 2.0** → **Get New Access Token**.
3. Valores típicos (ajusta puerto Keycloak si no es 8180):

   | Campo                     | Valor                                                                      |
   | ------------------------- | -------------------------------------------------------------------------- |
   | **Grant Type**            | Authorization Code (with PKCE)                                           |
   | **Callback URL**          | `https://oauth.pstmn.io/v1/callback`                                       |
   | **Auth URL**              | `http://localhost:8180/realms/mtl/protocol/openid-connect/auth`          |
   | **Access Token URL**      | `http://localhost:8180/realms/mtl/protocol/openid-connect/token`         |
   | **Client ID**             | `mtl-spa`                                                                  |
   | **Client Secret**         | (vacío)                                                                    |
   | **Code Challenge Method** | SHA-256                                                                    |
   | **Scope**                 | `openid profile email`                                                     |

4. **Get New Access Token** → login (`colaborador` / `colaborador_dev` o `admin_mtl` / `admin_mtl_dev`).
5. **Use Token** o variable de colección `{{access_token}}`.

---

## Peticiones al gateway

Variables sugeridas:

- `gateway` → `http://localhost:8080`
- `access_token` → token vigente

Cabeceras:

- `Authorization: Bearer …`
- opcional `X-Correlation-Id` (UUID)

| Método | URL                                                              |
| ------ | ---------------------------------------------------------------- |
| GET    | `{{gateway}}/api/catalog/species?page=0&size=20`                 |
| GET    | `{{gateway}}/api/catalog/species?page=0&size=5&q=cina`           |
| GET    | `{{gateway}}/api/catalog/provinces?page=0&size=20`               |
| GET    | `{{gateway}}/api/catalog/provinces?page=0&size=5&q=01`           |

Query opcionales: `q`, `unpaged=true` (según contrato). OpenAPI: [openapi.yaml](../api/openapi.yaml).

---

## Depuración directa al microservicio

Misma cabecera `Authorization` contra **`http://localhost:8081`** (`catalog-service`), mismas rutas bajo `/api/catalog/…`. Útil para aislar gateway vs servicio.
