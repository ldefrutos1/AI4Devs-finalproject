# HU-016 — Consulta de características de especie (ADMIN, MVP)

## 1. Historia refinada

| Campo | Valor |
|-------|--------|
| **ID** | HU-016 |
| **Épica** | Inteligencia artificial |
| **Título** | Consulta de características de especie (ADMIN, MVP) |
| **Estimación de complejidad** | M |
| **Prioridad** | Media |
| **Estado** | **Pendiente** (sin breakdown; API de especie **HU-015** **Cerrada**) |

**Historia de usuario**

Como usuario con rol **ADMIN**, quiero consultar o solicitar el enriquecimiento con IA de las características de una especie ya registrada en el catálogo taxonómico, para disponer de información ampliada (hábitat, distribución, datos ecológicos, referencias, etc.) como apoyo a la gestión de maestros, sin que el sistema presente la respuesta como veredicto científico ni exponga esta función a colaboradores en el MVP.

- **Entregable de la historia:** flujo **ADMIN** en **`/admin/masters`** donde el **frontend** orquesta dos servicios sin comunicación entre ellos: (1) invoca **ai-assistant-service** (`/api/ai/**`) para obtener y validar el JSON de enriquecimiento según [mongo.md](../data-model/mongo.md) §6.3; (2) muestra el resultado en pantalla para **revisión y edición** por el ADMIN; (3) al pulsar **Guardar**, el **frontend** envía el documento a **catalog-service**, que es quien persiste `especie_detalle` en MongoDB (**HU-015**). **ai-assistant-service** no llama a **catalog-service** ni accede a Mongo. Trazabilidad en **AUDITORIA_USO_IA** al invocar la IA.

### Alcance

#### Incluye

- **Solo rol ADMIN** (JWT con rol de administración); **403** para colaborador u otros roles.
- Consulta y enriquecimiento con IA de **especie** (no de ejemplar/árbol en este MVP).
- Pantalla **`/admin/masters`**: acción para solicitar enriquecimiento IA de la especie seleccionada o en edición (único punto de entrada MVP para disparar IA; no desde alta/edición de ejemplar — **HU-015**).
- **Frontend** como orquestador: llama a **ai-assistant-service** y a **catalog-service** por separado; **sin** llamadas **ai-assistant-service** → **catalog-service**.
- **ai-assistant-service:** invocación al proveedor de IA externo; el contexto mínimo de la especie (`speciesId`, nombre científico y común) lo envía el **frontend** en la petición (datos ya disponibles en la pantalla de maestros vía catálogo SQL).
- **Validación** del JSON del LLM en **ai-assistant-service** según [mongo.md](../data-model/mongo.md) §6.3 **antes de devolverlo al frontend** (rechazo de claves desconocidas en raíz, rangos, enumerados, URLs, etc.).
- **Frontend:** muestra el JSON devuelto (o el documento ya existente) en un formulario **editable**; el ADMIN puede corregir antes de guardar.
- **Persistencia:** solo cuando el ADMIN pulsa **Guardar**, el **frontend** envía el documento a **catalog-service** (endpoints de enriquecimiento definidos en **HU-015**); merge con proyección mínima y nombres desnormalizados en catálogo.
- **Lectura** de `especie_detalle` existente: el **frontend** consulta **catalog-service** (p. ej. al abrir la especie o antes de refrescar con IA).
- **Auditoría R3** en esquema `ai` (**AUDITORIA_USO_IA**: `subject_oidc`, `tipo_uso_ia`, resumen de prompt/resultado, `consultado_en`).
- Mensaje de producto: IA **orientativa** ([product-context](../../.cursor/rules/product-context.mdc)); sin veredictos definitivos en UI.
- Contrato HTTP del asistente bajo `/api/ai/**` (ruta concreta a cerrar en refinamiento; p. ej. enriquecimiento por `speciesId`).

#### Queda fuera de esta historia

- **Identificación orientativa por imagen** (**HU-009**, UC-05, `/ai/identify`, `POST /api/ai/species-identification`).
- **Chat asistido** (**HU-010**, UC-06, `/ai/chat`, `POST /api/ai/chat`).
- Enriquecimiento de **ejemplar** / notas de campo (`ejemplar_detalle`, observaciones embebidas) — alcance **HU-015**.
- **Acceso directo a MongoDB** desde **ai-assistant-service** (propiedad Mongo en **catalog-service**, [HU-015](HU-015-proyeccion-y-enriquecimiento-mongo.md)).
- **Comunicación ai-assistant-service ↔ catalog-service** (la persistencia la hace el **frontend** al guardar).
- **Edición manual** de `especie_detalle` en popup de alta/edición de ejemplar (**HU-015**); en **HU-016** el flujo de enriquecimiento con IA y guardado asociado vive en **`/admin/masters`**.
- Acceso de **colaborador** a funciones de IA.
- Disparar IA desde pantallas de ejemplar o consulta pública.
- Sincronización automática masiva de todas las especies del catálogo (batch); el MVP se limita a **bajo demanda** por especie seleccionada por el **ADMIN**.
- **Sincronización** de `especie_detalle` cuando se renombra o elimina especie en **HU-011** (deuda compartida con **HU-015**, fuera de esta versión).

### Dependencias

- **HU-001** (JWT, rol **ADMIN**).
- **HU-011** (especies en catálogo SQL; pantalla `/admin/masters`).
- **HU-013** (navegación y guardas por rol).
- **HU-015** (endpoints de **catalog-service** para lectura/escritura de `especie_detalle`; necesarios para **Guardar** y para cargar documento existente).
- Infra: **MongoDB** (vía **catalog-service**), **ai-assistant-service**, gateway `/api/ai/**` y `/api/catalog/**`; **frontend** como único coordinador entre IA y catálogo en este flujo.

### Riesgos

- **Coste y límites** del proveedor de IA (cuotas, timeouts); definir límites por ADMIN en refinamiento.
- **Calidad alucinaciones:** mitigar con copy de producto y revisión humana del ADMIN; validación estructural no garantiza veracidad de referencias.
- **Desalineación SQL/Mongo** si se renombra especie en **HU-011** sin invalidar `especie_detalle` (deuda aceptada; sin sync automática en esta versión).
- **Pérdida de borrador:** si el ADMIN cierra sin **Guardar**, los datos devueltos por la IA no quedan en Mongo (comportamiento esperado; copy de UI).
- **Fallo al guardar en catalog-service** tras edición en pantalla: aviso al ADMIN y posibilidad de reintento sin nueva llamada a IA.

### Decisiones de refinamiento (registro)

| Tema | Decisión |
|------|----------|
| **Propiedad Mongo** | Solo **catalog-service** lee y escribe Mongo ([HU-015](HU-015-proyeccion-y-enriquecimiento-mongo.md)). |
| **Orquestación** | **Frontend** llama a **ai-assistant-service** (IA) y a **catalog-service** (lectura/guardado). **Sin** comunicación entre microservicios de IA y catálogo. |
| **Punto de entrada IA** | Solo **`/admin/masters`** en MVP; no desde formularios de ejemplar (**HU-015**). |
| **Validación LLM** | **ai-assistant-service** (`mongo.md` §6.3) antes de devolver JSON al frontend. |
| **Persistencia** | El **frontend** envía a **catalog-service** al pulsar **Guardar**; upsert de `especie_detalle` con merge de proyección mínima en catálogo. |
| **Revisión humana** | El ADMIN **edita** en pantalla el resultado de la IA antes de guardar. |
| **Roles** | Solo **ADMIN**; colaborador **403** en `/api/ai/**` de esta función. |
| **Sync especie SQL→Mongo** | **No** en esta versión (renombre/DELETE en **HU-011**). |
| **Auditoría** | **AUDITORIA_USO_IA** en esquema `ai` por cada invocación de enriquecimiento con IA. |

### Aclaraciones pendientes (refinamiento)

- Path y verbo exactos en OpenAPI de **ai-assistant-service** (p. ej. `POST` para disparar enriquecimiento por `speciesId` + contexto de nombres).
- UX en `/admin/masters`: botones separados para **cargar existente**, **solicitar IA** y **Guardar**; comportamiento si ya hay `especie_detalle`.
- Contrato del endpoint de **catalog-service** para guardar `especie_detalle` (definido en **HU-015**; consumido por el frontend en este flujo y en el popup de ejemplar).
- Mensajes de error si falla la IA vs si falla el guardado en catálogo.

### Referencia

Coordinación Mongo: [HU-015-proyeccion-y-enriquecimiento-mongo.md](HU-015-proyeccion-y-enriquecimiento-mongo.md). Modelo: [mongo.md](../data-model/mongo.md) §§3.1 y 6.

## 2. Criterios de aceptación (BDD)

### Referencias

readme §2, §3.1.4; [mongo.md](../data-model/mongo.md) (`especie_detalle`, §6.3); [mongo-hybrid.mdc](../../.cursor/rules/mongo-hybrid.mdc); [data-model.md](../data-model/data-model.md) **R3**; [openapi.yaml](../api/openapi.yaml); **HU-011**, **HU-015**; [product-context.mdc](../../.cursor/rules/product-context.mdc).

### Escenario 1 — ADMIN solicita IA, revisa y guarda en catálogo

- **Dado que** soy **ADMIN** autenticado en `/admin/masters` y existe una especie con `speciesId` válido  
- **Cuando** solicito el enriquecimiento con IA  
- **Entonces** el **frontend** llama a **ai-assistant-service**, que invoca al proveedor de IA, valida el JSON según `mongo.md` §6.3 y **devuelve** el resultado al cliente  
- **Y** se registra una entrada en **AUDITORIA_USO_IA** con el `subject_oidc` del token  
- **Y** la UI muestra el contenido **editable** con aviso de que es orientativo  
- **Cuando** pulso **Guardar** tras revisar o corregir los datos  
- **Entonces** el **frontend** envía el documento a **catalog-service**, que persiste `especie_detalle` en Mongo  
- **Y** **ai-assistant-service** no ha llamado a **catalog-service** en ningún momento.

### Escenario 2 — Colaborador no puede usar la función

- **Dado que** soy un colaborador autenticado sin rol **ADMIN**  
- **Cuando** intento invocar el endpoint de enriquecimiento de especie por IA  
- **Entonces** recibo **403 Forbidden** (Problem Details).

### Escenario 3 — Especie inexistente

- **Dado que** soy **ADMIN** autenticado  
- **Cuando** solicito enriquecimiento para un `speciesId` inexistente en catálogo SQL  
- **Entonces** recibo **404 Not Found** (o error acordado en contrato) **sin** llamar al proveedor de IA.

### Escenario 4 — JSON del LLM no supera validación

- **Dado que** soy **ADMIN** y el proveedor de IA devuelve un JSON con estructura inválida (p. ej. claves desconocidas en raíz o rangos incorrectos según §6.3)  
- **Cuando** **ai-assistant-service** valida la respuesta  
- **Entonces** el **frontend** recibe un error (Problem Details) y **no** muestra formulario editable para guardar  
- **Y** **catalog-service** no recibe ninguna petición de persistencia.

### Escenario 5 — Carga de documento existente sin refrescar IA

- **Dado que** ya existe un `especie_detalle` para la especie seleccionada  
- **Cuando** como **ADMIN** abro o cargo las características sin solicitar un nuevo enriquecimiento con IA  
- **Entonces** el **frontend** obtiene el documento desde **catalog-service** y lo muestra editable  
- **Y** no se invoca a **ai-assistant-service** ni se registra nueva **AUDITORIA_USO_IA** salvo que el ADMIN pida refrescar con IA.

### Escenario 6 — IA no accesible desde pantallas de ejemplar

- **Dado que** soy **ADMIN** en alta o edición de un ejemplar (**HU-015**)  
- **Cuando** abro el popup de `especie_detalle` junto al selector de especie  
- **Entonces** puedo consultar o editar manualmente el documento según **HU-015**  
- **Y** **no** hay acción para disparar enriquecimiento con IA (reservado a `/admin/masters`).

## 3. Evaluación INVEST (resumen)

| Criterio | Comentario |
|----------|------------|
| **Independiente** | Parcialmente: depende de **HU-011**, **HU-013** y de endpoints de enriquecimiento en **HU-015** para guardar/cargar. |
| **Negociable** | Cerrado en *Decisiones de refinamiento* (frontend orquesta, sin enlace IA↔catálogo, revisión humana antes de guardar). |
| **Valiosa** | Sí: aporta enriquecimiento enciclopédico orientativo para gestión taxonómica sin exponer IA a colaboradores. |
| **Estimable** | Sí: estimación **M**; incertidumbre en contrato `/api/ai` y en UX de `/admin/masters` (cargar / IA / guardar). |
| **Small** | Acotada: un flujo ADMIN, un tipo de documento (`especie_detalle`), sin batch ni ejemplar. |
| **Testable** | Sí: WebMvc en ai-assistant con mock de LLM (sin cliente a catálogo); validación §6.3; 403/404; auditoría `ai`; frontend con mocks de ambos servicios. |

## 4. Esfuerzo estimado de implementación

Orden de magnitud **M**: **ai-assistant-service** (LLM, validación §6.3, respuesta al cliente, auditoría **AUDITORIA_USO_IA**, OpenAPI `/api/ai/**`); **frontend** en `/admin/masters` (llamada IA, formulario editable, **Guardar** vía **catalog-service**); pruebas en ambas capas. Capa Mongo y endpoints de guardado en **HU-015**. Cifra en persona-días: **no fijada en fuentes**.

## 5. Enlaces

- Backlog: [backlog.md](backlog.md) §3 (**HU-016**).
- Próximas versiones: **HU-009**, **HU-010**.
- Maestros: **HU-011**; Mongo y API de enriquecimiento: **HU-015**.
