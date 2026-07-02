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

Por defecto `mtl.ai.provider.mode=stub` (sin red ni clave OpenAI). Chat y enriquecimiento comparten infraestructura de proveedor; modelos: `mtl.ai.openai.enrichment-model` y `mtl.ai.openai.chat-model` (ver `application.properties`).

### Arranque con OpenAI real (perfil `dev`)

La clave **no** debe commitearse ni pegarse en ficheros del repo.

Variables en la **misma sesión** de terminal que arranca el proceso (PowerShell, desde la raíz del monorepo):

```powershell
$env:MTL_AI_PROVIDER_MODE = "openai"
$env:MTL_OPENAI_API_KEY = "sk-..."   # tu clave; no la subas a Git
mvn -f services/pom.xml -pl ai-assistant-service spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Equivalente en bash:

```bash
export MTL_AI_PROVIDER_MODE=openai
export MTL_OPENAI_API_KEY='sk-...'
mvn -f services/pom.xml -pl ai-assistant-service spring-boot:run -Dspring-boot.run.profiles=dev
```

Si `MTL_AI_PROVIDER_MODE=openai` y falta la clave, el servicio falla al arrancar (`OpenAiStartupValidator`). Tests (`mvn test` / CI) siguen en **`stub`** vía `application-test.properties`; no hace falta clave para desarrollo habitual sin red a OpenAI.

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
