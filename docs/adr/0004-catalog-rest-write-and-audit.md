# ADR-0004: Alta REST de ejemplar, `usuario_app` desde JWT y auditoría R3 en catalog-service

## Estado

Aceptada (nomenclatura HTTP/JPA actualizada según [ADR-0006](0006-ejemplar-aggregate-http-kafka-naming.md))

## Contexto

La HU-005 exige que un colaborador autenticado pueda dar de alta una ficha (`POST /api/catalog/trees`) con validaciones R1/R2, actor identificable en base de datos y traza en `AUDITORIA_CATALOGO` (R3).

El diseño debe cubrir:

- Contrato HTTP con DTO (sin entidades JPA en la API).
- Identidad OIDC (Keycloak) y tabla `usuario_app` para **FKs**, listados y auditoría **sin** consultar Keycloak en cada lectura SQL.
- Roles de negocio solo en el **JWT** (`realm_access.roles`); **no** persistir `rol` en `usuario_app`.
- Perfil en BD: `email` y **`nombre`** (nullable) desde claims del access token; actualizar solo si el valor normalizado **cambia**.

## Alternativas consideradas

1. **Exigir fila `usuario_app` preexistente (403 si falta)**  
   Trazabilidad estricta pero fricción operativa en el primer uso hasta un provisionamiento externo.

2. **Creación perezosa (elegida)**  
   Si existe `usuario_app` por `subject_oidc` → se reutiliza la PK y se **sincronizan** `email`/`nombre` si difieren. Si no existe → **INSERT** con datos mínimos del token. Colisiones concurrentes: reintento tras `DataIntegrityViolationException` y `merge` de perfil.

3. **`@EnableJpaAuditing` en `Ejemplar` (TASK-HU-005-11)**  
   Spring Data JPA Auditing en la entidad **`Ejemplar`**: `creado_en` / `modificado_en` / `creado_por` / `modificado_por` rellenados por `@EntityListeners(AuditingEntityListener.class)` y un `AuditorAware<Long>` que resuelve `usuario_app_id` desde el JWT (subject → `usuario_app`). La orquestación de alta materializa `usuario_app` en `EjemplarCreationService` **antes** del `save` del ejemplar para que el auditor resuelva en la misma transacción. Detalle: [HU-005-ticket-breakdown.md](../backlog/HU-005-ticket-breakdown.md) (TASK-HU-005-11).

## Decisión

- **REST:** `CatalogEjemplaresController` expone `POST /api/catalog/trees` con cuerpo **`CreateTreeRequest`** (validación Jakarta en borde) y respuesta **201** + `Location` + **`CreatedTreeResponse`** (`treeId`), según [openapi.yaml](../api/openapi.yaml) y [ADR-0006](0006-ejemplar-aggregate-http-kafka-naming.md) / [ADR-0007](0007-english-http-spanish-persistence.md).
- **Nomenclatura HTTP (implementación):** el contrato canónico usa los schemas OpenAPI anteriores. En `catalog-service`, las clases Java del borde pueden conservar aún el sufijo `Ejemplar` (`CreateEjemplarRequest`, `CreatedEjemplarResponse`); el JSON en wire coincide con OpenAPI (propiedades en inglés, p. ej. `speciesId`, `treeId`). Un refactor de nombres de clase Java queda fuera del alcance de este ADR.
- **Orquestación:** `EjemplarRegistrationService` (`@Transactional`) llama a `EjemplarCreationService.create` y después a `CatalogAuditService.recordEjemplarCreated` en la **misma transacción** que el insert de `ejemplar` y la fila de `usuario_app` si aplica, de modo que un fallo en auditoría **revierte** el alta.
- **JWT — claims mínimos para alta de `usuario_app`:** el access token debe incluir **`email`** (scope `email` en el cliente OIDC). Para **`nombre`**: claim estándar **`name`**, o composición de **`given_name`** + **`family_name`** si `name` no está presente (véase [OidcUserProfileExtractor](../../services/catalog-service/src/main/java/com/mtl/catalog/util/OidcUserProfileExtractor.java)). Si falta `email` cuando hace falta materializar usuario → **400** Problem, mensaje seguro (sin listar claims internos).
- **Seguridad HTTP:** `POST /api/catalog/trees` exige roles de realm **`COLABORADOR`** o **`ADMIN`** además de Bearer válido (`CatalogSecurityConfig`).
- **Auditoría:** `operacion` = `EJEMPLAR_CREADO`; `datos_nuevos_resumen` solo con **ids técnicos** (`ejemplar_id`, `especie_id`, `provincia_id`), sin PII ni texto libre de usuario.
- **Esquema SQL:** `usuario_app` sin columna `rol` y con `nombre` (nullable) en [`V1__baseline.sql`](../../services/catalog-service/src/main/resources/db/migration/V1__baseline.sql) (DDL único con CHECK e índice parcial de `ejemplar`).

## Consecuencias

- **JPA Auditing (TASK-HU-005-11):** la entidad `Ejemplar` usa `@CreatedDate` / `@LastModifiedDate` / `@CreatedBy` / `@LastModifiedBy`; el auditor devuelve el `usuario_app_id` del subject OIDC actual. `usuario_app` y maestros taxonómicos siguen sin listeners de auditoría JPA en este corte.
- **Cliente SPA / herramientas:** al obtener el token deben solicitar scopes que incluyan **`profile`** y **`email`** para que el access token lleve los claims necesarios (en dev, el realm importado `mtl` con `mtl-spa` y `fullScopeAllowed: true` hereda los *default client scopes* de Keycloak; conviene fijar `scope=openid profile email` en el flujo OIDC).
- **Kafka (`EJEMPLAR_CREADO` en topic `catalog.ejemplar.evento`)** queda fuera del alcance de auditoría SQL de este ADR (TASK-HU-005-05).
- **Tests:** `mvn test` con H2 y Flyway desactivado siguen validando capas con mocks; IT con Postgres requieren Docker donde aplique.

## Referencias

- [ADR-0006](0006-ejemplar-aggregate-http-kafka-naming.md)
- [HU-005-ticket-breakdown.md](../backlog/HU-005-ticket-breakdown.md)
- [jwt-gateway-strategy.md](../security/jwt-gateway-strategy.md)
- [openapi.yaml](../api/openapi.yaml)
- [V1__baseline.sql](../../services/catalog-service/src/main/resources/db/migration/V1__baseline.sql)
