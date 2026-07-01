# DevSecOps y calidad en CI (MyTreeLibrary)

Qué corre en cada PR, qué lanzar a mano y **comandos en local** (fuente canónica para paridad con CI). Mapa general: [canonical-sources.md](canonical-sources.md). Reglas: [api-security.mdc](../../.cursor/rules/api-security.mdc), [frontend-security.mdc](../../.cursor/rules/frontend-security.mdc). Tests por capa: [testing-java.md](testing-java.md), [testing-frontend.md](testing-frontend.md).

## Resumen

| Control | Workflow | ¿En cada PR? | ¿Bloquea merge? |
|---------|----------|--------------|-----------------|
| Tests Java | [ci.yml](../../.github/workflows/ci.yml) → `java` | Sí | Sí (si fallan tests) |
| Lint + typecheck + Vitest | [ci.yml](../../.github/workflows/ci.yml) → `frontend` | Sí | Sí |
| Gitleaks (secretos en diff) | [ci.yml](../../.github/workflows/ci.yml) → `gitleaks` | Sí | Sí |
| E2E Playwright (UI, Docker) | [e2e-playwright.yml](../../.github/workflows/e2e-playwright.yml) | No — **Actions → Run workflow** | No |
| npm audit + OWASP | [security-dependencies.yml](../../.github/workflows/security-dependencies.yml) | No (manual) | No (advisory) |

**CI** se dispara en PR o push a `main`. Los tres jobs de [ci.yml](../../.github/workflows/ci.yml) van en paralelo.

**Pendiente:** Dependabot, cron de audit, bloqueo por CVE high/critical (fase 2).

## Comandos en local

Desde la **raíz del repositorio**, salvo que se indique otra carpeta.

### Lo mismo que el CI de PR

```bash
# Backend (Surefire)
mvn -f services/pom.xml test

# Frontend
cd frontend
npm ci
npm run lint
npm run typecheck
npm run test
cd ..
```

PowerShell (equivalente):

```powershell
mvn -f services/pom.xml test
Set-Location frontend
npm ci; npm run lint; npm run typecheck; npm run test
Set-Location ..
```

### Qué no corre en CI (pero conviene en local)

| Control | En CI | Recomendación local |
|---------|-------|---------------------|
| **`mvn verify`** (Failsafe / `*IT`) | No — solo `mvn test` (Surefire) | Opcional antes del PR si tocaste integración o repositorios; ver [testing-java.md](testing-java.md) §1 y §2 |
| **`npm run build`** (frontend) | No | Recomendado si cambiaste Vue/TS; detecta errores de compilación que lint/typecheck pueden no cubrir |
| **E2E Playwright** (UI) | No — workflow manual | Local o Actions bajo demanda; ver [testing-e2e.md](testing-e2e.md) §5 |
| **Gitleaks** | Sí (en CI) | Opcional en local (sección siguiente) |
| **npm audit / OWASP** | No — workflow manual | Periódico o antes de releases; sección «Dependencias» más abajo |

### Atajos PowerShell ([scripts/README.md](../../scripts/README.md))

| Script | Equivalente CI |
|--------|----------------|
| `.\scripts\dev\test-backend.ps1 -Quick` | Sí — `mvn test` |
| `.\scripts\dev\test-backend.ps1` (sin `-Quick`) | No — `mvn verify` (unitarios + IT) |
| `.\scripts\dev\test-frontend.ps1` | Parcial — solo `npm ci` + `npm test`; añade `lint` y `typecheck` con los comandos de arriba |
| `.\scripts\dev\test-e2e.ps1` | No — E2E local; en CI solo vía workflow manual |

Flujo Git y checklist de PR: [github-branching.md](../onboarding/github-branching.md).

### Dependencias (CVE high/critical) — equivalente al workflow manual

Muestra vulnerabilidades **high** y **critical**; no sustituye el informe HTML de OWASP.

```bash
# Frontend — npm audit
cd frontend
npm audit --audit-level=high
cd ..

# Backend — OWASP Dependency-Check (1ª vez puede tardar varios minutos)
mvn -f services/pom.xml -Psecurity-check org.owasp:dependency-check-maven:aggregate -B -DskipTests
```

Informe OWASP: `services/target/dependency-check-report/dependency-check-report.html`. Supresiones: [owasp-suppressions.xml](../../services/owasp-suppressions.xml).

### Secretos — Gitleaks (opcional en local)

Requiere [Gitleaks](https://github.com/gitleaks/gitleaks) instalado. Config: [`.gitleaks.toml`](../../.gitleaks.toml).

```bash
gitleaks detect --source . --config .gitleaks.toml --redact --verbose
```

## En GitHub Actions

### CI automático ([ci.yml](../../.github/workflows/ci.yml))

Sin acción del usuario: cada PR hacia `main` ejecuta tests, lint, typecheck y escaneo de secretos en el **diff**.

### Dependencias manual ([security-dependencies.yml](../../.github/workflows/security-dependencies.yml))

**Actions → «Security — dependencias (manual)» → Run workflow**

- **npm audit:** high/critical en log y artifact `npm-audit-report`.
- **OWASP:** CVE con CVSS en artifact `owasp-dependency-check-report` (abrir HTML).
- Modo **advisory:** el workflow termina aunque haya hallazgos; revisar log y artifacts.

Opcional en el repo: secret `NVD_API_KEY` (acelera OWASP en CI).

### E2E Playwright manual ([e2e-playwright.yml](../../.github/workflows/e2e-playwright.yml))

**Actions → «E2E Playwright (alta de ejemplar)» → Run workflow.** Stack Docker completo; no bloquea el merge salvo acuerdo del equipo. Guía: [testing-e2e.md](testing-e2e.md).

## Pull requests

Checklist DevSecOps en [.github/PULL_REQUEST_TEMPLATE/feature-task.md](../../.github/PULL_REQUEST_TEMPLATE/feature-task.md) (PRs `feature/`) o el checklist mínimo en [fix-chore.md](../../.github/PULL_REQUEST_TEMPLATE/fix-chore.md).

## Referencias

- E2E Playwright (manual): [testing-e2e.md](testing-e2e.md)
- [e2e-playwright.yml](../../.github/workflows/e2e-playwright.yml)
