package com.mtl.catalog.application;

/**
 * Borrado de {@code especie_detalle} en Mongo al eliminar la especie maestra en PostgreSQL (HU-015).
 */
public interface EspecieDetalleEnrichmentDeletionPort {

  void deleteEnrichmentForSpecies(long speciesId);
}
