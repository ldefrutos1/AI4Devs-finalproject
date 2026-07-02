# Auditoría Mongo / híbrido SQL (`catalog-service`)

Revisión estática de Mongo y coherencia SQL↔Mongo. Complementa [db-postgresql-mtl](../db-postgresql-mtl/SKILL.md) en auditorías completas de catálogo.

**Implementación:** [mongo-hybrid.mdc](../../.cursor/rules/mongo-hybrid.mdc), [mongo.md](../../docs/data-model/mongo.md), [data-model-design.mdc](../../.cursor/rules/data-model-design.mdc) § Dónde persistir. **Esta skill solo audita.**

## Cuándo activar

- Auditoría de `catalog-service` con parte Mongo (solita o vía paso 3 de db-postgresql-mtl).
- Tras cambios en `@Document`, repos Mongo o handlers SQL→Mongo.
- **No** activar solo para decidir «¿SQL o Mongo?» al diseñar.

## Procedimiento

1. Confirmar alcance (diff, ficheros o módulo completo).
2. Escanear (sin `target/`):
   - `services/catalog-service/…/infrastructure/persistence/mongo/**`
   - `…/application/**` (sync SQL→Mongo)
   - `…/controller/**` — ❌ repos Mongo o orquestación híbrida fuera de `application`
3. Contrastar con [mongo.md](../../docs/data-model/mongo.md) y [mongo-hybrid.mdc](../../.cursor/rules/mongo-hybrid.mdc).
4. Evaluar dimensiones (✅ | ⚠️ | ❌) e informe (misma plantilla que db-postgresql-mtl).

## Dimensiones

### 1. Autoridad — ¿BD correcta?

| Dato | BD |
|------|-----|
| Maestros especie/ejemplar, taxonomía, transaccional | PostgreSQL |
| Enriquecimiento semiestructurado | Mongo (`especie_detalle`, `ejemplar_detalle`) |
| Binarios / fotos | Object storage ([media-service](../../docs/engineering/media-upload-hu006.md)) |

❌ maestros o integridad transaccional en Mongo.

### 2. Identificadores

- `_id` entero = `especie_pg_id` o `ejemplar_pg_id` ([mongo.md](../../docs/data-model/mongo.md)); ❌ `ObjectId` / prefijos `esp_`/`eje_` en MVP
- Enlaces explícitos `especie_pg_id` / `ejemplar_pg_id` en documentos
- ❌ IDs SQL obsoletos sin actualizar Mongo

### 3. Sync SQL → Mongo

- Denormalización actualizada vía **evento** → handler en `application`
- ❌ repo JPA llama repo Mongo; ❌ sync dentro de `@Transactional` SQL
- Estrategia documentada si el dato denormalizado puede quedar obsoleto

### 4. Resiliencia

- Maestros SQL operativos sin Mongo
- Escritura Mongo: async o try/catch + log; ❌ fallo Mongo revierte SQL

### 5. Documentos

- Campos semiestructurados en Mongo, no como columnas SQL salvo excepción documentada
- ❌ duplicar maestros SQL (coords, publicación…) sin sync
- `nombre_cientifico` / `nombre_comun` en `especie_detalle`: permitido con estrategia de actualización

### 6. Paquetes

Según [spring-boot-4-backend.mdc](../../.cursor/rules/spring-boot-4-backend.mdc) § Persistencia: JPA y Mongo separados bajo `infrastructure.persistence.*`; sync en `application`.

❌ sync en `domain` o repos JPA; ⚠️ `@Document` mezclado con `@Entity`.

### 7. Seguridad

- Validar `especie_pg_id` / `ejemplar_pg_id` y autorización en `application` antes de leer/escribir Mongo

## Informe (obligatorio)

Misma estructura que [db-postgresql-mtl](../db-postgresql-mtl/SKILL.md) § Informe. Cierre con veredicto: ¿apto para merge desde capa Mongo/híbrida?

## Reglas de auditoría

1. PostgreSQL manda.
2. Mongo = enriquecimiento, no BD de negocio.
3. Fix SQL → nueva migración Flyway (nunca reescribir `V*` aplicadas).
4. Fallo Mongo nunca bloquea operación sobre maestros SQL.
