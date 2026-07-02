package com.mtl.catalog.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "mtl.catalog.mongo.enabled", havingValue = "true")
public class MongoEspecieDetalleNamesSyncService implements EspecieDetalleNamesSyncPort {

  private static final Logger log =
      LoggerFactory.getLogger(MongoEspecieDetalleNamesSyncService.class);

  private final EspecieDetalleMongoNamesWriter especieDetalleMongoNamesWriter;

  public MongoEspecieDetalleNamesSyncService(
      EspecieDetalleMongoNamesWriter especieDetalleMongoNamesWriter) {
    this.especieDetalleMongoNamesWriter = especieDetalleMongoNamesWriter;
  }

  @Override
  public void syncNamesAfterMasterUpdate(
      long speciesId, String scientificName, String commonName) {
    try {
      especieDetalleMongoNamesWriter.updateNamesIfPresent(
          speciesId, scientificName, commonName);
    } catch (Exception ex) {
      log.error(
          "Fallo al sincronizar nombres de especie en Mongo tras edición SQL (speciesId={}): {}",
          speciesId,
          ex.toString());
      log.debug("Detalle fallo sync nombres especie Mongo", ex);
    }
  }
}
