# Infraestructura local (Docker Compose)

Entorno de **desarrollo** alineado con [readme.md](../../readme.md). Tres ficheros Compose: infra base, overlay de aplicación y stack E2E (§ Ficheros Compose).

## Requisitos

- Docker Engine y Docker Compose v2.

## Arranque

```bash
cd infra/compose
copy .env.example .env
docker compose up -d
```

En Unix: `cp .env.example .env`.

La primera vez, Postgres ejecuta los scripts en `init/postgres/` (esquemas, PostGIS, BD `keycloak` y rol `keycloak`). **`kafka-init`** crea el topic `catalog.ejemplar.evento`. **`minio-init`** crea el bucket `mtl-photos` (§ MinIO). **`mailpit`** expone SMTP de prueba (§ Mailpit).

Para la aplicación en contenedor usa [docker-compose.apps.yml](docker-compose.apps.yml) (§ Ficheros Compose). Los microservicios en el host (IDE/Maven) no están en este compose — [services/README.md](../../services/README.md).

## Ficheros Compose

| Fichero | Rol |
|---------|-----|
| [docker-compose.yml](docker-compose.yml) | Infra: Postgres, Mongo, Redis, MinIO, Kafka, Keycloak, Mailpit, Prometheus, Grafana |
| [docker-compose.apps.yml](docker-compose.apps.yml) | **Overlay** de aplicación sobre el anterior |
| [docker-compose.e2e.yml](docker-compose.e2e.yml) | Stack **autónomo** y efímero para E2E (no extiende los otros) |

### docker-compose.apps.yml

Añade **frontend** (Nginx + proxy `/api`), **api-gateway** y **catalog**, **media**, **notification**, **ai-assistant**. Imágenes `mtl/<servicio>:${MTL_IMAGE_TAG:-local}` — build con [build-images.ps1](../../scripts/dev/build-images.ps1).

Ajustes sobre infra: **MinIO** (CORS `8088`, presign `127.0.0.1:9000`, § MinIO) y **Prometheus** ([prometheus-docker.yml](../../platform/observability/prometheus/prometheus-docker.yml), scrape por DNS interno).

Requisitos: infra levantada (`docker compose up -d`) e imágenes construidas.

```powershell
# Desde la raíz
.\scripts\dev\start-docker-stack.ps1              # build + infra + apps
.\scripts\dev\start-docker-stack.ps1 -SkipBuild   # sin rebuild de imágenes
.\scripts\dev\start-docker-stack.ps1 -InfraOnly   # solo infra
.\scripts\dev\start-docker-stack.ps1 -AppsOnly    # solo apps
.\scripts\dev\start-docker-stack.ps1 -Down        # bajar todo (-KeepVolumes conserva datos)
```

Manual (`infra/compose`):

```bash
docker compose up -d
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --scale catalog-service=2   # varias réplicas catalog
```

**Réplicas de `catalog-service`:** añade `--scale catalog-service=N` al `up` de apps (p. ej. `N=2`). Sin `--scale`, una sola réplica. El gateway y media siguen con `http://catalog-service:8080`; Docker reparte el tráfico. Comprobar: `docker compose -f docker-compose.yml -f docker-compose.apps.yml ps catalog-service`. Tras cambiar observabilidad o escalar: `docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --force-recreate prometheus grafana`. En Grafana (**MTL Microservices**), filtra **Instancia** para ver cada réplica; el panel UP solo muestra targets `environment=docker` (no mezcla con `host.docker.internal:8081` del perfil Maven en host).

| Acceso | URL |
|--------|-----|
| SPA | `http://localhost:8088` (`FRONTEND_PORT`) |
| API Gateway | `http://localhost:8080` |
| Keycloak | `http://localhost:8180` (infra) |

OIDC: issuer `http://localhost:8180/realms/mtl` en el navegador; backends validan el mismo `iss` y obtienen JWKS vía `host.docker.internal`. Realm con redirect URIs `8088` en [mtl-realm.json](init/keycloak/mtl-realm.json) — si el realm ya existía en el volumen, añádelas en consola o recrea volúmenes.

### docker-compose.e2e.yml

Stack mínimo **autocontenido** (proyecto `mtl-e2e`) para el flujo login → alta → mis árboles → borrado. **No** incluye MinIO, notification ni ai-assistant. **No** publica puertos en el host.

| Componente | Notas |
|------------|-------|
| Datos | Postgres, Mongo, Kafka, Redis en **tmpfs** (efímeros) |
| Keycloak | H2 en memoria, realm **`mtl-e2e`** ([init/keycloak-e2e/](init/keycloak-e2e/)) |
| Apps | `catalog-service`, `media-service`, `api-gateway`, frontend |
| Tests | `system-e2e-tests` (HTTP/JWT) → **Playwright** (UI) |

Guía canónica: [testing-e2e.md](../../docs/engineering/testing-e2e.md).

```powershell
# Desde la raíz
.\scripts\dev\test-e2e.ps1
.\scripts\dev\test-e2e.ps1 -SkipBuild -KeepStack   # depurar stack levantado
.\scripts\dev\test-e2e.ps1 -Local                   # Playwright contra entorno ya arriba (p. ej. Vite :5173)
```

Manual (`infra/compose`, jars compilados):

```bash
docker compose -f docker-compose.e2e.yml up --build --abort-on-container-exit --exit-code-from playwright
docker compose -f docker-compose.e2e.yml down -v
```

El exit code de **Playwright** decide éxito o fallo del pipeline.

## Puertos por defecto

| Servicio   | Puerto host | Uso |
|------------|-------------|-----|
| PostgreSQL | 5433 (host; por defecto en `.env.example` para no usar 5432 si ya tienes otro Postgres local) | JDBC `jdbc:postgresql://localhost:5433/mtl` (usuario `POSTGRES_USER`; dentro del contenedor el puerto sigue siendo 5432) |
| MongoDB    | 27017       | URI con `authSource=admin` |
| Redis      | 6379        | — |
| MinIO API  | 9000        | S3 API |
| MinIO consola | 9001   | Interfaz web |
| Kafka (desde el host) | 9094 | `bootstrap.servers=localhost:9094` |
| Keycloak   | 8180        | Consola `http://localhost:8180` |
| Mailpit SMTP (desde el host) | 1025 (`MAILPIT_SMTP_PORT`) | `spring.mail.host=localhost`, `spring.mail.port` = este puerto (notification-service en dev) |
| Mailpit UI | 8025 (`MAILPIT_UI_PORT`) | Bandeja de mensajes capturados: `http://localhost:8025` |
| Prometheus | 9090 (`PROMETHEUS_PORT`) | Métricas: `http://localhost:9090` (targets en **Status → Targets**) |
| Grafana | 3000 (`GRAFANA_PORT`) | Dashboards: `http://localhost:3000` (credenciales `GRAFANA_ADMIN_*` en `.env`) |
| API Gateway (`docker-compose.apps.yml`) | 8080 | Gateway publicado al host |
| Frontend Docker (`docker-compose.apps.yml`) | 8088 (`FRONTEND_PORT`) | SPA Nginx: `http://localhost:8088` |

Dentro de la red Docker, Kafka PLAINTEXT: `kafka:9092`. El SMTP de Mailpit dentro de Compose: `mailpit:1025`.

## Observabilidad (Prometheus + Grafana)

Stack según [ADR-0005](../../docs/adr/0005-microservices-observability-spring-boot.md). Configuración en [platform/observability/](../../platform/observability/README.md).

Prometheus hace **scrape** de microservicios en el **host** (`localhost:8080`–`8084`, `mvn spring-boot:run`) o, con [docker-compose.apps.yml](docker-compose.apps.yml), por DNS interno (`prometheus-docker.yml`).

Descargar imágenes y levantar solo observabilidad:

```bash
cd infra/compose
docker compose pull prometheus grafana
docker compose up -d prometheus grafana
```

Los microservicios deben estar en marcha en el host para que los cinco targets aparezcan **UP** en Prometheus.

## Mailpit (SMTP de prueba, HU-007)

**Mailpit** recibe correo por SMTP y los muestra en la **interfaz web**; no entrega a dominios reales. Sirve para desarrollar **TASK-HU-007-04** (`notification-service`) sin credenciales de relay externos.

1. Arranque: `docker compose up -d` (el servicio `mailpit` queda en la red `mtl`).
2. **Ver correos:** abre `http://localhost:${MAILPIT_UI_PORT:-8025}` en el navegador.
3. **Microservicio en el host (IDE / `mvn spring-boot:run`):** apunta el cliente SMTP a `localhost` y al puerto **`MAILPIT_SMTP_PORT`** (por defecto **1025**). Sin auth TLS típico en local.
4. **Microservicio dentro de Docker** (si algún día se conteneriza): host SMTP `mailpit`, puerto **1025** en la red `mtl`.

Imagen: `axllent/mailpit` (versión fijada en [docker-compose.yml](docker-compose.yml)).

## MinIO y subida de fotos

Presign → `PUT` directo desde el navegador. Detalle: [media-upload-hu006.md](../../docs/engineering/media-upload-hu006.md).

| Pieza | Comportamiento |
|-------|----------------|
| **`minio-init`** | Crea `mtl-photos` al arrancar (`docker compose run --rm minio-init` si borraste el volumen). |
| **CORS** | Global en MinIO community (`MINIO_API_CORS_ALLOW_ORIGIN`). Solo infra: Vite `5173`; con [docker-compose.apps.yml](docker-compose.apps.yml): también `8088`. |
| **Presign (navegador)** | El overlay apps fija `MINIO_SERVER_URL` y `MTL_MEDIA_STORAGE_PUBLIC_ENDPOINT` en **`http://127.0.0.1:9000`**. En Windows+Docker, `localhost:9000` suele colgar (IPv6). |
| **Consola** | `http://localhost:9001` — credenciales `MINIO_ROOT_*` del `.env`. |

Tras cambiar CORS o endpoint público:

```powershell
cd infra/compose
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --force-recreate minio media-service
```

Vite en el host (`5173`) + MinIO en Docker: `mtl.media.storage.public-endpoint=http://127.0.0.1:9000` en `application-dev.properties`.

## Credenciales de desarrollo

Valores por defecto pensados **solo para local**; están en `.env.example` y en el SQL de init de Keycloak. Si cambias `KEYCLOAK_DB_PASSWORD` en `.env`, actualiza la misma cadena en `init/postgres/01-init.sql` y **elimina el volumen** `mtl_pgdata` antes de volver a levantar.

## Detener y borrar datos

```bash
docker compose down
docker compose down -v
```

`-v` elimina volúmenes (Postgres, Mongo, Kafka, etc.).

## Keycloak (realm `mtl` y JWT)

### `start-dev` solo para desarrollo

El comando **`start-dev`** de Keycloak está pensado para **entorno local**: configuración relajada, sin el endurecimiento operativo de producción. **No** usar esta imagen/comando tal cual en producción; allí se aplicará TLS, hostname estricto, modo de arranque y secretos según el despliegue (véase [readme.md](../../readme.md) §2.4–2.5).

### Import del realm al levantar Compose

- Los ficheros JSON en [init/keycloak/](init/keycloak/) se montan en `/opt/keycloak/data/import` y Keycloak arranca con **`--import-realm`**: se importa el realm **`mtl`** cuando la base de datos de Keycloak está vacía (primera vez tras crear el volumen de Postgres que contiene la BD `keycloak`).
- Si el realm **ya existe**, Keycloak aplica la estrategia configurada para imports (p. ej. `IGNORE_EXISTING`): puede **omitir** sobrescribir un realm ya presente. Para **forzar** una reimportación coherente con un JSON nuevo, suele hacer falta eliminar datos de la BD `keycloak` o usar export/import desde la consola de administración según la documentación de Keycloak.

### Parámetros útiles para front y back

| Concepto | Valor por defecto (dev) |
|----------|-------------------------|
| Realm | `mtl` |
| Issuer (URL absoluta, host) | `http://localhost:8180/realms/mtl` (puerto = `KEYCLOAK_PORT`, por defecto 8180) |
| Cliente SPA (público, PKCE) | `mtl-spa` |
| Redirect URIs | Vite: `http://localhost:5173/*`, `http://127.0.0.1:5173/*`. Frontend Docker: `http://localhost:8088/*`, `http://127.0.0.1:8088/*`. Postman OAuth: `https://oauth.pstmn.io/v1/callback`, `https://oauth.pstmn.io/v1/browser-callback` (misma cadena en el campo **Callback URL** de Postman que en Keycloak) |
| Web origins | `http://localhost:5173`, `http://127.0.0.1:5173`, `http://localhost:8088`, `http://127.0.0.1:8088`, `https://oauth.pstmn.io` |
| Roles de realm | `COLABORADOR`, `ADMIN` (asignar a usuarios desde la consola de administración) |
| Scopes OIDC (`mtl-spa`) | `fullScopeAllowed: true` en dev: el realm importado hereda los *default client scopes* estándar de Keycloak (incluye **`roles`** → `realm_access.roles` en el access token, y **`profile`** / **`email`**) |
| Uso con **catalog-service** (`POST /api/catalog/trees`) | El access token debe traer al menos **`email`** y datos de perfil para `nombre` (`name` o `given_name`/`family_name`). En la SPA OIDC, pedir explícitamente `scope=openid profile email` al obtener el token (véase [ADR-0004](../../docs/adr/0004-catalog-rest-write-and-audit.md)). |
| PKCE | Obligatorio **S256** (`pkce.code.challenge.method` en el cliente) |
| Post-logout redirect | Mismos orígenes Vite que redirect URIs (`post.logout.redirect.uris` en el cliente) |

Consola de administración: `http://localhost:8180` (credenciales de arranque `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` del `.env`).

### Verificación TASK-HU-001-01 (realm + cliente SPA)

Criterios de cierre para [HU-001-ticket-breakdown](../../docs/backlog/HU-001-ticket-breakdown.md) — **TASK-HU-001-01**:

1. **Descubrimiento OIDC** (Keycloak levantado, `KEYCLOAK_PORT` por defecto 8180):

   ```bash
   curl -s http://localhost:8180/realms/mtl/.well-known/openid-configuration | head
   ```

   Debe existir `issuer` `http://localhost:8180/realms/mtl` (o el host/puerto que uses) y endpoints `authorization_endpoint`, `token_endpoint`, `jwks_uri`.

2. **Consola (realm `mtl` → Clients → `mtl-spa`)**: Client authentication **Off** (público); **Standard flow** activo; **Direct access grants** desactivado; **Valid redirect URIs** y **Web origins** como en la tabla superior; **PKCE Method** S256; **Client scopes** → *Assigned default client scopes*: debe figurar el alcance **`roles`** (y típicamente `profile`, `email`, `web-origins`, `acr`), coherente con [mtl-realm.json](init/keycloak/mtl-realm.json).

3. **Token con roles**: Tras un login real (Authorization Code + PKCE desde la SPA o flujo manual en navegador), el **access token** JWT debe incluir **`realm_access.roles`** con los roles de realm del usuario (p. ej. `COLABORADOR` o `ADMIN`). Compruébalo en una herramienta de inspección de JWT (sin pegar tokens en chats públicos).

4. **Cambios en `mtl-realm.json`**: no se aplican solos a un realm ya importado; hace falta **reimportación** (véase arriba «Import del realm») o ajuste manual en consola hasta el siguiente `docker compose down -v` + arranque limpio.

### Usuarios del realm `mtl` (solo desarrollo)

Definidos en [init/keycloak/mtl-realm.json](init/keycloak/mtl-realm.json); se crean en el **primer import** del realm (BD `keycloak` vacía). Contraseñas **débiles a propósito**: no reutilizar fuera de local.

| Usuario realm `mtl` | Contraseña | Roles realm |
|---------------------|------------|-------------|
| `colaborador` | `colaborador_dev` | `COLABORADOR` |
| `admin_mtl` | `admin_mtl_dev` | `ADMIN`, `COLABORADOR` |

`admin_mtl` lleva también `COLABORADOR` para poder probar flujos de colaborador con la misma cuenta (alineado con la generalización Administrador → Colaborador en el modelo de casos de uso).

Si el realm **ya existía** y no ves estos usuarios (import `IGNORE_EXISTING`), borra el realm `mtl` desde la consola de administración de Keycloak y reinicia el contenedor, o bien crea los usuarios a mano y asigna los roles.

Estrategia de validación JWT en gateway y microservicios: [docs/security/jwt-gateway-strategy.md](../../docs/security/jwt-gateway-strategy.md). El gateway lee el issuer desde **`MTL_JWT_ISSUER_URI`** (por defecto la misma URL absoluta que la tabla de arriba); variables de enrutado hacia microservicios: [services/README.md](../../services/README.md) (sección API Gateway).

## Notas

- **Desarrollo habitual (host):** microservicios con IDE/Maven; infra con `docker compose up -d`. Puertos en [services/README.md](../../services/README.md).
- **Aplicación contenerizada:** § `docker-compose.apps.yml`. **E2E:** § `docker-compose.e2e.yml`.
- **Keycloak** en modo `start-dev`; realm `mtl` importado al primer arranque (§ Keycloak).
- **Mailpit:** § Mailpit y tabla de puertos.
