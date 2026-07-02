package com.mtl.catalog.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Stub cuando Mongo está desactivado. */
@Component
@ConditionalOnProperty(
    name = "mtl.catalog.mongo.enabled",
    havingValue = "false",
    matchIfMissing = true)
public class NoOpEspecieDetalleNamesSyncPort implements EspecieDetalleNamesSyncPort {

  private static final Logger log =
      LoggerFactory.getLogger(NoOpEspecieDetalleNamesSyncPort.class);

  @Override
  public void syncNamesAfterMasterUpdate(
      long speciesId, String scientificName, String commonName) {
    log.debug(
        "Mongo no configurado: omitiendo sync de nombres de especie (speciesId={})", speciesId);
  }
}
