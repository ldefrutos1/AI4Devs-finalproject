# Arranque local (guía operativa)

Complemento de [readme.md §2.4](../../readme.md#24-instrucciones-de-instalación): prerrequisitos, comandos concretos, puertos, usuarios de prueba e incidencias frecuentes. Normativa ampliada del backend: [services/README.md](../../services/README.md). Compose e infra: [infra/compose/README.md](../../infra/compose/README.md). OIDC en la SPA: [frontend/README.md](../../frontend/README.md).

## Prerrequisitos

- **Docker** Compose v2 (Postgres, Mongo, Redis, Kafka, MinIO, Keycloak, Mailpit, observabilidad).
- **Java 21** y **Maven** (microservicios Spring Boot 4).
- **Node.js** (frontend Vue 3 + Vite).

## 1. Infraestructura (Compose)

```bash
cd infra/compose
cp .env.example .env          # Windows: copy .env.example .env
docker compose up -d
```

Postgres queda en **`localhost:5433`** por defecto (ver `.env.example`). Keycloak: **http://localhost:8180**. Mailpit UI: **http://localhost:8025**.

## 2. Microservicios (Maven, perfil `dev`)

Los servicios **no** van en el Compose: se ejecutan en el host (IDE o terminal). Usa **una terminal por microservicio**. Comandos desde la **raíz del monorepo**:

```bash
mvn -f services/pom.xml -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=dev
mvn -f services/pom.xml -pl catalog-service spring-boot:run -Dspring-boot.run.profiles=dev
```

Sustituye el módulo según el flujo (`media-service`, `notification-service`, `ai-assistant-service`, …). Equivalente estando en `services/`:

```bash
cd services
mvn -pl catalog-service spring-boot:run -Dspring-boot.run.profiles=dev
```

**Siempre** arranca **api-gateway** (8080) antes de probar la SPA. El perfil **`dev`** conecta a Postgres, Redis, Kafka, MinIO y Mailpit del Compose según cada servicio.

| Módulo Maven | Puerto HTTP |
|--------------|-------------|
| api-gateway | 8080 |
| catalog-service | 8081 |
| media-service | 8082 |
| notification-service | 8083 |
| ai-assistant-service | 8084 |

### Qué levantar según el flujo

| Flujo | Compose adicional | Terminales Maven (`dev`) |
|-------|-------------------|---------------------------|
| Consulta pública | — | api-gateway, catalog-service |
| Alta / edición de árbol | Redis, Kafka | api-gateway, catalog-service (+ **media-service** si hay fotos) |
| Fotos (subida) | MinIO | api-gateway, media-service (+ catalog si la ficha ya existe) |
| Aviso por correo (alta) | Kafka, Mailpit | api-gateway, catalog-service, notification-service |
| Admin (maestros / suscripciones) | — | api-gateway, catalog-service; notification-service (suscripciones) |
| Consulta IA especie (ADMIN, stub) | — | api-gateway, catalog-service, ai-assistant-service |

> **Redis:** **catalog-service** en `dev` usa caché Redis; el contenedor Redis debe estar en marcha **antes** de arrancarlo.

### Datos iniciales (Flyway)

**catalog-service** aplica semillas de maestros (familia, género, especie, provincia) al arrancar. Mantenimiento taxonómico en app solo **ADMIN** (**HU-011**); **provincias** solo por semillas en el MVP. Reset de esquema en desarrollo: [flyway-dev-reset.md](../engineering/flyway-dev-reset.md).

## 3. Frontend (Vite)

```bash
cd frontend
cp .env.example .env          # Windows: copy .env.example .env
npm install
npm run dev
```

UI en **http://localhost:5173**. Vite reenvía `/api/*` al gateway (**8080**); no hace falta definir `VITE_GATEWAY_BASE_URL` en el caso habitual.

## Usuarios de prueba (Keycloak, solo local)

| Usuario | Contraseña | Roles |
|---------|------------|-------|
| `colaborador` | `colaborador_dev` | COLABORADOR |
| `admin_mtl` | `admin_mtl_dev` | ADMIN, COLABORADOR |

Detalle e import del realm: [infra/compose/README.md § Keycloak](../../infra/compose/README.md).

## Incidencias frecuentes

| Síntoma | Comprobación |
|---------|----------------|
| Catalog no arranca / timeout JDBC | Postgres en marcha; puerto **5433** en `.env` y JDBC del servicio |
| 401 / login falla | Keycloak **8180**; redirect URIs `http://localhost:5173/*` en cliente `mtl-spa` |
| Alta OK, sin correo en Mailpit | Kafka + Mailpit en Compose; **notification-service** en `dev`; suscriptor **ACTIVA** |
| Subida de foto falla (CORS) | MinIO en Compose; CORS en `.env` (`MINIO_API_CORS_ALLOW_ORIGIN`) |
| Puertos ocupados | `.\scripts\dev\check-ports.ps1` desde la raíz del repo |

Observabilidad (Prometheus/Grafana): [platform/observability/README.md](../../platform/observability/README.md).
