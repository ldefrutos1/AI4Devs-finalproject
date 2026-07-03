# Skills de revisión transversal

Revisión explícita del diff o del código actual, invocada con `/review-*` (no automática). Complementan las skills de persistencia [db-postgresql-mtl](../db-postgresql-mtl/SKILL.md) y [db-mongo-mtl](../db-mongo-mtl/SKILL.md).

| Skill | Comando | Enfoque |
|-------|---------|---------|
| [review-tests](review-tests/SKILL.md) | `/review-tests` | Calidad de tests (comportamiento, no cobertura de líneas) |
| [review-seguridad](review-seguridad/SKILL.md) | `/review-seguridad` | OWASP Top 10, Spring Security |
| [review-arquitectura](review-arquitectura/SKILL.md) | `/review-arquitectura` | Capas, dependencias, cohesión |
| [review-bugs](review-bugs/SKILL.md) | `/review-bugs` | Bugs y edge cases funcionales |
| [review-patrones](review-patrones/SKILL.md) | `/review-patrones` | Patrones y SOLID |
| [review-rendimiento](review-rendimiento/SKILL.md) | `/review-rendimiento` | N+1, streams, colecciones, logging |

Tras cada TASK, pedir al agente la revisión que corresponda al cambio (p. ej. `/review-tests` si tocaste tests o lógica con ramas nuevas).
