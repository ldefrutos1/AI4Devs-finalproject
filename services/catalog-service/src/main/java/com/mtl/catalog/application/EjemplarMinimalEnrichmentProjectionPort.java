package com.mtl.catalog.application;

import java.util.Optional;

/**
 * Proyección mínima Mongo tras persistir la ficha en PostgreSQL (TASK-HU-015-06). No revierte el
 * commit SQL si Mongo falla.
 */
public interface EjemplarMinimalEnrichmentProjectionPort {

  /**
   * Upsert mínimo de {@code especie_detalle} y {@code ejemplar_detalle}.
   *
   * @return mensaje de aviso para el cliente si Mongo falla; vacío si la proyección tuvo éxito
   */
  Optional<String> projectAfterEjemplarSqlPersisted(long ejemplarId, long speciesId);
}
