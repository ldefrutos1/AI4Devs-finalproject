# ai-assistant-service

Microservicio Spring Boot de IA orientativa (**puerto 8084** en local, perfil `dev`). Auditoría en PostgreSQL esquema **`ai`** (`AUDITORIA_USO_IA`).

**Arranque, JDBC, Flyway, variables `MTL_AI_*` y observabilidad:** [services/README.md](../README.md) (§1 y §2).

## Endpoints HTTP

| Ruta | Roles realm | Historia |
|------|-------------|----------|
| `POST /api/ai/species/enrichment-suggestions` | **ADMIN** | [HU-016](../../docs/backlog/HU-016-ticket-breakdown.md) |
| `POST /api/ai/chat/messages` | **COLABORADOR**, **ADMIN** | [HU-010](../../docs/backlog/HU-010-ticket-breakdown.md) |

Contrato OpenAPI: [docs/api/openapi.yaml](../../docs/api/openapi.yaml). El cliente accede vía **api-gateway** (`/api/ai/**`).

## Modo local

Por defecto `mtl.ai.provider.mode=stub` (sin red ni clave OpenAI). Chat y enriquecimiento comparten infraestructura de proveedor; modelo de chat: `mtl.ai.openai.chat-model` (ver `application.properties`).

## Tests

Desde `services/`:

```bash
mvn -pl ai-assistant-service test verify
```

Detalle Surefire/Failsafe: [testing-java.md](../../docs/engineering/testing-java.md).

## Documentación de producto

- Enriquecimiento especie: apartado **HU-016** en [services/README.md](../README.md).
- Chat asistido: apartado **HU-010** en [services/README.md](../README.md).
- Verificación manual UI: [frontend/README.md](../../frontend/README.md) (HU-016, HU-010).
