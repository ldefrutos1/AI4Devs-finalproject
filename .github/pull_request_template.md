## Trazabilidad (obligatorio)

| Campo | Valor |
|-------|--------|
| **HU** | HU-xxx |
| **Ticket(s)** | TASK-HU-xxx-nn |
| **Breakdown** | [docs/backlog/HU-xxx-ticket-breakdown.md](docs/backlog/HU-xxx-ticket-breakdown.md) |

**Alcance del ticket en esta PR** (1–2 frases; coherente con el breakdown):

- 

**Fuera de alcance** *(opcional)*:

- Ninguno

## Resumen

<!-- Qué problema resuelve y por qué este cambio aporta valor -->

- 

## Alcance

<!-- Marca lo que aplique -->

- [ ] Frontend
- [ ] Backend
- [ ] Infraestructura
- [ ] Documentación

## Cambios realizados

<!-- Lista breve y accionable -->

- 

## Evidencias (opcional)

<!-- Capturas, gifs o ejemplos (especialmente si hay cambios de UI) -->

- 

## Plan de pruebas

<!-- Checklist de validación ejecutada -->

- [ ] `frontend`: `npm run lint` y `npm run typecheck`
- [ ] `frontend`: `npm run test` (si aplica)
- [ ] `services`: `mvn -f services/pom.xml test` (módulo afectado, si aplica)
- [ ] Prueba manual en local (si aplica)

## Checklist único de calidad (front/back)

- [ ] No se rompe lógica de negocio ni navegación existente
- [ ] Se mantienen nombres claros y responsabilidad única
- [ ] No se introduce duplicación innecesaria
- [ ] Manejo básico de errores revisado
- [ ] Tests añadidos/actualizados según impacto del cambio
- [ ] Contratos y compatibilidad revisados (API/eventos/DTOs, si aplica)
- [ ] Seguridad revisada (auth, validaciones, secretos, permisos, si aplica)
- [ ] **Frontend (si aplica):** textos en `i18n`, consistencia visual y responsive
- [ ] **Backend (si aplica):** validaciones, manejo de errores y cobertura de tests

## Seguridad y dependencias (DevSecOps)

<!-- Ver docs/engineering/devsecops-ci.md -->

- [ ] No hay secretos reales en el diff (`.env`, tokens, claves); Gitleaks debe pasar en CI
- [ ] Si toca auth/JWT/roles: revisado según [api-security.mdc](.cursor/rules/api-security.mdc) y [jwt-gateway-strategy.md](docs/security/jwt-gateway-strategy.md)
- [ ] Si añade dependencia npm/Maven: sin CVE high/critical conocidos (o issue de mitigación)
- [ ] Si toca frontend con datos de usuario: sin `v-html` sin sanitizar; sin secretos en `VITE_*`
- [ ] Si el cambio es grande o toca deps: `npm audit` / OWASP en local o workflow *Security — dependencias* ([devsecops-ci.md](docs/engineering/devsecops-ci.md))

## Riesgos / impacto

<!-- Describe posibles impactos laterales o riesgos conocidos -->

- Riesgo:
- Mitigación:

## Notas para review

<!-- Puntos concretos donde quieres feedback del revisor -->

- 

## Cierre de trazabilidad (autor, antes de merge)

- [ ] Ticket(s) → **Hecho** en el breakdown
- [ ] Si era el último ticket: **Estado** de la HU en [backlog.md](docs/backlog/backlog.md) §3 → **Cerrada**
- [ ] Si quedan tickets pendientes: §3 → **En curso**
