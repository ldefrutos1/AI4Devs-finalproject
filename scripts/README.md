# Scripts de desarrollo local

Atajos PowerShell para tareas habituales. Los comandos Cursor (`.cursor/commands/`) cubren flujos con confirmación (commit, rama con cambios pendientes); estos scripts son para **ejecución directa en terminal**.

## Requisitos

- **Windows:** PowerShell 5.1+ o PowerShell 7.
- Herramientas en `PATH` según el script: `git`, `mvn`, `npm`, `docker`.

Para **stack Docker** (`build-images.ps1`, `start-docker-stack.ps1`): Docker Desktop en marcha y `infra/compose/.env` configurado — ver [infra/compose/README.md](../infra/compose/README.md).

Si la política de ejecución bloquea scripts:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
# o, solo para una invocación:
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\dev\test-backend.ps1
```

## Scripts (`scripts/dev/`)

| Script | Descripción |
|--------|-------------|
| [test-backend.ps1](dev/test-backend.ps1) | Test de backend: `mvn test` (`-Quick`) o `mvn verify` en `services/` |
| [test-frontend.ps1](dev/test-frontend.ps1) | Test de frontend: `npm ci` + `npm test` en `frontend/` |
| [test-e2e.ps1](dev/test-e2e.ps1) | E2E de UI (Playwright) |
| [build-images.ps1](dev/build-images.ps1) | Build Maven + imágenes Docker locales `mtl/*` (front + 5 microservicios) |
| [start-docker-stack.ps1](dev/start-docker-stack.ps1) | Build + infra (`docker-compose.yml`) + apps (`docker-compose.apps.yml`) |
| [sync-keycloak-spa-client.ps1](dev/sync-keycloak-spa-client.ps1) | Aplica redirect URIs / web origins de `mtl-realm.json` al cliente `mtl-spa` en Keycloak ya importado |
| [check-ports.ps1](dev/check-ports.ps1) | Comprueba puertos MTL en escucha (`-All` lista también libres) |
| [git-new-branch.ps1](dev/git-new-branch.ps1) | `main` + `pull` + `checkout -b prefijo/nombre` |

### Parámetros

**test-frontend.ps1**

| Parámetro | Descripción |
|-----------|-------------|
| `-SkipInstall` | Omite `npm ci` (dependencias ya instaladas). |

**test-e2e.ps1**

| Parámetro | Descripción |
|-----------|-------------|
| *(sin flag)* | Variante Docker: compila jars, levanta stack (`up -d --wait`), `run` Maven y Playwright en serie, `down -v`. |
| `-SkipBuild` | Docker sin recompilar jars (variante Docker). |
| `-KeepStack` | Docker: no baja el stack al terminar (depuración). |
| `-VerboseLogs` | Docker: arranque del stack en primer plano (todos los contenedores de infra/apps). Las pruebas Maven y Playwright siempre muestran su salida en `compose run`. |
| `-Local` | Playwright contra entorno **ya levantado** (p. ej. Vite en `:5173` o stack Docker completo). |
| `-SkipInstall` | Con `-Local`: omite `npm install` y `playwright install`. |
| `-Ui` | Con `-Local`: abre el runner UI de Playwright (`npm run e2e:ui`). |
| `-BaseUrl` | Con `-Local`: URL base (por defecto `http://localhost:5173`). |

**build-images.ps1**

| Parámetro | Descripción |
|-----------|-------------|
| `-Tag` | Etiqueta de imagen (por defecto `local` → `mtl/<servicio>:local`). |
| `-SkipMaven` | Omite `mvn package`; asume jars ya compilados en `services/`. |
| `-ServicesOnly` | Solo backend (5 microservicios); omite imagen del frontend. |

Lee `KEYCLOAK_PORT` de `infra/compose/.env` para el build-arg `VITE_OIDC_ISSUER` del frontend.

**start-docker-stack.ps1**

| Parámetro | Descripción |
|-----------|-------------|
| `-Tag` | Etiqueta pasada a `build-images.ps1` y variable `MTL_IMAGE_TAG` (por defecto `local`). |
| `-SkipBuild` | No reconstruye imágenes; asume `mtl/*` ya presentes en Docker local. |
| `-InfraOnly` | Solo dependencias (`docker-compose.yml`). |
| `-AppsOnly` | Solo aplicación (`docker-compose.apps.yml`; infra ya arriba). |
| `-Down` | Baja apps + infra. |
| `-KeepVolumes` | Con `-Down`: no borra volúmenes (`down` sin `-v`). |

Tras un rebuild de imágenes, recrea contenedores de apps (`--force-recreate`) para cargar jars nuevos.

**sync-keycloak-spa-client.ps1**

| Parámetro | Descripción |
|-----------|-------------|
| `-KeycloakPort` | Puerto HTTP de Keycloak (por defecto: `KEYCLOAK_PORT` en `.env` o `8180`). |
| `-AdminUser` | Usuario admin (por defecto: `KEYCLOAK_ADMIN` en `.env` o `admin`). |
| `-AdminPassword` | Contraseña admin (por defecto: `KEYCLOAK_ADMIN_PASSWORD` en `.env` o valor de desarrollo). |

Requiere Keycloak levantado.

**git-new-branch.ps1**

| Parámetro | Descripción |
|-----------|-------------|
| `-Prefix` | Obligatorio: `feature`, `fix` o `chore`. |
| `-Name` | Obligatorio: segmento de rama (minúsculas, números, guiones). |
| `-Stash` | Si hay cambios sin commit, `git stash push -u` antes de cambiar de rama. |

### Módulos compartidos (no ejecutar a mano)

| Fichero | Rol |
|---------|-----|
| `_common.ps1` | Raíz del repo, mensajes, `git`/directorios, comprobación de comandos |

## Notas

1. Levantar todo: `.\scripts\dev\start-docker-stack.ps1`
2. Comprobar puertos: `.\scripts\dev\check-ports.ps1`
3. Si el login OIDC falla por redirect URI (realm antiguo en volumen): `.\scripts\dev\sync-keycloak-spa-client.ps1`
4. Bajar stack: `.\scripts\dev\start-docker-stack.ps1 -Down` (añadir `-KeepVolumes` para conservar datos).

## Referencias

- Infra Docker Compose: [infra/compose/README.md](../infra/compose/README.md)
- Tests backend: [docs/engineering/testing-java.md](../docs/engineering/testing-java.md)
- Tests frontend: [docs/engineering/testing-frontend.md](../docs/engineering/testing-frontend.md)
- Tests E2E (Playwright): [docs/engineering/testing-e2e.md](../docs/engineering/testing-e2e.md) · [e2e/README.md](../e2e/README.md)
- Git y PR: [docs/onboarding/github-branching.md](../docs/onboarding/github-branching.md)
- Comandos pre-PR / CI: [docs/engineering/devsecops-ci.md](../docs/engineering/devsecops-ci.md)
- Comandos Cursor: [.cursor/commands/git-commit.md](../.cursor/commands/git-commit.md), [git-new-branch.md](../.cursor/commands/git-new-branch.md)
