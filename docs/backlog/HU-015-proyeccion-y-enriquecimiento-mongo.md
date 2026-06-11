# HU-015 — Proyección y enriquecimiento Mongo

## 1. Historia refinada

| Campo | Valor |
|-------|--------|
| **ID** | HU-015 |
| **Épica** | Catálogo colaborador |
| **Título** | Proyección y enriquecimiento Mongo |
| **Estimación de complejidad** | L |
| **Prioridad** | Media |

**Historia de usuario**

Como colaborador o administrador del catálogo, quiero consultar y, cuando corresponda, enriquecer en MongoDB la información ampliada de especies y ejemplares vinculada al maestro PostgreSQL, para disponer de notas y datos semiestructurados sin duplicar la autoridad relacional.

- **Entregable de la historia:** Colecciones y acceso Mongo en **catalog-service** según [mongo.md](../data-model/mongo.md) (`especie_detalle`, `ejemplar_detalle` con **observaciones embebidas**), con **proyección mínima** desde SQL (nombres científico y común de la especie desnormalizados al crear o referenciar documentos), **lectura y escritura exclusivas** de Mongo desde **catalog-service** (el **frontend** persiste vía API de catálogo; **ai-assistant-service** no accede a Mongo ni llama a catálogo — ver **HU-016**), UI de consulta/edición en alta y modificación de ejemplar y en **detalle de consulta pública**, y **eliminación física** en Mongo al borrar el ejemplar en PostgreSQL (**HU-008**). PostgreSQL sigue siendo *system of record*; Mongo es *system of enrichment*.

### Alcance

#### Incluye

- Modelo de documentos, índices y validación acordados en [mongo.md](../data-model/mongo.md) (observaciones como **subdocumentos embebidos** en `ejemplar_detalle`, no como colección independiente).
- Capa de persistencia Mongo en **catalog-service** (`infrastructure.persistence.mongo.*` según [mongo-hybrid.mdc](../../.cursor/rules/mongo-hybrid.mdc)): único componente que **lee y escribe** en MongoDB.
- Enlaces por PK numérica SQL (`especie_pg_id`, `ejemplar_pg_id`); `_id` numérico igual a la PK correspondiente.
- **Proyección mínima** al alta/edición de ejemplar: si no existe documento Mongo, crear o actualizar con identificadores y nombres de especie desnormalizados desde PostgreSQL.
- **Alta/edición de `ejemplar_detalle`** (incluidas observaciones embebidas): síncrona **después** del commit en PostgreSQL; mismas validaciones de negocio para **colaborador** y **ADMIN** sobre los campos del ejemplar.
- **Consulta de `especie_detalle`:** colaborador y visitante público en **solo lectura**; **ADMIN** puede **editar** el documento (no puede disparar IA desde estas pantallas; el enriquecimiento con IA queda en **HU-016** desde `/admin/masters`).
- **Resiliencia Mongo:** si falla la inserción o actualización en Mongo tras persistir correctamente la ficha en PostgreSQL, la operación de catálogo **se considera exitosa** y la UI **avisa** al usuario del problema con el enriquecimiento (mensaje no bloqueante; la ficha SQL queda guardada).
- **Borrado de ejemplar (HU-008):** eliminación física del documento `ejemplar_detalle` en Mongo (no tombstone), vía **[TASK-HU-015-01](HU-015-ticket-breakdown.md)** sustituyendo el stub actual.
- **UI** (alta, edición de ejemplar y detalle público de ficha publicada):
  - **`ejemplar_detalle`:** `div` **colapsable** en la parte inferior del formulario o detalle; al expandir, muestra y permite editar (colaborador/ADMIN en alta-edición) los campos Mongo del ejemplar y sus observaciones embebidas.
  - **`especie_detalle`:** **icono** junto al selector de especie que abre **popup/emergente** con el documento de la especie; solo lectura para colaborador y visitante; editable para **ADMIN** en pantallas autenticadas.
  - En **consulta pública** (detalle HU-002): mismo patrón de UI (**icono** + popup para especie, **div** colapsable para ejemplar), **solo lectura**; no se muestra en el listado público.
- Contrato HTTP en **catalog-service** / [openapi.yaml](../api/openapi.yaml) para **lectura y guardado** de enriquecimientos, consumido por el **frontend** (popup de especie en ejemplar, flujo **Guardar** de **HU-016** en `/admin/masters`, detalle público en lectura). Rutas concretas en desglose de tickets.
- Pruebas de integración con **Testcontainers Mongo** en repositorio, borrado en cascada y flujos de upsert/lectura que aporten valor frente a mocks.

#### Queda fuera de esta historia

- **Disparar enriquecimiento con IA** desde alta/edición de ejemplar o desde el popup de especie (**HU-016**, pantalla `/admin/masters`).
- **Flujo de enriquecimiento con IA** en `/admin/masters` (**HU-016**): la IA y la revisión en pantalla son de **HU-016**; esta historia solo expone los endpoints de catálogo que el **frontend** usa al **Guardar**.
- **Sincronización** de `especie_detalle` cuando se **renombra** o **elimina** una especie en PostgreSQL (**HU-011**): fuera de esta versión; deuda documentada.
- Sustituir el **listado** o la **autoridad** de consultas públicas **HU-002** / **HU-003** por lectura solo Mongo (el listado y los datos maestros del detalle siguen en SQL; solo se **añade** la visualización del enriquecimiento Mongo en detalle).
- Gestión de maestros taxonómicos (**HU-011**).
- Validación estricta del JSON generado por LLM (**mongo.md** §6.3): responsabilidad de **ai-assistant-service** (**HU-016**) antes de devolver al frontend; catálogo valida el contrato HTTP al guardar.

### Dependencias

- Infra **MongoDB** en Compose operativa.
- [mongo-hybrid.mdc](../../.cursor/rules/mongo-hybrid.mdc), [ADR-0002](../adr/0002-claves-primarias-numericas-frente-a-uuid.md) y [ADR-0006](../adr/0006-ejemplar-aggregate-http-kafka-naming.md).
- **HU-005** (alta de ejemplar), **HU-008** (edición/baja; hook de borrado stub → real), **HU-013** (rutas y guardas).
- **HU-002** (detalle público donde se muestra enriquecimiento en solo lectura).
- **HU-016:** el **frontend** de `/admin/masters` usa los mismos endpoints de catálogo para cargar/guardar `especie_detalle`; **ai-assistant-service** no se comunica con **catalog-service**.

### Riesgos

- **Desalineación SQL/Mongo** si se renombra especie en **HU-011** sin sincronización (aceptado en este corte; nombres desnormalizados en Mongo pueden quedar obsoletos).
- **Ficha guardada sin enriquecimiento** si Mongo falla tras commit SQL (mitigado con aviso al usuario; posible reintento manual en edición).
- **Documento `especie_detalle` compartido** entre muchos ejemplares: edición **ADMIN** en popup afecta a todas las fichas de esa especie (comportamiento esperado; copy de UI debe dejarlo claro).
- **Alcance L** (backend Mongo + OpenAPI + UI en tres contextos: alta, edición, detalle público).

### Decisiones de refinamiento (registro)

| Tema | Decisión |
|------|----------|
| **Propiedad Mongo** | Solo **catalog-service** lee y escribe Mongo. El **frontend** llama a catálogo para guardar/cargar; **sin** enlace **ai-assistant-service** ↔ **catalog-service**. |
| **Roles — `especie_detalle`** | Colaborador y público: **solo lectura**. **ADMIN:** lectura y edición en popup de alta/edición de ejemplar. |
| **Roles — `ejemplar_detalle`** | Colaborador y **ADMIN:** lectura y edición en div colapsable; mismas validaciones; colaborador **no** edita datos de especie. |
| **IA** | No invocable desde pantallas de esta HU; solo **HU-016** en maestros. |
| **Invalidación / sync especie** | **No** en esta versión (ni renombre ni DELETE de especie en SQL). |
| **Invalidación ejemplar** | **Sí:** borrado físico de `ejemplar_detalle` al eliminar árbol (**HU-008**). |
| **Fallo Mongo post-SQL** | Ficha SQL **guardada**; aviso al usuario; no rollback de PostgreSQL. |
| **Borrado árbol** | Eliminación física en Mongo (no tombstone). Ticket **[TASK-HU-015-01](HU-015-ticket-breakdown.md)**. |
| **UI** | Icono + popup (especie); div colapsable (ejemplar + observaciones embebidas). Mismo patrón en detalle público (solo lectura). |
| **Índices** | Crear los definidos en [mongo.md](../data-model/mongo.md) §4 al desplegar la capa Mongo (soportan búsqueda futura y lecturas por `especie_pg_id` / `ejemplar_pg_id`). |

### Aclaraciones pendientes (refinamiento)

- Rutas y contrato de enriquecimiento: cerrado en **[TASK-HU-015-03](HU-015-ticket-breakdown.md)** (autenticado por sub-recurso; público compuesto bajo `/public/trees/{treeId}/enrichment`).
- Aviso Mongo post-SQL: cerrado — campo opcional **`enrichmentWarning`** (string) en respuesta **`POST`/`PUT`** `/api/catalog/trees` ([desglose](HU-015-ticket-breakdown.md), tabla **Decisiones de refinamiento**).

### Referencia

Desglose de tickets: [HU-015-ticket-breakdown.md](HU-015-ticket-breakdown.md). Coordinación IA: [HU-016-consulta-admin-caracteristicas-especie-ia.md](HU-016-consulta-admin-caracteristicas-especie-ia.md).

## 2. Criterios de aceptación (BDD)

### Referencias

[mongo.md](../data-model/mongo.md); [mongo-hybrid.mdc](../../.cursor/rules/mongo-hybrid.mdc); **HU-005**, **HU-008**, **HU-002**, **HU-011**, **HU-016**; [openapi.yaml](../api/openapi.yaml).

### Escenario 1 — Colaborador edita enriquecimiento del ejemplar tras guardar ficha

- **Dado que** soy un **colaborador** autenticado en alta o edición de un ejemplar propio  
- **Cuando** guardo la ficha SQL correctamente y expando el **div colapsable** de enriquecimiento del ejemplar para editar medidas, etiquetas u observaciones embebidas  
- **Entonces** los cambios se persisten en `ejemplar_detalle` en Mongo vinculados a `ejemplar_pg_id`  
- **Y** no puedo modificar el documento `especie_detalle` desde el popup (solo consulta).

### Escenario 2 — ADMIN edita enriquecimiento de especie en popup

- **Dado que** soy **ADMIN** en la pantalla de alta o edición de un ejemplar  
- **Cuando** abro el **icono** junto al selector de especie y modifico campos de `especie_detalle`  
- **Entonces** el documento se actualiza en Mongo vía **catalog-service**  
- **Y** no se invoca al proveedor de IA (**HU-016**).

### Escenario 3 — Visitante consulta enriquecimiento en detalle público

- **Dado que** existe una ficha **publicada** con documentos de enriquecimiento en Mongo  
- **Cuando** un visitante sin sesión abre el **detalle público** de la ficha  
- **Entonces** puede ver `especie_detalle` (popup) y `ejemplar_detalle` (div colapsable) en **solo lectura**  
- **Y** el listado público no muestra estos bloques.

### Escenario 4 — Éxito SQL con fallo Mongo

- **Dado que** envío un alta o edición válida de ejemplar  
- **Cuando** PostgreSQL persiste correctamente pero Mongo no acepta el upsert de enriquecimiento  
- **Entonces** la ficha queda guardada en SQL  
- **Y** la UI muestra un **aviso** explícito de que el enriquecimiento no se ha guardado o está incompleto  
- **Y** puedo reintentar la edición del bloque Mongo sin perder la ficha SQL.

### Escenario 5 — Borrado de árbol elimina enriquecimiento Mongo

- **Dado que** existe un `ejemplar_detalle` para un árbol que voy a eliminar  
- **Cuando** confirmo el **DELETE** exitoso del ejemplar (**HU-008**, tras media y SQL)  
- **Entonces** el documento Mongo del ejemplar se **elimina físicamente** (sin tombstone)  
- **Y** no se elimina `especie_detalle` compartido con otros ejemplares.

### Escenario 6 — Renombre de especie en maestros no sincroniza Mongo

- **Dado que** existe un `especie_detalle` con nombres desnormalizados  
- **Cuando** un **ADMIN** renombra la especie en **HU-011**  
- **Entonces** el documento Mongo **no** se actualiza automáticamente en esta versión  
- **Y** el comportamiento queda documentado como deuda hasta una historia futura.

### Escenario 7 — Colaborador y ADMIN comparten validaciones en ejemplar

- **Dado que** soy **colaborador** o **ADMIN** editando `ejemplar_detalle`  
- **Cuando** envío datos inválidos según las reglas acordadas en catálogo (p. ej. tipos o campos obligatorios del contrato)  
- **Entonces** recibo el mismo tratamiento de error (**400** Problem Details) independientemente del rol  
- **Y** un colaborador no autorizado sobre ficha ajena sigue recibiendo **403** según **HU-008**.

## 3. Evaluación INVEST (resumen)

| Criterio | Comentario |
|----------|------------|
| **Independiente** | Parcialmente: depende de Mongo en Compose, **HU-005**/**HU-008** y detalle **HU-002**; endpoints de especie reutilizados por el frontend de **HU-016** sin acoplar microservicios entre sí. |
| **Negociable** | Cerrado en *Decisiones de refinamiento* (roles, sync acotada al borrado de ejemplar, resiliencia Mongo, patrón UI). |
| **Valiosa** | Sí: habilita enriquecimiento semiestructurado y base para IA administrativa sin romper autoridad SQL. |
| **Estimable** | Sí con reservas: estimación **L**; incertidumbre en contrato OpenAPI y tres superficies de UI. |
| **Small** | Límite alto: capa Mongo completa + API + frontend en alta, edición y detalle público. |
| **Testable** | Sí: IT Mongo, borrado cascada, upsert tras SQL, permisos por rol y aviso ante fallo Mongo. |

## 4. Esfuerzo estimado de implementación

Orden de magnitud **L**: **catalog-service** (Spring Data Mongo, repositorios, orquestación post-commit SQL, sustitución del stub de borrado, endpoints OpenAPI), **frontend** (componentes reutilizables popup/div colapsable en **CreateTreeView**, **EditTreeView**, **TreeDetailView**), pruebas unitarias e integración Testcontainers. Cifra en persona-días: **no fijada en fuentes**.
