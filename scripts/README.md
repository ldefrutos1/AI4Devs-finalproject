# Scripts de desarrollo local

Atajos PowerShell para tareas habituales del monorepo MyTreeLibrary. Los comandos Cursor (`.cursor/commands/`) cubren flujos con confirmación (commit, rama con cambios pendientes); estos scripts son para **ejecución directa en terminal**.

## Requisitos

- **Windows:** PowerShell 5.1+ o PowerShell 7.
- Herramientas en `PATH` según el script: `git`, `mvn`, `npm`, `docker` (este último solo para `test-e2e.ps1` en modo Docker).
- Ejecutar desde la **raíz del repositorio** (o cualquier ruta: los scripts resuelven la raíz vía `services/pom.xml`).

Si la política de ejecución bloquea scripts:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
# o, solo para una invocación:
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\dev\test-backend.ps1
```

## Scripts (`scripts/dev/`)

| Script | Descripción |
|--------|-------------|
| [test-backend.ps1](dev/test-backend.ps1) | `mvn test` (`-Quick`) o `mvn verify` en `services/` |
| [test-frontend.ps1](dev/test-frontend.ps1) | `npm ci` + `npm test` en `frontend/` (`-SkipInstall` omite `npm ci`) |
| [test-e2e.ps1](dev/test-e2e.ps1) | E2E de UI (Playwright). Por defecto stack autocontenido en Docker (build jars + `docker-compose.e2e.yml` + `down -v`); `-Local` contra entorno ya levantado |
| [check-ports.ps1](dev/check-ports.ps1) | Puertos MTL en escucha (`-All` lista también libres) |
| [git-new-branch.ps1](dev/git-new-branch.ps1) | `main` + `pull` + `checkout -b prefijo/nombre` (`-Stash` si hay cambios) |

### Módulos compartidos (no ejecutar a mano)

| Fichero | Rol |
|---------|-----|
| `_common.ps1` | Raíz del repo, mensajes, `git`/directorios, comprobación de comandos |
| `_ports.ps1` | Tabla de puertos alineada con [services/README.md](../services/README.md) e [infra/compose/README.md](../infra/compose/README.md) |

Si cambian puertos en Compose o en microservicios, actualizar **`_ports.ps1`** y la documentación canónica.

## Ejemplos

```powershell
cd C:\ruta\al\AI4Devs-finalproject

.\scripts\dev\check-ports.ps1
.\scripts\dev\test-backend.ps1
.\scripts\dev\test-backend.ps1 -Quick
.\scripts\dev\test-frontend.ps1 -SkipInstall
.\scripts\dev\test-e2e.ps1                       # E2E en Docker autocontenido (CI/PR)
.\scripts\dev\test-e2e.ps1 -Local                # E2E contra el entorno ya levantado
.\scripts\dev\git-new-branch.ps1 -Prefix fix -Name revision-bugs-entrega-dos
```

## Referencias

- Tests backend: [docs/engineering/testing-java.md](../docs/engineering/testing-java.md)
- Tests frontend: [docs/engineering/testing-frontend.md](../docs/engineering/testing-frontend.md)
- Tests E2E (Playwright): [docs/engineering/testing-e2e.md](../docs/engineering/testing-e2e.md) · [e2e/README.md](../e2e/README.md)
- Git y PR: [docs/onboarding/github-branching.md](../docs/onboarding/github-branching.md)
- Comandos Cursor: [.cursor/commands/git-commit.md](../.cursor/commands/git-commit.md), [git-new-branch.md](../.cursor/commands/git-new-branch.md)
