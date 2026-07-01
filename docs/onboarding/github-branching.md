# Git, ramas y pull requests

Estrategia sencilla: **rama corta por tarea**, integración a **`main` solo por PR**. No usamos GitFlow completo y por tanto no se emplean ramas develop, release/* y hotfix/*.

## Mapa de lectura

| Quiero… | Documento |
|---------|-----------|
| Abrir rama o PR, títulos, convención de commits | Este documento |
| Comandos exactos antes del PR (paridad con CI) | [devsecops-ci.md](../engineering/devsecops-ci.md) |
| Qué testear en backend / frontend | [testing-java.md](../engineering/testing-java.md) · [testing-frontend.md](../engineering/testing-frontend.md) |
| E2E Playwright (local o manual en Actions) | [testing-e2e.md](../engineering/testing-e2e.md) |
| Atajos PowerShell | [scripts/README.md](../../scripts/README.md) |

**GitHub Actions:** 
- [ci.yml](../../.github/workflows/ci.yml) corre **en cada PR y push a `main`** (tests Java, lint/typecheck/Vitest en frontend y Gitleaks; bloquea el merge si falla). 
- [e2e-playwright.yml](../../.github/workflows/e2e-playwright.yml): Action disponible en GitHub (**Actions → Run workflow**); solo se lanza **cuando tú lo pidas** (no en cada PR), por el coste de levantar contenedores.
- [security-dependencies.yml](../../.github/workflows/security-dependencies.yml): igual, **manual** desde Actions. Detalle: [devsecops-ci.md](../engineering/devsecops-ci.md).

## Flujo habitual

1. Actualizar `main` local y crear la nueva rama **en local**.
2. Commitear en esa rama.
3. Primer `push` → la nueva rama aparece en `origin` y queda enlazada.
4. Abrir PR en GitHub desde la nueva rama en `origin` hacia `main` .
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
- **Título:** en trabajo de backlog (`feature/`): `tipo(HU-xxx): TASK-HU-xxx-nn — resumen breve` (p. ej. `feat(HU-015): TASK-HU-015-04 — API enrichment especie`). En **`fix/`** y **`chore/`**: `fix: resumen` o `chore: resumen` (sin HU ni TASK).
- **Trazabilidad:** en PRs **feature/** usa la plantilla [feature-task](../../.github/PULL_REQUEST_TEMPLATE/feature-task.md); en **fix/chore** la plantilla [fix-chore](../../.github/PULL_REQUEST_TEMPLATE/fix-chore.md). Índice: [.github/PULL_REQUEST_TEMPLATE/](../../.github/PULL_REQUEST_TEMPLATE/).
- **Commits:** mensaje con el *por qué*; `TASK-HU-xxx-nn` en el cuerpo **opcional** (recomendado en backlog; en fix/chore suele omitirse) — [git-commit.md](../../.cursor/commands/git-commit.md).
- **Tras merge:** ticket → **Hecho** en el breakdown; HU → **En curso** o **Cerrada** en `backlog.md` §3 ([playbook IA](ai-development-playbook.md)).
- **Plan de pruebas:** marca solo lo ejecutado de verdad.

**Web:** tras el primer push, **Compare & pull request** → base `main`, compare tu rama. Elige plantilla **feature-task** o **fix-chore** en el desplegable de GitHub (carpeta [.github/PULL_REQUEST_TEMPLATE/](../../.github/PULL_REQUEST_TEMPLATE/)).

**CLI** ([gh](https://cli.github.com/)):

```bash
gh pr create --base main --title "fix: descripcion corta" --body-file .github/PULL_REQUEST_TEMPLATE/fix-chore.md
```

**Antes del PR** — ejecuta y marca en el plan de pruebas solo lo que hayas corrido de verdad:

- [ ] Frontend: `lint`, `typecheck`, `test` (Vitest)
- [ ] Backend: `mvn test` en `services/`
- [ ] (Opcional local) `mvn verify`, `npm run build`, E2E Playwright

**Comandos copy-paste y atajos:** [devsecops-ci.md](../engineering/devsecops-ci.md) (sección «Lo mismo que el CI de PR»). Tests por capa o un solo módulo Maven: [testing-java.md](../engineering/testing-java.md), [testing-frontend.md](../engineering/testing-frontend.md).

**Plantillas PR:** carpeta [.github/PULL_REQUEST_TEMPLATE/](../../.github/PULL_REQUEST_TEMPLATE/) — **feature-task** (HU/TASK) o **fix-chore**. Si el PR toca diagramas ER en `readme.md` §4, revisa la leyenda y convenciones ya descritas en esa sección.

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
