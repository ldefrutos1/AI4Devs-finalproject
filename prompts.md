> Detalla en esta sección los prompts principales utilizados durante la creación del proyecto, que justifiquen el uso de asistentes de código en todas las fases del ciclo de vida del desarrollo. Esperamos un máximo de 3 por sección, principalmente los de creación inicial o los de corrección o adición de funcionalidades que consideres más relevantes.
Puedes añadir adicionalmente la conversación completa como link o archivo adjunto si así lo consideras


## Índice

1. [Descripción general del producto](#1-descripción-general-del-producto)
2. [Arquitectura del sistema](#2-arquitectura-del-sistema)
3. [Modelo de datos](#3-modelo-de-datos)
4. [Especificación de la API](#4-especificación-de-la-api)
5. [Historias de usuario](#5-historias-de-usuario)
6. [Tickets de trabajo](#6-tickets-de-trabajo)
7. [Pull requests](#7-pull-requests)

---

## 1. Descripción general del producto

**Prompt 1:**

Soy un apasionado de los árboles y quiero desarrollar un proyecto web para almacenar las fotografías que les tomo junto con la ubicación en la que están. El sistema permitirá almacenar datos y fotografías de árboles, posicionar en un mapa (mediante API de terceros) su ubicación y compartir la información de forma pública. También contará con un módulo de notificaciones de novedades y una interacción con ChatGPT: tanto para identificar el árbol a partir de las fotografías como para interactuar en modo chat. La tecnología a usar debe ser Microservicios con Spring Boot en la parte back y Vue. El proyecto va a seguir todas las buenas prácticas de la ingeniería de software. En este momento estoy abordando la fase inicial para definir las características del producto y planificar adecuadamente. Te voy a pedir que me proporciones una descripción breve que permita entender y poner en valor el proyecto; antes de generarla confírmame si has entendido el objetivo y el contexto; pregúntame cualquier aclaración que necesites para elaborar dicha descripción y mejorar mi idea.

1.- el público objetivo será el aficionado general 
2.- un enfoque predominante de catalogación personal pero abierto a una comunidad colaborativa 
3.- la identificación con IA es una importante ayuda orientativa, aunque no una funcionalidad central 
4.- quiero que el proyecto se perciba como memoria/localización de árboles singulares añadiendo el componente de disfrute como hobby 
5.- El sistema público permitirá consulta sin registrarse 
6.- El nombre pensado es "MyTreeLibrary"


**Prompt 2:**

Actúa como un Product Manager con experiencia en aplicaciones web colaborativas y defíneme las características y funcionalidades específicas que debe tener el producto para satisfacer las necesidades identificadas. Sé conciso, céntrate en los puntos más importantes a incluir en un MVP. Pregúntame cualquier duda antes de empezar.

1.- quiero contemplar 3 roles: administrador, colaborador (con posibilidad de añadir y editar) y público (sin necesidad de identificación). Los dos primeros deben estar dados de alta en el sistema. 
2.- La publicación por parte del colaborador será directa en el sistema; el administrador tiene potestad para que las fotografías subidas por los colaboradores dejen de estar accesibles al público general e incluso borrarlas. El usuario público puede apuntarse para recibir notificaciones sin necesidad de estar logado,

**Prompt 3:**

Actúa como un analista de negocio experto y define el modelo de casos de uso del sistema. El modelo debe ajustarse a la descripción del sistema dada en @readme.md y a estas indicaciones:
---
Usuarios: 
Debe haber 3 tipos de usuario: Administrador, Colaborador y Público. 
---
Casos de uso:
Deben modelarse estos casos: 
- Usuario público: puede consultar y registrarse para recibir notificaciones - 
- Colaborador: además de los casos de uso públicos, puede dar de alta árboles o modificar los que ha registrado él; como punto de extensión está la consulta a la IA para identificar un árbol; puede consultar a la IA vía chat. 
- Administrador: puede realizar las tareas de los usuarios anteriores y Gestionar las tablas de catálogo; puede modificar las solicitudes de notificación pasando el estado entre activa y cancelada (baja lógica; sin borrado físico de suscriptores en el MVP alineado con data-model §2). 
- Además el sistema debe mandar notificaciones por mail a los usuarios registrados cuando se produzca una alta/modificación
---
Restricciones
Para los casos de uso de Colaborador y Administrador se requiere estar autenticado
Emplea las buenas prácticas de modelado de casos de uso y genera un archivo en PlantUML y una tabla resumen para incluir en formato md.
Si tienes alguna duda, pregúntame antes de seguir con el proceso.

---

## 2. Arquitectura del Sistema

### **2.1. Diagrama de arquitectura:**

**Prompt 1:**
Tengo que diseñar una arquitectura de microservicios para implementar el sistema descrito en los puntos 1 y 2 del documento @readme.md Las tecnologías base a emplear son Spring Boot en su última versión para el backend y Vue 3 para el front. La autenticación se hará mediante JWT empleando keycloak para la generación del token. El sistema empleará kafka para las comunicaciones asíncronas (en el alta se publica un evento que consumirá el microservicio de notificaciones) El modelo de datos se debe implementar en PostgreSQL (para la parte transaccional pura) y MongoDB para enriquecimiento semiestructurado. El almacenamiento de los ficheros imagen mediante API compatible con S3, para el desarrollo se usará MinIO. Para la cache se empleará Redis. El sistema debe cumplir con altos estándares de calidad y seguir los patrones y buenas prácticas del desarrollo de software. Actúa como un experto arquitecto de software y define la arquitectura del sistema; antes de proceder consúltame cualquier duda que tengas.

**Prompt 2:**

Actúa como un arquitecto de datos con gran experiencia y define el modelo conceptual del sistema a partir de los casos de uso y de la definición del sistema de los documentos. Al ser el modelo conceptual no hagas distinción sobre qué entidad irá después en cada tipo de almacenamiento (PostgreSQL, Mongo, ...); quiero un modelo general del sistema completo.
---
Ten además en cuenta:
---
Entidades principales
- Arbol
- Especie (relación N:1 con arbol; un arbol tiene una especie; una especie puede estar en N arboles)
- Características/Observaciones (relación 1:N con arbol; un árbol tiene N características/Observaciones) podrá contener información no estructurada  
- Usuario de notificación
- Notificaciones (relacionada con usuario y arbol)
- Fotografías (relación 1:N con arbol; un árbol tiene N fotografías)
---
Requisitos
- se debe guardar auditoría de las altas/modificaciones del catálogo 
- el árbol debe estar bien identificado con especie; nombre científico y nombre común 
- se deben guardar las coordenadas de la ubicación del árbol 
- las imágenes subidas pueden tener 2 categorías de visibilidad: PUBLIC y PRIVATE; PRIVATE solo la pueden ver el administrador y quien creó la fotografía (salvo reglas adicionales del producto); PUBLIC según acceso público a la ficha 
- las notificaciones se mandan a usuarios que previamente se han registrado proporcionando su mail
- debe haber unas tablas de catálogo como ESPECIE, PROVINCIA; solo el administrador puede gestionarlas (CRUD)

Si tienes alguna duda consúltame antes de continuar con el proceso

### **2.2. Descripción de componentes principales:**

**Prompt 1:**

Actúa como arquitecto de software. A partir del diagrama C2 ya definido en @readme.md (§3.1), redacta la **descripción de componentes principales** del sistema para incluir en la memoria del proyecto (§3.2).

Para cada componente indica: **nombre**, **tecnología** y **responsabilidad** en el MVP. Organiza el resultado **por capas** (aplicación y entrada, microservicios de dominio, seguridad, observabilidad, almacenamiento, mensajería y caché) usando tablas en Markdown.

Ten en cuenta:
- Los microservicios previstos: `api-gateway`, `catalog-service`, `media-service`, `notification-service` y `ai-assistant-service`.
- Un único PostgreSQL con **un esquema por servicio**; MongoDB para enriquecimiento en catálogo; MinIO para imágenes; Kafka para eventos de dominio; Redis para caché; Keycloak para OIDC/JWT.
- No detallar aquí dependencias externas puras (OpenStreetMap/Leaflet, proveedor IA comercial).

Si el diagrama C2 o alguna decisión de arquitectura no está clara, pregúntame antes de documentar.

**Prompt 2:**

Actúa como arquitecto de software especializado en sistemas event-driven. A partir de la arquitectura ya definida en @readme.md (§3.1 y §3.2.2), documenta el **flujo Kafka del MVP** para notificaciones tras el alta de un ejemplar.

Incluye en la memoria:
- **Productor** (`catalog-service`) y **consumidor** (`notification-service`): responsabilidad de cada uno.
- **Topic** `catalog.ejemplar.evento`, tipo de evento `EJEMPLAR_CREADO` y regla de negocio: solo en el **alta** del ejemplar, no en modificaciones (R7).
- **Contrato del mensaje** (campos mínimos del JSON) alineado con @docs/events/kafka-events.md.
- **Idempotencia** ante reentregas (`evento_id`) y qué ocurre si Kafka está desactivado en desarrollo o tests.

Añade diagramas Mermaid de apoyo (C3 productor/consumidor y, si procede, secuencia alta → correo). Mantén el texto conciso y orientado al MVP.

Si algún límite entre microservicios o el momento de publicación del evento no está claro, pregúntame antes de documentar.

### **2.3. Descripción de alto nivel del proyecto y estructura de ficheros**

**Prompt 1:**

Actúa como arquitecto de software. A partir de la arquitectura definida en @readme.md (§3.1–§3.2), redacta la **descripción de alto nivel del proyecto y la estructura de ficheros** del monorepo para la memoria (§3.3).

Incluye:
- Un párrafo breve que explique el enfoque **monorepo** (frontend, backend, infra, documentación y automatización en un solo repositorio).
- Un **árbol de directorios** en formato texto con las carpetas principales y una línea de propósito por rama:
  - `frontend/`, `services/` (gateway y microservicios), `platform/observability/`, `infra/`, `docs/`, `scripts/`, `.cursor/`, `.github/` y `readme.md`.
- Dentro de `services/`, lista los módulos previstos (`api-gateway`, `catalog-service`, `media-service`, `notification-service`, `ai-assistant-service`, `system-e2e-tests`).
- Dentro de `docs/`, resume las subcarpetas relevantes (`adr`, `api`, `backlog`, `data-model`, `engineering`, `events`, `onboarding`, `security`, etc.).

Mantén el resultado conciso, orientado al MVP y sin inventar carpetas que no encajen con el stack acordado.

Si la organización de algún módulo o la ubicación de la documentación no está clara, pregúntame antes de documentar.

**Prompt 2:**

Actúa como arquitecto backend Java. A partir de la estructura del monorepo en @readme.md (§3.3), del stack Spring Boot 4 acordado y de @.cursor/rules/spring-boot-4-backend.mdc, define las **convenciones de paquetes Java** para los microservicios en `services/`.

Documenta de forma concisa (tablas o listas en Markdown):
- **GroupId** `com.mtl` y tabla **módulo Maven ↔ paquete raíz** (`api-gateway` → `com.mtl.gateway`, `catalog-service` → `com.mtl.catalog`, etc.).
- **Capas permitidas** bajo `com.mtl.<contexto>` en `src/main/java`: `config`, `controller`, `application`, `domain`, `dto`, `exception`, `web`, `infrastructure`, `util` — con la responsabilidad de cada una.
- Ubicación de **persistencia** (JPA bajo `…infrastructure.persistence.jpa`, Mongo bajo `…infrastructure.persistence.mongo` cuando aplique) y de **integraciones** (clientes HTTP, Kafka, almacenamiento) siempre bajo `infrastructure`.
- Convención de **tests**: `src/test/java` (`*Test`) y `src/testIT/java` (`*IT`); referencia de implementación: **catalog-service**.

Toma **catalog-service** como plantilla de referencia hasta que exista un segundo servicio equivalente. No propongas paquetes de primer nivel fuera de la lista blanca sin justificarlo.

Si alguna capa o excepción (p. ej. `api-gateway` WebFlux) no está clara, pregúntame antes de cerrar la convención.

**Prompt 3:**

Actúa como arquitecto frontend. A partir del monorepo en @readme.md (§3.3), de @frontend/README.md y de @.cursor/rules/frontend-vue3.mdc, define las **convenciones de estructura y organización del frontend** Vue 3 en `frontend/`.

Documenta de forma concisa (tablas o listas en Markdown):
- **Stack** del MVP: Vue 3, TypeScript, Vite, Vue Router, Pinia, `vue-i18n`, `oidc-client-ts` (Keycloak).
- **Árbol bajo `frontend/src/`**: `views/`, `components/`, `composables/`, `stores/`, `services/`, `router/`, `types/`, `i18n/` y `styles/` — con la responsabilidad de cada carpeta.
- **Separación en tres capas**: vista (`.vue`), lógica (`composables/` / `stores/`), infraestructura (`services/`); sin `fetch` directo desde componentes.
- **Convenciones clave**: alias `@/` → `src/`; textos en i18n (no hardcodear copy); tipos alineados con OpenAPI en `types/`; rutas y wire con `treeId` / `/ejemplares` según ADR-0006.

Mantén el resultado orientado al MVP y coherente con las reglas del repositorio. No propongas librerías UI adicionales ni carpetas nuevas sin justificarlo.

Si alguna convención de rutas, auth o nomenclatura no está clara, pregúntame antes de cerrar la documentación.

### **2.4. Infraestructura y despliegue**

**Prompt 1:**
En este proyecto de microservicios con vue ya está definida la arquitectura de contenedores necesaria para la infraestructura en @infra/compose/docker-compose.yml pero queda pendiente la parte de generación de las imágenes propias del proyecto (la imagen del front y de las 5 imágenes del back: gateway y los 4 microservicios de la aplicación). Actúa como un experto arquitecto de software para terminar de definir la infraestructura. Prepara un plan con estos puntos 1.- Revisión y validación de la estructura del docker-compose actual 2.- definición de la estrategia y archivos de configuración necesarias para la generación de la imagen de front 3.-  definición de la estrategia y archivos de configuración necesarias para la generación de las imágenes de back. Si tienes alguna duda pregúntame antes de seguir

**Prompt 2:**

Quiero una implementación profesional pero lo más sencilla posible; el objetivo poder cerrar el punto de infraestructura y despliegue con la generación de las imágenes de la aplicación y el subsiguiente Docker-compose. De este modo se podría generar un script de automatización con 1.- creación de imágenes 2.- arranque de los contenedores de infraestructura (docker compose ya existente) 3.- arranque de las imágenes de la aplicación (docker compose con las imágenes del front y back de las aplicaciones creadas en el punto 1) por ahora no se va a incluir en un pipeline CI, solo lo vamos a dejar preparado para poder incluirlo fácilmente en el futuro. La propuesta son un docker-compose de la aplicación docker-compose.apps.yml y otro de la infraestructura (el que ya está definido) A eso se añadiría el docker-compose.e2e; este tercer compose lo usaríamos para la ejecución de pruebas e2e de playwright y también incluiría el microservicio system-e2e-test (que no se incluye en el despliegue de la aplicación) Por ahora solo se generan imágenes locales que se suben al docker local; sin subir a docker hub. El puerto del frontend dockerizado sería el que garantice correr los contenedores sin problemas

### **2.5. Seguridad**

**Prompt 1:**
La seguridad del sistema se implementará con JWT usando keycloak. Revisa la documentación de @readme y @infra/compose/docker-compose.yml confirmando que el enfoque es correcto, si ves algún punto no claro pregúntame.

**Prompt 2:**
Vamos a definir los siguientes puntos de la implementación 
1.- documenta de forma concisa que Keycloak en start-dev solo se debe usar en desarrollo

2.- define el Realm a configurar en keycloak y preparar el desplegarlo en el contenedor cuando se levante con compose up. Los roles a usar son COLABORADOR y ADMIN y lo que tiene que tenerse en cuenta en la parte front y back. 

3.- Prepara una estrategia de validación del JWT en el Gateway, los servicios implementarán la necesidad de autorización con un token validado por el Gateway para las operaciones del COLABORADOR y ADMIN permitiendo el consumo sin token de la parte pública.  

4.- Añade los puntos que consideres necesarios

**Prompt 3:**
Actúa como revisor de seguridad de aplicaciones (AppSec) especializado en OAuth2/OIDC, JWT, Spring Boot y SPAs.
Contexto del proyecto: MyTreeLibrary — monorepo con SPA Vue 3, API Gateway Spring, microservicios Spring Boot 4, Keycloak (realm `mtl`, roles de realm COLABORADOR y ADMIN), contrato en docs/api/openapi.yaml y normativa en .cursor/rules/api-security.mdc y docs/security/jwt-gateway-strategy.md.
Tarea: revisar la seguridad IMPLEMENTADA en el código y configuración que te indique (rutas, filtros Spring Security, CORS, gateway, clientes Keycloak, manejo de tokens en el front, logs, errores). No inventes requisitos: contrasta con OpenAPI, api-security.mdc y jwt-gateway-strategy.md.
Comprueba explícitamente:
1. Rutas públicas vs protegidas: coincidencia con OpenAPI (incl. POST /api/notifications/subscriptions sin Bearer) y ausencia de endpoints sensibles expuestos sin autenticación.
2. Validación JWT: issuer-uri/JWKS, audience si aplica, caducidad, rechazo de tokens malformados; coherencia entre gateway y microservicios (estrategia token relay documentada).
3. Autorización: comprobación de roles COLABORADOR y ADMIN donde el modelo lo exige (maestros solo ADMIN, etc.).
4. Front: almacenamiento y envío del access token; no uso del id_token como Bearer; PKCE; redirect URIs.
5. Cabeceras: correlación; no filtrar tokens ni PII en logs o respuestas de error (RFC 9457).
6. Superficie interna: si los microservicios son alcanzables sin pasar por el gateway y qué riesgo implica.
7. CORS y CSRF donde aplique al flujo elegido.
Salida pedida:
- Lista breve de hallazgos por severidad (crítico / alto / medio / bajo / informativo).
- Para cada hallazgo: ubicación (fichero o ruta), qué falla, remediación concreta.
- Si algo no se puede verificar por falta de contexto, indícalo como “no verificable” y qué habría que mostrar.
No generes código salvo que pida un snippet ilustrativo de una línea; prioriza diagnóstico y priorización.

### **2.6. Tests**

**Prompt 1:**

Tengo que incluir testing end to end en el sistema que pruebe el microservicio de alta ejemplar; Quiero usar playwright. La prueba consistiría en 1.- Acceder a la aplicación 2.- iniciar sesión con colaborador 3.- dar de alta un ejemplar 4.- consultar mis arboles 5.- borrar el ejemplar. Actúa como arquitecto experto y dame una estrategia para implementar la prueba; en la estrategia incluye: carpeta adecuada para contener los ficheros generados; cómo usar docker; como generar frontend y microservicios para la prueba, como integrarla con github para lanzar los test en los merge de PR. Actúa como arquitecto experto y hazme un plan. En esta prueba end2end se podrían usar base de datos en memoria. En el plan analiza también la conveniencia de tener además un test que vaya contra un entorno ya levantado; es decir en este caso el test no se preocuparía de levantar el entorno sino de ejecutar el test desde playwright. Haz el plan profesional y conforme a las buenas prácticas pero lo más sencillo posible; si tienes alguna duda pregunta antes de empezar

**Prompt 2:**

El proyecto actual usa playwright para test e2e. Actúa como ingeniero experto en QA y analiza la estructura actual de la carpeta e2e comprobando si: 
1. ¿Estructura de carpetas correcta y coherente con las buenas prácticas?
2. ¿Fixtures reutilizables?
3. ¿Selectores (`data-testid`) alineados?
4. ¿Existe solapamiento con `system-e2e-tests`?
5. ¿Existe convención para nombrar specs nuevos? 
6. ¿Es coherente con @docs/engineering/testing-e2e.md? ¿Sigue las buenas prácticas de automatización para este tipo de test? Si tienes alguna duda o ves algún punto más a considerar pregúntame

1.- Además del flujo actual de alta de ejemplar; también se implementará - Consulta pública (sin autenticación) - Admin maestros (usuario ADMIN) 
2.- Por el coste de levantar todos los contenedores; los test e2e se ejecutarán principalmente en variante local (pero es conveniente que también estén preparados para docker) 
3.- Vamos a mantener el código lo más simple posible, si no ves problema se mantendría helpers en fixtures para no complicar el mvp 4.- se necesita ADMIN para el nuevo test de Admin maestros  

adelante con la implementación de consulta-publica.spec.ts
1.- Ir a /ejemplares sin login.
2.- Verificar que carga el listado (conteo o al menos una tarjeta de semilla).
3.- Abrir detalle de un ejemplar publicado y verificar contenido básico (especie/ubicación).

vamos con el test de admin-maestros.spec.ts

1.- loginAsAdmin.
2.- Ir a /admin/masters.
3.- Crear especie de prueba (género existente de semilla).
4.- Verificar que aparece en la tabla.
5.- Borrarla y verificar ausencia.

revisa que el código que has implementado sigue los estándares definidos para este tipo de test

---

### 3. Modelo de Datos

**Prompt 1:**
Estoy diseñando el modelo de datos para una aplicación web que me permita gestionar mi colección personal de fotografías de árboles singulares. Además de las fotografías, cada ejemplar tiene una serie de datos asociados como especie, ubicación y descripción. La arquitectura definida consiste en una base de datos SQL (PostgreSQL) que mantiene el inventario y los datos esenciales mencionados anteriormente y una base de datos MongoDB que permite ampliar la información de cada ejemplar y especie con datos y notas sin una estructura claramente definida. Actúa como un ingeniero de datos experto, tu primer objetivo es validar la arquitectura propuesta y generar un primer diseño de alto nivel; solo con las entidades principales sin entrar por ahora en la definición de las columnas. El diseño debe ser profesional generando junto a la justificación los diagramas Mermaid ER correspondientes.

**Prompt 2:**

Una vez validada la arquitectura vamos a centrarnos solo en el diseño de la base de datos Mongo; ya si vamos a bajar al diseño de campos de cada entidad. Por decisión de diseño se ha decidido que las claves primarias serán numéricas; no uuid. Los metadatos asociados a cada fotografía se van a almacenar solo en PostgreSQL y la fotografía en S3; a partir de ahora no modeles estos conceptos relacionados con la imagen. Para facilitar búsquedas se quiere que el nombre común y científico de la especie también se guarde en MongoDB; estos dos datos se repetirán tanto en PostgreSQL como en MongoDB pero podemos considerarlo como una 'desnormalización' que nos permite ejecutar en MongoDB búsquedas más completas sin necesidad de acceder a PostgreSQL. Genera el modelo ER en Mermaid;

P: En el modelo anterior teníamos EJEMPLAR_DETALLE y ESPECIE_DETALLE como colecciones separadas. ¿Cómo quieres estructurar la relación entre ambas en MongoDB?
R: Si no ves problema en el diseño, EJEMPLAR_DETALLE y ESPECIE_DETALLE serían colecciones independientes referenciadas por ID

P: Las OBSERVACIONES del ejemplar, ¿cómo deben modelarse?
R: Embebidas como array dentro de EJEMPLAR_DETALLE


**Prompt 3:**
En el caso de uso los datos de ESPECIE_DETALLE se quieren cargar con una consulta a un LLM; define un JSON de ejemplo para cada colección que se pueda usar en la interacción con el LLM.

Genera un md resumen con el diseño que hemos definido. Incluye un ER en mermaid, la definición del modelo, los índices, los JSON de ejemplo. Si ves algún punto relevante que deba estar en un diseño profesional, pregúntame para ver si lo incluimos.

P: ¿Incluimos una sección de convenciones de nombrado y tipos de datos (ej: fechas en ISO 8601, IDs siempre int, arrays nunca null sino vacíos)?
R: No es necesario

P: ¿Incluimos consideraciones sobre la sincronización de los datos desnormalizados entre PostgreSQL y MongoDB (nombre_cientifico, nombre_comun)?
R: No, se gestiona fuera de este documento

P: ¿Incluimos una sección sobre el proceso de carga de ESPECIE_DETALLE vía LLM (prompt de referencia, campos a validar, riesgos)?
R: Sí, inclúyela

---

### 4. Especificación de la API

**Prompt 1:**

Actúa como arquitecto de APIs REST. A partir de @readme.md, los casos de uso en @docs/use-cases/use-case-summary.md, el modelo de datos en @docs/data-model/data-model.md y las reglas en @.cursor/rules/api-design.mdc y @.cursor/rules/api-contract.mdc, redacta el **contrato HTTP inicial del MVP** en OpenAPI 3 (`docs/api/openapi.yaml`).

Incluye:
- **Prefijos por contexto** vía API Gateway: `/api/catalog`, `/api/media`, `/api/notifications`, `/api/ai`.
- Operaciones del MVP alineadas con el backlog (consulta pública, alta/edición de fichas, fotos, suscripciones, maestros ADMIN, enriquecimiento y consulta IA cuando aplique).
- **Nomenclatura HTTP en inglés** (`treeId`, `/trees`, `speciesId`, …) con mapeo a persistencia en español según @docs/adr/0007-english-http-spanish-persistence.md y @docs/adr/0006-ejemplar-aggregate-http-kafka-naming.md.
- **Seguridad** por operación (JWT Bearer; rutas públicas sin token donde el producto lo exija).
- **Errores** en `application/problem+json` (RFC 9457) y listados paginados (`page`, `size`) donde corresponda.

Mantén el contrato coherente con microservicios ya previstos; no inventes endpoints sin respaldo en las fuentes indicadas.

Si alguna operación, rol o código de respuesta no está claro, pregúntame antes de cerrar el OpenAPI.


---

### 5. Historias de Usuario

**Prompt 1:**

Actúa como un Product Owner senior, Business Analyst y especialista en backlog ágil.

Usa el @readme.md y ten en cuenta la carpeta @docs/ Tu tarea es transformarlo en un backlog profesional, claro y priorizado, listo para una primera fase de refinamiento.

El documento debe tener un enfoque  LEAN, sin texto o información que no aporte:
- mínima información viable
- máxima claridad
- cero burocracia innecesaria
- backlog útil para decidir, priorizar y planificar

TRABAJA SOLO CON INFORMACIÓN DE LAS FUENTES INDICADAS

- No inventes requisitos, reglas ni comportamientos no respaldados por el PRD.
- Si falta información importante, no la completes por tu cuenta: recógela en una sección llamada “Suposiciones / Huecos detectados”.
- Si detectas ambigüedades, contradicciones o alcance poco definido, indícalo explícitamente.

OBJETIVO
Convertir el PRD en un backlog inicial útil, consistente, priorizable y accionable, separando claramente el backlog del detalle posterior de cada historia.

CRITERIOS DE TRABAJO

1. Identifica el objetivo del MVP, los tipos de usuario, las funcionalidades y las restricciones relevantes.
2. Agrupa los ítems por épicas o bloques funcionales cuando tenga sentido.
3. Redacta historias orientadas a valor para el usuario o para el producto.
4. No conviertas tareas técnicas internas en historias de usuario, salvo que sean habilitadores imprescindibles claramente derivados de los documentos.
5. Si un requisito es demasiado grande, divídelo en historias más pequeñas y manejables.
6. Evita duplicidades, solapamientos y redacción ambigua.
7. Prioriza pensando en una primera versión MVP, salvo que el PRD indique otra lógica.
8. Mantén un tono profesional, concreto y directo.
9. No incluyas criterios BDD, evaluación INVEST ni notas extensas dentro del backlog; ese nivel de detalle pertenece al refinamiento posterior de cada historia.


FORMATO OBLIGATORIO DE CADA ÍTEM DEL BACKLOG
Para cada historia incluye exactamente estos campos:

- ID: formato HU-001, HU-002, HU-003...
- Épica
- Título
- Historia de usuario: en formato “Como [rol], quiero [acción], para [beneficio]”
- Estimación: S, M o L
- Prioridad: Alta, Media o Baja

CRITERIOS DE CALIDAD
Cada historia debe ser:

- comprensible
- valiosa
- estimable a alto nivel
- suficientemente acotada
- redactada sin ambigüedad

REGLAS IMPORTANTES

- No mezcles varias necesidades distintas en una sola historia si pueden separarse.
- No generes historias excesivamente detalladas.
- No añadas criterios de aceptación, BDD ni validaciones funcionales en esta fase.
- No uses lenguaje impreciso o genérico.
- Si una historia parece demasiado grande o inmadura, mantenla en el backlog pero indícala después en la sección “Elementos que conviene refinar después”.

FORMATO DE SALIDA
Devuelve toda la respuesta en formato markdown generando un documento backlog.md en una carpeta backlog dentro de docs

SALIDA OBLIGATORIA
Devuelve el resultado en este orden exacto:

1. Resumen del MVP en 3 a 5 líneas
2. Lista de épicas identificadas
3. Backlog completo en formato tabla
4. Suposiciones / Huecos detectados
5. Elementos que conviene refinar después

FORMATO DE LA TABLA
Usa exactamente estas columnas:
| ID | Épica | Título | Historia de usuario | Estimación | Prioridad |

Antes de generar el backlog:

- analiza la documentación al completo
- asegúrate de que las historias no duplican requisitos
- asegúrate de que no mezclan objetivos distintos dentro de una misma historia


**Prompt 2 — Solo épicas (sin historias de usuario aún)**

Actúa como **Product Owner** y **analista de dominio** con experiencia en descomposición de backlog.

**Objetivo de esta sesión (solo esto):** proponer o **revisar la división en épicas** del MVP de MyTreeLibrary. **No** redactes historias de usuario (formato “Como… quiero… para…”); **no** asignes HU-00x; **no** entres en criterios de aceptación. Eso irá en un **paso posterior** con otro prompt.

**Fuentes obligatorias** (léelas y cítalas al agrupar):

- [readme.md](readme.md) (visión, arquitectura de alto nivel, reglas de negocio relevantes al MVP).
- [docs/use-cases/use-case-summary.md](docs/use-cases/use-case-summary.md) y, si hace falta, [docs/use-cases/use-case-model.puml](docs/use-cases/use-case-model.puml).
- [docs/data-model/data-model.md](docs/data-model/data-model.md) solo para acotar alcance funcional, no para diseñar tablas.
- Si ya existe backlog por épicas: [docs/backlog/backlog.md](docs/backlog/backlog.md) §2 (lista de épicas).

**Qué es una épica aquí**

- Bloque de **valor de producto** coherente (no una tarea técnica salvo un habilitador imprescindible y explícito en las fuentes).
- Tamaño: debe poder **descomponerse** después en varias historias; una épica no es una sola botonera ni un solo endpoint.
- **Límites claros:** indica qué queda **fuera** de cada épica cuando pueda solaparse con otra (p. ej. “Notificaciones” vs “Catálogo”).

**Cómo trabajar**

1. Lista las **épicas** que consideres necesarias (puedes mantener, fusionar o dividir las del `backlog.md` actual si lo justificas).
2. Para cada épica: **nombre corto**, **objetivo en 1–3 frases**, **alcance** (qué incluye), **límites** (qué excluye explícitamente), **trazabilidad** (UC / sección del readme / regla R# si aplica).
3. Señala **dependencias o precondiciones** entre épicas solo a alto nivel (qué debe existir antes), sin plan de sprints.
4. Incluye una sección **“Riesgos de solapamiento”** si dos épicas podrían pisarse y cómo las separas.
5. Cierra con **“Huecos o decisiones pendientes”** (solo lo que impida cerrar bien los límites de épica).

**Formato de salida (markdown)**

1. Tabla: | Épica | Objetivo | Incluye (resumen) | Excluye / límite con otras | Trazabilidad (UC, readme, reglas) |
2. Diagrama o lista breve de **orden de dependencia** entre épicas (opcional).
3. **Cambios respecto al backlog actual** (si comparas con §2 de `backlog.md`): fusionar / partir / renombrar, con motivo en una línea.
4. **Huecos o decisiones pendientes** (sin inventar requisitos).

Si falta información en las fuentes, **no la inventes**: listada como hueco.

---

Actúa como **Product Owner** y **analista de negocio**. Tu trabajo es **descomponer las épicas del MVP en historias de usuario concretas** y **actualizar el fichero** [docs/backlog/backlog.md](docs/backlog/backlog.md) de forma coherente.

**Entradas que debes considerar (todas):**

- Las **fuentes normativas** del propio backlog (cabecera del `backlog.md`: readme, casos de uso, data-model, mongo, kafka, OpenAPI, ADR).
- La **lista de épicas** actual o revisada en `backlog.md` §2, **alineada con los límites y trazabilidad (UC, R#)** que hayas establecido con el **Prompt 2** (épicas: objetivo, incluye/excluye, dependencias, riesgos de solapamiento).
- Si en el chat se ha pegado la **salida del Prompt 2**, úsala como guía explícita para no duplicar ni mezclar responsabilidades entre historias.

**Alcance funcional**

- Cada historia debe cubrir **una intención de valor** clara; si una épica amerita **varias** historias (p. ej. separar flujos, actores o entregables), **divide**; si dos líneas actuales del §3 duplican el mismo requisito, **fusiona** y renumera.
- Formato de historia: **“Como [rol], quiero [acción], para [beneficio]”**.
- Estimación: **S**, **M** o **L**; Prioridad: **Alta**, **Media** o **Baja**, alineado con MVP.
- **No** escribas criterios de aceptación detallados ni BDD en la tabla; **no** inventes requisitos no respaldados por las fuentes.

**Salida obligatoria (solo esto, aplicada sobre `docs/backlog/backlog.md`):**

1. **§3 — Backlog completo en formato tabla**  
   - Sustituye o amplía la tabla con las columnas **exactas**:  
     `| ID | Épica | Título | Historia de usuario | Estimación | Prioridad |`  
   - IDs consecutivos **`HU-001`, `HU-002`, …** (renumera si cambia el número de filas).  
   - Cada fila ha de asignarse a **una épica** de §2 (nombre coherente con la tabla de épicas actual o la revisión del Prompt 2).

2. **Resto del documento** — revisión y actualización **consistente** con el nuevo §3 y con el análisis de épicas (Prompt 2):  
   - **§1** Resumen del MVP: ajústalo si la descomposición revela alcance distinto (sin alargar; 3–5 líneas).  
   - **§2** Lista de épicas: alinea textos si renombraste, partiste o fusionaste épicas.  
   - **§3.1** Desgloses por HU: añade o corrige filas si hay HUs nuevas o IDs cambiados (enlaces solo si existen ficheros; si no, mención “pendiente” según convención [backlog/README.md](docs/backlog/README.md)).  
   - **§4** Suposiciones / huecos: integra nuevos huecos o cierra los que la nueva descomposición resuelva; no contradigas las fuentes.  
   - **§5** Refinar después: prioriza según dependencias entre historias o riesgos detectados al partir épicas.

**Qué no hagas**

- No generes otro documento aparte ni dupliques la tabla fuera de `backlog.md`.  
- No añadas tareas puramente técnicas como historias salvo habilitador imprescindible y citado en fuentes.

**Formato de entrega**

- Entrega el **contenido íntegro actualizado de `docs/backlog/backlog.md`** listo para reemplazar el fichero (o aplica los cambios en el archivo del repositorio si tu entorno lo permite).

---

**Prompt 3 — Definición de historias a partir del listado de `backlog.md`**

[readme]
[Backlog]
[Historia de usuario]

A partir del [readme] y del [Backlog], revisa y completa la siguiente [Historia de usuario].

Objetivo:

- validar que la información existente sea correcta
- comprobar si la historia cumple INVEST
- completar solo la información necesaria para refinamiento y desarrollo

Comprueba la información existente:

- Título descriptivo
- Historia en formato “Como [rol], quiero [acción], para [beneficio]”
- Estimación de complejidad (S/M/L)
- Prioridad

Añade:

- 3 criterios de aceptación en formato BDD con “Dado que / Cuando / Entonces”
- Evaluación breve contra INVEST
- Esfuerzo estimado de implementación
- Riesgos
- Dependencias
- Huecos o aclaraciones necesarias

Reglas:

- No inventes información no respaldada por el readme o el backlog
- Si detectas inconsistencias, indícalas
- Si la historia es demasiado grande, dilo y propón división
- Usa lenguaje claro, concreto y profesional
- Devuelve la respuesta en markdown
- si hay huecos, listarlos en “Aclaraciones pendientes”.

Estructura de salida:

1. Validación de la información existente
2. Historia refinada con
  Criterios de aceptación BDD
     Referencias
3. Evaluación INVEST

Salida: genera un único documento Markdown con esta estructura:

Título # HU-XXX — …
Tabla inicial con: ID, Épica, Título, Estimación de complejidad, Prioridad
Historia de usuario: texto en formato “Como… quiero… para…” sin referencias (sin citas a secciones del PRD ni notas al pie)
Una viñeta final bajo la historia que defina con precisión el entregable de la historia
Alcance con subapartados Incluye y Queda fuera de esta historia (listas con viñetas)
Dependencias
Riesgos
Aclaraciones pendientes (refinamiento)
Criterios de aceptación (BDD) con escenarios en Dado que / Cuando / Entonces
Evaluación INVEST (resumen) en tabla con columnas Criterio y Comentario

Guarda el resultado en HU-XXX_…md (nombre en kebab-case coherente con el título).

---
### 6. Tickets de Trabajo

**Prompt 1:**

Digamos que no tenemos nada —lo cual es cierto— y queremos construir lo suficiente de la **HU-005** para el MVP y para obtener aprendizaje validado. ¿Qué necesitamos construir?

El stack tecnológico y los diagramas de la arquitectura están en [readme.md](readme.md). El equipo está formado por **un ingeniero full-stack** que además tiene conocimientos sólidos de HTML/CSS.

Dame una **lista de tickets** (IDs estables `TASK-HU-005-<nn>`) para implementar el desarrollo, con **orden y dependencias** razonables.

**Salida esperada:** documento [docs/backlog/HU-005-ticket-breakdown.md](docs/backlog/HU-005-ticket-breakdown.md) (patrón `HU-<id>-ticket-breakdown.md`), actualizando el índice en [docs/backlog/README.md](docs/backlog/README.md) y la tabla §3.1 de [docs/backlog/backlog.md](docs/backlog/backlog.md) cuando proceda.

**Prompt 2:**

Actúa como UX/UI Designer Senior + Frontend Engineer especializado en diseño visual para aplicaciones web Vue 3.
Contexto:
- Proyecto: MyTreeLibrary (frontend en Vue 3 + TypeScript).
- Objetivo: elevar la calidad visual de la aplicación.
- Enfoque deseado: estilo muy profesional, moderno, limpio y sencillo (sin sobrecargar la interfaz).
- Restricción: priorizar consistencia, accesibilidad y mantenibilidad.
Tarea:
1) Revisa los estilos actuales del frontend (estructura, tipografía, espaciados, paleta, botones, formularios, estados, layout, responsive).
2) Detecta problemas de consistencia visual, jerarquía, legibilidad y UX.
3) Define una propuesta de sistema de estilos mínimo pero robusto para todo el proyecto.
4) Implementa los cambios necesarios en CSS y componentes para aplicar esa propuesta.
5) Mantén la lógica funcional intacta: solo mejorar apariencia/UX visual.
Criterios de diseño:
- Visual: profesional, moderno, claro, sobrio.
- Simplicidad: evitar adornos innecesarios.
- Coherencia: unificar patrones de botones, inputs, cards, títulos, feedback.
- Accesibilidad: contraste correcto, foco visible, tamaños y espaciados legibles.
- Responsive: buena experiencia en móvil y escritorio.
- Mantenibilidad: usar tokens CSS y clases reutilizables; evitar duplicación.
Entregables esperados:
- Resumen inicial de hallazgos (breve y accionable).
- Sistema visual propuesto:
  - Paleta (tokens)
  - Tipografía (escala)
  - Espaciado (escala)
  - Radios, bordes y sombras
  - Estados (hover, focus, error, success, disabled)
- Cambios implementados en archivos concretos.
- Mini guía de uso para mantener el estilo en futuras pantallas.
Archivos a revisar como mínimo:
- frontend/src/style.css
- frontend/src/App.vue
- frontend/src/views/HomeView.vue
- frontend/src/views/CreateTreeView.vue
- frontend/src/components/** (si aplica)
Reglas de trabajo:
- No introducir librerías UI nuevas.
- No romper rutas ni lógica de negocio.
- Si algo es ambiguo, elige la opción más simple compatible con MVP.
- Mantén textos en español y consistentes con el tono del producto.

**Prompt 3:**

Actúa como Product Designer + UX para una aplicación Vue 3 (MyTreeLibrary).
Quiero que identifiques y propongas la estructura de páginas del MVP teniendo en cuenta 3 tipos de usuario:
1) COLABORADOR (autenticado)
2) ADMINISTRADOR (autenticado)
3) Público (sin login)
Contexto funcional (apóyate en backlog y readme):
- Consulta pública de árboles publicados (listado y detalle).
- Alta/edición de árboles para colaborador y administrador.
- Gestión de maestros y suscripciones para administrador.
- Debe existir una Home que contemple claramente los 3 perfiles.
- Debe decidirse si hace falta menú global o no (y por qué).
Entregables:
1. Mapa de páginas por tipo de usuario (qué ve cada perfil).
2. Propuesta de Home (bloques, CTAs y flujo principal por perfil).
3. Decisión sobre menú:
   - si SÍ: estructura mínima de navegación.
   - si NO: alternativa de navegación (cards/CTAs/rutas directas).
4. Reglas de visibilidad por rol (qué páginas son públicas y cuáles protegidas).
5. Recomendación final para MVP: opción más simple, profesional y escalable.
Formato de respuesta:
- Breve y accionable.
- En español.
- Usa listas claras.
- Sin código.

**Prompt 4:**


Usar Vue 3 con la librería vue-zoomable para crear un visor de fotografía.
El componente debe permitir zoom con la rueda del ratón hasta 600%, usando maxZoom = 6.
El zoom mínimo debe ser 100%, usando minZoom = 1.
Debe permitir pan o desplazamiento arrastrando con el ratón cuando la imagen esté ampliada.
El visor debe tener overflow hidden, fondo oscuro, bordes redondeados y altura responsive.
Debe incluir botón para restablecer zoom y, si la librería lo permite, indicador del porcentaje de zoom.
La imagen debe tener draggable="false", alt accesible y no debe seleccionarse al arrastrar.

---

### 7. Pull Requests

**Prompt 1:**

He cerrado el **TASK-HU-xxx-nn** de la **HU-xxx** en la rama `feature/hu-xxx-task-nn-descripcion`. Ayúdame a redactar la **pull request** hacia `main` siguiendo @docs/onboarding/github-branching.md y la plantilla @.github/pull_request_template.md.

Incluye en el cuerpo del PR:
- **Trazabilidad:** HU, TASK y enlace al breakdown (`docs/backlog/HU-xxx-ticket-breakdown.md`).
- **Título** con la convención del repo: `feat(HU-xxx): TASK-HU-xxx-nn — resumen breve`.
- **Resumen** (qué problema resuelve y por qué), **alcance** (front/back/docs) y **cambios realizados** (lista breve).
- **Plan de pruebas:** marca solo lo que se haya ejecutado de verdad (paridad con @docs/engineering/devsecops-ci.md).
- **Notas para review** si hay decisiones técnicas relevantes o puntos fuera de alcance.

No inventes pruebas ni cambios no incluidos en el diff. Si falta contexto para el resumen o el plan de pruebas, pregúntame antes de cerrar el texto.

