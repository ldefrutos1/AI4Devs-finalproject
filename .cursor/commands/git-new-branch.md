# Nueva rama de trabajo

Crea una rama desde **`main` actualizado**. La rama se abre **en local**; GitHub la ve en el primer `push`. Norma: [github-branching.md](../../docs/onboarding/github-branching.md).

Invoca con `/git-new-branch` o `@.cursor/commands/git-new-branch.md`.

## Reglas

- **No** mergear la rama anterior en `main` local (integración solo por PR).
- **No** hacer `push` de la rama nueva salvo petición explícita.
- Working tree limpio antes de ramificar: commit (recomendado), `git stash`, o abortar.

## Pedir al usuario

1. **Nombre** (sin prefijo): p. ej. `control-acceso-fotografias`.
2. **Prefijo:** `feature` | `fix` | `chore`.
3. Si hay **cambios sin commitear:** commit ([git-commit.md](git-commit.md)), stash, o abortar.

## Pasos del agente (en orden)

1. `git status` y `git branch --show-current`. Si hay cambios: `git diff --stat`, confirmar con el usuario, luego commit vía [git-commit.md](git-commit.md) o `git stash push -m "WIP antes de nueva rama"` (recuperar: `git stash pop`). Si aborta: parar.

2. `git checkout main` (si falla, volver al paso 1).

3. `git pull origin main` (conflicto → parar; que lo resuelva el usuario).

4. **No** `git merge <rama-anterior>` en `main` salvo petición explícita.

5. `git checkout -b <prefijo>/<nombre>` (minúsculas, guiones).

6. Informar: rama creada, rama anterior intacta, recordatorio `git push -u origin HEAD` y PR hacia `main` ([github-branching.md](../../docs/onboarding/github-branching.md)).

**Opcional** (solo si lo pide al cerrar la rama anterior): en la rama vieja, `git merge main` (traer `main` **a la feature**, no al revés).

## Manual rápido

```powershell
# Alternativa: .\scripts\dev\git-new-branch.ps1 -Prefix feature -Name mi-tarea

git checkout main
git pull origin main
git checkout -b feature/mi-tarea
git push -u origin HEAD   # cuando toque subir
```

## Errores frecuentes

| Situación | Acción |
|-----------|--------|
| Cambios sin commitear | Commit o `git stash` |
| Ya en `main` limpio | Saltar paso 1; pull + `checkout -b` |
| Rama creada desde `main` viejo | `git checkout main`, `git pull`, borrar rama errónea si no tiene commits, recrear |

## Referencias

- [github-branching.md](../../docs/onboarding/github-branching.md) · [git-commit.md](git-commit.md)
