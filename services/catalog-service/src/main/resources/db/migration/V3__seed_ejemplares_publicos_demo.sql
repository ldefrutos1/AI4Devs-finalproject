SET search_path TO catalog;
-- Fichas publicadas de demostración para consulta pública (HU-002) y E2E Playwright.
-- Idempotente: no duplica si ya existe la semilla por municipio y estado.

INSERT INTO usuario_app (subject_oidc, email, nombre)
SELECT 'seed-catalogo-e2e', 'seed@mtl.test', 'Semilla catálogo'
WHERE NOT EXISTS (SELECT 1 FROM usuario_app WHERE subject_oidc = 'seed-catalogo-e2e');

INSERT INTO ejemplar (
    especie_id,
    provincia_id,
    usuario_app_id,
    municipio,
    descripcion,
    visibilidad_mapa_publico,
    latitud,
    longitud,
    estado_publicacion,
    creado_por,
    modificado_por
)
SELECT
    e.especie_id,
    p.provincia_id,
    u.usuario_app_id,
    'Parque del Retiro',
    'Ejemplar de demostración para consulta pública (semilla Flyway).',
    'PUBLICO',
    40.4150000,
    -3.6840000,
    'PUBLICADO',
    u.usuario_app_id,
    u.usuario_app_id
FROM usuario_app u
CROSS JOIN especie e
CROSS JOIN provincia p
WHERE u.subject_oidc = 'seed-catalogo-e2e'
  AND e.nombre_cientifico = 'Quercus ilex'
  AND p.codigo = '28'
  AND NOT EXISTS (
    SELECT 1
    FROM ejemplar ex
    WHERE ex.municipio = 'Parque del Retiro'
      AND ex.estado_publicacion = 'PUBLICADO'
      AND ex.visibilidad_mapa_publico = 'PUBLICO'
  );
