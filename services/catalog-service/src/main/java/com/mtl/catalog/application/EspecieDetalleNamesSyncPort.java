package com.mtl.catalog.application;

/**
 * Sincroniza nombres desnormalizados de especie en Mongo tras cambios en el maestro SQL (HU-015).
 */
public interface EspecieDetalleNamesSyncPort {

  void syncNamesAfterMasterUpdate(long speciesId, String scientificName, String commonName);
}
