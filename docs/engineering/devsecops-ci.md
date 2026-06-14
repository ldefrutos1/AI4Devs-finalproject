# DevSecOps y calidad en CI (MyTreeLibrary)

Qué corre en cada PR, qué lanzar a mano y **comandos en local**. Reglas: [api-security.mdc](../../.cursor/rules/api-security.mdc), [frontend-security.mdc](../../.cursor/rules/frontend-security.mdc). Tests: [testing-java.md](testing-java.md), [testing-frontend.md](testing-frontend.md).

## Resumen

| Control | Workflow | ¿En cada PR? | ¿Bloquea merge? |
|---------|----------|--------------|-----------------|
| Tests Java | [ci.yml](../../.github/workflows/ci.yml) → `java` | Sí | Sí (si fallan tests) |
| Lint + typecheck + Vitest | [ci.yml](../../.github/workflows/ci.yml) → `frontend` | Sí | Sí |
| Gitleaks (secretos en diff) | [ci.yml](../../.github/workflows/ci.yml) → `gitleaks` | Sí | Sí |
| npm audit + OWASP | [security-dependencies.yml](../../.github/workflows/security-dependencies.yml) | No (manual) | No (advisory) |

**CI** se dispara en PR o push a `main` / `develop`. Los tres jobs de [ci.yml](../../.github/workflows/ci.yml) van en paralelo.

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

Sin acción del usuario: cada PR hacia `main` o `develop` ejecuta tests, lint, typecheck y escaneo de secretos en el **diff**.

### Dependencias manual ([security-dependencies.yml](../../.github/workflows/security-dependencies.yml))

**Actions → «Security — dependencias (manual)» → Run workflow**

- **npm audit:** high/critical en log y artifact `npm-audit-report`.
- **OWASP:** CVE con CVSS en artifact `owasp-dependency-check-report` (abrir HTML).
- Modo **advisory:** el workflow termina aunque haya hallazgos; revisar log y artifacts.

Opcional en el repo: secret `NVD_API_KEY` (acelera OWASP en CI).

## Pull requests

Checklist DevSecOps en [.github/pull_request_template.md](../../.github/pull_request_template.md).

## Referencias

- E2E Playwright (manual): [testing-e2e.md](testing-e2e.md)
- [e2e-playwright.yml](../../.github/workflows/e2e-playwright.yml)
