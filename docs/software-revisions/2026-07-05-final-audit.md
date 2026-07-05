## 1. Resumen ejecutivo

MVP sólido y coherente para entrega académica: bounded contexts claros, capas Spring bien separadas, JWT end-to-end, Problem Details + correlación, OpenAPI canónico y pirámide de tests razonable (unit + Testcontainers + E2E gateway).

Fortalezas: catalog-service como referencia; frontend Vue 3 idiomático (Composition API, apiClient, Pinia mínima); idempotencia Kafka en notification; E2E reales con Keycloak.

Riesgos: resiliencia limitada (sin circuit breakers; Kafka fire-and-forget en alta); orquestación síncrona frágil catalog↔media; duplicación del patrón MVC/seguridad en 4 servicios; CI sin npm run build ni mvn verify; E2E Playwright solo manual.

## 2. Tabla de puntuaciones

### Por aspecto (sistema)

| # | Aspecto | Nota |
|---|---------|------|
| 1 | Arquitectura microservicios | 4/5 |
| 2 | Spring Boot (patrones/capas) | 4/5 |
| 3 | Calidad de código | 4/5 |
| 4 | Tests (estructura, no %) | 4/5 |
| 5 | Frontend Vue | 4/5 |
| 6 | Seguridad | 4/5 |
| 7 | Documentación | 4/5 |
| 8 | CI/CD y observabilidad | 3.5/5 |

### Por microservicio (aspectos 1–4 + 6)

| Servicio | Arq. | Spring | Código | Tests | Seg. |
|----------|:----:|:------:|:------:|:-----:|:----:|
| api-gateway | 4 | N/A* | 4 | 4 | 4 |
| catalog-service | 4 | 4 | 4 | 5 | 4 |
| media-service | 4 | 4 | 4 | 3 | 4 |
| notification-service | 4 | 4 | 4 | 4 | 4 |
| ai-assistant-service | 4 | 4 | 4 | 4 | 4 |
| system-e2e-tests | 4 | N/A | 4 | 4 | 4 |

\*Gateway WebFlux: capas distintas al MVC, bien aplicadas.

## 3. Detalle por aspecto (síntesis)

### 1. Arquitectura microservicios — 4/5

Evidencia: esquemas PG separados; Kafka catalog.ejemplar.evento → notification con dedup (CatalogEjemplarEventoConsumoService); borrado catalog→media con timeouts (MediaRestClientConfig); deuda saga documentada en services/README.md HU-008.

Gap: sin circuit breakers; publish Kafka con log-only en fallo (KafkaEjemplarCreadoEventPublisher).

Recomendaciones: timeouts en gateway; outbox/retry Kafka; evaluar Resilience4j en clientes REST.

### 2. Spring Boot — 4/5

Evidencia: DTOs vs JPA (CatalogEjemplaresController); @Transactional granular; ports Mongo NoOp*; patrón MVC documentado en services/README.md §4 (CatalogSecurityConfig, CatalogExceptionHandler).

Gap: MediaExceptionHandler incompleto vs catalog/notification/ai; TaxonomyAdminService grande.

Recomendaciones: paridad handlers en media; partir taxonomy admin; acortar transacción en ChatMessageService (IA fuera de TX).

### 3. Calidad de código — 4/5

Evidencia: correlación X-Correlation-Id gateway + 4 servicios; Logstash JSON; config 12-factor (MTL_*, perfiles dev/prod); ADR naming HTTP/persistencia.

Gap: boilerplate duplicado (KeycloakRealmRoleConverter, Problem/correlation ×4).

Recomendaciones: mantener deuda MVP documentada o extraer lib cuando duela; propagar correlación en RestClient media→catalog.

### 4. Tests — 4/5

Evidencia: *Test/*IT Maven; Testcontainers catalog/notification; gateway IT + WireMock; E2E Hu001*, Hu005*, Hu010*; frontend ~61 Vitest con mocks HTTP.

Gap: media sin IT MinIO/Postgres; notification/admin sin E2E Java; CI solo Surefire.

Recomendaciones: IT presign media; E2E suscripciones; mvn verify opcional pre-PR.

### 5. Frontend Vue — 4/5

Evidencia: 30 .vue <script setup>; apiClient.ts centralizado; Pinia solo auth; composables + tests por dominio.

Gap: sin test 401/silent-renew; 7 vistas sin test componente; tipos OpenAPI manuales.

Recomendaciones: tests apiClient retry; codegen OpenAPI parcial; smoke vistas auth.

### 6. Seguridad — 4/5

Evidencia: OIDC PKCE (oidc.ts); guards con /auth/error; roles solo UI; sin v-html; Bearer no en URL.

Gap: tokens en localStorage; sin CSP en nginx.conf.

Recomendaciones: cabeceras seguridad nginx; documentar política XSS; test silent renew.

### 7. Documentación — 4/5

Evidencia: docs/api/openapi.yaml; ADRs; READMEs operativos alineados (post-commit reciente); local-setup-guide.md.

Gap: OpenAPI no validado en CI; alineación manual código↔contrato.

Recomendaciones: check OpenAPI en CI; quickstart frontend.

### 8. CI/CD y observabilidad — 3.5/5

Evidencia: ci.yml (Java test + ESLint + typecheck + Vitest + Gitleaks); Actuator/Prometheus 8080–8084; Grafana dashboard en platform/observability/.

Gap: sin npm run build en CI; E2E Playwright manual; sin trazas distribuidas.

Recomendaciones: build en CI; E2E cron o label; smoke /actuator/health.

## 4. Top 5 recomendaciones (impacto / esfuerzo)

1. CI: npm run build en cada PR — evita roturas de bundle (bajo esfuerzo).
2. Kafka/outbox en alta de ejemplar — no silenciar fallo de publish (alto impacto operativo).
3. Resiliencia REST — timeouts gateway + Resilience4j catalog↔media (alto impacto).
4. Paridad MediaExceptionHandler + IT MinIO — consistencia y confianza en fotos (medio).
5. CSP nginx + tests 401/silent-renew — refuerzo seguridad SPA (medio).

## 5. Nota de alcance

- Auditoría sobre código fuente; pipelines no ejecutados en runtime.
- Cobertura % excluida, según la rúbrica.
- Vídeo demo §2.5 readme: no evaluado.
- Contract tests (Pact): N/A — no existen; gateway IT + WireMock es el sustituto.

## Uso del skill en Cursor

Copia el bloque PROMPT (líneas 12–101) a otra herramienta, o pídeme @.cursor/skills/review/auditoria/SKILL.md con alcance concreto (p. ej. solo catalog-service, solo tests, comparar con auditoría anterior).

¿Quieres la auditoría solo de un microservicio con más profundidad, o exportar esto a un fichero en docs/software-revisions/?
