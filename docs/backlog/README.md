# Backlog — convención de documentos

Esta carpeta contiene el [backlog resumido](backlog.md) y, para cada historia de usuario que se desglose en trabajo ejecutable, un **fichero de tickets** siguiendo un nombre fijo.


## Patrón de nombre de fichero

**`HU-<id>-ticket-breakdown.md`**

| Parte | Significado |
|-------|-------------|
| `HU-<id>` | Identificador de la historia en [backlog.md](backlog.md) (p. ej. `HU-001`, `HU-004`). |
| `ticket-breakdown` | Sufijo fijo: desglose en tickets/tareas técnicas vinculadas a esa HU. |

Ejemplos futuros: `HU-004-ticket-breakdown.md`, `HU-010-ticket-breakdown.md`.

## Contenido esperado de cada `HU-*-ticket-breakdown.md`

- Metadatos de la HU (título, épica, enlace a la fila del backlog).
- Lista de tickets con **ID estable** recomendado: **`TASK-HU-<id>-<nn>`** (dos dígitos, p. ej. `TASK-HU-001-01`), para trazabilidad en commits, PRs y tableros.
- Criterios de aceptación o notas por ticket cuando aporte valor.
- Orden o dependencias entre bloques de tickets, si aplica.
