# AGENTS.md

## Contexto mínimo

MyTreeLibrary es una plataforma para registrar, consultar y compartir árboles singulares. La visión de producto, stack y arquitectura están en [readme.md](readme.md). La regla de producto detallada está en [.cursor/rules/product-context.mdc](.cursor/rules/product-context.mdc).

## Mapa del repositorio

- `frontend/`: SPA Vue 3.
- `services/`: microservicios Spring Boot.
- `infra/`: Docker Compose e infraestructura local.
- `platform/observability/`: observabilidad.
- `docs/`: arquitectura, contrato, ADRs y decisiones estables.
- `scripts/`: atajos PowerShell de desarrollo.

Arranque backend, puertos y build: [services/README.md](services/README.md).  
Infra local: [infra/compose/README.md](infra/compose/README.md).  
Tests Java: [docs/engineering/testing-java.md](docs/engineering/testing-java.md).  
Scripts: [scripts/README.md](scripts/README.md).

## Cómo encargar trabajo al agente

Flujo habitual (detalle en [ai-development-playbook.md](docs/onboarding/ai-development-playbook.md)):

1. **Refinar HU** → [.cursor/skills/hu-refinement-mtl/SKILL.md](.cursor/skills/hu-refinement-mtl/SKILL.md) (mensaje breve con `HU-XXX`; genera o actualiza `docs/backlog/HU-XXX-*.md`).
2. **Desglosar en tickets** → [.cursor/skills/hu-breakdown-mtl/SKILL.md](.cursor/skills/hu-breakdown-mtl/SKILL.md) (mismo `HU-XXX`; genera `docs/backlog/HU-XXX-ticket-breakdown.md` con reglas por capa).
3. **Implementar cada TASK** → [.cursor/skills/encargo-mtl/SKILL.md](.cursor/skills/encargo-mtl/SKILL.md) (mensaje mínimo: `TASK-HU-XXX-nn` + `@` al breakdown; encargo completo solo en TASKs complejos).

No repitas aquí el cuerpo de las skills; si cambian, modifica solo esos ficheros. Backlog de referencia: [docs/backlog/backlog.md](docs/backlog/backlog.md).

## Fuentes canónicas

Antes de tocar una zona, consulta el mapa tema → fuente canónica: [docs/engineering/canonical-sources.md](docs/engineering/canonical-sources.md).

En especial:
- Backend Spring: [.cursor/rules/spring-boot-4-backend.mdc](.cursor/rules/spring-boot-4-backend.mdc)
- Checklist `services/`: [.cursor/rules/backend-generation-standard.mdc](.cursor/rules/backend-generation-standard.mdc)
- Contrato HTTP: [docs/api/openapi.yaml](docs/api/openapi.yaml)
- Seguridad/JWT: [docs/security/jwt-gateway-strategy.md](docs/security/jwt-gateway-strategy.md)
- Frontend Vue 3: [.cursor/rules/frontend-vue3.mdc](.cursor/rules/frontend-vue3.mdc)
- Nomenclatura: [docs/engineering/naming-conventions.md](docs/engineering/naming-conventions.md)

## Criterios de trabajo

- No exponer entidades JPA directamente en la API.
- Mantener DTOs, mappers y entidades separados.
- Añadir validación, manejo de errores y pruebas básicas en código nuevo.
- Revisar si existe una pieza reutilizable antes de crear código nuevo.
- Elegir la opción más simple compatible con el MVP cuando haya ambigüedad.
- No crear documentación nueva salvo que el encargo lo pida o sea necesaria para una decisión estable.

## Idioma y nomenclatura

- Documentación: contenido en español; nombres de archivo en inglés.
- Base de datos: tablas y columnas en español.
- API HTTP: contrato en inglés según OpenAPI.
- Persistencia/API se conectan mediante DTOs y mappers explícitos.

Fuente normativa: [docs/engineering/naming-conventions.md](docs/engineering/naming-conventions.md), [ADR-0007](docs/adr/0007-english-http-spanish-persistence.md), [ADR-0006](docs/adr/0006-ejemplar-aggregate-http-kafka-naming.md).