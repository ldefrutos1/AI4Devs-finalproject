# Backend Java (MyTreeLibrary)

Reactor Maven: `services/pom.xml` (Spring Boot 4, Java 21). Normas: [spring-boot-4-backend.mdc](../.cursor/rules/spring-boot-4-backend.mdc), [backend-generation-standard.mdc](../.cursor/rules/backend-generation-standard.mdc), [api-contract.mdc](../.cursor/rules/api-contract.mdc), [api-security.mdc](../.cursor/rules/api-security.mdc).

**Tests:** comandos Maven y perfiles en §1; reglas por capa en [testing-java.md](../docs/engineering/testing-java.md). E2E gateway: [system-e2e-tests/README.md](system-e2e-tests/README.md) (`Hu001Scenario*`, `Hu005*`, `Hu010Scenario*`). E2E UI: [testing-e2e.md](../docs/engineering/testing-e2e.md).

Arranque paso a paso (Compose, flujos por HU, frontend): [local-setup-guide.md](../docs/onboarding/local-setup-guide.md).

---

## 1. Arranque local

Ejecuta Maven **desde `services/`** o con `-f services/pom.xml` desde la raíz. No uses `-pl` dentro de un submódulo.

```bash
cd services
mvn verify
mvn -pl catalog-service spring-boot:run -Dspring-boot.run.profiles=dev
```

**Orden:** 1) infra Compose ([infra/compose/README.md](../infra/compose/README.md)) → 2) microservicios con perfil **`dev`** → 3) opcional observabilidad ([platform/observability/README.md](../platform/observability/README.md)).

**Postgres en el host:** puerto **5433** por defecto (`POSTGRES_PORT` en `infra/compose/.env.example`). JDBC en `application-dev.properties` apunta a `localhost:5433/mtl`.

**Flyway:** scripts en `src/main/resources/db/migration/`. Reset en desarrollo: [flyway-dev-reset.md](../docs/engineering/flyway-dev-reset.md).

| Módulo | Puerto |
|--------|--------|
| api-gateway | 8080 |
| catalog-service | 8081 |
| media-service | 8082 |
| notification-service | 8083 |
| ai-assistant-service | 8084 |

**Kafka (catalog-service):** topic `catalog.ejemplar.evento`; bootstrap en host `localhost:9094`. Contrato: [kafka-events.md](../docs/events/kafka-events.md). Propiedades: `mtl.catalog.kafka.enabled`, `mtl.catalog.kafka.ejemplar-evento-topic`, `MTL_KAFKA_BOOTSTRAP_SERVERS`.

**Notificaciones (HU-004, HU-007, HU-012):** la SPA y los E2E usan el **gateway (8080)**, no el puerto **8083** directo. Correo en dev vía Mailpit del Compose. **HU-012:** `GET|PATCH /api/notifications/subscriptions` requieren JWT **ADMIN** vía gateway ([openapi.yaml](../docs/api/openapi.yaml)). Verificación manual HU-007 (Mailpit tras alta de ejemplar): [local-setup-guide.md](../docs/onboarding/local-setup-guide.md).

**Tests:** `mvn test` (unitarios); `mvn verify` (+ IT en `testIT`). Sin Docker, algunos IT se omiten — [testing-java.md](../docs/engineering/testing-java.md) §4.

---

## 2. Perfil `prod` y variables JDBC

Con `SPRING_PROFILES_ACTIVE=prod`, inyectar credenciales desde el orquestador (no van en el repo).

| Servicio | Esquema | Variables JDBC |
|----------|---------|----------------|
| catalog-service | `catalog` | `MTL_DATASOURCE_URL`, `MTL_DATASOURCE_USERNAME`, `MTL_DATASOURCE_PASSWORD` |
| media-service | `media` | Idem |
| notification-service | `notification` | Idem |
| ai-assistant-service | `ai` | Idem |

Otras variables habituales:

| Variable | Uso |
|----------|-----|
| `MTL_JWT_ISSUER_URI` | Resource server (todos) |
| `MTL_KAFKA_*`, `MTL_CATALOG_KAFKA_ENABLED`, `MTL_NOTIFICATION_KAFKA_ENABLED` | Kafka |
| `MTL_REDIS_*`, `MTL_CACHE_TYPE` | Caché catalog |
| `MTL_MEDIA_BASE_URL` | Cliente catalog → media (**HU-008**) |
| `MTL_CATALOG_MONGO_ENABLED`, `SPRING_MONGODB_URI` | Mongo (**HU-015**) |
| `MTL_AI_PROVIDER_MODE`, `MTL_OPENAI_API_KEY` | IA (**HU-010**, **HU-016**) — detalle en [ai-assistant-service/README.md](ai-assistant-service/README.md) |

Flyway en prod: `clean-disabled=true`; Hibernate `validate` (no `ddl-auto=update`).

---

## 3. Contrato HTTP

Fuente de verdad: [openapi.yaml](../docs/api/openapi.yaml). Rutas públicas vs JWT: [jwt-gateway-strategy.md](../docs/security/jwt-gateway-strategy.md).

Alta de ejemplar: token con `email` y perfil (`scope=openid profile email`) — [ADR-0004](../docs/adr/0004-catalog-rest-write-and-audit.md).

---

## 4. Gateway y patrón MVC común

**Gateway:** `mvn -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=dev`. Un **502** *Servicio de destino no disponible* suele indicar que el microservicio destino (p. ej. catalog **8081**) no está en marcha.

Configuración (rutas, relay JWT, CORS, correlación): [jwt-gateway-strategy.md](../docs/security/jwt-gateway-strategy.md). Código: `services/api-gateway/`.

**Patrón MVC** (referencia hasta librería compartida): tomar **catalog-service** para seguridad, Problem Details y correlación.

| Responsabilidad | Referencia |
|-----------------|------------|
| JWT y rutas por rol | `.../config/CatalogSecurityConfig.java` |
| Roles Keycloak → `ROLE_*` | `.../config/KeycloakRealmRoleConverter.java` |
| `X-Correlation-Id` | `.../web/CorrelationIdFilter.java` |
| 401/403 Problem JSON | `.../web/error/ProblemAuthenticationEntryPoint.java`, `ProblemAccessDeniedHandler.java` |
| Advice global | `.../web/error/CatalogExceptionHandler.java` |

Checklist: stateless; solo rutas públicas de OpenAPI + actuator; ProblemDetail en errores; correlación en MDC; no loguear tokens ni PII.

---

## 5. Notas operativas por historia

Detalle funcional y BDD: documento de cada HU y [frontend/README.md](../frontend/README.md). Contrato HTTP: OpenAPI.

**HU-006 (fotos):** flujo presign → PUT MinIO → confirm en `media-service`. Historia: [HU-006](../docs/backlog/HU-006-fotografias-asociadas-al-arbol.md). Stack local: gateway **8080**, media **8082**, MinIO **9000**.

**HU-008 (edición y baja):** orden de baja en catalog: (1) `DELETE` en media (Bearer relay) — si falla, **no** se borra en PostgreSQL; (2) borrado SQL; (3) borrado Mongo si `mtl.catalog.mongo.enabled=true`. **Sin rollback compensatorio** si falla SQL/Mongo tras media OK (deuda MVP; auditoría `EJEMPLAR_ELIMINACION_PARCIAL_FALLIDA`). Cliente catalog→media: `mtl.media.base-url` (dev: `http://localhost:8082`). Verificación manual: frontend README (HU-008).

**HU-015 (Mongo):** activar en dev con `mtl.catalog.mongo.enabled=true` y URI del Compose; en prod `MTL_CATALOG_MONGO_ENABLED` + `SPRING_MONGODB_URI`. Borrado de `ejemplar_detalle` en cascada con la baja de HU-008. Modelo y endpoints: [mongo.md](../docs/data-model/mongo.md), [HU-015](../docs/backlog/HU-015-proyeccion-y-enriquecimiento-mongo.md).

**HU-016 / HU-010 (IA):** microservicio **ai-assistant-service** (**8084**), modo **`stub`** por defecto. Endpoints, roles, rate limit del chat (40 turnos/h, 2 s entre peticiones) y OpenAI real: [ai-assistant-service/README.md](ai-assistant-service/README.md). E2E opcional: `Hu010Scenario*` en [system-e2e-tests/README.md](system-e2e-tests/README.md).
