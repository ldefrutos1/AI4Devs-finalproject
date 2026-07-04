# Auditoría PostgreSQL / JPA / Flyway (MTL)

Skill de **revisión estática** e informe de hallazgos. **No** sustituye reglas de implementación:
[data-model-design.mdc](../../rules/data-model-design.mdc),
[naming-conventions.md](../../../docs/engineering/naming-conventions.md),
[spring-boot-4-backend.mdc](../../rules/spring-boot-4-backend.mdc) § Persistencia.

## Cuándo activar

- El usuario pide auditar/revisar/validar la capa de datos o un diff pre-PR.
- Tras un TASK que toque `db/migration/`, entidades JPA o repositorios.
- **No** activar solo para diseñar algo nuevo.

## Procedimiento

1. Confirmar **módulo** y alcance (`catalog-service` | `media-service` | `notification-service` | `ai-assistant-service`; diff, ficheros o módulo completo). Preguntar si falta.
2. Escanear (sin `target/`):
   - `services/<módulo>/src/main/resources/db/migration/`
   - `…/infrastructure/persistence/jpa/**`
   - `…/application/**` (N+1, `@Transactional`)
   - `…/controller/**` — ❌ si hay `Repository`, `@Query`, `EntityManager` o `JdbcTemplate`
3. Si es **`catalog-service`**, aplicar también [db-mongo-mtl](../db-mongo-mtl/SKILL.md).
4. Evaluar cada **dimensión** (✅ | ⚠️ | ❌) y entregar **informe** (plantilla al final).

## Dimensiones

### 1. Flyway

- `V{version}__{descripcion}.sql`; FK solo a tablas de versiones anteriores
- ❌ editar migraciones ya aplicadas
- Idempotencia (`IF NOT EXISTS`) donde aplique; `spring.flyway.clean-disabled=true` en prod

### 2. Tipos PostgreSQL

| Uso | OK | Evitar |
|-----|-----|--------|
| IDs | `BIGSERIAL` / `UUID` | `INTEGER` si escala |
| Texto | `VARCHAR(n)` / `TEXT` | `CHAR`, floats para dinero |
| Fechas | `TIMESTAMPTZ` | `TIMESTAMP` sin TZ |
| Flags | `BOOLEAN` | `CHAR(1)`, `INTEGER` |
| JSON | `JSONB` (+ GIN si se filtra) | `TEXT` opaco |

### 3. Naming

Canónico: [naming-conventions.md](../../../docs/engineering/naming-conventions.md) § **N1**. Marcar ❌ desvíos y `@Table`/`@Column` desalineados con Flyway o [ADR-0007](../../../docs/adr/0007-english-http-spanish-persistence.md).

### 4. Integridad

- FK JPA ↔ `FOREIGN KEY` en SQL
- `ON DELETE CASCADE` solo en composición; `RESTRICT` en referencias maestras
- Tablas huérfanas o relaciones ausentes

### 5. Índices y rendimiento

- Índice en columnas FK (PostgreSQL no lo crea solo)
- Índices en filtros/joins/orden frecuentes; parciales con soft delete (`eliminado_en IS NULL`)
- N+1: `LAZY` en colecciones; ❌ `EAGER` sin justificación

### 6. JPA ↔ SQL

- `@Enumerated(STRING)`; fechas `OffsetDateTime`/`Instant` con `TIMESTAMPTZ`
- `ddl-auto=validate` en prod; `PostgreSQLDialect`; `@SequenceGenerator` alineado con SQL

### 7. Diseño físico

- Redundancias 3FN; tablas muy anchas (>30 cols)
- Auditoría: `creado_en`, `modificado_en`, `creado_por`, `modificado_por` donde aplique

### 8. Seguridad

- Native SQL: solo parámetros nombrados; ❌ concatenación/`String.format` con input usuario
- ❌ `@Entity` en API o binding request → entidad; usar DTO ([ADR-0007](../../../docs/adr/0007-english-http-spanish-persistence.md))
- ❌ PII o filas completas en logs ([logging.mdc](../../rules/logging.mdc))

## Informe (obligatorio)

```
## Resumen ejecutivo
## Hallazgos por dimensión — [Dimensión] [✅/⚠️/❌]
  Problema | Impacto | Solución (fragmento concreto)
## Críticos | Importantes | Opcionales
## Checklist — fixes como nueva migración V{N}__…
```

## Reglas de auditoría

1. No inventar: pedir artefacto o diff que falte.
2. Fixes SQL → **nueva** migración Flyway, nunca reescribir `V*` aplicadas.
3. Prioridad: seguridad/producción > rendimiento > deuda > estilo.

## Anexo — esquemas y SQL útil

| Módulo | Esquema PG |
|--------|------------|
| `catalog-service` | `catalog` |
| `media-service` | `media` |
| `notification-service` | `notification` |
| `ai-assistant-service` | `ai` |

Sustituir `:schema` al ejecutar en local. Ejemplos Flyway asumen `search_path` del servicio.

```sql
-- FK sin índice
SELECT tc.table_schema, tc.table_name, kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
  ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_schema = :schema
  AND NOT EXISTS (
    SELECT 1 FROM pg_indexes pi
    WHERE pi.schemaname = tc.table_schema AND pi.tablename = tc.table_name
      AND pi.indexdef LIKE '%' || kcu.column_name || '%');

-- Tablas sin PK
SELECT t.table_schema, t.table_name FROM information_schema.tables t
WHERE t.table_schema = :schema AND t.table_type = 'BASE TABLE'
  AND NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints tc
    WHERE tc.table_schema = t.table_schema AND tc.table_name = t.table_name
      AND tc.constraint_type = 'PRIMARY KEY');
```

Patrón de fix idempotente (informe): `CREATE INDEX IF NOT EXISTS idx_ejemplar_especie_id ON ejemplar(especie_id);`
