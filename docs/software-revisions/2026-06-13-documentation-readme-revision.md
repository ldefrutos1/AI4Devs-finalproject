# Revisión — documentación del proyecto (readme y docs/)

| Campo | Valor |
|-------|--------|
| **Fecha** | 2026-06-13 |
| **Alcance** | Memoria principal ([readme.md](../../readme.md)), guías operativas en `docs/onboarding/`, índice y coherencia de enlaces; no código de producto |
| **Tipo** | Revisión editorial y estructural tras cierre de HU-016 y pulido de entrega |
| **Nota global documentación** | **4,7 / 5** |

---

## Contexto

Revisión iterativa de la documentación de entrega de MyTreeLibrary, centrada en:

- Reducir solapamientos y mejorar la **entrada** al proyecto (arranque local, demo, backlog).
- Mantener **un único documento de memoria** (`readme.md`) sin duplicar §1–§2 en ficheros paralelos.
- Alinear índice, anclas y títulos tras cambios en §4 (modelo de datos).

Referencias previas: [2026-05-31-integral-project-evaluation-partial-delivery.md](2026-05-31-integral-project-evaluation-partial-delivery.md) (documentación **4,5 / 5**).

---

## Decisiones de diseño documental

| Decisión | Motivo |
|----------|--------|
| **Descartar** `docs/executive-summary.md` | Solapaba §1–§2 y §3.1; añadía un tercer punto de entrada sin aportar claridad |
| **Memoria única** en `readme.md` | Formato exigido por el curso; §2.4–§2.5 orientan al evaluador sin otro documento paralelo |
| **Eliminar §4.3** del readme | Contenido redundante con §4.1 (usuario OIDC) y §4.2 (PostgreSQL + Mongo, HU-015/016) |
| **No modificar §6** (por ahora) | Tabla HU y evidencia de skills ya cumplen; enlaces por fila opcionales |

---

## Cambios aplicados

### Arranque e instalación

- **§2.4:** tres pasos equilibrados (Compose → `mvn` con perfil `dev` → frontend), tabla por flujo e incluida fila IA stub.
- **`docs/onboarding/local-setup-guide.md`:** prerrequisitos, puertos, usuarios Keycloak, Flyway, incidencias frecuentes; enlazado desde §2.4 y [docs/README.md](../README.md).

### Demo y producto

- **§2.5:** guion de demo en tabla (5 pasos), vídeo marcado como *pendiente*; enlace a §6 y `backlog.md`.
- **§2.1:** eliminado párrafo duplicado sobre IA (ya en §1.3 y §2.2).

### Arquitectura y técnica

- **§3.2.2 (C4 Kafka):** redacción del flujo 201 → commit → publicación asíncrona y deduplicación por `evento_id`.
- **§3.2.4:** corrección de redacción (modo `stub` en local).

### Modelo de datos (§4)

- Título **§4.2** actualizado a *Diagrama de entidad-relación (implementación física)*.
- **§4.3 eliminado;** párrafo *Usuario de aplicación* integrado en **§4.1**.
- Índice y nav interna de §4 alineados; anclas corregidas.

### Navegación

- **Iconos** solo en apartados principales §1–§8 (índice + encabezados `##`): 📋 🌳 🏗️ 🛢️ 🔌 📖 🎫 🔀.
- Subapartados sin iconos (criterio acordado).

### Backlog en memoria

- **§6:** tabla compacta HU-001…016 con estados; enlaces a `backlog.md` y convención de desgloses.

---

## Evaluación por criterio (1–5)

| Criterio | Nota | Comentario |
|----------|------|------------|
| **Completitud** | **5,0** | Producto, arquitectura, modelo, API, Kafka, seguridad, backlog, tests, CI, onboarding, ADRs |
| **Sincronización doc ↔ código** | **4,7** | HU-016 y backlog alineados; OpenAPI con endpoint IA MVP; guía local coherente |
| **Trazabilidad HU → tickets** | **4,5** | Desgloses, skills, §6–§8 con evidencia; tabla §6 sin enlace por HU |
| **Accesibilidad / entrada** | **4,3** | §2.4, §2.5, iconos §1–§8 y `local-setup-guide.md` mejoran escaneo; readme ~1.620 líneas |
| **Estructura y redacción** | **4,8** | Memoria única depurada; menos redundancia en §4; tono más claro en demo e instalación |
| **Gobernanza documental** | **4,5** | [canonical-sources.md](../engineering/canonical-sources.md), reglas Cursor, playbook IA (sin cambio sustancial) |

**Nota global documentación: 4,7 / 5**

---

## Evolución de la nota

| Momento | Nota | Observación |
|---------|------|-------------|
| 2026-05-31 (entrega parcial) | **4,5** | Muy completa; sync puntual y falta de síntesis |
| Revisión intermedia (post HU-016) | **4,6** | Mejor arranque local y sync backlog |
| **2026-06-13 (esta revisión)** | **4,7** | Estructura depurada, guía local, demo §2.5, §4 sin 4.3, índice/iconos |

---

## Fortalezas actuales

1. **`readme.md`** como memoria canónica del curso (ficha → PRs).
2. **Capa operativa** separada: [local-setup-guide.md](../onboarding/local-setup-guide.md), [services/README.md](../../services/README.md), [infra/compose/README.md](../../infra/compose/README.md).
3. **§2.4–§2.5** cubren arranque y guion de demostración sin documento extra.
4. **`docs/`** estructurado con mapa canónico y backlog HU-001…016 cerradas en MVP.
5. **Coherencia de producto** (HU-009/010 fuera de MVP, IA stub, híbrido SQL + Mongo).

---

## Limitaciones y pendientes

| Pendiente | Impacto en la nota |
|-----------|-------------------|
| **Vídeo** en §2.5 (*pendiente de publicación*) | Demostrabilidad visual; el guion en texto ya está |
| **Extensión del readme** | Aceptable para memoria; requiere índice e iconos para navegar |
| **Duplicación menor** | Tabla §6 ↔ `backlog.md`; proceso breakdown en §6 y §7 |
| **Textos técnicos puntuales** | Revisar notas envejecidas (p. ej. Flyway en §4.2.5 si la migración ya existe) |

Para acercarse a **4,9–5 / 5**: (1) publicar vídeo en §2.5, (2) línea opcional de orientación tras el índice, (3) pasada rápida de detalles obsoletos post-MVP.

---

## Ficheros tocados en esta revisión

| Fichero | Acción |
|---------|--------|
| [readme.md](../../readme.md) | Modificado (§2.1, §2.4, §2.5, §3.2.2, §3.2.4, §4, §6, índice, iconos) |
| [docs/onboarding/local-setup-guide.md](../onboarding/local-setup-guide.md) | Creado |
| [docs/README.md](../README.md) | Enlace a guía local |
| `docs/executive-summary.md` | Creado y **eliminado** (descartado por solapamiento) |

---

## Veredicto

Documentación **de nivel alto para entrega académica-profesional**: completa, trazable, operativamente usable y **mejor organizada** que en la evaluación de mayo. La nota **4,7 / 5** refleja un estado **maduro y entregable**; el margen restante depende sobre todo del **vídeo de demo** y de pulidos menores, no de carencias estructurales graves.

Para el **estado operativo del producto**, seguir usando [backlog.md](../backlog/backlog.md), [devsecops-ci.md](../engineering/devsecops-ci.md) y el propio [readme.md](../../readme.md) como fuentes vivas.
