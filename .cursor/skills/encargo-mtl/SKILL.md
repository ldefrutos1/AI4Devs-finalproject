# Encargo MTL (plantilla de prompt)

Plantilla para acotar tareas al agente: **objetivo**, **alcance**, **documento que manda**, **definición de hecho** y **modo**.

La especificación del trabajo vive en `docs/backlog/HU-XXX-ticket-breakdown.md`. Esta skill define **cómo pedírselo** al agente; no hace falta rellenar todos los corchetes a mano si el breakdown ya describe el TASK.

## Mensaje mínimo (habitual)

Cuando el TASK ya existe en el breakdown, basta con un mensaje corto. El agente lee el breakdown, aplica las **Reglas aplicables por capa** de ese fichero y organiza el trabajo según las secciones de esta skill (Objetivo → Alcance → DoD → Modo).

**Nota:** si ya insertaste esta skill con **`/encargo-mtl`** o **`@encargo-mtl`**, no hace falta volver a adjuntar `@.cursor/skills/encargo-mtl/SKILL.md` en el mensaje. Sí conviene **`@docs/backlog/HU-XXX-ticket-breakdown.md`** (y módulos concretos si aplica).

### Implementar (ejemplo; sustituye `HU-XXX` y `nn`)

```
@docs/backlog/HU-XXX-ticket-breakdown.md

Implementa TASK-HU-XXX-nn según el breakdown y la estructura encargo-mtl (modo Implementar).
Rellena objetivo, alcance y definición de hecho a partir del breakdown y las reglas citadas ahí.
Pregúntame si falta algo.
```

### Solo revisión (tras implementar un TASK)

```
@docs/backlog/HU-XXX-ticket-breakdown.md

Revisa el código implementado para TASK-HU-XXX-nn (modo Solo revisión).
Comprueba cumplimiento de las reglas del breakdown y de las reglas Cursor aplicables.
Lista incumplimientos o confirma conformidad; no modifiques código.
```

## Encargo completo (TASKs complejos)

Si el TASK es grande, transversal o el breakdown no basta (muchas exclusiones o decisiones técnicas), rellena tú las secciones desde `## Objetivo` hasta `## Modo` y pégalas en el chat, o pide al agente que las redacte primero y las valides antes de implementar. Ejemplo documentado: [hu-breakdown-and-encargo.md](../../docs/ai-process-evidence/hu-breakdown-and-encargo.md) (TASK-HU-016-02).

## Prompt operativo (agente)

Cuando el usuario pide trabajar un **`TASK-HU-XXX-nn`** con esta skill:

1. **Localizar el TASK** en `docs/backlog/HU-XXX-ticket-breakdown.md` (descripción, dependencias, criterios y **Reglas aplicables por capa**).
2. **Redactar el encargo** (aunque el usuario no pegue la plantilla): Objetivo, Alcance (incluye / excluye / módulos), documentación de referencia del breakdown y DoD.
3. **Respetar el modo** indicado por el usuario (por defecto **Implementar** si no dice otro):
   - **Implementar:** cambios de código, tests y docs permitidos por el alcance.
   - **Solo diseño / plan:** análisis y propuesta; **sin** modificar ficheros.
   - **Solo revisión:** informe de conformidad o incumplimientos; **sin** modificar ficheros.
4. **Aplicar** las reglas citadas en el breakdown y las reglas Cursor del área tocada; si hay contradicción con contrato, seguridad o arquitectura, **pregunta** antes de implementar.
5. **Al cerrar (modo Implementar):** cumplir la definición de hecho del encargo (tests/comandos según breakdown y [devsecops-ci.md](../../docs/engineering/devsecops-ci.md) cuando aplique).
6. **Al cerrar (modo Solo revisión):** lista explícita de incumplimientos o confirmación de conformidad; no proponer cambios de código salvo que el usuario lo pida después.

## Cómo usar la plantilla manualmente (pasos)

(Véase la [nota](#mensaje-mínimo-habitual) sobre `/encargo-mtl` y el `@` al breakdown.)

1. **Abre el chat del Agent** (o el modo donde Cursor liste *skills* / comandos con `/`).
2. **Inserta la plantilla en el mensaje**, de una de estas formas (la que veas en tu versión):
   - Escribe **`/`** y elige **`encargo-mtl`** (o el nombre que Cursor muestre para este skill), **o**
   - Escribe **`@`** y busca **`encargo-mtl`**, **`SKILL`**, o el fichero **`.cursor/skills/encargo-mtl/SKILL.md`** para adjuntarlo al contexto, **o**
   - Abre este fichero en el editor, **copia** desde `## Objetivo` hasta el final y **pega** en el chat.
3. **Antes de pulsar Enviar**, en el propio cuadro de texto del mensaje:
   - Sustituye todo lo que esté entre **`[corchetes]`** por texto real (o borra líneas que no apliquen), **o** usa el [mensaje mínimo](#mensaje-mínimo-habitual) y deja que el agente complete las secciones desde el breakdown.
   - Opcional: añade **`@`** a ficheros concretos (p. ej. `@services/api-gateway/...`) para dar contexto extra.
4. **Envía** el mensaje.

Si envías **solo** la plantilla vacía (sin TASK ni breakdown), el agente no tiene una tarea ejecutable.

---

## Objetivo

[Una frase: qué debe quedar hecho al cerrar la tarea.]

## Alcance

- **Incluye:** […]
- **Excluye / no tocar:** [archivos, módulos, `docs/`, etc.]
- **Paths o módulos Maven:** [p. ej. solo `services/api-gateway/`]

## Documentación de referencia (fuente de verdad si hay duda)

Si el encargo incluye documentación de referencia, considérala fuente de verdad y haz que prevalezca ante dudas o contradicciones con el resto de instrucciones. Si esa contradicción afecta al alcance, al contrato público o a una decisión de arquitectura, pregunta antes de implementar.

## Definición de hecho

- Comandos: [p. ej. `mvn -pl … verify` o `mvn test` desde `services/`]
- Documentación: [actualizar / no actualizar; qué ficheros como máximo]
- Trazabilidad: PR con HU + TASK; tras merge, ticket **Hecho** en breakdown y **Estado** coherente en `backlog.md` §3

## Modo

[Elegir uno:]

- **Implementar:** el agente puede modificar código, pruebas y documentación permitida por el alcance.
- **Solo diseño / plan:** el agente no modifica ficheros; entrega análisis, propuesta técnica y riesgos.
- **Solo revisión:** el agente revisa lo existente y reporta hallazgos; no cambia código.

