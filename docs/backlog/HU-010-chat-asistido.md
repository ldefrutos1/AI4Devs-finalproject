# HU-010 — Chat asistido

## 1. Validación de la información existente

| Campo | En backlog | En fuentes (readme, casos de uso) | Valoración |
|-------|------------|-----------------------------------|------------|
| **ID** | HU-010 | Coherente con UC-06 | Correcto |
| **Épica** | Inteligencia artificial | readme §2.2, §3.2.4; backlog §2 | Correcto |
| **Título** | Chat asistido | UC-06 «Consultar asistente IA (chat)» | Correcto |
| **Historia** | Como colaborador autenticado, quiero mantener una conversación con el asistente de IA (UC-06), para resolver dudas relacionadas con el catálogo. | readme §2.2.2 (UC-06, actor Colaborador); [use-case-summary.md](../use-cases/use-case-summary.md) — interacción conversacional | Correcta; refinada en §2 |
| **Estimación** | M | Sin contradicción | **Se mantiene M** |
| **Prioridad** | Media | Sin contradicción | **Se mantiene Media** |
| **Estado** | **Cerrada** | Breakdown [HU-010-ticket-breakdown.md](HU-010-ticket-breakdown.md); tickets 01–10 **Hecho**; documentación coordinada en **TASK-HU-010-11** (en curso) |

**Notas de cierre (§1)**

- El modelo de actores UML hace que **ADMIN** herede de **Colaborador**; UC-06 usa actor Colaborador y la implementación admite **COLABORADOR** y **ADMIN** en `POST /api/ai/chat/messages` y `EditTreeView`.
- Contrato y endpoint operativos: `POST /api/ai/chat/messages` en [openapi.yaml](../api/openapi.yaml) y **ai-assistant-service**; auditoría con `ejemplar_id` = `treeId` (§2.2–§2.4).
- [readme.md](../../readme.md), [backlog.md](backlog.md), [use-case-summary.md](../use-cases/use-case-summary.md), [services/README.md](../../services/README.md), [frontend/README.md](../../frontend/README.md) y [services/ai-assistant-service/README.md](../../services/ai-assistant-service/README.md) alineados con UC-06 en MVP (**TASK-HU-010-11**).
- La historia de usuario habla de dudas «sobre ese ejemplar» en el **contexto de edición**; el LLM no recibe metadatos de la ficha (§2.3).

**INVEST (preliminar):** historia acotable y valiosa; depende de identidad, navegación y la base de **ai-assistant-service** ya entregada en **HU-016**; entregada en **M** según refinamiento §2.1–§2.6.

---

## 2. Historia refinada

| Campo | Valor |
|-------|--------|
| **ID** | HU-010 |
| **Épica** | Inteligencia artificial |
| **Título** | Chat asistido |
| **Estimación de complejidad** | M |
| **Prioridad** | Media |
| **Estado** | **Cerrada** |

**Historia de usuario**

Como colaborador autenticado, quiero mantener una conversación con el asistente de IA (**UC-06**) desde la pantalla de **mantenimiento de un ejemplar** ya existente, para resolver dudas orientativas sobre ese ejemplar, el catálogo, la taxonomía o el uso de la plataforma, sin que el sistema sustituya mi criterio experto ni presente las respuestas como veredicto científico.

- **Entregable de la historia:** asistente de **chat conversacional** en la vista de **edición de ficha** (`EditTreeView`, **HU-008** / UC-04), accesible para **COLABORADOR** y **ADMIN**, respaldado por **ai-assistant-service** (`POST /api/ai/chat/messages`). Cada turno invoca al proveedor (o `stub` local) y registra **AUDITORIA_USO_IA** con `ejemplar_id` = identificador del ejemplar en edición. La historia **no** incluye **HU-009** ni **HU-016**.

### Alcance

#### Incluye

- **Solo usuarios autenticados** con rol **COLABORADOR** o **ADMIN**; **401/403** para anónimos o roles no autorizados.
- Interacción **conversacional** con el asistente (**UC-06**) desde **`EditTreeView`** (mantenimiento de ejemplar existente).
- Cada petición incluye **`treeId`** obligatorio (identificador del ejemplar en edición) y persiste **`ejemplar_id`** en **AUDITORIA_USO_IA**.
- **UX no intrusiva:** disparador compacto en la página de edición; panel de chat en **capa superpuesta** (`<dialog>` / overlay), no embebido en el flujo del formulario, sin interferir con **Eliminar** / **Guardar** (pie sticky en móvil, §2.2).
- Ámbito temático acotado a **dudas relacionadas con el catálogo** y el dominio de la plataforma (registro y consulta de ejemplares, especies, uso de funciones colaborativas), según la redacción del backlog.
- **Frontend** como consumidor de **ai-assistant-service** vía API Gateway (`/api/ai/**`); copy de producto que deje claro carácter **orientativo** de la IA ([product-context](../../.cursor/rules/product-context.mdc)).
- **ai-assistant-service:** construcción de prompt de chat (system prompt genérico + `messages` del usuario/asistente; **sin** `treeId` ni metadatos del ejemplar en el LLM), invocación al proveedor (reutilizando la infraestructura OpenAI Responses / `stub` existente), manejo de errores y timeouts con Problem Details.
- **Auditoría R3** en esquema `ai` (**AUDITORIA_USO_IA**) por interacción, con `tipo_uso_ia` = **`chat-message`** (§2.4).
- Contrato HTTP nuevo en OpenAPI para el flujo de chat, coherente con [api-contract.mdc](../../.cursor/rules/api-contract.mdc).

#### Queda fuera de esta historia

- **Identificación orientativa por imagen** (**HU-009**, UC-05).
- **Consulta de características de especie para ADMIN** (**HU-016**, UC-10).
- Acceso al chat para **visitantes sin sesión**, desde **consulta pública** o desde **alta de ejemplar** (`CreateTreeView`, UC-03: aún no existe `treeId`).
- **Persistencia de historiales de chat** en servidor, **`sessionStorage`** o al recargar / salir de la ficha (§2.3: el hilo se pierde al desmontar la vista).
- **Metadatos del ejemplar** (especie, provincia, coordenadas, etc.) en el prompt enviado al proveedor IA; **`treeId` solo para auditoría** (§2.3).
- **Llamadas síncronas** de **ai-assistant-service** a **catalog-service** para enriquecer contexto.
- Sincronización con **notificaciones**, **medios** o flujos de alta/edición de fichas más allá de responder dudas en chat.
- **Batch**, exportación de conversaciones o panel de administración de chats.
- Cambios en **HU-009** o en el alcance MVP de identificación por imagen.

### Dependencias

- **HU-001** (JWT, roles **COLABORADOR** / **ADMIN**).
- **HU-008** (edición de ficha; vista `EditTreeView` donde se integra el chat).
- **HU-013** (navegación, estructura de páginas y guardas por rol).
- **HU-016** (infraestructura operativa de **ai-assistant-service**, gateway `/api/ai/**`, auditoría **AUDITORIA_USO_IA**, modo `stub` / proveedor externo).
- Infra: **Keycloak**, **API Gateway**, PostgreSQL esquema `ai`.

### Riesgos

- **Desalineación documental:** cerrada en **TASK-HU-010-11** (fuentes de producto y READMEs alineados).
- **Coste y límites** del proveedor de IA (cuotas, timeouts); definir límites por usuario en implementación.
- **Calidad / alucinaciones:** mitigar con copy orientativo y sin presentar la IA como fuente autoritativa; el colaborador debe validar la información.
- **Contrato conversacional:** cerrado en §2.1–§2.6.
- **Solapamiento funcional con HU-016:** distintos roles y propósitos (ADMIN enriquece especie; colaborador chatea); la seguridad por endpoint debe mantenerse separada.
- **Timeout o error del proveedor:** la UI debe informar sin perder mensajes ya mostrados en pantalla (según modelo de estado elegido).

### Aclaraciones pendientes (refinamiento)

- ~~Path y verbo exactos en OpenAPI~~ → **cerrado** en §2.1.
- ~~Modelo de continuidad conversacional~~ → **cerrado** en §2.1.
- ~~Uso de `ejemplar_id` / ubicación UI~~ → **cerrado** en §2.2.
- ~~Persistencia del hilo en cliente~~ → **cerrado** en §2.3 (sin persistencia al salir).
- ~~Contenido del prompt / metadatos del ejemplar~~ → **cerrado** en §2.3.
- ~~Valor canónico de `tipo_uso_ia`~~ → **cerrado** en §2.4 (`chat-message`).
- ~~Límites operativos~~ → **cerrado** en §2.5.
- ~~System prompt~~ → **cerrado** en §2.6.
- Documentación de producto alineada (**TASK-HU-010-11**): `readme.md`, `backlog.md`, `use-case-summary.md`, `services/README.md`, `services/ai-assistant-service/README.md`, `frontend/README.md`, este documento.

### 2.1 Decisión de refinamiento — Contrato OpenAPI (chat)

**Enfoque elegido:** turno conversacional **stateless** con **historial en cliente** (patrón habitual en integraciones LLM tipo OpenAI/Anthropic: el cliente reenvía el hilo acumulado en cada petición). El servidor **no** persiste conversaciones; solo procesa el turno, devuelve la respuesta del asistente y registra **AUDITORIA_USO_IA** por invocación.

**Motivos (arquitectura / MVP MTL):**

- Coherente con readme §4.2.5: no hay entidad de conversación, solo auditoría por consulta.
- Alineado con **HU-016**: **ai-assistant-service** ya es stateless y sin llamadas a **catalog-service**.
- Menor complejidad operativa (sin TTL de sesiones, sin migraciones nuevas, sin listados paginados de hilos).
- Escalable en evolución: más adelante se puede añadir `POST /api/ai/chat/conversations` sin romper el contrato si hiciera falta persistencia.

**Operación canónica**

| Aspecto | Decisión |
|---------|----------|
| **Path** | `POST /api/ai/chat/messages` |
| **Verbo** | `POST` (acción de envío de turno; no idempotente) |
| **Autenticación** | Bearer JWT; roles **COLABORADOR** o **ADMIN** |
| **Tag OpenAPI** | `ai` (ampliar descripción del tag para incluir chat **HU-010**) |

**Modelo de hilo**

- El cliente genera un **`conversationId`** (UUID v4) al abrir el chat y lo reenvía en cada turno para **correlación** en auditoría y logs; el servidor **no** valida existencia previa ni almacena el hilo.
- Cuerpo **`messages`**: array ordenado cronológicamente de turnos previos + mensaje nuevo del usuario. Cada elemento: `{ "role": "user" \| "assistant", "content": string }`.
- **Regla:** el último elemento de `messages` debe ser `role: user` (el turno que se quiere contestar).
- **Contexto de ejemplar en API:** **`treeId` obligatorio** en el cuerpo de `POST /api/ai/chat/messages` desde `EditTreeView`; el servicio lo usa **únicamente** para `ejemplar_id` en auditoría, **no** se inyecta en el prompt al proveedor IA (§2.3).
- **Límites de contrato:** ver §2.5 (validación en API y OpenAPI).

**Esquemas (referencia OpenAPI)**

Request `AiChatMessageRequest`:

```json
{
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "treeId": 42,
  "messages": [
    { "role": "user", "content": "¿Qué datos necesito para dar de alta un árbol?" },
    { "role": "assistant", "content": "Necesitas especie, coordenadas..." },
    { "role": "user", "content": "¿La especie tiene que existir en maestros?" }
  ]
}
```

Response `AiChatMessageResponse` (**200**):

```json
{
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "message": {
    "role": "assistant",
    "content": "Sí, según las reglas del catálogo...",
    "createdAt": "2026-07-01T10:15:30Z"
  }
}
```

**Códigos de respuesta**

| Código | Uso |
|--------|-----|
| **200** | Turno procesado; `message` con respuesta del asistente |
| **400** | Validación (§2.5: array, roles, tamaños, `treeId`, `conversationId`) |
| **401** / **403** | Sin JWT o sin rol **COLABORADOR** / **ADMIN** |
| **422** | Mensaje fuera de política de contenido (si se implementa filtro) |
| **429** | Rate limit por usuario (§2.5) |
| **502** | Fallo o timeout del proveedor IA tras reintentos (mismo criterio que **HU-016**) |

**Queda explícitamente fuera de este contrato (MVP)**

- `GET /api/ai/chat/conversations` o recuperación de historial desde servidor.
- `DELETE` de conversaciones.
- Streaming SSE/WebSocket (valorar en versión posterior si la UX lo exige).

**Alternativa descartada para MVP:** sesiones persistidas en servidor (`POST /api/ai/chat/conversations` + `POST .../messages`). Es el patrón «enterprise» cuando hay retención legal, uso multi-dispositivo o moderación centralizada; aquí añade modelo de datos y operaciones CRUD sin requisito documentado en las fuentes.

### 2.2 Decisión de refinamiento — Ubicación en UI y contexto de ejemplar

**Pantalla de integración**

| Aspecto | Decisión |
|---------|----------|
| **Vista** | `EditTreeView` — mantenimiento / edición de ejemplar existente (**HU-008**, UC-04) |
| **Fuera de alcance UI** | `CreateTreeView` (alta UC-03: sin `treeId` persistido), consulta pública (`TreeDetailView`), listados |
| **Disponibilidad** | Solo cuando la ficha está cargada (`isReady`); oculto o deshabilitado si hay error de carga |
| **Roles** | **COLABORADOR** (fichas propias) y **ADMIN** (cualquier ficha), coherente con permisos de edición |

**Contexto de ejemplar y auditoría**

- Cada turno envía **`treeId`** = identificador del ejemplar en edición (ADR-0006).
- **ai-assistant-service** persiste **`ejemplar_id`** en **AUDITORIA_USO_IA** con ese valor en toda invocación exitosa.
- El servicio **no** consulta **catalog-service** y **no** recibe metadatos del formulario para el LLM (§2.3).

**Patrón UX (no ensuciar el formulario)**

Referencia de producto: `SpeciesEnrichmentPopup` — disparador discreto + contenido en capa modal, sin añadir secciones permanentes al formulario.

| Principio | Criterio de diseño |
|-----------|-------------------|
| **No bloquear Guardar / Eliminar** | El chat **no** vive en `page-actions-footer` ni como FAB en esquina inferior derecha (zona del pie sticky en móvil, `z-index: 15`) |
| **Disparador** | Botón secundario compacto en la **cabecera de página** (`page-header`), etiqueta del tipo «Asistente IA» / icono + texto corto; visible en desktop y móvil |
| **Panel de chat** | `<dialog>` modal o overlay a pantalla casi completa en móvil; en desktop, panel lateral o diálogo centrado con altura acotada |
| **Cierre** | Cerrar el chat **no** altera el formulario ni dispara guardado; el usuario puede seguir editando y pulsar **Guardar** / **Eliminar** con normalidad |
| **Móvil** | Diálogo ocupa viewport útil (respetando `safe-area-inset`); scroll interno del hilo; campo de envío fijo al fondo **del diálogo**, no del documento |
| **Accesibilidad** | Foco atrapado en el diálogo abierto; `Esc` cierra; `aria-labelledby` / roles de región para el hilo |
| **Copy** | Aviso breve de IA **orientativa** en la primera apertura o cabecera del panel ([product-context](../../.cursor/rules/product-context.mdc)) |

**Comportamiento del hilo en cliente**

- Historial **solo en memoria** del componente mientras el diálogo está abierto y la vista `EditTreeView` permanece montada.
- Al **cerrar el diálogo y volver a abrirlo**, **recargar la página**, **navegar fuera** de la ficha o **eliminar** el ejemplar: el hilo se **reinicia** (nuevo `conversationId`; sin `sessionStorage` ni persistencia en servidor). Detalle en §2.3.

**Esquema de capas (móvil)**

```
┌─────────────────────────────┐
│ ← Volver    Editar #42  [IA]│  ← disparador en header (no en footer)
├─────────────────────────────┤
│  Formulario (scroll)        │
│  especie, mapa, fotos…      │
│                             │
├─────────────────────────────┤
│ [Volver]  [Eliminar][Guardar]│  ← sticky footer; sin chat aquí
└─────────────────────────────┘

Al pulsar [IA] → <dialog> casi fullscreen con hilo + input propio
```

### 2.3 Decisión de refinamiento — Ciclo de vida del hilo, prompt y auditoría

**Ciclo de vida del hilo (opción A — sin persistencia al salir)**

| Evento | Comportamiento |
|--------|----------------|
| Usuario envía varios mensajes con el diálogo abierto | El cliente acumula `messages[]` en memoria y los reenvía en cada `POST` (§2.1) |
| Usuario cierra el diálogo y lo vuelve a abrir en la misma ficha | **Nuevo** `conversationId`; hilo vacío |
| Recarga de página (F5) | Hilo perdido |
| Navegación a otra ruta y regreso a la misma ficha | Hilo perdido (vista desmontada) |
| Alta de ejemplar (`CreateTreeView`) | Chat **no disponible** (sin `treeId`; UC-03 excluido) |

No se usa `sessionStorage`, `localStorage` ni tabla de conversaciones.

**Contenido enviado al proveedor IA**

| Dato | ¿Va al LLM? | ¿Va a auditoría / API? |
|------|-------------|-------------------------|
| Texto del usuario (`messages[].content`, rol `user`) | **Sí** | Resumen en `prompt` / `resultado_resumen` |
| Respuestas previas del asistente (rol `assistant`) | **Sí** (continuidad del hilo en la misma sesión de diálogo) | Incluidas en el resumen según política de recorte |
| **`treeId`** | **No** | **Sí** → `ejemplar_id` en **AUDITORIA_USO_IA** |
| Metadatos del formulario (especie, provincia, coordenadas, estado, fotos…) | **No** | **No** |
| Llamada a **catalog-service** | **No** | **No** |

**System prompt:** definido en §2.6 (fijo en **ai-assistant-service**, sin datos del ejemplar en el LLM).

**Resumen:** el contrato HTTP incluye `treeId` por trazabilidad de negocio; el **prompt al proveedor** se limita al **system prompt** fijo + array **`messages`** de la conversación en curso.

### 2.4 Decisión de refinamiento — `tipo_uso_ia` en auditoría

| Aspecto | Decisión |
|---------|----------|
| **Campo** | `auditoria_uso_ia.tipo_uso_ia` (esquema `ai`, readme §4.2.5) |
| **Valor canónico (HU-010)** | **`chat-message`** |
| **Convención** | Literal en **inglés**, **kebab-case**, alineado con `species-enrichment-suggestion` (**HU-016**) |
| **Uso** | Toda invocación exitosa de `POST /api/ai/chat/messages` desde el flujo de chat |
| **Implementación** | Constante en **ai-assistant-service** (p. ej. `ChatMessageService.TIPO_USO_IA`); mismo valor en tests de auditoría |

**Catálogo de valores conocidos en el servicio (referencia):**

| `tipo_uso_ia` | Historia / función |
|---------------|-------------------|
| `species-enrichment-suggestion` | **HU-016** — sugerencia IA de enriquecimiento de especie (ADMIN) |
| `chat-message` | **HU-010** — turno de chat colaborador desde `EditTreeView` |

### 2.5 Decisión de refinamiento — Límites operativos

Contexto: aplicación **colaborativa no comercial** (MVP, pocos usuarios concurrentes). Los límites priorizan **uso legítimo** del colaborador que edita una ficha, **contención de coste** del proveedor IA y **simplicidad** de implementación (sin Redis ni cuotas comerciales por plan).

**Principios**

- Reutilizar timeouts y reintentos al proveedor ya configurados en **HU-016** (`application.properties`).
- Validar en **ai-assistant-service** (fuente de verdad); el frontend aplica los mismos topes en UX (deshabilitar envío, `maxlength`).
- Ante abuso ligero: **429** con Problem Details; no bloqueo permanente de cuenta en MVP.

#### Contrato HTTP (`POST /api/ai/chat/messages`)

| Límite | Valor | HTTP si se incumple |
|--------|-------|---------------------|
| Mensajes en `messages[]` | máx. **20** | **400** |
| `content` por mensaje (cualquier `role`) | máx. **2 000** caracteres | **400** |
| Último mensaje del array | `role` = **`user`**, `content` no vacío (trim) | **400** |
| `treeId` | entero ≥ 1, obligatorio | **400** |
| `conversationId` | UUID v4 válido, obligatorio | **400** |
| Roles permitidos en `messages[].role` | solo `user` \| `assistant` | **400** |

**Nota:** 20 turnos en array ≈ 10 intercambios usuario↔asistente en una sesión de edición; coherente con hilo no persistente (§2.3).

#### Auditoría (`AUDITORIA_USO_IA`)

| Campo | Recorte al persistir (igual criterio que **HU-016**) |
|-------|------------------------------------------------------|
| `prompt` | máx. **8 000** caracteres |
| `resultado_resumen` | máx. **4 000** caracteres |

#### Proveedor IA (timeouts y reintentos servidor)

Reutilizar propiedades existentes de OpenAI en **ai-assistant-service** (sin duplicar valores salvo propiedad específica de modelo de chat en implementación):

| Parámetro | Valor por defecto |
|-----------|-------------------|
| Connect timeout | **5 s** (`mtl.ai.openai.connect-timeout`) |
| Read timeout | **60 s** (`mtl.ai.openai.read-timeout`) |
| Reintentos al proveedor | **3** intentos (`mtl.ai.openai.retry.max-attempts`) |
| Backoff | **500 ms** → **3 s** (`initial-backoff` / `max-backoff`) |

Solo reintentos ante fallos **transitorios** de red o **5xx** del proveedor. Tras agotar reintentos → **502** al cliente (mismo criterio que enriquecimiento de especie).

#### Rate limiting (uso razonable, no comercial)

| Regla | Valor | HTTP |
|-------|-------|------|
| Turnos de chat por usuario (`subject_oidc`) | máx. **40 / hora** (ventana deslizante) | **429** |
| Intervalo mínimo entre dos peticiones del mismo `subject_oidc` | **2 s** | **429** |

- Alcance: solo `POST /api/ai/chat/messages` (no afecta a **HU-016**).
- Implementación MVP: contador **en memoria** por instancia del servicio (suficiente para despliegue pequeño); documentar en tickets que en clúster multi-réplica el límite efectivo puede ser algo mayor.
- **Sin** rate limit por IP en MVP (usuarios autenticados; UC-02 ya asume riesgo distinto en suscripciones públicas).

#### Frontend (UX)

| Comportamiento | Criterio |
|----------------|----------|
| Campo de entrada | `maxlength` = **2 000**; contador opcional |
| Envío | Deshabilitado mientras hay petición en curso |
| Doble envío | No encolar: ignorar pulsaciones adicionales hasta respuesta o error |
| Error **502** | Mensaje neutro + botón «Reintentar» que reenvía **el mismo** turno (un reintento manual; no bucle automático) |
| Error **429** | Mensaje de «demasiadas consultas»; no reintentar automáticamente |

#### Fuera de alcance MVP (no imponer ahora)

- Cuotas por rol (ADMIN vs COLABORADOR distintas).
- Límite diario global de la plataforma.
- Streaming / cancelación mid-flight.
- Rate limiting distribuido (Redis).
- Moderación automática de contenido con **422** en servidor (MVP: rechazo suave vía instrucciones del system prompt; §2.6).

### 2.6 Decisión de refinamiento — System prompt

**Ubicación:** mensaje de sistema fijo construido en **ai-assistant-service** (p. ej. `AiPromptFactory.buildChatSystemPrompt()`), enviado al proveedor en cada turno junto con `messages[]`. **No** incluye `treeId` ni metadatos del formulario (§2.3).

#### Idioma

| Regla | Detalle |
|-------|---------|
| **Idioma por defecto** | **Castellano** |
| **Otros idiomas** | **Permitidos:** si el usuario escribe en otro idioma, el asistente **puede** responder en ese idioma |
| **Preferencia** | Si el mensaje mezcla idiomas o es ambiguo, priorizar **castellano** |

#### Alcance temático (dentro de dominio)

Responder sobre:

- **Botánica y árboles:** taxonomía, morfología, fisiología básica, ecología, distribución, identificación orientativa, singularidad de ejemplares, conservación y documentación de árboles notables.
- **MyTreeLibrary (genérico):** cómo registrar o mantener **fichas de ejemplares arbóreos** en la plataforma (campos habituales, especies desde maestros, coordenadas, fotografías, borrador vs publicado), **sin** afirmar datos concretos del ejemplar en edición (el asistente **no** conoce la ficha actual).

#### Fuera de dominio (rechazo educado)

Si la pregunta **no** guarda relación con botánica, árboles o la documentación de ejemplares en MyTreeLibrary:

- Declinar con cortesía y brevedad.
- Invitar a reformular en torno a árboles o al catálogo.
- **No** inventar respuesta útil fuera de dominio.

Ejemplos típicos fuera de alcance: medicina o toxicología clínica, asesoramiento legal, política, programación general, entretenimiento u otros temas no vinculados a árboles o a la plataforma en ese contexto.

#### Tono y carácter orientativo

- Respuestas **claras, breves y prudentes**; priorizar utilidad práctica para un aficionado o colaborador del catálogo.
- Dejar explícito que la información es **orientativa**, no un dictamen científico ni una identificación definitiva ([product-context](../../.cursor/rules/product-context.mdc)).
- **No** afirmar haber consultado la ficha, el mapa ni las fotos del usuario.
- Ante incertidumbre taxonómica o identificativa, recomendar contrastar con fuentes fiables o criterio experto humano.
- Sin Markdown excesivo salvo listas cortas cuando ayuden a la lectura.

#### Borrador canónico (implementación)

Texto de referencia para el factory (ajustable en código sin cambiar alcance funcional):

```
Eres un asistente orientativo de MyTreeLibrary para colaboradores que documentan árboles singulares.

Idioma: responde en castellano por defecto. Si el usuario escribe en otro idioma, puedes responder en ese idioma.

Alcance: solo botánica, árboles (taxonomía, morfología, ecología, identificación orientativa, documentación de ejemplares) y uso genérico de MyTreeLibrary para registrar o mantener fichas de ejemplares arbóreos. No tienes acceso a los datos concretos de la ficha que el usuario está editando.

Estilo: respuestas útiles, concisas y prudentes. Indica que tu ayuda es orientativa y no sustituye el criterio de un experto ni una identificación definitiva. Si no sabes algo, dilo. Si la pregunta no está relacionada con árboles o con la documentación de ejemplares en MyTreeLibrary, recházala amablemente e invita a reformular dentro de ese ámbito.
```

#### Validación en servidor (MVP)

- **No** se exige filtro semántico adicional ni **422** por «tema fuera de alcance» en MVP: el cumplimiento del alcance se delega en las instrucciones del system prompt y en la revisión humana del colaborador.
- Mensajes vacíos, demasiado largos o rate limit siguen en §2.5.

---

## 3. Criterios de aceptación (BDD)

### Referencias

readme §2.2 (Integración con IA), §2.2.2 (**UC-06**), §3.2.4, §4.2.5; [use-case-summary.md](../use-cases/use-case-summary.md); [data-model.md](../data-model/data-model.md) **R3**; **HU-001**, **HU-013**, **HU-016**; [product-context.mdc](../../.cursor/rules/product-context.mdc); contrato chat en [openapi.yaml](../api/openapi.yaml) (`POST /api/ai/chat/messages`).

### Escenario 1 — Colaborador chatea desde la edición de un ejemplar

- **Dado que** soy un colaborador autenticado editando un ejemplar existente en `EditTreeView`  
- **Y** abro el asistente desde el disparador de la cabecera sin interferir con **Guardar** ni **Eliminar**  
- **Cuando** envío un mensaje en el panel de chat  
- **Entonces** el **frontend** llama a `POST /api/ai/chat/messages` con `treeId` del ejemplar en edición  
- **Y** recibo una respuesta textual **orientativa**  
- **Y** se registra **AUDITORIA_USO_IA** con mi `subject_oidc`, `tipo_uso_ia` = **`chat-message`**, `ejemplar_id` = ese `treeId` y resumen de prompt/resultado  
- **Y** el proveedor IA **no** recibe metadatos del ejemplar ni el `treeId` en el prompt  
- **Y** la UI deja claro que la respuesta no sustituye criterio experto.

### Escenario 2 — El hilo no persiste al salir de la ficha

- **Dado que** mantuve una conversación con varios mensajes en `EditTreeView`  
- **Cuando** recargo la página, navego fuera de la edición o cierro el diálogo y lo vuelvo a abrir  
- **Entonces** el hilo visible se **reinicia** vacío  
- **Y** no quedan mensajes recuperados desde `sessionStorage` ni desde el servidor.

### Escenario 3 — Usuario no autorizado no puede usar el chat

- **Dado que** soy un visitante sin sesión o un usuario autenticado **sin** rol **COLABORADOR** ni **ADMIN**  
- **Cuando** intento invocar el endpoint de chat del asistente  
- **Entonces** recibo **401 Unauthorized** o **403 Forbidden** (Problem Details)  
- **Y** no se registra auditoría de uso de IA para esa petición rechazada.

### Escenario 4 — Fallo del proveedor de IA

- **Dado que** soy un colaborador autenticado con acceso al chat  
- **Y** el proveedor de IA no responde, excede el timeout o devuelve un error  
- **Cuando** **ai-assistant-service** procesa mi mensaje  
- **Entonces** el **frontend** recibe un error (Problem Details) comprensible para el usuario  
- **Y** no se presenta como respuesta válida del asistente contenido inventado por el backend  
- **Y** los mensajes ya mostrados en la UI permanecen disponibles según el modelo de estado acordado en implementación.

---

## 4. Evaluación INVEST (resumen)

| Criterio | Comentario |
|----------|------------|
| **Independiente** | Parcialmente: depende de **HU-001**, **HU-013** y de la base de **ai-assistant-service** de **HU-016**; no requiere **HU-009**. |
| **Negociable** | Mínimo: redacción fina del texto en `AiPromptFactory` sin cambiar alcance; núcleo funcional cerrado en §2.1–§2.6. |
| **Valiosa** | Sí: apoya al colaborador en dudas del catálogo sin sustituir su criterio, alineado con la visión original del producto (prompts iniciales y UC-06). |
| **Estimable** | Sí: estimación **M**; refinamiento técnico sustancialmente cerrado. |
| **Small** | Sí: un flujo de chat colaborador con un endpoint (o familia acotada) y UI dedicada, sin imagen ni enriquecimiento ADMIN. |
| **Testable** | Sí: WebMvc en ai-assistant con mock de LLM; 401/403; auditoría; frontend con mocks del servicio; modo `stub` en integración local. |

---

## 5. Esfuerzo estimado de implementación

Orden de magnitud **M**: extensión de **ai-assistant-service** y **frontend** en `EditTreeView` (disparador en cabecera, diálogo de chat responsive, composable + servicio HTTP). Reutiliza infraestructura de **HU-016**. Cifra en persona-días: **no fijada en fuentes**.
