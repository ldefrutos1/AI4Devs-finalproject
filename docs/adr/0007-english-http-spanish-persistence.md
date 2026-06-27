# ADR-0007: Contratos HTTP en inglés con persistencia en español

## Estado

Aceptada

## Contexto

- **Regla de negocio:** columnas y modelo relacional en **castellano**, porque el dominio se trabaja con el cliente en español; traducir conceptos al inglés en la capa de datos genera etiquetas incorrectas y dificulta leer el modelo real.
- **Documentación:** contenido en **español** (nombres de fichero en inglés por convención del repo).
- **Código fuente:** convención habitual del ecosistema en **inglés** (paquetes, clases, composables) no es un problema si las otras capas encajan.
- Un borrador inicial planteó contratos HTTP íntegramente en español, pero el OpenAPI y gran parte del backend **ya** exponían inglés (`speciesId`, `/species`, `latitude`). Forzar español en API implicaba **renombrar lo ya desplegado** y aumentaba riesgo de traducciones literales (`visibilidadMapaPublico` frente a vocabulario ya estable en código). Ese borrador **no se adoptó**.
- [ADR-0006](0006-ejemplar-aggregate-http-kafka-naming.md) fija el agregado **`ejemplar`** en persistencia y eventos; en el wire HTTP, rutas **`/trees`** y propiedad **`treeId`** (mapeo desde `ejemplar_id`).

## Por qué BD en castellano y HTTP en inglés

A primera vista parece **incongruente** usar dos idiomas en capas vecinas. No lo es si se entiende que **no comparten frontera ni público**:

| Dimensión | Persistencia (castellano) | Contrato HTTP (inglés) |
|-----------|---------------------------|-------------------------|
| **Quién lo lee** | Personas de producto, soporte, auditoría de datos, SQL en revisiones; alineado al lenguaje del cliente | Código de SPA y servicios, OpenAPI, herramientas y convenciones del ecosistema Java/TypeScript |
| **Qué representa** | **Modelo de dominio durable** (verdad a largo plazo, informes, migraciones, legalidad de datos) | **Vista publicada** hacia el cliente HTTP (puede evolucionar por versión sin renombrar columnas) |
| **Riesgo del naming** | Un mal nombre en columna (**traducción literal incorrecta**) se fossiliza en Flyway, ERD y conversaciones con negocio | Un nombre en JSON se corrige en DTO/OpenAPI con impacto acotado al contrato |
| **Estándar del sector** | Esquemas locales en idioma del negocio en productos no internacionales | APIs REST y props en inglés son la convención habitual de integración, aunque el negocio sea local |

En DDD, la BD materializa el **lenguaje ubicuo** (español); el contrato REST **proyecta** ese modelo en inglés vía DTO (tabla de mapeo más abajo), sin renombrar columnas.

**Regla:** un idioma homogéneo por capa; **prohibido** mezclar en el mismo JSON (`speciesId` + `nombreComun`). No traducir columnas SQL al inglés (doble glosario con negocio); no forzar el HTTP al español (coste de renombrar contrato ya en inglés). En API, recurso **`trees`** / propiedad **`treeId`** ([ADR-0006](0006-ejemplar-aggregate-http-kafka-naming.md)); en SQL, `ejemplar_id`.

## Decisión

Modelo de **tres capas** con mapeo explícito y **mínima ruptura**:

| Capa | Idioma | Responsabilidad |
|------|--------|-----------------|
| **Persistencia** (SQL, columnas, Mongo de negocio) | Español `snake_case` | Verdad de dominio |
| **Aplicación** (entidades JPA, servicios, comandos) | Español en atributos de dominio **o** inglés con `@Column` español; clases en inglés | Lógica y alineación con BD |
| **Contrato HTTP** (OpenAPI, JSON, paths de recurso) | **Inglés `camelCase`** homogéneo | Interfaz hacia SPA y clientes |
| **Kafka** (payload entre servicios) | Español `snake_case` según [kafka-events.md](../events/kafka-events.md) | Sin cambio; cercano a BD, consumo interno |

### Reglas del contrato HTTP

1. Rutas de recurso en **inglés** plural: `/species`, `/provinces`, `/families`, `/genera`, `/photos`, `/trees` ([ADR-0006](0006-ejemplar-aggregate-http-kafka-naming.md)).
2. Propiedades JSON en **inglés** (`speciesId`, `commonName`, `latitude`, `publicMapVisibility`, `photoId`, …).
3. **Mapeo obligatorio** en DTO/assembler/servicio: no exponer entidades JPA; traducir español (BD) ↔ inglés (JSON y nombres de query) en un solo sitio por operación. Los repositorios y SQL nativo usan claves de dominio (`especie`, `estado`, …), no tokens del OpenAPI (`species`, `publicationState`).
4. Prefijo `/api/<contexto>/` y enums de valor (`BORRADOR`, `PUBLICADO`, …): sin cambio (técnico / códigos de dominio).
5. **Prohibido** mezclar en el mismo esquema JSON inglés y español **ad hoc** (`speciesId` + `nombreComun`). Las excepciones **7–10** son conjuntos **cerrados** documentados; no añadir propiedades en español sueltas en otros recursos.
6. Parámetros de consulta en **inglés** homogéneo con las propiedades JSON del recurso (`speciesId`, `publicationState`, `createdFrom`, …). El parámetro **`sort`**: por defecto tokens en **inglés** alineados a props JSON (`species`, `publicationState`, `species,asc`). Los literales de enum de negocio en query (`BORRADOR`, `PUBLICO`, …) no se traducen.
7. **Excepción documentada de `sort` (MVP):** en `GET /api/catalog/trees` (listado colaborador, **HU-008**) los tokens admitidos son **`modificado_en`** y **`creado_en`** (`modificado_en,desc` por defecto), porque el `ORDER BY` de la consulta nativa usa nombres de columna SQL. La respuesta JSON sigue en inglés (`modifiedAt`, `createdAt`). **No** generalizar esta excepción a endpoints nuevos sin decisión explícita; el listado público (`GET /api/catalog/public/trees`) mantiene `sort` en inglés (`species`, `publicationState`, …).
8. **Excepción documentada — administración de suscripciones (MVP, HU-012):** en **notification-service**, los endpoints admin bajo `/api/notifications/subscriptions` (`GET` listado, `PATCH` estado) usan propiedades y query en **camelCase español** alineadas a columnas de `suscriptor`: `estadoSuscripcion`, `altaEn`, `confirmadoEn`, `bajaEn`; filtro query `estadoSuscripcion`. Conviven con identificadores neutros (`subscriptionId`, `email`). El alta pública `POST` solo expone `email` en respuesta. **No** renombrar sin ADR; endpoints nuevos fuera de este corte siguen la regla 2.
9. **Excepción documentada — permiso de subida media (MVP, HU-006):** `MediaSubmissionPermissionResponse.actorUsuarioAppId` en catalog-service materializa `usuario_app_id` para trazabilidad en media-service; nombre histórico **cerrado**. Preferir inglés homogéneo (`appUserId`, …) en recursos nuevos.
10. **Excepción documentada — `ecologicalData` de enriquecimiento de especie (HU-015 / HU-016):** el objeto anidado refleja [mongo.md](../data-model/mongo.md) (`datos_ecologicos`, §6.3). Nivel raíz de enriquecimiento (`synonyms`, `distribution`, `references`, …) sigue la regla 2. Dentro de **`ecologicalData`**: claves de dominio **`clima`**, **`suelo`** y similares en **español/camelCase** alineadas a Mongo; **`SpeciesEcologicalData`** (catálogo `GET`/`PUT` …/enrichment) admite enums **`growthRate`/`leafType` en español** (`lento`, `caduca`, …) y `additionalProperties: true`. **`AiSpeciesEcologicalData`** (respuesta **HU-016**) comparte claves `clima`/`suelo`; **`growthRate`/`leafType`** se **normalizan a enums ingleses** (`slow`, `deciduous`, …) en ai-assistant-service. Precarga en UI: misma **forma estructural** que `SpeciesEnrichmentReplaceRequest`, no identidad byte a byte de schemas. Unificar dialectos en un solo wire sería cambio de contrato (nuevo ADR).

### Recurso HTTP `trees`

- Rutas: `/api/catalog/trees`, `/api/catalog/public/trees`, `/api/media/trees/...`
- Propiedad JSON y path param: **`treeId`** / `{treeId}` ([ADR-0006](0006-ejemplar-aggregate-http-kafka-naming.md)).
- No reintroducir legacy API: `arbol`, rutas `/api/.../ejemplares`, `ejemplarId` en JSON, `catalog.arbol.evento`.

### Tabla de mapeo BD → JSON (referencia)

| Columna SQL (español) | JSON API (inglés) |
|-----------------------|-------------------|
| `ejemplar_id` | `treeId` |
| `especie_id` | `speciesId` |
| `provincia_id` | `provinceId` |
| `nombre_comun` | `commonName` |
| `nombre_cientifico` | `scientificName` |
| `latitud` / `longitud` | `latitude` / `longitude` |
| `municipio` | `municipality` |
| `descripcion` | `description` |
| `altitud` | `altitude` |
| `visibilidad_mapa_publico` | `publicMapVisibility` |
| `estado_publicacion` | `publicationState` |
| `fotografia_id` | `photoId` |
| `modificado_en` / `creado_en` | `modifiedAt` / `createdAt` (JSON); en query **`sort`** del listado colaborador solo: tokens `modificado_en` / `creado_en` ([regla 7](#reglas-del-contrato-http)) |

#### Media (`media-service`, tabla `fotografia`)

| Columna SQL (español) | JSON API (inglés) |
|-----------------------|-------------------|
| `nombre_fichero_original` | `originalFileName` |
| `tipo_mime` | `mimeType` |
| `tamano_bytes` | `sizeBytes` |
| `ancho_px` | `widthPx` (metadatos/confirm) o `width` (galería) |
| `alto_px` | `heightPx` (metadatos/confirm) o `height` (galería) |
| `orden` | `order` |
| `es_principal` | `isPrimary` |
| `subida_en` | `uploadedAt` |
| `categoria` | `category` |

#### Notificaciones (`notification-service`, tabla `suscriptor`) — excepción regla 8

| Columna SQL (español) | JSON API (excepción MVP) |
|-----------------------|---------------------------|
| `suscriptor_id` | `subscriptionId` |
| `estado_suscripcion` | `estadoSuscripcion` |
| `alta_en` | `altaEn` |
| `confirmado_en` | `confirmadoEn` |
| `baja_en` | `bajaEn` |

#### Catálogo — permiso media (excepción regla 9)

| Columna SQL (español) | JSON API (excepción MVP) |
|-----------------------|---------------------------|
| `usuario_app_id` (actor) | `actorUsuarioAppId` en `MediaSubmissionPermissionResponse` |

#### Enriquecimiento especie — `ecologicalData` (excepción regla 10)

| Persistencia Mongo (`datos_ecologicos`) | Wire HTTP catálogo (`SpeciesEcologicalData`) | Wire HTTP IA (`AiSpeciesEcologicalData`, HU-016) |
|----------------------------------------|-----------------------------------------------|--------------------------------------------------|
| `clima`, `suelo`, `velocidad_crecimiento`, `tipo_hoja`, … | `clima`, `suelo`, `growthRate`, `leafType` (enums §6.3 en **español** admitidos) | `clima`, `suelo`, …; `growthRate`/`leafType` enums en **inglés** tras validación IA |
| `altitud_min_m` / `altitud_max_m` | `altitudMinM` / `altitudMaxM` | `altitudMinM` / `altitudMaxM` |

## Consecuencias

- Se **conservan** paths y DTO de escritura ya en inglés; catálogo (lecturas) y **contrato JSON de media** (presign, confirm, metadatos, galería) homogéneos en inglés con mapeo en DTO.
- Query y `sort` del listado público (`GET /api/catalog/public/trees`) en inglés, alineados a las propiedades JSON (`species`, `publicationState`, `treeId,asc`, …).
- Excepción acotada: `sort` del listado colaborador (`GET /api/catalog/trees`) con tokens `modificado_en` / `creado_en` (regla 7); props de respuesta `modifiedAt` / `createdAt` sin cambio.
- Excepciones acotadas en **notification-service** (regla 8), `actorUsuarioAppId` (regla 9) y `ecologicalData` de enriquecimiento de especie (regla 10); resto del contrato sigue inglés homogéneo.
- [api-design.mdc](../../.cursor/rules/api-design.mdc) y [naming-conventions.md](../engineering/naming-conventions.md) reflejan este ADR.

## Referencias

- [ADR-0006](0006-ejemplar-aggregate-http-kafka-naming.md)
- [openapi.yaml](../api/openapi.yaml)
- [naming-conventions.md](../engineering/naming-conventions.md)
