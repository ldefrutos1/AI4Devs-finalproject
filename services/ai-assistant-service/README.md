# ai-assistant-service

Microservicio Spring Boot de IA orientativa (**puerto 8084** en local, perfil `dev`). Auditoría en PostgreSQL esquema **`ai`** (`AUDITORIA_USO_IA`).

**Arranque, JDBC, Flyway, variables `MTL_AI_*` y observabilidad:** [services/README.md](../README.md) (§1 y §2).

## Endpoints HTTP

| Ruta | Roles realm | Historia |
|------|-------------|----------|
| `POST /api/ai/species/enrichment-suggestions` | **ADMIN** | [HU-016](../../docs/backlog/HU-016-ticket-breakdown.md) |
| `POST /api/ai/chat/messages` | **COLABORADOR**, **ADMIN** | [HU-010](../../docs/backlog/HU-010-ticket-breakdown.md) |

Contrato: [openapi.yaml](../../docs/api/openapi.yaml). Acceso vía **api-gateway** (`/api/ai/**`). UI: [frontend/README.md](../../frontend/README.md) (HU-010, HU-016).

## Modo proveedor (`stub` / OpenAI)

Por defecto `mtl.ai.provider.mode=stub` (sin red ni clave). Modelos en `application.properties`: `mtl.ai.openai.enrichment-model`, `mtl.ai.openai.chat-model`. Tests y CI usan **`stub`** (`application-test.properties`).

**OpenAI real (perfil `dev`):** la clave **no** va en el repo. Exporta en la misma terminal que arranca el servicio:

```powershell
$env:MTL_AI_PROVIDER_MODE = "openai"
$env:MTL_OPENAI_API_KEY = "sk-..."   # no subir a Git
mvn -f services/pom.xml -pl ai-assistant-service spring-boot:run "-Dspring-boot.run.profiles=dev"
```

```bash
export MTL_AI_PROVIDER_MODE=openai
export MTL_OPENAI_API_KEY='sk-...'
mvn -f services/pom.xml -pl ai-assistant-service spring-boot:run -Dspring-boot.run.profiles=dev
```

Sin clave con `MTL_AI_PROVIDER_MODE=openai`, falla al arrancar (`OpenAiStartupValidator`).

## Tests

`mvn -pl ai-assistant-service test verify` (desde `services/`). Detalle: [testing-java.md](../../docs/engineering/testing-java.md).
