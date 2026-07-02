# Playbook IA (rápido)

Guía breve para trabajar con IA en este repo sin perder trazabilidad ni coherencia.

## Flujo recomendado

1. Definir HU en `docs/backlog/backlog.md` (sin tickets).
2. Refinar HU con `hu-refinement-mtl`.
3. Desglosar en tickets con `hu-breakdown-mtl` (incluye **Reglas aplicables por capa** en el breakdown).
4. Implementar tickets por orden y dependencias (ideal: **un PR = un TASK**), con [encargo-mtl](../../.cursor/skills/encargo-mtl/SKILL.md) relleno por TASK.
5. **Validar** lo implementado: pedir al agente una **revisión explícita** de que el cambio cumple las normas del proyecto (ver § siguiente).
6. Abrir PR: plantilla **feature-task** o **fix-chore** vía enlaces en [.github/pull_request_template.md](../../.github/pull_request_template.md) o `--body-file` ([github-branching.md](github-branching.md)).
7. Tras merge: ticket(s) → **Hecho** en el breakdown; actualizar **Estado** de la HU en `backlog.md` §3.

## Validación contra reglas del proyecto

Dos capas complementarias (no sustituyen CI ni review humano del PR):

1. **Normas citadas en el breakdown** — Al desglosar la HU, `hu-breakdown-mtl` exige una sección **Reglas aplicables por capa (referencia rápida)** en `HU-*-ticket-breakdown.md` (frontend, backend, API, tests, etc.). Esas reglas son la checklist de implementación del corte.
2. **Revisión explícita tras implementar** — Cuando el agente termina un TASK, pedir una pasada de validación contra esas reglas y contra [devsecops-ci.md](../engineering/devsecops-ci.md) (lint, tests, contrato OpenAPI si aplica). Formulación típica: *«Revisa el código implementado para TASK-HU-XXX-NN y comprueba si cumple las reglas indicadas en el breakdown y las reglas Cursor aplicables; lista incumplimientos o confirma conformidad.»* Opcional: usar [encargo-mtl](../../.cursor/skills/encargo-mtl/SKILL.md) en modo **Solo revisión** sin cambiar código.

La supervisión humana confirma el resultado de esa revisión antes de abrir PR.

## Skills de uso diario

- Refinamiento HU: `.cursor/skills/hu-refinement-mtl/SKILL.md`
- Breakdown HU: `.cursor/skills/hu-breakdown-mtl/SKILL.md`
- Encargo de implementación (o revisión): `.cursor/skills/encargo-mtl/SKILL.md`

## Reglas operativas

- `backlog.md`: solo historias de usuario.
- `HU-<id>-ticket-breakdown.md`: tickets técnicos + reglas aplicables por capa.
- No inventar requisitos fuera de `readme.md` + `docs/backlog/backlog.md`.
- Usar roles de Keycloak: `COLABORADOR`, `ADMIN`.

## Estados de la HU (`backlog.md` §3)

| Estado | Cuándo |
|--------|--------|
| **Pendiente** | Ningún ticket empezado |
| **En curso** | Hay trabajo mergeado pero faltan TASK por cerrar |
| **Cerrada** | Todos los TASK obligatorios del breakdown en **Hecho** |

## Checklist mínimo antes de cerrar una tarea

- Reglas aplicables por capa citadas en el breakdown y **revisión explícita** solicitada al agente tras implementar.
- Checks mínimos según capa ([devsecops-ci.md](../engineering/devsecops-ci.md)); checks extra del breakdown si los define.
- Alcance respectado (sin meter trabajo de otra HU).
- Copy y i18n coherentes si hay cambios frontend.
- PR con HU + TASK; breakdown y §3 del backlog actualizados tras merge.
