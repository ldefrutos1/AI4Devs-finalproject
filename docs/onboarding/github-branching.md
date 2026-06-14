# Git, ramas y pull requests

Estrategia sencilla: **rama corta por tarea**, integración a **`main` solo por PR**. No usamos GitFlow completo.

## Flujo habitual

La rama **no** se crea primero en GitHub. Orden:

1. Actualizar `main` local y crear la rama **en local**.
2. Commitear en esa rama.
3. Primer `push` → la rama aparece en `origin` y queda enlazada.
4. Abrir PR hacia `main` en GitHub.
5. Tras el merge: `git checkout main`, `git pull`, borrar rama local/remota si ya no la necesitas.

```bash
git checkout main
git pull origin main
git checkout -b feature/mi-tarea

# … trabajo y commits …

git push -u origin feature/mi-tarea   # primera subida; crea la rama en GitHub
git push                              # subidas siguientes
```

**Excepción:** si la rama ya existe en GitHub (otra persona o la web), en local:

```bash
git fetch origin
git checkout feature/mi-tarea
# o: git checkout -b feature/mi-tarea origin/feature/mi-tarea
```

**Antes de ramificar:** working tree limpio (commit o `git stash`). **No** merges la rama anterior en `main` local: el merge a `main` es vía PR; la rama anterior puede seguir abierta para su PR.

Atajos: comando Cursor [.cursor/commands/git-new-branch.md](../../.cursor/commands/git-new-branch.md) · script `.\scripts\dev\git-new-branch.ps1 -Prefix feature -Name mi-tarea` ([scripts/README.md](../../scripts/README.md)).

## Nombrado

`prefijo/descripcion` en minúsculas y guiones:

| Prefijo | Uso |
|---------|-----|
| `feature/` | Nueva capacidad |
| `fix/` | Corrección |
| `chore/` | Tooling, deps, docs, refactor sin cambio funcional claro |

Opcional con issue: `feature/123-descripcion`. Con HU/ticket: `feature/hu-015-task-04-enrichment-api`. Norma ampliada: [naming-conventions.md](../engineering/naming-conventions.md) §10.

## Pull requests

- **Base:** `main` (salvo acuerdo explícito del equipo).
- **Un PR = un tema revisable** (idealmente un **TASK**). No mezclar HUs ni tickets no relacionados.
- **Título:** `tipo(HU-xxx): TASK-HU-xxx-nn — resumen breve` (p. ej. `feat(HU-015): TASK-HU-015-04 — API enrichment especie`).
- **Trazabilidad:** rellenar la sección obligatoria de la [plantilla](../../.github/pull_request_template.md) (HU, TASK, enlace al breakdown).
- **Commits:** mensaje con el *por qué*; incluir `TASK-HU-xxx-nn` en el cuerpo ([git-commit.md](../../.cursor/commands/git-commit.md)).
- **Tras merge:** ticket → **Hecho** en el breakdown; HU → **En curso** o **Cerrada** en `backlog.md` §3 ([playbook IA](ai-development-playbook.md)).
- **Plan de pruebas:** marca solo lo ejecutado de verdad.

**Web:** tras el primer push, **Compare & pull request** → base `main`, compare tu rama. Rellena la [plantilla del repo](../../.github/pull_request_template.md) (GitHub la carga sola).

**CLI** ([gh](https://cli.github.com/)):

```bash
gh pr create --base main --title "fix: descripcion corta" --body-file .github/pull_request_template.md
```

Comandos habituales para el plan de pruebas:

| Ámbito | Comando |
|--------|---------|
| Frontend | `cd frontend` → `npm run build`, `npm run test` |
| Backend | `cd services` → `mvn verify` |
| Un servicio | `cd services` → `mvn -pl catalog-service verify` |
| Manual | [services/README.md](../../services/README.md), [infra/compose/README.md](../../infra/compose/README.md) |

Más detalle: [testing-java.md](../engineering/testing-java.md), [testing-frontend.md](../engineering/testing-frontend.md), [vue-development-guide.md](vue-development-guide.md) §16. Si tocas contrato HTTP, OpenAPI o Kafka: [openapi.yaml](../api/openapi.yaml), [canonical-sources.md](../engineering/canonical-sources.md).

**Plantilla ER** (solo diagramas ER en `readme.md`): añade al PR el checklist de [pull_request_er_doc_template.md](../../.github/pull_request_er_doc_template.md); no sustituye la plantilla principal.

Ejemplos de tono y detalle: [readme.md §8](../../readme.md).

## Atajos locales

| Operación | Cursor | Script (`scripts/dev/`) |
|-----------|--------|-------------------------|
| Nueva rama | [git-new-branch.md](../../.cursor/commands/git-new-branch.md) | `git-new-branch.ps1` |
| Commit con resumen | [git-commit.md](../../.cursor/commands/git-commit.md) | — |
| Tests backend | — | `test-backend.ps1` |
| Tests frontend | — | `test-frontend.ps1` |
| Puertos local | — | `check-ports.ps1` |

## Referencias

- [docs/README.md](../README.md) · [AGENTS.md](../../AGENTS.md) · [canonical-sources.md](../engineering/canonical-sources.md)
