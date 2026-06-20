# Playbook IA (rápido)

Guía breve para trabajar con IA en este repo sin perder trazabilidad ni coherencia.

## Flujo recomendado

1. Definir HU en `docs/backlog/backlog.md` (sin tickets).
2. Refinar HU con `hu-refinement-mtl`.
3. Desglosar en tickets con `hu-breakdown-mtl`.
4. Implementar tickets por orden y dependencias (ideal: **un PR = un TASK**).
5. Abrir PR con sección **Trazabilidad** completa ([plantilla](../../.github/pull_request_template.md)).
6. Tras merge: ticket(s) → **Hecho** en el breakdown; actualizar **Estado** de la HU en `backlog.md` §3.

## Skills de uso diario

- Refinamiento HU: `.cursor/skills/hu-refinement-mtl/SKILL.md`
- Breakdown HU: `.cursor/skills/hu-breakdown-mtl/SKILL.md`
- Encargo de implementación: `.cursor/skills/encargo-mtl/SKILL.md`

## Reglas operativas

- `backlog.md`: solo historias de usuario.
- `HU-<id>-ticket-breakdown.md`: tickets técnicos.
- No inventar requisitos fuera de `readme.md` + `docs/backlog/backlog.md`.
- Usar roles de Keycloak: `COLABORADOR`, `ADMIN`.

## Estados de la HU (`backlog.md` §3)

| Estado | Cuándo |
|--------|--------|
| **Pendiente** | Ningún ticket empezado |
| **En curso** | Hay trabajo mergeado pero faltan TASK por cerrar |
| **Cerrada** | Todos los TASK obligatorios del breakdown en **Hecho** |

## Checklist mínimo antes de cerrar una tarea

- Reglas aplicables por capa citadas en el breakdown.
- Checks mínimos según capa ([devsecops-ci.md](../engineering/devsecops-ci.md)); checks extra del breakdown si los define.
- Alcance respectado (sin meter trabajo de otra HU).
- Copy y i18n coherentes si hay cambios frontend.
- PR con HU + TASK; breakdown y §3 del backlog actualizados tras merge.
