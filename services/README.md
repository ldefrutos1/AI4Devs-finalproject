# Backend Java (MyTreeLibrary)

**Maven:** `services/pom.xml` (`com.mtl:mtl-parent`, Spring Boot 4.0.x, Java 21). **Normas de código:** `.cursor/rules/spring-boot-4-backend.mdc`, `backend-generation-standard.mdc`, `microservices-patterns.mdc`, `api-contract.mdc`, `api-security.mdc`.

## 1. Arranque local

Guía operativa (flujos, accesos, incidencias): [local-setup-guide.md](../docs/onboarding/local-setup-guide.md). Infra Docker: [infra/compose/README.md](../infra/compose/README.md).

### Dónde ejecutar Maven

El reactor está en **`services/pom.xml`**. Lanza `mvn -pl …` **desde `services/`** (no desde dentro de un módulo suelto).

```bash
cd services
mvn verify
mvn -pl catalog-service spring-boot:run -Dspring-boot.run.profiles=dev
```

Desde la raíz del monorepo: `mvn -f services/pom.xml -pl catalog-service spring-boot:run -Dspring-boot.run.profiles=dev`.

### Spring Boot DevTools (IDE)

Restart al cambiar `.class` en `target/classes`. [`.vscode/settings.json`](../.vscode/settings.json) activa autocompilación Maven en Cursor/VS Code. Si no reinicia: **Java: Force Java Compilation** o `mvn -f services/pom.xml -pl <módulo> compile`.

### Perfil `dev`

Actívalo con `-Dspring-boot.run.profiles=dev`, `--spring.profiles.active=dev` o `SPRING_PROFILES_ACTIVE=dev`. JDBC, Kafka, Redis, Mailpit y Mongo apuntan al Compose local (Postgres **`localhost:5433`** por defecto — ver [infra/compose/README.md](../infra/compose/README.md)).

**Kafka** (`catalog-service`, `notification-service`): topic `catalog.ejemplar.evento`; bootstrap `localhost:9094` en host. Contrato: [kafka-events.md](../docs/events/kafka-events.md).

**Redis** (`catalog-service`): caché activa en `dev` (`CatalogCacheConfig`); requiere Redis en Compose.

**Flyway:** scripts en `src/main/resources/db/migration/` (`V1__…`, `V2__…`). Reset en desarrollo: [flyway-dev-reset.md](../docs/engineering/flyway-dev-reset.md).

**Tests:** `mvn test` (Surefire); `mvn verify` añade Failsafe (`*IT`). Detalle, Docker e IT: [testing-java.md](../docs/engineering/testing-java.md).

### Puertos HTTP (desarrollo)

| Módulo | Puerto |
|--------|--------|
| api-gateway | 8080 |
| catalog-service | 8081 |
| media-service | 8082 |
| notification-service | 8083 |
| ai-assistant-service | 8084 |

Actuator (`/actuator/health`, `/actuator/prometheus`) y stack Prometheus/Grafana: [platform/observability/README.md](../platform/observability/README.md).

---

## 2. Perfil `prod` y variables de entorno JDBC

Con **`SPRING_PROFILES_ACTIVE=prod`**, los `application-prod.properties` **no** incluyen URLs ni contraseñas: inyectarlas desde el orquestador.

### Variables obligatorias por servicio (JDBC)

| Servicio | Esquema Flyway / JPA | Variables requeridas |
|----------|----------------------|----------------------|
| **catalog-service** | `catalog` | `MTL_DATASOURCE_URL`, `MTL_DATASOURCE_USERNAME`, `MTL_DATASOURCE_PASSWORD` |
| **media-service** | `media` | Idem |
| **notification-service** | `notification` | Idem |
| **ai-assistant-service** | `ai` | Idem |

**Formato típico de URL** (una instancia PostgreSQL, varios esquemas):

```text
MTL_DATASOURCE_URL=jdbc:postgresql://<host>:5432/mtl
MTL_DATASOURCE_USERNAME=<usuario_aplicacion>
MTL_DATASOURCE_PASSWORD=<secreto>
```

En **catalog-service**, `application-prod.properties` fija `spring.datasource.hikari.connection-init-sql=SET search_path TO catalog, public`.

### Otras variables habituales en `prod`

| Variable | Servicios | Uso |
|----------|-----------|-----|
| `MTL_JWT_ISSUER_URI` | Todos con OAuth2 resource server | Issuer Keycloak (sin default en prod) |
| `MTL_KAFKA_BOOTSTRAP_SERVERS` | catalog, notification | Bootstrap Kafka |
| `MTL_CATALOG_KAFKA_ENABLED` | catalog | Publicación de eventos (`true`/`false`) |
| `MTL_NOTIFICATION_KAFKA_ENABLED` | notification | Consumo de eventos |
| `MTL_REDIS_HOST`, `MTL_REDIS_PORT`, `MTL_CACHE_TYPE` | catalog | Caché Redis (por defecto `redis` en prod) |
| `MTL_MEDIA_BASE_URL` | catalog | Cliente HTTP hacia media-service |
| `MTL_CATALOG_MONGO_ENABLED`, `SPRING_MONGODB_URI` | catalog | Enriquecimiento Mongo |
| `MTL_CATALOG_BASE_URL` | media | Permiso de subida vía catálogo |
| `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD`, `MTL_MEDIA_STORAGE_ENDPOINT` | media | Almacenamiento S3/MinIO |
| `MTL_NOTIFICATION_MAIL_*` | notification | SMTP saliente |
| `MTL_AI_PROVIDER_MODE` | ai-assistant | `stub` (local/tests) o `openai` (prod) |
| `MTL_OPENAI_API_KEY` / `OPENAI_API_KEY` | ai-assistant | Clave OpenAI; **nunca** en Git. Obligatoria si `MTL_AI_PROVIDER_MODE=openai` |
| `MTL_OPENAI_*` (opc.) | ai-assistant | Base URL, modelo, timeouts y reintentos (ver `application.properties`) |

**OpenAI real en local:** [ai-assistant-service/README.md](ai-assistant-service/README.md) § Modo proveedor.

**Flyway en prod:** `spring.flyway.clean-disabled=true` y `spring.flyway.create-schemas=false`. Solo **`validate`** en JPA (`ddl-auto`).

**Local vs prod:** en **`dev`**, JDBC a `localhost:5433` (Compose). En **`prod`**, `MTL_DATASOURCE_*` sustituye esa configuración.

---
