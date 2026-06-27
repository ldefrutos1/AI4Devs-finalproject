# Desglose en tickets y encargos (evidencia)

Registro histórico de prompts de desglose ([hu-breakdown-mtl](../../.cursor/skills/hu-breakdown-mtl/SKILL.md)) e implementación ([encargo-mtl](../../.cursor/skills/encargo-mtl/SKILL.md)). Índice: [README.md](README.md).

---

## HU-008 — Desglose e implementación parcial

> *Registro histórico:* los prompts siguientes documentan el desglose de HU-008 en su momento; el contenido puede no reflejar el breakdown final ([HU-008-ticket-breakdown.md](../backlog/HU-008-ticket-breakdown.md)).

**Prompt 1:**

Vamos a generar los tickets de la historia; a partir de aquí incluye mis prompts, utiliza la información que tienes en el contexto y a partir de la sección **Ejemplo del proceso: Ticket 1 — HU-008** (readme §7, ahora en este fichero) solo incluye mi parte, no tu respuesta al prompt. Usa la información que hemos definido y /hu-breakdown-mtl HU-008

**Prompt 2:**

Vamos con TASK-HU-008-01, Cierre OpenAPI catálogo y media (HU-008). Además de las operaciones que propone el ticket vamos a incluir además del endpoint de borrado de todas las fotografías el endpoint del borrado de una fotografía (va también dentro de /api/media)

**Prompt 3** (TASK-HU-008-02 — Listado colaborador con filtros):

así está bien, implementa el endpoint del Listado de TASK-HU-008-02, si tienes alguna duda preguntame antes; recuerda las reglas que se deben seguir ya indicadas en @docs/backlog/HU-008-ticket-breakdown.md para la parte back

---

## HU-016 — Desglose e encargo TASK-HU-016-02

> *Registro histórico:* los prompts siguientes documentan el desglose de HU-016 en su momento; el contenido puede no reflejar el breakdown final ([HU-016-ticket-breakdown.md](../backlog/HU-016-ticket-breakdown.md)).

**Prompt 1** (invocación de skill; no modifica la plantilla):

/hu-breakdown-mtl HU-016.

**Prompt 2** (arranque de implementación):

ok empieza con TASK-HU-016-01, si tienes alguna duda preguntame

**Encargo TASK-HU-016-02** (especificación enviada al agente para implementar ese ticket; estructura `encargo-mtl`, rellena para esta tarea):

## Objetivo
Integrar **OpenAI Responses API** en `ai-assistant-service` para HU-016 (enriquecimiento orientativo de especie por ADMIN), sustituyendo el adaptador HTTP genérico actual, manteniendo el modo `stub` para local/tests y dejando una base reutilizable para HU-009 (visión) y HU-010 (chat).
## Alcance
- **Incluye:**
  - Módulo Maven: `services/ai-assistant-service/` únicamente.
  - Cliente OpenAI con **RestClient** (no WebClient, no RestTemplate) contra la **API oficial** (`POST /v1/responses`).
  - `@ConfigurationProperties` para OpenAI (`mtl.ai.openai.*`): `apiKey`, `baseUrl` (default `https://api.openai.com`), `model` (enriquecimiento), `connectTimeout`, `readTimeout`, parámetros de retry acotados.
  - API key **nunca hardcodeada**: `${MTL_OPENAI_API_KEY}` en `application.properties` / `application-prod.properties` (sin valor por defecto en prod).
  - Implementación `OpenAiSpeciesEnrichmentAiProvider` (`@ConditionalOnProperty` `mtl.ai.provider.mode=openai`) que implemente el puerto existente `SpeciesEnrichmentAiProvider`.
  - Clase compartida `OpenAiResponsesClient` (+ DTOs wire + parser de `output` → texto/JSON) en `com.mtl.ai.infrastructure.client.openai`.
  - Timeouts HTTP configurables.
  - Retry con backoff exponencial **solo** para errores transitorios (429, 502, 503, timeout/conexión); **no** reintentar 400, 401, 403, 404 ni otros 4xx de cliente.
  - Mapeo diferenciado de errores OpenAI → `AiAssistantException` / HTTP: 502 proveedor no disponible, 404 sin resultado utilizable, 422 respuesta inválida (delegando validación estructural a `SpeciesEnrichmentValidationService` como ahora).
  - Logging estructurado (SLF4J, ya hay logstash): loguear modelo, duración, correlationId; **no** loguear API key, Authorization ni prompt completo con datos sensibles.
  - Mantener `AiPromptFactory` (ajustar solo si hace falta para Responses API / JSON mode); el prompt debe seguir exigiendo JSON raíz con claves `synonyms`, `distribution`, `ecologicalData`, `references`; enums `growthRate` y `leafType` en **inglés** (`slow|moderate|fast`, `deciduous|evergreen|marcescent`).
  - Flujo sin cambios en controller/DTO público: `SpeciesEnrichmentSuggestionService` → provider → `SpeciesEnrichmentValidationService` → `AiSpeciesEnrichmentSuggestionResponse` + auditoría `AUDITORIA_USO_IA`.
  - Tests: unitarios del parser/cliente (mock RestClient o WireMock), mantener/ajustar tests existentes; no romper `StubSpeciesEnrichmentAiProvider` ni `mode=stub` en `application-test.properties`.
- **Excluye / no tocar:**
  - `docs/` salvo que el cambio de variables de entorno lo exija explícitamente en `services/README.md` (máximo una línea en tabla de puertos/env si aplica).
  - OpenAPI (el contrato HTTP expuesto no cambia).
  - `api-gateway`, frontend, `catalog-service`, Mongo.
  - Endpoints de chat o identificación por imagen (solo preparar cliente OpenAI reutilizable).
  - WebClient / streaming.
## Documentación de referencia (fuente de verdad)
- Reglas backend: `.cursor/rules/backend-generation-standard.mdc`, `spring-boot-4-backend.mdc`, `api-security.mdc`, `logging.mdc`, `quality-and-testing.mdc`
- Tests: `docs/engineering/testing-java.md` §2
- Contrato HTTP: `docs/api/openapi.yaml` (`POST /api/ai/species/enrichment-suggestions`, schemas `AiSpeciesEnrichmentSuggestion*`)
- Validación estructural: `docs/data-model/mongo.md` §6.3 (referencia; la implementación vive en `SpeciesEnrichmentValidationService`)
- HU: `docs/backlog/HU-016-consulta-admin-caracteristicas-especie-ia.md`
- Producto IA orientativa: `.cursor/rules/product-context.mdc`
- Código existente a respetar/evolucionar:
  - `AiPromptFactory`, `SpeciesEnrichmentSuggestionService`, `SpeciesEnrichmentValidationService`
  - `StubSpeciesEnrichmentAiProvider`, `HttpSpeciesEnrichmentAiProvider` (sustituir/deprecar)
  - `AiAssistantExceptionHandler`, `AiProviderConfig`
## Diseño obligatorio
1. **Separación de capas**
   - `application`: orquestación, prompt, validación, auditoría (sin HTTP).
   - `infrastructure.client.openai`: RestClient, request/response OpenAI, retry, parser.
   - No devolver al frontend el JSON completo de OpenAI; solo DTO validado.
2. **Configuración**
   - `mtl.ai.provider.mode=stub|openai` (default `stub` en local).
   - `mtl.ai.openai.api-key=${MTL_OPENAI_API_KEY:}`.
   - Validar en arranque (prod): si `mode=openai` y falta API key → fallo claro al boot o al primer uso (preferible `@PostConstruct` / `ApplicationRunner` con mensaje operativo, sin exponer la clave).
3. **OpenAI Responses API**
   - Usar endpoint oficial `/v1/responses`.
   - Instrucciones de sistema + input de usuario desde `AiPromptFactory`.
   - Preferir salida JSON estructurada si la API lo permite (`text.format` / JSON mode); si no, parsear texto del `output`.
   - Extraer solo el contenido utilizable antes de pasar a `SpeciesEnrichmentValidationService`.
4. **Manejo de errores**
   - Reutilizar `@RestControllerAdvice` existente.
   - Mensajes al cliente genéricos en 500; detalle técnico solo en logs WARN/ERROR sin PII ni secretos.
5. **Convenciones del monorepo**
   - Paquetes lista blanca bajo `com.mtl.ai.*`.
   - Nombres de test: `*Test` (Surefire), `*IT` (Failsafe).
   - Jackson Boot 4: `JsonMapper` / `ObjectMapper` según patrón ya usado en el módulo.
## Definición de hecho
- `mvn -pl ai-assistant-service test` en verde.
- `mvn -pl ai-assistant-service verify` en verde (incluye IT existentes).
- Con `MTL_AI_PROVIDER_MODE=stub`, el flujo actual sigue funcionando sin clave OpenAI.
- Con `MTL_AI_PROVIDER_MODE=openai` y `MTL_OPENAI_API_KEY` configurada, el provider llama a OpenAI (tests del cliente mockeados; no test de integración real contra OpenAI en CI).
- Sin API key en código, logs ni respuestas Problem.
- Sin `System.out.println`.
## Modo
**Implementar** (código + tests mínimos por capa según testing-java.md §2).
## Nota de producto
La respuesta es **orientativa**, no veredicto científico; el prompt no debe presentar la salida como determinación oficial de especie.
