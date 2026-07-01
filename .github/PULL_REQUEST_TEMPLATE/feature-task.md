> **Plantilla:** PR de `feature/` con **HU + TASK** (ideal: un PR = un TASK).  
> **Título:** `feat(HU-xxx): TASK-HU-xxx-nn — resumen breve` · [github-branching.md](../../docs/onboarding/github-branching.md)  
> Marca checkboxes **solo** de lo comprobado; el CI ejecuta lint/tests/Gitleaks en paralelo.

## Trazabilidad

| Campo | Valor |
|-------|--------|
| **HU** | HU-xxx |
| **Ticket(s)** | TASK-HU-xxx-nn |
| **Breakdown** | [docs/backlog/HU-xxx-ticket-breakdown.md](../../docs/backlog/HU-xxx-ticket-breakdown.md) |

**Alcance del ticket en esta PR** (1–2 frases; coherente con el breakdown):

-

**Fuera de alcance** *(opcional)*:

- Ninguno

## Resumen

<!-- Qué problema resuelve y por qué este cambio aporta valor -->

-

## Alcance

- [ ] Frontend
- [ ] Backend
- [ ] Infraestructura
- [ ] Documentación

## Cambios realizados

-

## Evidencias *(opcional; sobre todo UI)*

-

## Plan de pruebas

Comandos: [devsecops-ci.md](../../docs/engineering/devsecops-ci.md). Marca solo lo ejecutado; indica *no aplica* si no toca esa capa.

- [ ] Frontend: `npm run lint` + `npm run typecheck` — *no aplica / ejecutado*
- [ ] Frontend: `npm run test` — *no aplica / ejecutado*
- [ ] Backend: `mvn -f services/pom.xml -pl <módulo> test` — *módulo: … / no aplica*
- [ ] Manual local: …

## Checklist de calidad *(marca solo lo relevante)*

**Código de producto**
- [ ] No se rompe lógica de negocio ni navegación existente
- [ ] Nombres claros y responsabilidad única; sin duplicación innecesaria
- [ ] Manejo básico de errores revisado
- [ ] Tests añadidos/actualizados según impacto

**Si aplica**
- [ ] Contratos revisados ([openapi.yaml](../../docs/api/openapi.yaml) / [kafka-events.md](../../docs/events/kafka-events.md))
- [ ] **Frontend:** i18n, consistencia visual y responsive
- [ ] **Backend:** validaciones y cobertura de tests según breakdown
- [ ] **Auth/JWT:** [api-security.mdc](../../.cursor/rules/api-security.mdc) · [jwt-gateway-strategy.md](../../docs/security/jwt-gateway-strategy.md)
- [ ] Sin secretos en el diff; sin `v-html` sin sanitizar; sin secretos en `VITE_*`
- [ ] Si añade dependencia: sin CVE high/critical conocidos (o issue de mitigación)

## Riesgos / impacto *(opcional)*

- Riesgo:
- Mitigación:

## Notas para review *(opcional)*

-

## Después del merge *(autor; no marcar antes de aprobar el PR)*

- [ ] Ticket(s) → **Hecho** en el breakdown
- [ ] Si era el último ticket: **Estado** de la HU en [backlog.md](../../docs/backlog/backlog.md) §3 → **Cerrada**
- [ ] Si quedan tickets pendientes: §3 → **En curso**
