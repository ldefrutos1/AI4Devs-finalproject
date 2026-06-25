# Backlog — convención de documentos

Esta carpeta contiene el [backlog resumido](backlog.md) y, para cada historia de usuario que se desglose en trabajo ejecutable, un **fichero de tickets** siguiendo un nombre fijo.


## Patrón de nombre de fichero

**`HU-<id>-ticket-breakdown.md`**

| Parte | Significado |
|-------|-------------|
| `HU-<id>` | Identificador de la historia en [backlog.md](backlog.md) (p. ej. `HU-001`, `HU-004`). |
| `ticket-breakdown` | Sufijo fijo: desglose en tickets/tareas técnicas vinculadas a esa HU. |

Ejemplos: `HU-004-ticket-breakdown.md`, `HU-016-ticket-breakdown.md` (HU cerrada). Sin breakdown aún: `HU-010-ticket-breakdown.md`.

## Contenido esperado de cada `HU-*-<nombre>.md` (refinamiento)

Fichero de historia refinada (p. ej. `HU-004-suscripcion-por-correo-sin-cuenta-colaborador.md`). Metadatos mínimos en **§1 Historia refinada**:

| Campo | Obligatorio |
|-------|-------------|
| **ID**, **Épica**, **Título** | Sí |
| **Estimación de complejidad**, **Prioridad** | Sí |
| **Estado** | Sí — última fila de la tabla de metadatos |

Valores de **Estado** (mismos que [backlog.md](backlog.md) §3): **Pendiente**, **En curso**, **Cerrada**, **Próxima versión**.

Plantilla mínima:

```markdown
| **Estado** | **Cerrada** |
```

Plantilla de refinamiento: skill [.cursor/skills/hu-refinement-mtl/SKILL.md](../../.cursor/skills/hu-refinement-mtl/SKILL.md).

## Contenido esperado de cada `HU-*-ticket-breakdown.md`

- Metadatos de la HU en tabla de cabecera: **Historia**, **Refinamiento**, **Épica**, **Título HU**, **Estado HU** (obligatorio si la HU no está **Pendiente**).
- Lista de tickets con **ID estable** recomendado: **`TASK-HU-<id>-<nn>`** (dos dígitos, p. ej. `TASK-HU-001-01`), para trazabilidad en commits, PRs y tableros.
- Criterios de aceptación o notas por ticket cuando aporte valor.
- Orden o dependencias entre bloques de tickets, si aplica.
- **Checks transversales:** enlace a [devsecops-ci.md](../engineering/devsecops-ci.md) (no copiar comandos CI en cada HU).
- **Checks específicos de la HU:** módulos, flujos manuales y criterios funcionales propios del corte.

Plantilla mínima de **Estado HU** en el breakdown (tras **Título HU**):

```markdown
| **Estado HU** | **Cerrada** (<hechos>/<total> tickets **Hecho**[; notas]) |
```

Ejemplos en el repo: [HU-006-ticket-breakdown.md](HU-006-ticket-breakdown.md) (`14/14`), [HU-008-ticket-breakdown.md](HU-008-ticket-breakdown.md) (ticket **Rechazado** documentado). Si no se cuenta: `(tickets **Hecho**)` como mínimo.

Plantilla de desglose: skill [.cursor/skills/hu-breakdown-mtl/SKILL.md](../../.cursor/skills/hu-breakdown-mtl/SKILL.md).

## Jerarquía de estados

| Nivel | Fichero | Campo |
|-------|---------|--------|
| 1 | [backlog.md](backlog.md) §3 | **Estado** (fuente de verdad del producto) |
| 2 | `HU-*-<nombre>.md` | **Estado** |
| 3 | `HU-*-ticket-breakdown.md` | **Estado HU** + resumen de tickets |
| 4 | Filas `TASK-HU-*` | **Estado** por ticket |

## Sincronización de estados

Tras merge de un PR que cierre o avance una HU, alinear en este orden:

1. **Estado** de cada ticket obligatorio en el breakdown (**Pendiente** / **En curso** / **Hecho** / **Rechazado**).
2. **`Estado HU`** en `HU-*-ticket-breakdown.md` (conteo `Hecho`/`total` si aplica; anotar **Rechazado** con motivo).
3. **`Estado`** en `HU-*-<nombre>.md` (§1 Historia refinada).
4. **Estado** de la fila en [backlog.md](backlog.md) §3: **Cerrada** cuando todos los TASK obligatorios están **Hecho** (o **Rechazado** documentado); **Próxima versión** para HU fuera del MVP.
5. Tabla §6 de [readme.md](../../readme.md) si cambia el estado o el título visible del producto.

Convención operativa: [ai-development-playbook.md](../onboarding/ai-development-playbook.md).
