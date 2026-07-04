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

La primera vez, Postgres ejecuta los scripts en `init/postgres/` (esquemas, PostGIS, BD `keycloak` y rol `keycloak`). **`kafka-init`** crea el topic `catalog.ejemplar.evento`. **`minio-init`** crea el bucket `mtl-photos` (§ MinIO). **`mailpit`** expone SMTP de prueba (puertos en § Puertos).

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

Login OIDC/JWT: § Keycloak; detalle en [jwt-gateway-strategy.md](../../docs/security/jwt-gateway-strategy.md) §4–5.

### docker-compose.e2e.yml

Stack mínimo **autocontenido** (proyecto `mtl-e2e`) para el flujo login → alta → mis árboles → borrado. **No** incluye MinIO, notification ni ai-assistant. **No** publica puertos en el host.

| Componente | Notas |
|------------|-------|
| Datos | Postgres, Mongo, Kafka, Redis en **tmpfs** (efímeros) |
| Keycloak | H2 en memoria, realm **`mtl-e2e`** ([init/keycloak-e2e/](init/keycloak-e2e/)) |
| Apps | `catalog-service`, `media-service`, `api-gateway`, frontend |
| Tests | `system-e2e-tests` (HTTP/JWT) → **Playwright** (UI) |

Guía y comandos: [testing-e2e.md](../../docs/engineering/testing-e2e.md). Atajo: `.\scripts\dev\test-e2e.ps1` desde la raíz.

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
| Prometheus | 9090 (`PROMETHEUS_PORT`) | Métricas: `http://localhost:9090` |
| Grafana | 3000 (`GRAFANA_PORT`) | Dashboards: `http://localhost:3000` (credenciales `GRAFANA_ADMIN_*` en `.env`) |
| API Gateway (`docker-compose.apps.yml`) | 8080 | Gateway publicado al host |
| Frontend Docker (`docker-compose.apps.yml`) | 8088 (`FRONTEND_PORT`) | SPA Nginx: `http://localhost:8088` |

Dentro de la red Docker: Kafka PLAINTEXT `kafka:9092`; Mailpit SMTP `mailpit:1025`.

Prometheus y Grafana: [platform/observability/README.md](../../platform/observability/README.md).

## MinIO y subida de fotos

Presign → `PUT` directo desde el navegador. Contrato HTTP: [openapi.yaml](../../docs/api/openapi.yaml); historia y criterios: [HU-006](../../docs/backlog/HU-006-fotografias-asociadas-al-arbol.md).

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

## Credenciales de desarrollo

Valores por defecto pensados **solo para local**; están en `.env.example` y en el SQL de init de Keycloak. Si cambias `KEYCLOAK_DB_PASSWORD` en `.env`, actualiza la misma cadena en `init/postgres/01-init.sql` y **elimina el volumen** `mtl_pgdata` antes de volver a levantar.

## Detener y borrar datos

```bash
docker compose down
docker compose down -v
```

`-v` elimina volúmenes (Postgres, Mongo, Kafka, etc.).

## Keycloak (realm `mtl`)

### `start-dev` solo para desarrollo

El comando **`start-dev`** de Keycloak está pensado para **entorno local**: configuración relajada, sin el endurecimiento operativo de producción. **No** usar esta imagen/comando tal cual en producción; allí se aplicará TLS, hostname estricto, modo de arranque y secretos según el despliegue (véase [readme.md](../../readme.md) §2.4–2.5).

### Import del realm al levantar Compose

- Los ficheros JSON en [init/keycloak/](init/keycloak/) se montan en `/opt/keycloak/data/import` y Keycloak arranca con **`--import-realm`**: se importa el realm **`mtl`** cuando la base de datos de Keycloak está vacía (primera vez tras crear el volumen de Postgres que contiene la BD `keycloak`).
- Si el realm **ya existe**, Keycloak aplica la estrategia configurada para imports (p. ej. `IGNORE_EXISTING`): puede **omitir** sobrescribir un realm ya presente. Para **forzar** una reimportación coherente con un JSON nuevo, suele hacer falta eliminar datos de la BD `keycloak` o usar export/import desde la consola de administración según la documentación de Keycloak.

Realm **`mtl`**, cliente **`mtl-spa`**, issuer y redirect URIs: [init/keycloak/mtl-realm.json](init/keycloak/mtl-realm.json) y [jwt-gateway-strategy.md](../../docs/security/jwt-gateway-strategy.md) §4–5. Consola: `http://localhost:8180` (`KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` del `.env`).

### Usuarios del realm `mtl` (solo desarrollo)

Definidos en [init/keycloak/mtl-realm.json](init/keycloak/mtl-realm.json); se crean en el **primer import** del realm (BD `keycloak` vacía). Contraseñas **débiles a propósito**: no reutilizar fuera de local.

| Usuario realm `mtl` | Contraseña | Roles realm |
|---------------------|------------|-------------|
| `colaborador` | `colaborador_dev` | `COLABORADOR` |
| `admin_mtl` | `admin_mtl_dev` | `ADMIN`, `COLABORADOR` |

Si el realm **ya existía** y no ves estos usuarios (import `IGNORE_EXISTING`), borra el realm `mtl` desde la consola de administración de Keycloak y reinicia el contenedor, o bien crea los usuarios a mano y asigna los roles.
