> **Plantilla:** PR de `fix/` o `chore/` (sin HU/TASK).  
> **Título:** `fix: resumen` o `chore: resumen` · [github-branching.md](../../docs/onboarding/github-branching.md)  
> Para trabajo de backlog con HU, usa la plantilla **feature-task**.

## Resumen

<!-- Qué se corrige o qué mantenimiento se hace y por qué -->

-

## Cambios realizados

-

## Plan de pruebas

Comandos: [devsecops-ci.md](../../docs/engineering/devsecops-ci.md). Marca solo lo ejecutado.

- [ ] Frontend: `npm run lint` + `npm run typecheck` — *no aplica / ejecutado*
- [ ] Frontend: `npm run test` — *no aplica / ejecutado*
- [ ] Backend: `mvn -f services/pom.xml -pl <módulo> test` — *módulo: … / no aplica*
- [ ] Manual local: …

## Checklist *(solo si aplica a este cambio)*

- [ ] Sin secretos en el diff (Gitleaks en CI)
- [ ] Tests o validación acorde al impacto del cambio
- [ ] Si toca API/auth/deps: revisión mínima según [devsecops-ci.md](../../docs/engineering/devsecops-ci.md)

## Notas para review *(opcional)*

-
