# HU-016 — Consulta de características de especie (ADMIN, MVP)

## 1. Validación de la información existente

| Campo | En backlog | En fuentes (readme, modelo, HU-015) | Valoración |
|-------|------------|--------------------------------------|------------|
| **ID** | HU-016 | Coherente | Correcto |
| **Épica** | Inteligencia artificial | readme §2.2 (Integración con IA), §3.2.4; backlog §2 | Correcto |
| **Título** | Consulta de características de especie (ADMIN, MVP) | Épica IA; enriquecimiento `especie_detalle` vía **HU-015** (no UC-07; maestros en **HU-011**) | Correcto |
| **Historia** | Como usuario administrador, quiero consultar a la IA las características de una especie… | Caso de uso **UC-10**; acotar a rol **ADMIN**, IA **orientativa** y precarga en la pantalla ya existente de edición de características de especie ([product-context](../../.cursor/rules/product-context.mdc)) | Refinada en §2 |
| **Estimación** | M | Sin contradicción | **Se mantiene M** |
| **Prioridad** | Media | Sin contradicción | **Se mantiene Media** |
| **Estado** | **Cerrada** | Breakdown [HU-016-ticket-breakdown.md](HU-016-ticket-breakdown.md); tickets 01–09 **Hecho** |

**Inconsistencias detectadas**

- «Usuario administrador» y **ADMIN** se consideran equivalentes en la documentación funcional de esta historia.
- readme §2.2 (Integración con IA), §3.2.4 y backlog §3 son coherentes: en el MVP solo entra la consulta de características de especie; identificación por imagen y chat quedan en **HU-009** / **HU-010** (próximas versiones).
- La persistencia Mongo de `especie_detalle` queda fuera de **HU-016** y pertenece al alcance ya cerrado de **HU-015**.
- El contrato `/api/ai/**` de esta historia debe limitarse a la **consulta a IA** para obtener un JSON compatible con la pantalla existente de edición de características de especie.

**INVEST (preliminar):** historia acotada y valiosa; depende de **HU-001**, **HU-013** y de la pantalla ya existente de edición / enriquecimiento de especie; estimable en **M**.

---

## 2. Historia refinada

| Campo | Valor |
|-------|--------|
| **ID** | HU-016 |
| **Épica** | Inteligencia artificial |
| **Título** | Consulta de características de especie (ADMIN, MVP) |
| **Estimación de complejidad** | M |
| **Prioridad** | Media |
| **Estado** | **Cerrada** |

**Historia de usuario**

Como usuario con rol **ADMIN**, quiero consultar a la IA las características ampliadas de una especie ya registrada en el catálogo taxonómico (**UC-10**), para precargar con esa información la pantalla ya existente de edición de características de especie y apoyar así la gestión de maestros con datos orientativos sobre hábitat, distribución, datos ecológicos y referencias, sin que el sistema presente la respuesta como veredicto científico ni exponga esta función a colaboradores en el MVP.

- **Entregable de la historia:** acción disponible solo para **ADMIN** en la pantalla ya existente de edición de características de especie: el **frontend** invoca **ai-assistant-service** (`/api/ai/**`) enviando **nombre científico** y **nombre común** de la especie, recibe un **JSON validado** compatible con los campos editables de esa pantalla y lo usa para **precargarlos**. La historia **no** incluye guardar esos datos en Mongo ni invocar a **catalog-service**; la persistencia del enriquecimiento ya pertenece a **HU-015**. Trazabilidad en **AUDITORIA_USO_IA** al invocar la IA.

### Alcance

#### Incluye

- **Solo rol ADMIN** (JWT); **403** para colaborador u otros roles.
- Consulta a IA de **características de especie** (no de ejemplar en este MVP).
- La funcionalidad parte de la **pantalla ya existente** de edición de características de especie; al activarla, precarga sus campos con la respuesta de la IA.
- La acción de consulta solo está disponible si **aún no existen datos de enriquecimiento** de esa especie en Mongo.
- **Frontend** como consumidor único de **ai-assistant-service**; esta historia no requiere llamadas a **catalog-service**.
- **ai-assistant-service:** invocación al proveedor de IA externo; la petición envía **nombre científico** y **nombre común** de la especie.
- **Validación** del JSON del LLM en **ai-assistant-service** según [mongo.md](../data-model/mongo.md) §6.3 **antes de devolverlo al frontend**, adaptada al formato que necesita la pantalla existente.
- **Respuesta del servicio IA:** JSON compatible con los campos de edición de características de especie, de forma que pueda cargarse directamente en la UI.
- **Frontend:** muestra los datos precargados en la pantalla editable; el copy de producto debe dejar claro que la IA es **orientativa** ([product-context](../../.cursor/rules/product-context.mdc)).
- **Auditoría R3** en esquema `ai` (**AUDITORIA_USO_IA**: `subject_oidc`, `tipo_uso_ia`, resumen de prompt/resultado, `consultado_en`).
- Contrato HTTP del asistente bajo `/api/ai/**`, limitado a la consulta de enriquecimiento de especie.

#### Queda fuera de esta historia

- **Identificación orientativa por imagen** (**HU-009**, UC-05).
- **Chat asistido** (**HU-010**, UC-06).
- Enriquecimiento de **ejemplar** / notas de campo (`ejemplar_detalle`) — alcance **HU-015**.
- **Persistencia** del enriquecimiento en MongoDB o en cualquier otro almacén.
- **Acceso directo a MongoDB** desde **ai-assistant-service**.
- **Comunicación ai-assistant-service ↔ catalog-service**.
- Cambios en la pantalla existente más allá de añadir la acción de consulta y la precarga de campos.
- Acceso de **colaborador** a funciones de IA.
- Disparar IA desde pantallas de consulta pública o desde flujos de identificación por imagen / chat.
- Sincronización automática masiva de todas las especies (batch).
- **Sincronización** de `especie_detalle` cuando se renombra o elimina especie en **HU-011** (deuda de **HU-015**, fuera de HU-016).

### Dependencias

- **HU-001** (JWT, rol **ADMIN**).
- **HU-011** (maestros de catálogo; especie existente con nombre científico y común).
- **HU-013** (navegación y guardas por rol).
- **HU-015** (pantalla ya existente de edición de características de especie y persistencia Mongo fuera del alcance de esta historia).
- Infra: **ai-assistant-service** y gateway `/api/ai/**`.

### Riesgos

- **Coste y límites** del proveedor de IA (cuotas, timeouts); definir límites por ADMIN en implementación.
- **Calidad / alucinaciones:** mitigar con copy de producto y revisión humana del ADMIN; la validación estructural no garantiza veracidad de referencias.
- **Formato de respuesta del LLM:** si el JSON no encaja exactamente con la pantalla existente, la precarga será incompleta o exigirá mapeos manuales.
- **Dependencia de datos existentes:** si ya hay enriquecimiento en Mongo, la acción no debe mostrarse para evitar mezclar fuentes.
- **Timeout o error del proveedor IA:** debe informarse al ADMIN sin alterar los datos ya existentes en pantalla.

### Aclaraciones pendientes (refinamiento)

- Path y verbo exactos en OpenAPI de **ai-assistant-service** para consulta de enriquecimiento de especie.
- Contrato exacto del JSON de salida para que se pueda cargar directamente en la pantalla existente de edición de características.
- Comportamiento visual cuando ya existe enriquecimiento en Mongo: ocultar, deshabilitar o sustituir la acción.
- Mensajes de error si falla la IA o si la respuesta no supera la validación estructural.

---

## 3. Criterios de aceptación (BDD)

### Referencias

readme §2.2 (Integración con IA), §3.2.4; [use-case-summary.md](../use-cases/use-case-summary.md) (**UC-10**); [mongo.md](../data-model/mongo.md) (`especie_detalle`, §6.3); [data-model.md](../data-model/data-model.md) **R3**; **HU-011**, **HU-015**; [product-context.mdc](../../.cursor/rules/product-context.mdc).

### Escenario 1 — ADMIN solicita IA y se precargan los campos

- **Dado que** soy **ADMIN** autenticado y estoy en la pantalla existente de edición de características de una especie con nombre científico y común informados  
- **Y** esa especie aún no tiene datos de enriquecimiento persistidos en Mongo  
- **Cuando** solicito el enriquecimiento con IA  
- **Entonces** el **frontend** llama a **ai-assistant-service**, que invoca al proveedor de IA, valida el JSON según `mongo.md` §6.3 y devuelve un resultado compatible con los campos de esa pantalla  
- **Y** se registra una entrada en **AUDITORIA_USO_IA** con el `subject_oidc` del token  
- **Y** la UI precarga esos campos para que el ADMIN pueda revisarlos o ajustarlos manualmente  
- **Y** **ai-assistant-service** no ha llamado a **catalog-service** en ningún momento.

### Escenario 2 — Colaborador no puede usar la función

- **Dado que** soy un colaborador autenticado sin rol **ADMIN**  
- **Cuando** intento invocar el endpoint de enriquecimiento de especie por IA  
- **Entonces** recibo **403 Forbidden** (Problem Details).

### Escenario 3 — La consulta no procede o la respuesta es inválida

- **Dado que** soy **ADMIN** autenticado  
- **Y** la especie ya tiene datos de enriquecimiento persistidos en Mongo  
- **Cuando** abro la pantalla de edición de características  
- **Entonces** la acción de consulta a IA no está disponible  
- **Y** no se invoca al proveedor de IA  
- **Dado que** el proveedor de IA devuelve un JSON que no supera la validación de `mongo.md` §6.3  
- **Cuando** **ai-assistant-service** valida la respuesta  
- **Entonces** el **frontend** recibe un error (Problem Details)  
- **Y** no se precargan datos en la pantalla  
- **Y** **catalog-service** no recibe ninguna petición.

---

## 4. Evaluación INVEST (resumen)

| Criterio | Comentario |
|----------|------------|
| **Independiente** | Parcialmente: depende de **HU-011**, **HU-013** y de la pantalla ya existente entregada en **HU-015**. |
| **Negociable** | Sí: UX de activación y contrato `/api/ai` refinables; el núcleo (solo ADMIN, consulta IA, sin persistencia) está cerrado. |
| **Valiosa** | Sí: enriquecimiento enciclopédico orientativo para gestión taxonómica sin exponer IA a colaboradores. |
| **Estimable** | Sí: estimación **M**; incertidumbre principal en contrato `/api/ai` y formato exacto del JSON de respuesta. |
| **Small** | Sí: un flujo ADMIN de consulta y precarga, sin persistencia ni batch. |
| **Testable** | Sí: WebMvc en ai-assistant con mock de LLM; validación §6.3; 403; ocultación de la acción si ya existe enriquecimiento; frontend con mocks del servicio IA. |

---

## 5. Esfuerzo estimado de implementación

Orden de magnitud **M**: **ai-assistant-service** (LLM, validación §6.3, respuesta al cliente, auditoría **AUDITORIA_USO_IA**, OpenAPI `/api/ai/**`) y **frontend** en la pantalla ya existente de edición de características de especie (disparo de la consulta, precarga de campos y manejo de errores). La persistencia Mongo y los endpoints de guardado quedan fuera y pertenecen a **HU-015**. Cifra en persona-días: **no fijada en fuentes**.

**Nota contractual (post-cierre):** el JSON de salida comparte la forma estructural de `SpeciesEnrichmentReplaceRequest`; `ecologicalData` en IA (`AiSpeciesEcologicalData`) difiere del schema de catálogo en enums `growthRate`/`leafType` (inglés tras validación vs español en Mongo/catálogo). Claves `clima`/`suelo` en ambos. Ver [ADR-0007](../adr/0007-english-http-spanish-persistence.md) regla 10 y [openapi.yaml](../api/openapi.yaml).
